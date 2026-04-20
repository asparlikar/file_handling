package com.file.controller;

import java.io.IOException;
import java.net.http.HttpHeaders;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.file.Repository.FileRepository;
import com.file.dto.FileResponseDTO;
import com.file.entity.FileEntity;
import com.file.entity.User;
import com.file.service.FileStorageService;

@RestController
@RequestMapping("/api/files")
//@RequiredArgsConstructor

public class FileController {

	public FileStorageService fileStorageService;
	public FileRepository fileRepository;

	// ── SINGLE FILE UPLOAD ───────────────────────────────────

	public FileController(FileStorageService fileStorageService, FileRepository fileRepository) {

		this.fileStorageService = fileStorageService;
		this.fileRepository = fileRepository;
	}

	@PostMapping("/upload")
	public ResponseEntity<FileResponseDTO> uploadFile(@RequestParam("file") MultipartFile file,
			@RequestParam("userId") Long Id) throws IOException {

		// In real app, get user from SecurityContext
		// User user = new User();
		User user = null;
		if (user != null) {
			user = new User();
			user.setId(Id);
		}

		FileEntity saved = fileStorageService.storeFile(file, user);
		return ResponseEntity.status(HttpStatus.CREATED).body(toResponseDTO(saved));
	}

	// ── MULTIPLE FILE UPLOAD ─────────────────────────────────

	@PostMapping("/upload/multiple")
	public ResponseEntity<List<FileResponseDTO>> uploadMultipleFiles(@RequestParam("files") List<MultipartFile> files,
			@RequestParam("userId") Long userId) {

		User user = new User();
		user.setId(userId);

		List<FileEntity> saved = fileStorageService.storeMultipleFiles(files, user);
		List<FileResponseDTO> response = saved.stream().map(this::toResponseDTO).collect(Collectors.toList());

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	// ── DOWNLOAD ─────────────────────────────────────────────

	@GetMapping("/download/{fileId}")
	public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) throws IOException {

		Resource resource = fileStorageService.loadFileAsResource(fileId);

		Object fileEntity = fileRepository.findById(fileId).get();

		return ResponseEntity.ok().body(resource);
	}

	// ── VIEW IN BROWSER (inline) ─────────────────────────────

	@GetMapping("/view/{fileId}")
	public ResponseEntity<Resource> viewFile(@PathVariable Long fileId) throws IOException {

		Resource resource = fileStorageService.loadFileAsResource(fileId);
		FileEntity fileEntity = (FileEntity) fileRepository.findById(fileId).get();

		return ResponseEntity.ok().body(resource);
	}

	// ── GET FILE METADATA ────────────────────────────────────

	@GetMapping("/{fileId}")
	public ResponseEntity<FileResponseDTO> getFileInfo(@PathVariable Long fileId) {
		Object fileEntity = fileRepository.findById(fileId).orElseThrow();
		return ResponseEntity.ok(toResponseDTO((FileEntity) fileEntity));
	}

	// ── LIST FILES BY USER ───────────────────────────────────

	@GetMapping("/user/{userId}")
	public ResponseEntity<List<FileResponseDTO>> getFilesByUser(@PathVariable Long userId) {
		List<FileEntity> files = fileRepository.findByUploadedById(userId);
		List<FileResponseDTO> response = files.stream().map(this::toResponseDTO).collect(Collectors.toList());
		return ResponseEntity.ok(response);
	}

	// ── DELETE ───────────────────────────────────────────────

	@DeleteMapping("/{fileId}")
	public ResponseEntity<Void> deleteFile(@PathVariable Long fileId) throws IOException {
		fileStorageService.deleteFile(fileId);
		return ResponseEntity.noContent().build();
	}

	// ── Helper ───────────────────────────────────────────────

	private FileResponseDTO toResponseDTO(FileEntity entity) {
		return FileResponseDTO.builder().id(entity.getId()).originalFileName(entity.getOriginalFileName())
				.contentType(entity.getContentType()).fileSize(entity.getFileSize()).uploadedAt(entity.getUploadedAt())
				.downloadUrl("/api/files/download/" + entity.getId()).viewUrl("/api/files/view/" + entity.getId())
				.build();
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<String> handleException(Exception ex) {
		ex.printStackTrace();
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + ex.getMessage());
	}
}
