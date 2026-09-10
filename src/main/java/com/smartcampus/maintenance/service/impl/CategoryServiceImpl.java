package com.smartcampus.maintenance.service.impl;

import com.smartcampus.maintenance.entity.Category;
import com.smartcampus.maintenance.repository.CategoryRepository;
import com.smartcampus.maintenance.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Category> getAllActiveCategories() {
        return categoryRepository.findByActiveTrueOrderByNameAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + id));
    }

    @Override
    public Category createCategory(String name, String description) {
        if (categoryRepository.existsByName(name)) {
            throw new IllegalArgumentException("Category with this name already exists");
        }
        Category category = new Category(name.trim(), description);
        return categoryRepository.save(category);
    }

    @Override
    public Category updateCategory(Long id, String name, String description, boolean active) {
        Category category = getById(id);
        category.setName(name.trim());
        category.setDescription(description);
        category.setActive(active);
        return categoryRepository.save(category);
    }
}
