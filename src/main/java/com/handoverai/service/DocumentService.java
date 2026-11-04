package com.handoverai.service;

import com.handoverai.entity.Document;
import com.handoverai.entity.DocumentChunk;
import com.handoverai.entity.Project;
import com.handoverai.file.DocParser;
import com.handoverai.repository.DocumentChunkRepository;
import com.handoverai.repository.DocumentRepository;
import com.handoverai.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository chunkRepository;
    private final ProjectRepository projectRepository;
    private final DocParser docParser;
    private final String uploadDir;

    public DocumentService(DocumentRepository documentRepository,
                           DocumentChunkRepository chunkRepository,
                           ProjectRepository projectRepository,
                           DocParser docParser,
                           @Value("${handoverai.upload-dir:./uploads}") String uploadDir) {
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.projectRepository = projectRepository;
        this.docParser = docParser;
        this.uploadDir = uploadDir;

        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();
    }

    public List<Document> getProjectDocuments(Long projectId) {
        return documentRepository.findByProjectId(projectId);
    }

    public List<DocumentChunk> getProjectChunks(Long projectId) {
        return chunkRepository.findByDocumentProjectId(projectId);
    }

    @Transactional
    public Document uploadDocument(Long projectId, MultipartFile file, String uploadedBy) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) originalFilename = "document.txt";

        String extension = "";
        int i = originalFilename.lastIndexOf('.');
        if (i > 0) extension = originalFilename.substring(i + 1).toUpperCase();

        try {
            File targetFile = new File(uploadDir, System.currentTimeMillis() + "_" + originalFilename);
            try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                fos.write(file.getBytes());
            }

            Document doc = new Document(project, originalFilename, extension, targetFile.getAbsolutePath(), file.getSize(), uploadedBy);
            doc = documentRepository.save(doc);

            // Parse and chunk
            List<DocParser.ParsedChunk> parsed = docParser.parseDocument(targetFile, originalFilename);
            for (DocParser.ParsedChunk pc : parsed) {
                DocumentChunk chunk = new DocumentChunk(doc, pc.getChunkIndex(), pc.getPageNumber(), pc.getSectionTitle(), pc.getContent(), pc.getSourceCitation());
                chunkRepository.save(chunk);
            }

            return doc;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload and process document: " + e.getMessage(), e);
        }
    }
}
