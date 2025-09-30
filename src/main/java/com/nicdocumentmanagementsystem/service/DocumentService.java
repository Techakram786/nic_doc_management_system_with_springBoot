package com.nicdocumentmanagementsystem.service;

import com.nicdocumentmanagementsystem.model.Document;
import com.nicdocumentmanagementsystem.model.DocumentType;
import com.nicdocumentmanagementsystem.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final FileStorageService fileStorageService;

    public DocumentService(DocumentRepository documentRepository, FileStorageService fileStorageService) {
        this.documentRepository = documentRepository;
        this.fileStorageService = fileStorageService;
    }

    public void saveDocument(MultipartFile file, String title, String description, DocumentType docType) {
        String fileName = fileStorageService.storeFile(file);

        try {
            Document doc = new Document();
            doc.setTitle(title);
            doc.setDescription(description);
            doc.setType(docType);
            doc.setFileName(fileName);
            doc.setOriginalFileName(Objects.requireNonNull(file.getOriginalFilename()));
            doc.setSize(file.getSize());
            doc.setContentType(file.getContentType());
            doc.setUploadDate(LocalDateTime.now());

            documentRepository.save(doc);
        } catch (Exception e) {
            // If the database save fails, delete the orphaned file
            fileStorageService.deleteFile(fileName);
            throw new RuntimeException("Failed to save document metadata to the database.", e);
        }
    }

    public List<Document> getDocumentsByType(DocumentType docType) {
        return documentRepository.findByTypeOrderByIdDesc(docType);
    }

    public Optional<Document> getDocumentByFileName(String fileName) {
        return documentRepository.findByFileName(fileName);
    }

    /**
     * Deletes a document from the database and the physical file from storage.
     * @param id The ID of the document to delete.
     */
    public void deleteDocument(Long id) {
        // Find the document record to get the filename
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + id));

        // 1. Delete the physical file from the server
        fileStorageService.deleteFile(doc.getFileName());
        // 2. Delete the record from the database
        documentRepository.delete(doc);
    }
}
