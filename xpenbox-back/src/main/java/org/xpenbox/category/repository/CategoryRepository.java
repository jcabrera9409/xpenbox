package org.xpenbox.category.repository;

import org.xpenbox.category.entity.Category;
import org.xpenbox.common.repository.GenericRepository;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CategoryRepository extends GenericRepository<Category> {
    
}
