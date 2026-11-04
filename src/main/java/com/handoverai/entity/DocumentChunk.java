package com.handoverai.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "document_chunks")
public class DocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    private int chunkIndex;
    private int pageNumber;
    private String sectionTitle;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    private String sourceCitation; // e.g., "VPS Renewal SOP.pdf, Page 3, Section 2.1"

    public DocumentChunk() {}

    public DocumentChunk(Document document, int chunkIndex, int pageNumber, String sectionTitle, String content, String sourceCitation) {
        this.document = document;
        this.chunkIndex = chunkIndex;
        this.pageNumber = pageNumber;
        this.sectionTitle = sectionTitle;
        this.content = content;
        this.sourceCitation = sourceCitation;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Document getDocument() { return document; }
    public void setDocument(Document document) { this.document = document; }

    public int getChunkIndex() { return chunkIndex; }
    public void setChunkIndex(int chunkIndex) { this.chunkIndex = chunkIndex; }

    public int getPageNumber() { return pageNumber; }
    public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }

    public String getSectionTitle() { return sectionTitle; }
    public void setSectionTitle(String sectionTitle) { this.sectionTitle = sectionTitle; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getSourceCitation() { return sourceCitation; }
    public void setSourceCitation(String sourceCitation) { this.sourceCitation = sourceCitation; }
}
