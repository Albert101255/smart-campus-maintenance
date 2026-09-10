package com.smartcampus.maintenance.dto;

import jakarta.validation.constraints.NotBlank;

public class CommentRequest {

    @NotBlank(message = "Comment content cannot be empty")
    private String content;

    private boolean isInternal = false;

    public CommentRequest() {
    }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public boolean isInternal() { return isInternal; }
    public void setInternal(boolean internal) { isInternal = internal; }
}
