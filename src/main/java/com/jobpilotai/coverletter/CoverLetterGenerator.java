package com.jobpilotai.coverletter;

import com.jobpilotai.ai.AiClient;
import com.jobpilotai.logs.AppLogger;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Handles generating and exporting cover letters.
 */
public class CoverLetterGenerator {

    /**
     * Calls AI to generate a cover letter based on Profile, Resume, and Job Description.
     */
    public static String generate(String profileData, String resumeData, String jobData) {
        try {
            return AiClient.getInstance().generateCoverLetter(profileData, resumeData, jobData);
        } catch (Exception e) {
            AppLogger.error("Failed to generate cover letter", e);
            return "Error generating cover letter: " + e.getMessage();
        }
    }

    /**
     * Exports a string cover letter to a DOCX file.
     */
    public static boolean exportToDocx(String text, File outputFile) {
        try (XWPFDocument document = new XWPFDocument();
             FileOutputStream out = new FileOutputStream(outputFile)) {
             
            String[] paragraphs = text.split("\n\n");
            for (String paraText : paragraphs) {
                XWPFParagraph paragraph = document.createParagraph();
                XWPFRun run = paragraph.createRun();
                // Handle single newlines within a paragraph block
                String[] lines = paraText.split("\n");
                for (int i = 0; i < lines.length; i++) {
                    run.setText(lines[i]);
                    if (i < lines.length - 1) {
                        run.addBreak();
                    }
                }
                run.setFontFamily("Arial");
                run.setFontSize(11);
            }
            
            document.write(out);
            AppLogger.info("Cover letter exported to DOCX: " + outputFile.getAbsolutePath());
            return true;
        } catch (Exception e) {
            AppLogger.error("Failed to export cover letter to DOCX", e);
            return false;
        }
    }
}
