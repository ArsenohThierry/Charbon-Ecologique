package com.example.charbonecolo.repository;

import com.example.charbonecolo.model.ImportExcelModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportExcelRepository extends JpaRepository<ImportExcelModel, Integer> {
}
