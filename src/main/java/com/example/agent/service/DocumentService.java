package com.example.agent.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG servisi.
 *
 * Yükleme pipeline'ı:
 *   MultipartFile → DocumentReader → TokenTextSplitter → SimpleVectorStore
 *                                                              ↑
 *                                                    OllamaEmbeddingModel
 *                                                    (nomic-embed-text)
 *
 * Sorgulama:
 *   query string → embed → cosine similarity → topK chunk → döndür
 *
 * SimpleVectorStore in-memory çalışır — uygulama kapanınca sıfırlanır.
 * Kalıcılık için pgvector veya ChromaDB'ye geçmek yeterli (sadece dep swap).
 */
@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final SimpleVectorStore vectorStore;

    // Spring AI otomatik olarak OllamaEmbeddingModel bean'ini inject eder.
    // Bu model, nomic-embed-text ile metni vektöre çevirir.
    public DocumentService(EmbeddingModel embeddingModel) {
        this.vectorStore = SimpleVectorStore.builder(embeddingModel).build();
    }

    /**
     * Dosyayı yükle, chunk'la, embed et, VectorStore'a kaydet.
     * Desteklenen formatlar: .txt, .md
     */
    public String loadDocument(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown";
        byte[] bytes = file.getBytes();
        ByteArrayResource resource = new ByteArrayResource(bytes) {
            @Override
            public String getFilename() { return filename; }
        };

        // Format'a göre uygun reader seç
        List<Document> rawDocs;
        if (filename.endsWith(".md")) {
            MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                    .withHorizontalRuleCreateDocument(true)
                    .withIncludeCodeBlock(true)
                    .withIncludeBlockquote(true)
                    .build();
            rawDocs = new MarkdownDocumentReader(resource, config).get();
        } else {
            // .txt ve diğer düz metin formatları
            rawDocs = new TextReader(resource).get();
        }

        // Uzun metinleri ~800 token'lık chunk'lara böl.
        // LLM'e context olarak vermek için küçük parçalar daha iyi sonuç verir.
        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(800)
                .withMinChunkSizeChars(100)
                .withKeepSeparator(true)
                .build();

        List<Document> chunks = splitter.split(rawDocs);

        // Her chunk için nomic-embed-text ile embedding üret ve sakla
        vectorStore.add(chunks);

        return String.format("'%s' yüklendi: %d chunk oluşturuldu ve vektör DB'ye kaydedildi.",
                filename, chunks.size());
    }

    /**
     * Kullanıcı sorusunu embed et, en yakın 3 chunk'ı bul, birleştirip döndür.
     * Bu metin LLM'e context olarak verilir.
     */
    public String search(String query) {
        log.info(">>> searchDocuments tool çağrıldı | query: '{}'", query);

        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(3)
                        .build()
        );

        log.info(">>> {} chunk bulundu", results.size());

        if (results.isEmpty()) {
            return "İlgili doküman bulunamadı. Önce /documents/upload ile bir dosya yükle.";
        }

        return results.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n---\n"));
    }
}
