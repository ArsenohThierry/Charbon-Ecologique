package com.example.charbonecolo.service;

import com.example.charbonecolo.model.JournalFinancierModel;
import com.example.charbonecolo.service.JournalFinancierService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class ExportFinanceService {
    
    private final JournalFinancierService journalService;

    public ExportFinanceService(JournalFinancierService journalService) {
        this.journalService = journalService;
    }

    public byte[] exportJournalExcel() throws Exception {

        List<JournalFinancierModel> ecritures = journalService.findAll();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Journal Financier");

        // Header
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("ID");
        header.createCell(1).setCellValue("Date");
        header.createCell(2).setCellValue("Type");
        header.createCell(3).setCellValue("Origine");
        header.createCell(4).setCellValue("Debit");
        header.createCell(5).setCellValue("Credit");
        header.createCell(6).setCellValue("Reference");
        header.createCell(7).setCellValue("Description");

        int rowIdx = 1;

        for (JournalFinancierModel e : ecritures) {
            Row row = sheet.createRow(rowIdx++);

            row.createCell(0).setCellValue(e.getId());
            row.createCell(1).setCellValue(e.getDateOperation().toString());
            row.createCell(2).setCellValue(e.getTypeJournal().getLibelle());
            row.createCell(3).setCellValue(
                    e.getOrigine() != null ? e.getOrigine().getLibelle() : ""
            );
            row.createCell(4).setCellValue(e.getDebit().doubleValue());
            row.createCell(5).setCellValue(e.getCredit().doubleValue());
            row.createCell(6).setCellValue(
                    e.getReference() != null ? e.getReference() : ""
            );
            row.createCell(7).setCellValue(
                    e.getDescription() != null ? e.getDescription() : ""
            );
        }

        for (int i = 0; i < 8; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return out.toByteArray();
    }
}
