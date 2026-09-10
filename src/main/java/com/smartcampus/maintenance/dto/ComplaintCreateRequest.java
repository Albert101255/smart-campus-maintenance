package com.smartcampus.maintenance.dto;

import com.smartcampus.maintenance.enums.PriorityLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public class ComplaintCreateRequest {

    @NotBlank(message = "Complaint title is required")
    private String title;

    @NotNull(message = "Category selection is required")
    private Long categoryId;

    @NotNull(message = "Building selection is required")
    private Long buildingId;

    @NotBlank(message = "Floor level is required")
    private String floor;

    @NotBlank(message = "Room number / location is required")
    private String roomNumber;

    private String locationDescription;

    @NotBlank(message = "Detailed issue description is required")
    private String description;

    @NotNull(message = "Urgency / Priority level is required")
    private PriorityLevel priority = PriorityLevel.MEDIUM;

    private MultipartFile image;

    public ComplaintCreateRequest() {
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public Long getBuildingId() { return buildingId; }
    public void setBuildingId(Long buildingId) { this.buildingId = buildingId; }

    public String getFloor() { return floor; }
    public void setFloor(String floor) { this.floor = floor; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getLocationDescription() { return locationDescription; }
    public void setLocationDescription(String locationDescription) { this.locationDescription = locationDescription; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public PriorityLevel getPriority() { return priority; }
    public void setPriority(PriorityLevel priority) { this.priority = priority; }

    public MultipartFile getImage() { return image; }
    public void setImage(MultipartFile image) { this.image = image; }
}
