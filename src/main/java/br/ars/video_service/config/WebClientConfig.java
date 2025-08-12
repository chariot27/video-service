// br/ars/video_service/config/WebClientConfig.java
package br.ars.video_service.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    @Bean
    WebClient bunnyWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 20_000)
                .responseTimeout(Duration.ofMinutes(10))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(10, TimeUnit.MINUTES))
                        .addHandlerLast(new WriteTimeoutHandler(10, TimeUnit.MINUTES)));

        // Estratégias sem aumentar buffer (usa padrão, não força cache do body)
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecs -> {
                    // deixe o default; não aumente maxInMemory para não tentar carregar tudo
                })
                .build();

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                .build();
    }
}
