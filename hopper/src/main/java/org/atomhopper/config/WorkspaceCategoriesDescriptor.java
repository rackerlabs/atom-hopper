package org.atomhopper.config;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * Configuration class for workspace category descriptors
 */
public class WorkspaceCategoriesDescriptor {
    
    private Set<String> allowedCategories = new HashSet<>();
    
    public void setAllowedCategories(List<String> categories) {
        this.allowedCategories = new HashSet<>(categories);
    }
    
    public Set<String> getAllowedCategories() {
        return allowedCategories;
    }
    
    public boolean isCategoryAllowed(String category) {
        return allowedCategories.isEmpty() || allowedCategories.contains(category);
    }
}