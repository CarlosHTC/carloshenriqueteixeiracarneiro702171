package br.com.avaliacao.apimusicmanagement.infrastructure.storage;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

@Component
public class S3ArtistaFotoStorage implements ArtistaFotoStorage {

    private final S3Client s3Client;
    private final S3Presigner presigner;
    private final MinioProperties minioProperties;

    public S3ArtistaFotoStorage(S3Client s3Client, S3Presigner presigner, MinioProperties minioProperties) {
        this.s3Client = s3Client;
        this.presigner = presigner;
        this.minioProperties = minioProperties;
    }

    @Override
    public void upload(String objectKey, MultipartFile file) {
        File tmp = null;
        try {
            tmp = File.createTempFile("upload-", ".bin");
            file.transferTo(tmp);

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(minioProperties.getArtistBucket())
                    .key(objectKey)
                    .contentType(file.getContentType())
                    .contentLength((long) tmp.length())
                    .build();

            s3Client.putObject(request, RequestBody.fromFile(tmp));
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao preparar arquivo para upload.", e);
        } finally {
            if (tmp != null && tmp.exists()) {
                tmp.delete();
            }
        }
    }

    @Override
    public String presignGetUrl(String objectKey, Duration duration) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(minioProperties.getArtistBucket())
                .key(objectKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(duration)
                .getObjectRequest(request)
                .build();

        return presigner.presignGetObject(presignRequest).url().toString();
    }

    @Override
    public void delete(String objectKey) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(minioProperties.getArtistBucket())
                .key(objectKey)
                .build();
        s3Client.deleteObject(request);
    }
}
