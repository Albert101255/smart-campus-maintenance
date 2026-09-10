package com.smartcampus.maintenance.controller;

import com.smartcampus.maintenance.entity.Building;
import com.smartcampus.maintenance.entity.User;
import com.smartcampus.maintenance.security.CustomUserDetails;
import com.smartcampus.maintenance.service.BuildingService;
import com.smartcampus.maintenance.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/buildings")
public class AdminBuildingController {

    @Autowired
    private BuildingService buildingService;

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public String listBuildings(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User admin = userDetails.getUser();
        model.addAttribute("buildings", buildingService.getAllBuildings());
        model.addAttribute("unreadCount", notificationService.getUnreadCount(admin));
        return "admin/buildings";
    }

    @PostMapping
    public String createBuilding(@RequestParam("name") String name,
                                 @RequestParam(value = "code", required = false) String code,
                                 @RequestParam(value = "location", required = false) String location,
                                 RedirectAttributes redirectAttributes) {
        try {
            buildingService.createBuilding(name, code, location);
            redirectAttributes.addFlashAttribute("successMessage", "Building '" + name + "' added successfully");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/buildings";
    }

    @PostMapping("/{id}/toggle")
    public String toggleBuilding(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Building building = buildingService.getById(id);
        buildingService.updateBuilding(id, building.getName(), building.getCode(), building.getLocation(), !building.isActive());
        redirectAttributes.addFlashAttribute("successMessage", "Building status updated");
        return "redirect:/admin/buildings";
    }
}
