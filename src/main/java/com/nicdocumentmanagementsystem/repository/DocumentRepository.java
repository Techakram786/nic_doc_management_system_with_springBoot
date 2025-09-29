package com.nicdocumentmanagementsystem.repository;

import com.nicdocumentmanagementsystem.model.Document;
import com.nicdocumentmanagementsystem.model.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByTypeOrderByIdDesc(DocumentType type);
    Optional<Document> findByFileName(String fileName);
}