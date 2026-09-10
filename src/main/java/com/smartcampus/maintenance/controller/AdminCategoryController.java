package com.smartcampus.maintenance.controller;

import com.smartcampus.maintenance.entity.Category;
import com.smartcampus.maintenance.entity.User;
import com.smartcampus.maintenance.security.CustomUserDetails;
import com.smartcampus.maintenance.service.CategoryService;
import com.smartcampus.maintenance.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public String listCategories(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User admin = userDetails.getUser();
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("unreadCount", notificationService.getUnreadCount(admin));
        return "admin/categories";
    }

    @PostMapping
    public String createCategory(@RequestParam("name") String name,
                                 @RequestParam("description") String description,
                                 RedirectAttributes redirectAttributes) {
        try {
            categoryService.createCategory(name, description);
            redirectAttributes.addFlashAttribute("successMessage", "Category '" + name + "' added successfully");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/{id}/toggle")
    public String toggleCategory(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Category category = categoryService.getById(id);
        categoryService.updateCategory(id, category.getName(), category.getDescription(), !category.isActive());
        redirectAttributes.addFlashAttribute("successMessage", "Category status updated");
        return "redirect:/admin/categories";
    }
}
