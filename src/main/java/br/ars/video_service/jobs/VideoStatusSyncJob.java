package br.ars.video_service.jobs;

import br.ars.video_service.enums.VideoStatus;
import br.ars.video_service.repositories.VideoRepository;
import br.ars.video_service.services.VideoIngestStreamService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@EnableScheduling
@Component
public class VideoStatusSyncJob {

    private final VideoRepository repo;
    private final VideoIngestStreamService ingest;

    public VideoStatusSyncJob(VideoRepository repo, VideoIngestStreamService ingest) {
        this.repo = repo;
        this.ingest = ingest;
    }

    @Scheduled(fixedDelayString = "60000") // a cada 60s
    public void run() {
        repo.findReady(VideoStatus.UPLOADED, true, null)  // pega UPLOADED
            .forEach(v -> safeRefresh(v.getId()));
        repo.findReady(VideoStatus.PROCESSING, true, null) // pega PROCESSING
            .forEach(v -> safeRefresh(v.getId()));
    }

    private void safeRefresh(java.util.UUID id) {
        try { ingest.refreshStatus(id); } catch (Exception ignored) {}
    }
}

