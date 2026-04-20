
package com.file.dto;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.http.ResponseEntity.HeadersBuilder;

import com.file.entity.User;

public class FileResponseDTO {

    private Long id;
    private String originalFileName;
    private String contentType;
    private Long fileSize;
    private LocalDateTime uploadedAt;
    private String downloadUrl;
    private String viewUrl;

    // ── Default Constructor ──────────────────────────────
    public FileResponseDTO() {}

    // ── All Args Constructor ─────────────────────────────
    public FileResponseDTO(Long id, String originalFileName, String contentType,
                           Long fileSize, LocalDateTime uploadedAt,
                           String downloadUrl, String viewUrl) {
        this.id = id;
        this.originalFileName = originalFileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.uploadedAt = uploadedAt;
        this.downloadUrl = downloadUrl;
        this.viewUrl = viewUrl;
    }

    // ── Getters ──────────────────────────────────────────
    public Long getId() {
        return id;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getViewUrl() {
        return viewUrl;
    }

    // ── Setters ──────────────────────────────────────────
    public void setId(Long id) {
        this.id = id;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public void setViewUrl(String viewUrl) {
        this.viewUrl = viewUrl;
    }

    // ── Builder ──────────────────────────────────────────
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Long id;
        private String originalFileName;
        private String contentType;
        private Long fileSize;
        private LocalDateTime uploadedAt;
        private String downloadUrl;
        private String viewUrl;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder originalFileName(String originalFileName) {
            this.originalFileName = originalFileName;
            return this;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder fileSize(Long fileSize) {
            this.fileSize = fileSize;
            return this;
        }

        public Builder uploadedAt(LocalDateTime uploadedAt) {
            this.uploadedAt = uploadedAt;
            return this;
        }

        public Builder downloadUrl(String downloadUrl) {
            this.downloadUrl = downloadUrl;
            return this;
        }

        public Builder viewUrl(String viewUrl) {
            this.viewUrl = viewUrl;
            return this;
        }

        public FileResponseDTO build() {
            FileResponseDTO dto = new FileResponseDTO();
            dto.setId(this.id);
            dto.setOriginalFileName(this.originalFileName);
            dto.setContentType(this.contentType);
            dto.setFileSize(this.fileSize);
            dto.setUploadedAt(this.uploadedAt);
            dto.setDownloadUrl(this.downloadUrl);
            dto.setViewUrl(this.viewUrl);
            return dto;
        }

		
    }

    // ── toString ─────────────────────────────────────────
    @Override
    public String toString() {
        return "FileResponseDTO{" +
                "id=" + id +
                ", originalFileName='" + originalFileName + '\'' +
                ", contentType='" + contentType + '\'' +
                ", fileSize=" + fileSize +
                ", uploadedAt=" + uploadedAt +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", viewUrl='" + viewUrl + '\'' +
                '}';
    }
}