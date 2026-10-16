package com.smartcampus.maintenance.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SecurityAndControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicEndpointsShouldBeAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));

        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));

        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"));
    }

    @Test
    void protectedEndpointsShouldRedirectUnauthenticatedUsersToLogin() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(get("/student/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        mockMvc.perform(get("/staff/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "student@smartcampus.com", roles = {"STUDENT"})
    void studentShouldNotAccessAdminOrStaffEndpoints() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/staff/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("admin@smartcampus.com")
    void adminCanAccessAdminDashboardAndComplaintsList() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attributeExists("stats", "recentComplaints"));

        mockMvc.perform(get("/admin/complaints"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/complaints_list"))
                .andExpect(model().attributeExists("complaints", "categories", "buildings", "staffMembers"));
    }

    @Test
    @WithUserDetails("student@smartcampus.com")
    void studentCanAccessDashboardAndSubmitComplaint() throws Exception {
        mockMvc.perform(get("/student/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/dashboard"))
                .andExpect(model().attributeExists("stats", "recentComplaints"));

        mockMvc.perform(get("/student/complaints/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/complaint_new"));
    }

    @Test
    @WithUserDetails("staff@smartcampus.com")
    void staffCanAccessStaffDashboardAndAssignedTasks() throws Exception {
        mockMvc.perform(get("/staff/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("staff/dashboard"))
                .andExpect(model().attributeExists("stats", "recentTasks"));

        mockMvc.perform(get("/staff/complaints"))
                .andExpect(status().isOk())
                .andExpect(view().name("staff/complaints_list"))
                .andExpect(model().attributeExists("complaints"));
    }

    @Test
    void studentRegistrationValidationAndSuccessFlow() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("name", "New Test Student")
                        .param("email", "newstudent@smartcampus.com")
                        .param("registerNumber", "STU2026999")
                        .param("department", "Mechanical Engineering")
                        .param("phoneNumber", "+1 555-8888")
                        .param("password", "Student@123")
                        .param("confirmPassword", "Student@123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}
