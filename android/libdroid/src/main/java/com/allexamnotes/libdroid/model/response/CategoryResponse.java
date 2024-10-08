package com.allexamnotes.libdroid.model.response;


import com.allexamnotes.libdroid.model.category.Category;

import java.util.List;

public class CategoryResponse {

    int totalPages;
    List<Category> categories;

    public CategoryResponse(List<Category> categories, int totalPages) {
        this.totalPages = totalPages;
        this.categories = categories;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public List<Category> getCategories() {
        return categories;
    }
}
