package com.cityconnect.app.controller;

import com.cityconnect.app.model.Complaint;
import com.cityconnect.app.repository.ComplaintRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureMockMvc
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Test
    void testHomePage() throws Exception {
        mockMvc.perform(get("/"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/templates/index.html"));
    }

    @Test
    void testComplaintsPage() throws Exception {
        mockMvc.perform(get("/complaints"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/templates/complaints.html"));
    }

    @Test
    void testIncidentsPage() throws Exception {
        mockMvc.perform(get("/incidents"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/templates/incidents.html"));
    }

    @Test
    void testSubmitComplaint() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test image".getBytes());
        mockMvc.perform(multipart("/api/complaints")
                .param("complaintType", "kseb")
                .param("description", "Power outage")
                .param("location", "Main St")
                .file(image))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/complaints?success=true"));

        // Verify the complaint was saved
        var complaints = complaintRepository.findAll();
        assertEquals(1, complaints.size(), "Exactly one complaint should be saved");
        Complaint savedComplaint = complaints.get(0);
        assertEquals("kseb", savedComplaint.getComplaintType());
        assertEquals("Power outage", savedComplaint.getDescription());
        assertEquals("Main St", savedComplaint.getLocation());
        assertEquals("Open", savedComplaint.getStatus());
        assertEquals("test.jpg", savedComplaint.getImageFileName());
        assertNotNull(savedComplaint.getDate());
    }

    @Test
    void testGetAllComplaints() throws Exception {
        // Save a test complaint
        Complaint complaint = new Complaint();
        complaint.setComplaintType("kwa");
        complaint.setDescription("Water leak");
        complaint.setLocation("Park Ave");
        complaint.setStatus("Open");
        complaint.setDate(java.time.LocalDate.now());
        complaintRepository.save(complaint);

        // Test the API endpoint
        mockMvc.perform(get("/api/complaints")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("kwa")))
                .andExpect(content().string(containsString("Water leak")))
                .andExpect(content().string(containsString("Park Ave")))
                .andExpect(content().string(containsString("Open")));
    }
}