package com.unigear.tracker.features.auth.service;

import com.unigear.tracker.features.auth.config.AuthEmailProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.awt.Color;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final AuthEmailProperties emailProperties;
    private final SpringTemplateEngine templateEngine;

    public EmailService(@Nullable JavaMailSender mailSender, AuthEmailProperties emailProperties, @Nullable SpringTemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.emailProperties = emailProperties;
        this.templateEngine = templateEngine;
    }

    public String renderTemplate(String templateName, Map<String, Object> model) {
        if (templateEngine == null) return null;
        Context ctx = new Context();
        if (model != null) ctx.setVariables(model);
        return templateEngine.process(templateName, ctx);
    }

    public void sendWelcomeEmail(String to, String name) {
        sendMessage(
                to,
                "Welcome to UniGear Tracker",
                "Hi " + safeName(name) + ",\n\nYour UniGear Tracker account has been created successfully. You can now sign in using your email address.\n\nLogin here: " + emailProperties.loginUrl() + "\n\nIf you did not create this account, please ignore this email."
        );
    }

    public void sendPasswordResetEmail(String to, String name, String resetToken) {
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("name", safeName(name));
        model.put("resetUrl", emailProperties.passwordResetUrl(resetToken));
        model.put("supportEmail", emailProperties.senderAddress().isBlank() ? "support@unigear.com" : emailProperties.senderAddress());

        String rendered = renderTemplate("forgot-password", model);
        if (rendered != null) {
            sendHtmlEmail(to, "UniGear Tracker Password Reset", rendered);
            return;
        }

        sendMessage(
                to,
                "UniGear Tracker Password Reset",
                "Hi " + safeName(name) + ",\n\nWe received a request to reset your password. Use the link below to create a new password:\n\n" + emailProperties.passwordResetUrl(resetToken) + "\n\nThis link will expire soon. If you did not request a password reset, you can ignore this email."
        );
    }

    /**
     * Public helper to send arbitrary emails from other services.
     */
    public void sendGenericEmail(String to, String subject, String text) {
        sendMessage(to, subject, text);
    }

    /**
     * Send an HTML email. Falls back to plain text if HTML sending is not configured.
     */
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        sendHtmlEmailWithAttachment(to, subject, htmlBody, null, null, null);
    }

    public void sendHtmlEmailWithAttachment(String to, String subject, String htmlBody,
                                            @Nullable byte[] attachmentBytes,
                                            @Nullable String attachmentFilename,
                                            @Nullable String attachmentContentType) {
        if (mailSender == null) {
            log.warn("Mail sender is not configured. Falling back to plain text for {}", to);
            sendMessage(to, subject, htmlBody.replaceAll("<[^>]*>", ""));
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            if (!emailProperties.senderAddress().isBlank()) {
                helper.setFrom(emailProperties.senderAddress());
            }
            // try to attach inline logo if available
            try {
                ClassPathResource logo = new ClassPathResource("static/images/logo.png");
                if (logo.exists()) {
                    helper.addInline("logo.png", logo);
                }
            } catch (Exception e) {
                log.debug("No inline logo attached: {}", e.getMessage());
            }

            if (attachmentBytes != null && attachmentBytes.length > 0 && attachmentFilename != null && !attachmentFilename.isBlank()) {
                helper.addAttachment(
                        attachmentFilename,
                        new ByteArrayResource(attachmentBytes),
                        attachmentContentType != null ? attachmentContentType : "application/octet-stream"
                );
            }

            mailSender.send(mimeMessage);
        } catch (MessagingException ex) {
            log.warn("Failed to send HTML email to {}: {}. Falling back to plain text.", to, ex.getMessage());
            sendMessage(to, subject, htmlBody.replaceAll("<[^>]*>", ""));
        } catch (Exception ex) {
            log.warn("Unexpected error sending HTML email to {}: {}", to, ex.getMessage());
            throw new RuntimeException("Unable to send email right now. Please try again later.");
        }
    }
    private void sendMessage(String to, String subject, String text) {
        if (mailSender == null) {
            log.warn("Mail sender is not configured. Skipping email to {} with subject {}", to, subject);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        if (!emailProperties.senderAddress().isBlank()) {
            message.setFrom(emailProperties.senderAddress());
        }

        try {
            mailSender.send(message);
        } catch (Exception ex) {
            log.warn("Failed to send email to {}: {}", to, ex.getMessage());
            throw new RuntimeException("Unable to send email right now. Please try again later.");
        }
    }

    private String safeName(String name) {
        return (name == null || name.isBlank()) ? "User" : name;
    }

    public byte[] generateApprovalPdf(Map<String, Object> model) {
        String requestId = stringValue(model.get("requestId"));
        String equipmentName = stringValue(model.get("equipmentName"));
        String quantity = stringValue(model.get("quantity"));
        String studentName = stringValue(model.get("studentName"));
        String schoolIdNumber = stringValue(model.get("schoolIdNumber"));
        String borrowDate = stringValue(model.get("borrowDate"));
        String returnDate = stringValue(model.get("returnDate"));
        String yearLevel = stringValue(model.get("yearLevel"));
        String course = stringValue(model.get("course"));
        String notes = stringValue(model.get("notes"));

        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                float margin = 50f;
                float width = page.getMediaBox().getWidth() - (2 * margin);
                float y = page.getMediaBox().getHeight() - 70f;

                content.beginText();
                content.setLeading(16f);

                y = writeLine(content, PDType1Font.HELVETICA_BOLD, 20f, margin, y, "UniGear Tracker");
                y = writeLine(content, PDType1Font.HELVETICA_BOLD, 16f, margin, y - 6f, "Request Approved");

                content.setNonStrokingColor(new Color(102, 0, 0));
                y = writeWrappedText(content, PDType1Font.HELVETICA, 11f, margin, y - 10f, width,
                        "Your equipment request has been approved. Please review the details below and keep this PDF for your records.");
                content.setNonStrokingColor(Color.BLACK);

                y -= 12f;
                y = writeKeyValue(content, margin, width, y, "Request ID", requestId);
                y = writeKeyValue(content, margin, width, y, "Equipment", equipmentName);
                y = writeKeyValue(content, margin, width, y, "Quantity", quantity);
                y = writeKeyValue(content, margin, width, y, "Student", studentName);
                y = writeKeyValue(content, margin, width, y, "School ID", schoolIdNumber);
                y = writeKeyValue(content, margin, width, y, "Borrow Date", borrowDate);
                y = writeKeyValue(content, margin, width, y, "Return Date", returnDate);
                y = writeKeyValue(content, margin, width, y, "Year / Course", (yearLevel + " | " + course).trim());

                if (!notes.isBlank()) {
                    y = writeWrappedText(content, PDType1Font.HELVETICA, 11f, margin, y - 4f, width,
                            "Notes: " + notes);
                }

                y -= 16f;
                writeWrappedText(content, PDType1Font.HELVETICA_OBLIQUE, 10f, margin, y, width,
                        "If you have questions, please contact UniGear Tracker support.");

                content.endText();
            }

            document.save(output);
            return output.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to generate approval PDF: " + ex.getMessage(), ex);
        }
    }

    private float writeKeyValue(PDPageContentStream content, float margin, float width, float y, String label, String value) throws IOException {
        y = writeLine(content, PDType1Font.HELVETICA_BOLD, 11f, margin, y, label + ":");
        return writeWrappedText(content, PDType1Font.HELVETICA, 11f, margin + 110f, y - 2f, width - 110f, value);
    }

    private float writeLine(PDPageContentStream content, PDType1Font font, float size, float x, float y, String text) throws IOException {
        content.setFont(font, size);
        content.newLineAtOffset(x, y);
        content.showText(text);
        content.newLineAtOffset(-x, -y);
        return y - (size + 2f);
    }

    private float writeWrappedText(PDPageContentStream content, PDType1Font font, float size, float x, float y, float maxWidth, String text) throws IOException {
        content.setFont(font, size);
        float currentY = y;
        for (String line : wrapText(font, size, maxWidth, text)) {
            content.newLineAtOffset(x, currentY);
            content.showText(line);
            content.newLineAtOffset(-x, -currentY);
            currentY -= size + 3f;
        }
        return currentY;
    }

    private List<String> wrapText(PDType1Font font, float fontSize, float maxWidth, String text) throws IOException {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return lines;
        }

        String[] words = text.trim().split("\\s+");
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            String candidate = current.length() == 0 ? word : current + " " + word;
            float candidateWidth = font.getStringWidth(candidate) / 1000f * fontSize;
            if (candidateWidth > maxWidth && current.length() > 0) {
                lines.add(current.toString());
                current = new StringBuilder(word);
            } else {
                if (current.length() > 0) current.append(' ');
                current.append(word);
            }
        }
        if (current.length() > 0) {
            lines.add(current.toString());
        }
        return lines;
    }

    private String stringValue(Object value) {
        return value == null ? "" : value.toString();
    }
}