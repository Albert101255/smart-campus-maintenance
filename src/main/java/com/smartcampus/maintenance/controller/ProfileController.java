package com.smartcampus.maintenance.controller;

import com.smartcampus.maintenance.dto.PasswordChangeRequest;
import com.smartcampus.maintenance.dto.ProfileUpdateRequest;
import com.smartcampus.maintenance.entity.User;
import com.smartcampus.maintenance.security.CustomUserDetails;
import com.smartcampus.maintenance.service.NotificationService;
import com.smartcampus.maintenance.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public String viewProfile(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userService.findById(userDetails.getId());

        ProfileUpdateRequest profileReq = new ProfileUpdateRequest();
        profileReq.setName(user.getName());
        profileReq.setPhoneNumber(user.getPhoneNumber());
        profileReq.setDepartment(user.getDepartment());

        model.addAttribute("user", user);
        model.addAttribute("profileUpdateRequest", profileReq);
        model.addAttribute("passwordChangeRequest", new PasswordChangeRequest());
        model.addAttribute("unreadCount", notificationService.getUnreadCount(user));

        return "profile/index";
    }

    @PostMapping("/update")
    public String updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @Valid @ModelAttribute("profileUpdateRequest") ProfileUpdateRequest request,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        User user = userDetails.getUser();
        if (!bindingResult.hasErrors()) {
            userService.updateProfile(user, request);
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update profile. Check your inputs.");
        }
        return "redirect:/profile";
    }

    @PostMapping("/password")
    public String changePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @Valid @ModelAttribute("passwordChangeRequest") PasswordChangeRequest request,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        User user = userDetails.getUser();
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Password must be at least 6 characters.");
            return "redirect:/profile";
        }

        try {
            userService.changePassword(user, request);
            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/profile";
    }
}
