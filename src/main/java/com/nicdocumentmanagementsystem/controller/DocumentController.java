package com.nicdocumentmanagementsystem.controller;

import com.nicdocumentmanagementsystem.model.Document;
import com.nicdocumentmanagementsystem.model.DocumentType;
import com.nicdocumentmanagementsystem.service.DocumentService;
import com.nicdocumentmanagementsystem.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class DocumentController {

    private final DocumentService documentService;
    private final FileStorageService fileStorageService;

    public DocumentController(DocumentService documentService, FileStorageService fileStorageService) {
        this.documentService = documentService;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Web page for a specific document type (e.g., ANDROID).
     * This page shows the upload form and the list of existing documents.
     */
    @GetMapping("/documents/{type}")
    public String getDocumentsPage(@PathVariable("type") DocumentType type, Model model) {
        List<Document> documents = documentService.getDocumentsByType(type);
        model.addAttribute("documents", documents);
        model.addAttribute("docType", type.name());
        // This message attribute is used to show success/error messages after upload attempts.
        if (!model.containsAttribute("message")) {
            model.addAttribute("message", null);
        }
        return "documents"; // Renders documents.html
    }

    /**
     * Handles the file upload from the form on the documents.html page.
     */
    @PostMapping("/documents/upload")
    public String uploadDocument(@RequestParam("file") MultipartFile file,
                                 @RequestParam("title") String title,
                                 @RequestParam("description") String description,
                                 @RequestParam("type") DocumentType type,
                                 RedirectAttributes redirectAttributes) {

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Upload failed: Please select a file to upload.");
            return "redirect:/documents/" + type.name();
        }

        documentService.saveDocument(file, title, description, type);
        redirectAttributes.addFlashAttribute("message", "File uploaded successfully: '" + file.getOriginalFilename() + "'");

        return "redirect:/documents/" + type.name();
    }

    // --- API Endpoints ---

    /**
     * API to get a list of documents for a specific type.
     */
    @GetMapping("/api/documents/{type}")
    @ResponseBody
    public List<Document> getDocumentsApi(@PathVariable("type") DocumentType type) {
        return documentService.getDocumentsByType(type);
    }

    /**
     * API to download a specific file.
     */
    @GetMapping("/download/{fileName:.+}")
    @ResponseBody
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        Document doc = documentService.getDocumentByFileName(fileName)
                .orElseThrow(() -> new RuntimeException("File not found"));

        Resource resource = fileStorageService.loadFileAsResource(fileName);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getOriginalFileName() + "\"")
                .contentType(MediaType.parseMediaType(doc.getContentType()))
                .body(resource);
    }

    /**
     * API to view a specific file in the browser.
     */
    @GetMapping("/view/{fileName:.+}")
    @ResponseBody
    public ResponseEntity<Resource> viewFile(@PathVariable String fileName) {
        Document doc = documentService.getDocumentByFileName(fileName)
                .orElseThrow(() -> new RuntimeException("File not found"));

        Resource resource = fileStorageService.loadFileAsResource(fileName);

        // The "inline" content disposition tells the browser to attempt to display the file.
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getOriginalFileName() + "\"")
                .contentType(MediaType.parseMediaType(doc.getContentType()))
                .body(resource);
    }

    /**
     * Handles the document deletion request from the UI.
     */
    @PostMapping("/documents/delete/{id}")
    public String deleteDocument(@PathVariable Long id,
                                 @RequestParam("type") String type,
                                 RedirectAttributes redirectAttributes) {
        try {
            documentService.deleteDocument(id);
            redirectAttributes.addFlashAttribute("message", "Document deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Error: Could not delete the document. " + e.getMessage());
        }
        // Redirect back to the document list page for the correct type
        return "redirect:/documents/" + type;
    }


}