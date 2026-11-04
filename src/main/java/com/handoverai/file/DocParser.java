package com.handoverai.file;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Component
public class DocParser {

    public static class ParsedChunk {
        private final int chunkIndex;
        private final int pageNumber;
        private final String sectionTitle;
        private final String content;
        private final String sourceCitation;

        public ParsedChunk(int chunkIndex, int pageNumber, String sectionTitle, String content, String sourceCitation) {
            this.chunkIndex = chunkIndex;
            this.pageNumber = pageNumber;
            this.sectionTitle = sectionTitle;
            this.content = content;
            this.sourceCitation = sourceCitation;
        }

        public int getChunkIndex() { return chunkIndex; }
        public int getPageNumber() { return pageNumber; }
        public String getSectionTitle() { return sectionTitle; }
        public String getContent() { return content; }
        public String getSourceCitation() { return sourceCitation; }
    }

    public List<ParsedChunk> parseDocument(File file, String originalFilename) {
        String lower = originalFilename.toLowerCase();
        if (lower.endsWith(".pdf")) {
            return parsePdf(file, originalFilename);
        } else if (lower.endsWith(".docx")) {
            return parseDocx(file, originalFilename);
        } else {
            return parsePlainText(file, originalFilename);
        }
    }

    private List<ParsedChunk> parsePdf(File file, String filename) {
        List<ParsedChunk> chunks = new ArrayList<>();
        try (PDDocument doc = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            int totalPages = doc.getNumberOfPages();
            int chunkIdx = 0;
            for (int page = 1; page <= totalPages; page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                String text = stripper.getText(doc).trim();
                if (!text.isEmpty()) {
                    String citation = filename + ", Page " + page;
                    chunks.add(new ParsedChunk(chunkIdx++, page, "Page " + page, text, citation));
                }
            }
        } catch (Exception e) {
            // Fallback: single chunk with error note
            chunks.add(new ParsedChunk(0, 1, "General", "Failed to parse PDF content: " + e.getMessage(), filename));
        }
        return chunks;
    }

    private List<ParsedChunk> parseDocx(File file, String filename) {
        List<ParsedChunk> chunks = new ArrayList<>();
        try (InputStream is = new FileInputStream(file);
             XWPFDocument doc = new XWPFDocument(is)) {
            StringBuilder sb = new StringBuilder();
            int chunkIdx = 0;
            int sectionNum = 1;
            for (XWPFParagraph p : doc.getParagraphs()) {
                String text = p.getText().trim();
                if (!text.isEmpty()) {
                    sb.append(text).append("\n");
                    if (sb.length() > 600) {
                        String citation = filename + ", Section " + sectionNum;
                        chunks.add(new ParsedChunk(chunkIdx++, sectionNum, "Section " + sectionNum, sb.toString(), citation));
                        sb.setLength(0);
                        sectionNum++;
                    }
                }
            }
            if (sb.length() > 0) {
                String citation = filename + ", Section " + sectionNum;
                chunks.add(new ParsedChunk(chunkIdx, sectionNum, "Section " + sectionNum, sb.toString(), citation));
            }
        } catch (Exception e) {
            chunks.add(new ParsedChunk(0, 1, "General", "Failed to parse DOCX content: " + e.getMessage(), filename));
        }
        return chunks;
    }

    private List<ParsedChunk> parsePlainText(File file, String filename) {
        List<ParsedChunk> chunks = new ArrayList<>();
        try {
            String text = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            String[] lines = text.split("\n\n");
            int chunkIdx = 0;
            for (int i = 0; i < lines.length; i++) {
                String trimmed = lines[i].trim();
                if (!trimmed.isEmpty()) {
                    String citation = filename + ", Section " + (i + 1);
                    chunks.add(new ParsedChunk(chunkIdx++, i + 1, "Section " + (i + 1), trimmed, citation));
                }
            }
            if (chunks.isEmpty() && !text.trim().isEmpty()) {
                chunks.add(new ParsedChunk(0, 1, "General", text.trim(), filename + ", Section 1"));
            }
        } catch (Exception e) {
            chunks.add(new ParsedChunk(0, 1, "General", "Failed to parse TXT content: " + e.getMessage(), filename));
        }
        return chunks;
    }
}
