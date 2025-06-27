package br.com.casa_moreno.casa_moreno_backend.infra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URI;
import java.net.URISyntaxException;

@Component
public class S3StorageAdapter implements StoragePort {

    private final String region;
    private final String bucketName;
    private final S3Client s3Client;

    public S3StorageAdapter(
            @Value("${aws.s3.bucket.region}") String region,
            @Value("${aws.s3.bucket.name}") String bucketName,
            S3Client s3Client) {
        this.region = region;
        this.bucketName = bucketName;
        this.s3Client = s3Client;
    }

    @Override
    public String uploadFile(byte[] fileData, String fileName, String contentType) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(contentType)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileData));

        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucketName, region, fileName);
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            // Extrai o 'key' (caminho do arquivo) da URL completa
            URI uri = new URI(fileUrl);
            String key = uri.getPath().substring(1); // Remove a barra inicial '/'

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);

        } catch (URISyntaxException e) {
            System.err.println("URL do arquivo S3 inválida: " + fileUrl);
            e.printStackTrace();
        }
    }
}
