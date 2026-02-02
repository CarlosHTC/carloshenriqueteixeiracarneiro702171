package br.com.avaliacao.apimusicmanagement.infrastructure.storage;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(MinioProperties.class)
public class S3Config {

    private StaticCredentialsProvider credentialsProvider(MinioProperties props) {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(props.getAccessKey(), props.getSecretKey())
        );
    }

    private S3Configuration s3PathStyle() {
        return S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();
    }

    @Bean
    public S3Client s3Client(MinioProperties props) {
        return S3Client.builder()
                .httpClient(UrlConnectionHttpClient.builder().build())
                .endpointOverride(URI.create(props.getInternalUrl()))
                .credentialsProvider(credentialsProvider(props))
                .region(Region.US_EAST_1)
                .serviceConfiguration(s3PathStyle())
                .build();
    }

    @Bean
    public S3Presigner s3Presigner(MinioProperties props) {
        return S3Presigner.builder()
                .endpointOverride(URI.create(props.getPublicUrl()))
                .credentialsProvider(credentialsProvider(props))
                .region(Region.US_EAST_1)
                .serviceConfiguration(s3PathStyle())
                .build();
    }
}
