package com.example.charbonecolo.repository;

import com.example.charbonecolo.model.ImportExcelModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImportExcelRepository extends JpaRepository<ImportExcelModel, Long> {
    List<ImportExcelModel> findAllByOrderByDateImportDesc();
}
