package br.com.avaliacao.apimusicmanagement.config.integration;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;


@Configuration
public class RegionalInterationConfig {

//    @Bean(name = "regionalWebClient")
//    public WebClient regionalWebClient(RegionalIntegrationProperties integrationProperties) {
//        HttpClient httpClient = HttpClient.create()
//                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, integrationProperties.getConnectTimeoutMs())
//                .responseTimeout(Duration.ofMillis(integrationProperties.getReadTimeoutMs()))
//                .doOnConnected(conn -> conn
//                        .addHandlerLast(new ReadTimeoutHandler(integrationProperties.getReadTimeoutMs() / 1000))
//                        .addHandlerLast(new WriteTimeoutHandler(integrationProperties.getReadTimeoutMs() / 1000))
//                );
//
//        return WebClient.builder()
//                .clientConnector(new ReactorClientHttpConnector(httpClient))
//                .build();
//    }

    @Bean
    public WebClient regionalWebClient(RegionalIntegrationProperties integrationProperties) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, integrationProperties.getConnectTimeoutMs())
                .responseTimeout(Duration.ofMillis(integrationProperties.getReadTimeoutMs()))
                .doOnConnected(connection -> connection
                        .addHandlerLast(new ReadTimeoutHandler(integrationProperties.getReadTimeoutMs() / 1000))
                        .addHandlerLast(new WriteTimeoutHandler(integrationProperties.getReadTimeoutMs() /1000)));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
