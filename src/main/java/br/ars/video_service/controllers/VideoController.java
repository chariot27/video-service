package br.ars.video_service.controllers;

import br.ars.video_service.dto.VideoRequestDTO;
import br.ars.video_service.dto.VideoResponseDTO;
import br.ars.video_service.services.StreamUrlService;
import br.ars.video_service.services.VideoIngestStreamService;
import br.ars.video_service.services.VideoQueryService;
import br.ars.video_service.services.VideoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/videos", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
public class VideoController {

    private static final Logger log = LoggerFactory.getLogger(VideoController.class);

    private final VideoIngestStreamService streamIngest;
    private final VideoService videoService;
    private final VideoQueryService videoQueryService;
    private final ObjectMapper objectMapper;
    private final StreamUrlService streamUrlService;

    public VideoController(VideoIngestStreamService streamIngest,
                           VideoService videoService,
                           VideoQueryService videoQueryService,
                           ObjectMapper objectMapper,
                           StreamUrlService streamUrlService) {
        this.streamIngest = streamIngest;
        this.videoService = videoService;
        this.videoQueryService = videoQueryService;
        this.objectMapper = objectMapper;
        this.streamUrlService = streamUrlService;
    }

    /** Upload para Bunny Stream (gerenciado) */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VideoResponseDTO> upload(
            @RequestPart("data") String dataJson,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo ausente ou vazio.");
        }

        var data = objectMapper.readValue(dataJson, VideoRequestDTO.class);

        log.info("UPLOAD IN: userId={} nomeArq={} size={} type={}",
                data.getUserId(), file.getOriginalFilename(), file.getSize(), file.getContentType());

        var dto = streamIngest.uploadToStream(data, file);

        // Se já tivermos o GUID do Stream no DTO, devolve com URL assinada
        // (ajuste de acordo com seu DTO: streamVideoId / streamGuid / etc.)
        if (dto.getStreamVideoId() != null && !dto.getStreamVideoId().isBlank()) {
            dto.setHlsMasterUrl(streamUrlService.signedHls(dto.getStreamVideoId()));
        } else if (dto.getHlsMasterUrl() != null) {
            dto.setHlsMasterUrl(streamUrlService.ensureSignedFromUrl(dto.getHlsMasterUrl()));
        }

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(dto);
    }

    /** Lista somente vídeos prontos (READY) com URL HLS assinada */
    @GetMapping("/ready")
    public ResponseEntity<List<VideoResponseDTO>> listReady(
            @RequestParam(defaultValue = "12") int limit
    ) {
        int safeLimit = Math.max(1, Math.min(50, limit)); // clamp 1..50
        var list = videoQueryService.listReady(safeLimit);

        var signed = list.stream().map(dto -> {
            if (dto.getStreamVideoId() != null && !dto.getStreamVideoId().isBlank()) {
                dto.setHlsMasterUrl(streamUrlService.signedHls(dto.getStreamVideoId()));
            } else if (dto.getHlsMasterUrl() != null) {
                dto.setHlsMasterUrl(streamUrlService.ensureSignedFromUrl(dto.getHlsMasterUrl()));
            }
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(signed);
    }

    /** Lista geral (se ainda usa em outras telas) — sem alterar */
    @GetMapping
    public List<VideoResponseDTO> list() {
        return videoService.listarVideos();
    }

    @GetMapping("/{id}")
    public VideoResponseDTO get(@PathVariable UUID id) {
        var dto = videoService.buscarPorId(id);
        if (dto.getStreamVideoId() != null && !dto.getStreamVideoId().isBlank()) {
            dto.setHlsMasterUrl(streamUrlService.signedHls(dto.getStreamVideoId()));
        } else if (dto.getHlsMasterUrl() != null) {
            dto.setHlsMasterUrl(streamUrlService.ensureSignedFromUrl(dto.getHlsMasterUrl()));
        }
        return dto;
    }

    /** Força refresh do status no Bunny e retorna o DTO atualizado com URL assinada */
    @GetMapping("/{id}/status")
    public VideoResponseDTO refresh(@PathVariable UUID id) {
        var dto = streamIngest.refreshStatus(id);
        if (dto.getStreamVideoId() != null && !dto.getStreamVideoId().isBlank()) {
            dto.setHlsMasterUrl(streamUrlService.signedHls(dto.getStreamVideoId()));
        } else if (dto.getHlsMasterUrl() != null) {
            dto.setHlsMasterUrl(streamUrlService.ensureSignedFromUrl(dto.getHlsMasterUrl()));
        }
        return dto;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        videoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    /* ==== Handlers de erro ==== */

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        return problem(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleIntegrity(DataIntegrityViolationException ex) {
        return problem(HttpStatus.CONFLICT, "Violação de integridade: " + (ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        log.error("Erro inesperado no VideoController", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno ao processar a requisição.");
    }

    private ResponseEntity<Map<String, Object>> problem(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", OffsetDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
