package com.unigear.tracker.features.request.controller;

import com.unigear.tracker.features.user.entity.User;
import com.unigear.tracker.features.request.repository.EquipmentRequestRepository;
import com.unigear.tracker.features.user.repository.UserRepository;
import com.unigear.tracker.features.equipment.entity.Equipment;
import com.unigear.tracker.features.equipment.repository.EquipmentRepository;
import com.unigear.tracker.features.auth.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("PDF Upload Feature Integration Tests")
class PdfUploadIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EquipmentRequestRepository equipmentRequestRepository;
    
    @Autowired
    private EquipmentRepository equipmentRepository;

    private String userToken;
    private String adminToken;
    private User testUser;
    private User adminUser;

    @BeforeEach
    void setup() {
        // Clean up
        equipmentRequestRepository.deleteAll();
        equipmentRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("user@test.com");
        testUser.setPassword("hashedpassword");
        testUser.setRole(User.Role.STUDENT);
        testUser = userRepository.save(testUser);

        adminUser = new User();
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@test.com");
        adminUser.setPassword("hashedpassword");
        adminUser.setRole(User.Role.ADMIN);
        adminUser = userRepository.save(adminUser);

        // Create test equipment
        Equipment laptop = new Equipment();
        laptop.setName("Laptop");
        laptop.setCategory("Computing");
        laptop.setDescription("Laptop computer");
        laptop.setLocation("Lab 101");
        laptop.setSpecifications("Intel i7, 16GB RAM");
        laptop.setTotalQuantity(5);
        laptop.setAvailableQuantity(5);
        equipmentRepository.save(laptop);

        Equipment projector = new Equipment();
        projector.setName("Projector");
        projector.setCategory("Equipment");
        projector.setDescription("Media projector");
        projector.setLocation("Hall 1");
        projector.setSpecifications("1080p, 3000 lumens");
        projector.setTotalQuantity(3);
        projector.setAvailableQuantity(3);
        equipmentRepository.save(projector);

        Equipment camera = new Equipment();
        camera.setName("Camera");
        camera.setCategory("Equipment");
        camera.setDescription("Digital camera");
        camera.setLocation("Lab 201");
        camera.setSpecifications("24MP, 4K video");
        camera.setTotalQuantity(2);
        camera.setAvailableQuantity(2);
        equipmentRepository.save(camera);

        Equipment microphone = new Equipment();
        microphone.setName("Microphone");
        microphone.setCategory("Equipment");
        microphone.setDescription("Audio microphone");
        microphone.setLocation("Studio");
        microphone.setSpecifications("Condenser, XLR");
        microphone.setTotalQuantity(4);
        microphone.setAvailableQuantity(4);
        equipmentRepository.save(microphone);

        Equipment speaker = new Equipment();
        speaker.setName("Speaker");
        speaker.setCategory("Equipment");
        speaker.setDescription("Audio speaker");
        speaker.setLocation("Studio");
        speaker.setSpecifications("Stereo, 100W");
        speaker.setTotalQuantity(3);
        speaker.setAvailableQuantity(3);
        equipmentRepository.save(speaker);

        Equipment monitor = new Equipment();
        monitor.setName("Monitor");
        monitor.setCategory("Equipment");
        monitor.setDescription("Computer monitor");
        monitor.setLocation("Lab 102");
        monitor.setSpecifications("27\" 4K");
        monitor.setTotalQuantity(6);
        monitor.setAvailableQuantity(6);
        equipmentRepository.save(monitor);

        // Generate tokens
        userToken = jwtUtil.generateJwtToken(testUser.getEmail());
        adminToken = jwtUtil.generateJwtToken(adminUser.getEmail());
    }

    // ==================== TEST CASE 1: PDF Upload with Valid File ====================
    @Test
    @DisplayName("TEST 1: Upload PDF with valid file - Should succeed and store PDF")
    void testUploadValidPdf() throws Exception {
        // Arrange
        byte[] pdfContent = getPdfFileContent();
        MockMultipartFile pdfFile = new MockMultipartFile(
                "eventApprovalPdf",
                "approval.pdf",
                "application/pdf",
                pdfContent
        );

        LocalDate borrowDate = LocalDate.now();
        LocalDate returnDate = borrowDate.plusDays(7);

        // Act & Assert
        mockMvc.perform(multipart("/api/requests")
                .file(pdfFile)
                .param("equipmentName", "Laptop")
                .param("category", "Computing")
                .param("description", "For event")
                .param("quantity", "1")
                .param("borrowDate", borrowDate.toString())
                .param("returnDate", returnDate.toString())
                .param("studentName", "Test User")
                .param("schoolIdNumber", "17-0635-488")
                .param("yearLevel", "2nd Year")
                .param("course", "BS Computer Science")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.equipmentName", equalTo("Laptop")))
                .andExpect(jsonPath("$.eventApprovalPdf", notNullValue()))
                .andReturn();

        System.out.println("✅ TEST 1 PASSED: Valid PDF uploaded successfully");
    }

    // ==================== TEST CASE 2: PDF Upload Without File (Optional) ====================
    @Test
    @DisplayName("TEST 2: Upload without PDF - Should succeed (PDF is optional)")
    void testUploadWithoutPdf() throws Exception {
        // Arrange
        LocalDate borrowDate = LocalDate.now();
        LocalDate returnDate = borrowDate.plusDays(7);

        // Act & Assert
        mockMvc.perform(multipart("/api/requests")
                .param("equipmentName", "Projector")
                .param("category", "Equipment")
                .param("description", "Regular borrow")
                .param("quantity", "1")
                .param("borrowDate", borrowDate.toString())
                .param("returnDate", returnDate.toString())
                .param("studentName", "Test User")
                .param("schoolIdNumber", "17-0635-488")
                .param("yearLevel", "2nd Year")
                .param("course", "BS Computer Science")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.equipmentName", equalTo("Projector")))
                .andExpect(jsonPath("$.eventApprovalPdf", nullValue()))
                .andReturn();

        System.out.println("✅ TEST 2 PASSED: Request created without PDF");
    }

    // ==================== TEST CASE 3: PDF Rejection (Non-PDF File) ====================
    @Test
    @DisplayName("TEST 3: Reject non-PDF files - Should fail with 400 error")
    void testRejectNonPdfFile() throws Exception {
        // Arrange
        byte[] txtContent = "This is not a PDF".getBytes();
        MockMultipartFile txtFile = new MockMultipartFile(
                "eventApprovalPdf",
                "document.txt",
                "text/plain",
                txtContent
        );

        LocalDate borrowDate = LocalDate.now();
        LocalDate returnDate = borrowDate.plusDays(7);

        // Act & Assert
        mockMvc.perform(multipart("/api/requests")
                .file(txtFile)
                .param("equipmentName", "Monitor")
                .param("category", "Equipment")
                .param("description", "Event request")
                .param("quantity", "2")
                .param("borrowDate", borrowDate.toString())
                .param("returnDate", returnDate.toString())
                .param("studentName", "Test User")
                .param("schoolIdNumber", "17-0635-488")
                .param("yearLevel", "2nd Year")
                .param("course", "BS Computer Science")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andReturn();

        System.out.println("✅ TEST 3 PASSED: Non-PDF file rejected");
    }

    // ==================== TEST CASE 4: User PDF Download ====================
    @Test
    @DisplayName("TEST 4: User downloads their own PDF - Should succeed")
    void testUserDownloadOwnPdf() throws Exception {
        // Arrange - Create request with PDF
        byte[] pdfContent = getPdfFileContent();
        MockMultipartFile pdfFile = new MockMultipartFile(
                "eventApprovalPdf",
                "approval.pdf",
                "application/pdf",
                pdfContent
        );

        LocalDate borrowDate = LocalDate.now();
        LocalDate returnDate = borrowDate.plusDays(7);

        MvcResult createResult = mockMvc.perform(multipart("/api/requests")
                .file(pdfFile)
                .param("equipmentName", "Camera")
                .param("category", "Equipment")
                .param("description", "Event coverage")
                .param("quantity", "1")
                .param("borrowDate", borrowDate.toString())
                .param("returnDate", returnDate.toString())
                .param("studentName", "Test User")
                .param("schoolIdNumber", "17-0635-488")
                .param("yearLevel", "2nd Year")
                .param("course", "BS Computer Science")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andReturn();

        // Extract request ID from response
        String response = createResult.getResponse().getContentAsString();
        Long requestId = extractRequestId(response);

        // Act & Assert - Download PDF
        mockMvc.perform(get("/api/requests/" + requestId + "/pdf")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andReturn();

        System.out.println("✅ TEST 4 PASSED: User downloaded their own PDF");
    }

    // ==================== TEST CASE 5: Admin PDF Download ====================
    @Test
    @DisplayName("TEST 5: Admin downloads user PDF - Should succeed")
    void testAdminDownloadUserPdf() throws Exception {
        // Arrange - User creates request with PDF
        byte[] pdfContent = getPdfFileContent();
        MockMultipartFile pdfFile = new MockMultipartFile(
                "eventApprovalPdf",
                "approval.pdf",
                "application/pdf",
                pdfContent
        );

        LocalDate borrowDate = LocalDate.now();
        LocalDate returnDate = borrowDate.plusDays(7);

        MvcResult createResult = mockMvc.perform(multipart("/api/requests")
                .file(pdfFile)
                .param("equipmentName", "Microphone")
                .param("category", "Equipment")
                .param("description", "Event audio")
                .param("quantity", "3")
                .param("borrowDate", borrowDate.toString())
                .param("returnDate", returnDate.toString())
                .param("studentName", "Test User")
                .param("schoolIdNumber", "17-0635-488")
                .param("yearLevel", "2nd Year")
                .param("course", "BS Computer Science")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andReturn();

        String response = createResult.getResponse().getContentAsString();
        Long requestId = extractRequestId(response);

        // Act & Assert - Admin downloads PDF
        mockMvc.perform(get("/api/requests/" + requestId + "/pdf")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andReturn();

        System.out.println("✅ TEST 5 PASSED: Admin downloaded user PDF");
    }

    // ==================== TEST CASE 6: Unauthorized PDF Download ====================
    @Test
    @DisplayName("TEST 6: User downloads other user's PDF - Should be blocked")
    void testUnauthorizedPdfDownload() throws Exception {
        // Arrange - User1 creates request with PDF
        byte[] pdfContent = getPdfFileContent();
        MockMultipartFile pdfFile = new MockMultipartFile(
                "eventApprovalPdf",
                "approval.pdf",
                "application/pdf",
                pdfContent
        );

        LocalDate borrowDate = LocalDate.now();
        LocalDate returnDate = borrowDate.plusDays(7);

        MvcResult createResult = mockMvc.perform(multipart("/api/requests")
                .file(pdfFile)
                .param("equipmentName", "Speaker")
                .param("category", "Equipment")
                .param("description", "Event sound")
                .param("quantity", "2")
                .param("borrowDate", borrowDate.toString())
                .param("returnDate", returnDate.toString())
                .param("studentName", "Test User")
                .param("schoolIdNumber", "17-0635-488")
                .param("yearLevel", "2nd Year")
                .param("course", "BS Computer Science")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andReturn();

        String response = createResult.getResponse().getContentAsString();
        Long requestId = extractRequestId(response);

        // Create second user
        User anotherUser = new User();
        anotherUser.setName("Another User");
        anotherUser.setEmail("another@test.com");
        anotherUser.setPassword("hashedpassword");
        anotherUser.setRole(User.Role.STUDENT);
        anotherUser = userRepository.save(anotherUser);
        String anotherUserToken = jwtUtil.generateJwtToken(anotherUser.getEmail());

        // Act & Assert - Another user tries to download PDF
        mockMvc.perform(get("/api/requests/" + requestId + "/pdf")
                .header("Authorization", "Bearer " + anotherUserToken))
                .andExpect(status().isForbidden())
                .andReturn();

        System.out.println("✅ TEST 6 PASSED: Unauthorized access blocked");
    }

    // ==================== TEST CASE 7: Missing Authorization ====================
    @Test
    @DisplayName("TEST 7: PDF download without auth - Should return 401")
    void testPdfDownloadWithoutAuth() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/requests/1/pdf"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        System.out.println("✅ TEST 7 PASSED: Unauthenticated access rejected");
    }

    // ==================== TEST CASE 8: Large File Rejection ====================
    @Test
    @DisplayName("TEST 8: Reject file exceeding size limit - Should fail")
    void testRejectLargeFile() throws Exception {
        // Arrange - Create a file larger than 10MB limit (simulated)
        byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
        MockMultipartFile largeFile = new MockMultipartFile(
                "eventApprovalPdf",
                "large.pdf",
                "application/pdf",
                largeContent
        );

        LocalDate borrowDate = LocalDate.now();
        LocalDate returnDate = borrowDate.plusDays(7);

        // Act & Assert
        mockMvc.perform(multipart("/api/requests")
                .file(largeFile)
                .param("equipmentName", "Equipment")
                .param("category", "Category")
                .param("description", "Description")
                .param("quantity", "1")
                .param("borrowDate", borrowDate.toString())
                .param("returnDate", returnDate.toString())
                .param("studentName", "Test User")
                .param("schoolIdNumber", "17-0635-488")
                .param("yearLevel", "2nd Year")
                .param("course", "BS Computer Science")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isPayloadTooLarge())
                .andReturn();

        System.out.println("✅ TEST 8 PASSED: Large file rejected");
    }

    // ==================== Helper Methods ====================
    private byte[] getPdfFileContent() {
        // Minimal valid PDF header
        return new byte[]{
                0x25, 0x50, 0x44, 0x46, 0x2D, 0x31, 0x2E, 0x34, // %PDF-1.4
                0x0A, 0x25, (byte)0xE2, (byte)0xE3, (byte)0xCF, (byte)0xD3, 0x0A       // Line with binary data
        };
    }

    private Long extractRequestId(String jsonResponse) {
        // Simple extraction - in real code, use ObjectMapper
        int idIndex = jsonResponse.indexOf("\"id\":");
        if (idIndex != -1) {
            String idString = jsonResponse.substring(idIndex + 5);
            idString = idString.substring(0, idString.indexOf(","));
            return Long.parseLong(idString.trim());
        }
        return 1L;
    }
}
