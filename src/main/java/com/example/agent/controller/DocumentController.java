package com.example.agent.controller;

import com.example.agent.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * Dosya yükle ve vektör DB'ye indexle.
     *
     * Kullanım:
     *   curl -X POST http://localhost:8080/documents/upload -F "file=@/path/to/file.txt"
     *   curl -X POST http://localhost:8080/documents/upload -F "file=@/path/to/file.md"
     */
    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Dosya boş.");
        }

        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "";
        if (!filename.endsWith(".txt") && !filename.endsWith(".md")) {
            return ResponseEntity.badRequest().body("Sadece .txt ve .md dosyaları desteklenir.");
        }

        try {
            String result = documentService.loadDocument(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Dosya yüklenirken hata: " + e.getMessage());
        }
    }
}
