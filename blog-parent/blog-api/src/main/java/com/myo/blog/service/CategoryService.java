package com.myo.blog.service;

import com.myo.blog.entity.CategoryVo;
import com.myo.blog.entity.Result;

public interface CategoryService {

    CategoryVo findCategoryById(Long categoryId);

    Result findAll();

    Result findAllDetail();

    Result categoryDetailById(Long id);
}
