package br.ars.video_service.controllers;

import br.ars.video_service.dto.VideoRequestDTO;
import br.ars.video_service.dto.VideoResponseDTO;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/videos", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
public class VideoController {

    private static final Logger log = LoggerFactory.getLogger(VideoController.class);

    private final VideoIngestStreamService streamIngest;
    private final VideoService videoService;
    private final VideoQueryService videoQueryService;
    private final ObjectMapper objectMapper;

    public VideoController(VideoIngestStreamService streamIngest,
                           VideoService videoService,
                           VideoQueryService videoQueryService,
                           ObjectMapper objectMapper) {
        this.streamIngest = streamIngest;
        this.videoService = videoService;
        this.videoQueryService = videoQueryService;
        this.objectMapper = objectMapper;
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
        // 202 Accepted: upload aceito e processamento no Bunny em andamento
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(dto);
    }

    /** Lista somente vídeos prontos (READY) com URL HLS do CDN */
    @GetMapping("/ready")
    public ResponseEntity<List<VideoResponseDTO>> listReady(
            @RequestParam(defaultValue = "12") int limit
    ) {
        int safeLimit = Math.max(1, Math.min(50, limit)); // clamp 1..50
        return ResponseEntity.ok(videoQueryService.listReady(safeLimit));
    }

    /** Lista geral (se ainda usa em outras telas) */
    @GetMapping
    public List<VideoResponseDTO> list() {
        return videoService.listarVideos();
    }

    @GetMapping("/{id}")
    public VideoResponseDTO get(@PathVariable UUID id) {
        return videoService.buscarPorId(id);
    }

    /** Força refresh do status no Bunny e retorna o DTO atualizado */
    @GetMapping("/{id}/status")
    public VideoResponseDTO refresh(@PathVariable UUID id) {
        return streamIngest.refreshStatus(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        videoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    /* ==== Handlers simples de erro (respostas mais amigáveis no front) ==== */

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
