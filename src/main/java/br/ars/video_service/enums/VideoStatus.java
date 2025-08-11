package br.ars.video_service.enums;

public enum VideoStatus {
    UPLOADED,     // enviado à Bunny, aguardando encode
    PROCESSING,   // Bunny está processando
    READY,        // pronto para reprodução
    FAILED        // falha no processamento
}
