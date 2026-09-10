package com.smartcampus.maintenance.service;

import com.smartcampus.maintenance.entity.Category;
import java.util.List;

public interface CategoryService {
    List<Category> getAllActiveCategories();
    List<Category> getAllCategories();
    Category getById(Long id);
    Category createCategory(String name, String description);
    Category updateCategory(Long id, String name, String description, boolean active);
}
