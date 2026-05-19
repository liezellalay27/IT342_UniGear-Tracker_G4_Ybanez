package com.unigear.tracker.pattern.factory.validators;

import com.unigear.tracker.pattern.factory.interfaces.RequestValidator;
import org.springframework.web.multipart.MultipartFile;
import com.unigear.tracker.pattern.singleton.ConfigurationManager;

/**
 * Factory Method Pattern: Concrete Validator for PDF File Validation
 * Validates event approval PDF file
 */
public class PdfFileValidator implements RequestValidator {
    
    private final MultipartFile file;
    private static final String PDF_CONTENT_TYPE = "application/pdf";
    
    public PdfFileValidator(MultipartFile file) {
        this.file = file;
    }
    
    @Override
    public void validate() {
        // File is optional, so null is valid
        if (file == null || file.isEmpty()) {
            return;
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals(PDF_CONTENT_TYPE)) {
            throw new IllegalArgumentException("Only PDF files are allowed for event approval");
        }
        
        ConfigurationManager config = ConfigurationManager.getInstance();
        if (!config.isFileSizeValid(file.getSize())) {
            throw new IllegalArgumentException(
                "PDF file cannot exceed " + (config.getMaxFileSize() / 1024 / 1024) + "MB"
            );
        }
    }
    
    @Override
    public String getValidatorName() {
        return "PdfFileValidator";
    }
}
