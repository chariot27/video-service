package br.ars.video_service.controllers;

import br.ars.video_service.dto.VideoRequestDTO;
import br.ars.video_service.dto.VideoResponseDTO;
import br.ars.video_service.services.VideoIngestStreamService;
import br.ars.video_service.services.VideoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/videos")
@CrossOrigin
public class VideoController {

    private static final Logger log = LoggerFactory.getLogger(VideoController.class);

    private final VideoIngestStreamService streamIngest;
    private final VideoService videoService;
    private final ObjectMapper objectMapper;

    public VideoController(VideoIngestStreamService streamIngest,
                           VideoService videoService,
                           ObjectMapper objectMapper) {
        this.streamIngest = streamIngest;
        this.videoService = videoService;
        this.objectMapper = objectMapper;
    }

    // Upload para Bunny Stream (gerenciado)
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<VideoResponseDTO> upload(
            @RequestPart("data") String dataJson,
            @RequestPart("file") MultipartFile file) throws IOException {

        VideoRequestDTO data;
        try {
            data = objectMapper.readValue(dataJson, VideoRequestDTO.class);
        } catch (Exception e) {
            log.warn("Part 'data' inválida: {}", dataJson, e);
            return ResponseEntity.badRequest().build();
        }

        log.info("UPLOAD IN: userId={} nomeArq={} size={} bytes contentType={}",
                data.getUserId(),
                file.getOriginalFilename(),
                file.getSize(),
                file.getContentType());

        VideoResponseDTO dto = streamIngest.uploadToStream(data, file);
        // 202 pois o encode ainda pode estar em processamento no Bunny
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(dto);
    }

    @GetMapping
    public List<VideoResponseDTO> list() {
        return videoService.listarVideos();
    }

    @GetMapping("/{id}")
    public VideoResponseDTO get(@PathVariable UUID id) {
        return videoService.buscarPorId(id);
    }

    @GetMapping("/{id}/status")
    public VideoResponseDTO refresh(@PathVariable UUID id) {
        return streamIngest.refreshStatus(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        videoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
