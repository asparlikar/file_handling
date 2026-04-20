package com.example.demo.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.FileEntity;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {

	List<FileEntity> findByUploadedById(Long userId);

	Optional<FileEntity> findByStoredFileName(String storedFileName);

	List<FileEntity> findByContentTypeStartingWith(String type); // e.g. "image/"

	FileEntity save(FileEntity fileEntity);

	void delete(FileEntity fileEntity);
}
