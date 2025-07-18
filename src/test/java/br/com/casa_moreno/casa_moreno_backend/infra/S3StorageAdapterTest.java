package br.com.casa_moreno.casa_moreno_backend.infra;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("S3StorageAdapter Tests")
@ExtendWith(MockitoExtension.class)
class S3StorageAdapterTest {

    @Mock
    private S3Client s3Client;

    private S3StorageAdapter s3StorageAdapter;

    private final String region = "us-east-2";
    private final String bucketName = "test-bucket";

    @BeforeEach
    void setUp() {
        s3StorageAdapter = new S3StorageAdapter(region, bucketName, s3Client);
    }

    @Test
    @DisplayName("Should upload file successfully and return the correct S3 URL")
    void shouldUploadFileSuccessfullyAndReturnCorrectUrl() {
        byte[] fileData = "test-content".getBytes();
        String fileName = "profile-pictures/user-123.jpg";
        String contentType = "image/jpeg";
        String expectedUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, fileName);

        String actualUrl = s3StorageAdapter.uploadFile(fileData, fileName, contentType);

        assertEquals(expectedUrl, actualUrl, "The returned URL should be correctly formatted.");

        ArgumentCaptor<PutObjectRequest> putObjectRequestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        ArgumentCaptor<RequestBody> requestBodyCaptor = ArgumentCaptor.forClass(RequestBody.class);

        verify(s3Client, times(1)).putObject(putObjectRequestCaptor.capture(), requestBodyCaptor.capture());

        PutObjectRequest capturedRequest = putObjectRequestCaptor.getValue();
        assertEquals(bucketName, capturedRequest.bucket());
        assertEquals(fileName, capturedRequest.key());
        assertEquals(contentType, capturedRequest.contentType());
        assertEquals(fileData.length, requestBodyCaptor.getValue().contentLength());
    }

    @Test
    @DisplayName("Should delete file successfully when a valid URL is provided")
    void shouldDeleteFileSuccessfully() {
        String fileKey = "profile-pictures/user-to-delete.png";
        String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, fileKey);

        s3StorageAdapter.deleteFile(fileUrl);

        ArgumentCaptor<DeleteObjectRequest> deleteObjectRequestCaptor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client, times(1)).deleteObject(deleteObjectRequestCaptor.capture());

        DeleteObjectRequest capturedRequest = deleteObjectRequestCaptor.getValue();
        assertEquals(bucketName, capturedRequest.bucket());
        assertEquals(fileKey, capturedRequest.key());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "this-is-not-a-valid-url",                              // Cobre uri.getHost() == null
            "https://test-bucket.s3.us-east-2.amazonaws.com",       // Cobre uri.getPath().isEmpty()
            "https://test-bucket.s3.us-east-2.amazonaws.com/"       // Cobre uri.getPath().equals("/")
    })
    @DisplayName("Should not call delete for various malformed URLs")
    void shouldNotCallDeleteForMalformedUrls(String invalidUrl) {
        assertDoesNotThrow(() -> s3StorageAdapter.deleteFile(invalidUrl));
        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("Should not call delete when URL path is null (e.g., mailto URI)")
    void shouldNotCallDeleteWhenUrlPathIsNull() {
        String urlWithNullPath = "mailto:test@example.com";

        assertDoesNotThrow(() -> s3StorageAdapter.deleteFile(urlWithNullPath));
        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("Should catch URISyntaxException for syntactically invalid URL")
    void shouldCatchURISyntaxExceptionForMalformedUrl() {
        String syntacticallyInvalidUrl = "http:// ex ample.com";

        assertDoesNotThrow(() -> s3StorageAdapter.deleteFile(syntacticallyInvalidUrl));
        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("Should propagate S3Exception when upload fails")
    void shouldPropagateS3ExceptionWhenUploadFails() {
        byte[] fileData = "test-content".getBytes();
        String fileName = "fail-upload.txt";
        String contentType = "text/plain";

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(S3Exception.builder().message("Access Denied").build());

        assertThrows(S3Exception.class, () -> s3StorageAdapter.uploadFile(fileData, fileName, contentType));
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("Should propagate S3Exception when deletion fails")
    void shouldPropagateS3ExceptionWhenDeletionFails() {
        String fileUrl = "https://test-bucket.s3.us-east-2.amazonaws.com/file-to-delete.jpg";

        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenThrow(S3Exception.builder().message("Deletion Failed").build());

        assertThrows(S3Exception.class, () -> s3StorageAdapter.deleteFile(fileUrl));
        verify(s3Client, times(1)).deleteObject(any(DeleteObjectRequest.class));
    }
}
