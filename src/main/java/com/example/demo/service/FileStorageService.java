package com.example.demo.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.Repository.FileRepository;
import com.example.demo.entity.FileEntity;
import com.example.demo.entity.User;

@Service
public class FileStorageService {

	// ── Logger ───────────────────────────────────────────────
	private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

	private final Path fileStorageLocation;
	private final FileRepository fileRepository;

	public FileStorageService(@Value("${file.upload-dir}") String uploadDir, FileRepository fileRepository) {
		this.fileRepository = fileRepository;
		this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

		logger.info("Initializing FileStorageService...");
		logger.info("Upload directory path: {}", this.fileStorageLocation);

		try {
			Files.createDirectories(this.fileStorageLocation);
			logger.info("Upload directory is ready: {}", this.fileStorageLocation);
		} catch (IOException e) {
			logger.error("Could not create upload directory: {}", uploadDir, e);
			throw new RuntimeException("Could not create upload directory", e);
		}
	}

	// ── UPLOAD ───────────────────────────────────────────────

	public FileEntity storeFile(MultipartFile file, User uploadedBy) throws IOException {

		logger.info("Received file upload request: originalFileName={}, size={} bytes, contentType={}",
				file.getOriginalFilename(), file.getSize(), file.getContentType());

		// Validate file
		try {
			validateFile(file);
			logger.debug("File validation passed for: {}", file.getOriginalFilename());
		} catch (IllegalArgumentException e) {
			logger.warn("File validation failed: {}", e.getMessage());
			throw e;
		}

		// Generate unique filename
		String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
		String extension = getFileExtension(originalFileName);
		String storedFileName = UUID.randomUUID().toString() + "." + extension;

		logger.debug("Generated stored filename: {}", storedFileName);

		// Copy file to storage location
		Path targetLocation = fileStorageLocation.resolve(storedFileName);

		try {
			Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
			logger.info("File saved to disk: {} -> {}", originalFileName, targetLocation);
		} catch (IOException e) {
			logger.error("Failed to save file to disk: {}", targetLocation, e);
			throw e;
		}

		// Save metadata to DB
		FileEntity fileEntity = FileEntity.builder().originalFileName(originalFileName).storedFileName(storedFileName)
				.filePath(targetLocation.toString()).contentType(file.getContentType()).fileSize(file.getSize())
				.uploadedBy(uploadedBy).build();

		FileEntity saved = fileRepository.save(fileEntity);
		logger.info("File metadata saved to DB: id={}, originalFileName={}, storedFileName={}", saved.getId(),
				saved.getOriginalFileName(), saved.getStoredFileName());

		return saved;
	}

	// ── MULTIPLE FILES UPLOAD ────────────────────────────────

	public List<FileEntity> storeMultipleFiles(List<MultipartFile> files, User uploadedBy) {
		logger.info("Received multiple file upload request: totalFiles={}", files.size());

		List<FileEntity> savedFiles = files.stream().map(file -> {
			try {
				logger.debug("Processing file: {}", file.getOriginalFilename());
				return storeFile(file, uploadedBy);
			} catch (IOException e) {
				logger.error("Failed to store file: {}", file.getOriginalFilename(), e);
				throw new RuntimeException("Failed to store file: " + file.getOriginalFilename(), e);
			}
		}).collect(Collectors.toList());

		logger.info("Successfully uploaded {} out of {} files", savedFiles.size(), files.size());

		return savedFiles;
	}

	// ── DOWNLOAD ─────────────────────────────────────────────

	public Resource loadFileAsResource(Long fileId) throws IOException {
		logger.info("File download requested: fileId={}", fileId);

		FileEntity fileEntity = fileRepository.findById(fileId).orElseThrow(() -> {
			logger.warn("File not found in DB: fileId={}", fileId);
			return new RuntimeException("File not found with id: " + fileId);
		});

		logger.debug("File metadata found: originalFileName={}, storedFileName={}", fileEntity.getOriginalFileName(),
				fileEntity.getStoredFileName());

		Path filePath = fileStorageLocation.resolve(fileEntity.getStoredFileName()).normalize();

		logger.debug("Resolved file path: {}", filePath);

		try {
			Resource resource = new UrlResource(filePath.toUri());

			if (!resource.exists() || !resource.isReadable()) {
				logger.error("File not found on disk or not readable: path={}", filePath);
				throw new RuntimeException("File not found or not readable: " + fileEntity.getOriginalFileName());
			}

			logger.info("File ready for download: fileId={}, path={}", fileId, filePath);
			return resource;

		} catch (IOException e) {
			logger.error("Error loading file as resource: fileId={}, path={}", fileId, filePath, e);
			throw e;
		}
	}

	// ── DELETE ───────────────────────────────────────────────

	public void deleteFile(Long fileId) throws IOException {
		logger.info("File delete requested: fileId={}", fileId);

		FileEntity fileEntity = fileRepository.findById(fileId).orElseThrow(() -> {
			logger.warn("File not found for deletion: fileId={}", fileId);
			return new RuntimeException("File not found with id: " + fileId);
		});

		Path filePath = fileStorageLocation.resolve(fileEntity.getStoredFileName());
		logger.debug("Deleting file from disk: path={}", filePath);

		try {
			boolean deleted = Files.deleteIfExists(filePath);
			if (deleted) {
				logger.info("File deleted from disk: path={}", filePath);
			} else {
				logger.warn("File not found on disk: path={}", filePath);
			}
		} catch (IOException e) {
			logger.error("Error deleting file from disk: path={}", filePath, e);
			throw e;
		}

		fileRepository.delete(fileEntity);
		logger.info("File metadata deleted from DB: fileId={}, originalFileName={}", fileId,
				fileEntity.getOriginalFileName());
	}

	// ── VALIDATION ───────────────────────────────────────────

	private void validateFile(MultipartFile file) {
		logger.debug("Validating file: name={}, size={}, contentType={}", file.getOriginalFilename(), file.getSize(),
				file.getContentType());

		if (file.isEmpty()) {
			logger.warn("Rejected empty file: {}", file.getOriginalFilename());
			throw new IllegalArgumentException("File is empty");
		}

		List<String> allowedTypes = List.of("image/jpeg", "image/png", "image/gif", "application/pdf",
				"application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
				"text/plain");

		if (!allowedTypes.contains(file.getContentType())) {
			logger.warn("Rejected file with unsupported type: contentType={}, fileName={}", file.getContentType(),
					file.getOriginalFilename());
			throw new IllegalArgumentException("File type not allowed: " + file.getContentType());
		}

		if (file.getSize() > 10 * 1024 * 1024) {
			logger.warn("Rejected file exceeding size limit: size={} bytes, fileName={}", file.getSize(),
					file.getOriginalFilename());
			throw new IllegalArgumentException("File size exceeds 10MB limit");
		}

		logger.debug("File validation successful: {}", file.getOriginalFilename());
	}

	private String getFileExtension(String fileName) {
		String extension = Optional.ofNullable(fileName).filter(f -> f.contains("."))
				.map(f -> f.substring(f.lastIndexOf(".") + 1)).orElse("bin");

		logger.debug("Extracted file extension: {} from {}", extension, fileName);
		return extension;
	}
}