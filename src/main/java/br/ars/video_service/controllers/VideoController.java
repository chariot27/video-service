package br.ars.video_service.controllers;

import br.ars.video_service.dto.VideoRequestDTO;
import br.ars.video_service.dto.VideoResponseDTO;
import br.ars.video_service.services.VideoIngestStreamService;
import br.ars.video_service.services.VideoService;
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

    private final VideoIngestStreamService streamIngest;
    private final VideoService videoService;

    public VideoController(VideoIngestStreamService streamIngest, VideoService videoService) {
        this.streamIngest = streamIngest;
        this.videoService = videoService;
    }

    // Upload para Bunny Stream (gerenciado)
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VideoResponseDTO> upload(
            @RequestPart("data") VideoRequestDTO data,
            @RequestPart("file") MultipartFile file) throws IOException {
        // Upload + criação no Bunny Stream
        VideoResponseDTO dto = streamIngest.uploadToStream(data, file);
        // Retorna 202 pois o encode ainda pode estar em processamento
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

    // Atualiza/consulta status no Bunny e reflete no banco
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
