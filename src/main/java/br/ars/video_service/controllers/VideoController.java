package br.ars.video_service.controllers;

import br.ars.video_service.dto.VideoRequestDTO;
import br.ars.video_service.dto.VideoResponseDTO;
import br.ars.video_service.services.VideoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    private final VideoService service;

    public VideoController(VideoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<VideoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(service.listarVideos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VideoResponseDTO> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<VideoResponseDTO> upload(
            @RequestPart("video") MultipartFile video,
            @RequestPart("dados") VideoRequestDTO dto) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.uploadVideo(dto, video));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/otimizar")
    public ResponseEntity<Void> marcarComoOtimizado(@PathVariable UUID id, @RequestParam String cdnUrl) {
        service.marcarComoOtimizado(id, cdnUrl);
        return ResponseEntity.noContent().build();
    }
}
