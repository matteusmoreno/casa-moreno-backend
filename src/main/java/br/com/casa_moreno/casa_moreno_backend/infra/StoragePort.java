package br.com.casa_moreno.casa_moreno_backend.infra;

public interface StoragePort {
    String uploadFile(byte[] fileData, String fileName, String contentType);

    void deleteFile(String fileUrl);
}
