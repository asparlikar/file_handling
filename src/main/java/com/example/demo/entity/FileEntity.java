package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;


@Entity
@Table(name = "files")
public class FileEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String originalFileName;

	@Column(nullable = false)
	private String storedFileName; // ← this field

	@Column(nullable = false)
	private String filePath;

	@Column(nullable = false)
	private String contentType;

	private Long fileSize;

	@Column(name = "uploaded_at")
	private LocalDateTime uploadedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User uploadedBy;

	// ── Default Constructor ──────────────────────────────
	public FileEntity() {
	}

	// ── Getters ──────────────────────────────────────────
	public Long getId() {
		return id;
	}

	public String getOriginalFileName() {
		return originalFileName;
	}

	public String getStoredFileName() { // ← this was missing
		return storedFileName;
	}

	public String getFilePath() {
		return filePath;
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

	public User getUploadedBy() {
		return uploadedBy;
	}

	// ── Setters ──────────────────────────────────────────
	public void setId(Long id) {
		this.id = id;
	}

	public void setOriginalFileName(String originalFileName) {
		this.originalFileName = originalFileName;
	}

	public void setStoredFileName(String storedFileName) {
		this.storedFileName = storedFileName;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
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

	public void setUploadedBy(User uploadedBy) {
		this.uploadedBy = uploadedBy;
	}

	// ── PrePersist ───────────────────────────────────────
	@PrePersist
	public void prePersist() {
		this.uploadedAt = LocalDateTime.now();
	}

	// ── Builder ──────────────────────────────────────────
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private String originalFileName;
		private String storedFileName;
		private String filePath;
		private String contentType;
		private Long fileSize;
		private User uploadedBy;

		public Builder originalFileName(String originalFileName) {
			this.originalFileName = originalFileName;
			return this;
		}

		public Builder storedFileName(String storedFileName) {
			this.storedFileName = storedFileName;
			return this;
		}

		public Builder filePath(String filePath) {
			this.filePath = filePath;
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

		public Builder uploadedBy(User uploadedBy) {
			this.uploadedBy = uploadedBy;
			return this;
		}

		public FileEntity build() {
			FileEntity entity = new FileEntity();
			entity.setOriginalFileName(this.originalFileName);
			entity.setStoredFileName(this.storedFileName);
			entity.setFilePath(this.filePath);
			entity.setContentType(this.contentType);
			entity.setFileSize(this.fileSize);
			entity.setUploadedBy(this.uploadedBy);
			return entity;
		}
	}

}
