package com.example.charbonecolo.util;

import org.springframework.stereotype.Component;

@Component("thymeleafUtils")
public class ThymeleafUtils {

    public String toggleSortColumn(String currentSort, String column) {
        if (currentSort != null && currentSort.startsWith(column + ",")) {
            String dir = currentSort.substring(column.length() + 1);
            if ("asc".equalsIgnoreCase(dir)) {
                return column + ",desc";
            }
            return null; // 3e clic : suppression du tri
        }
        return column + ",asc";
    }
}