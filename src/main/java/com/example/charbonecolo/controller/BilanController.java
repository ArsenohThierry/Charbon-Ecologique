package com.example.charbonecolo.controller;

import com.example.charbonecolo.service.JournalFinancierService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/finance")
public class BilanController {

    private final JournalFinancierService financeService;

    public BilanController(JournalFinancierService financeService) {
        this.financeService = financeService;
    }

    @GetMapping("/bilan")
    public String afficher(
            @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            Model model) {

        LocalDateTime d = financeService.calculerDateDebut(debut, -1);
        LocalDateTime f = financeService.calculerDateFin(fin);

        model.addAttribute("ca",          financeService.calculerCA(d, f));
        model.addAttribute("totalEntrees",financeService.calculerTotalEntrees(d, f));
        model.addAttribute("totalSorties",financeService.calculerTotalSorties(d, f));
        model.addAttribute("benefice",    financeService.calculerBenefice(d, f));
        model.addAttribute("solde",       financeService.calculerSolde());
        return "stitch/module_finance/bilan";
    }

    @GetMapping("/bilan/export-excel")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {

        LocalDateTime d = financeService.calculerDateDebut(debut, -1);
        LocalDateTime f = financeService.calculerDateFin(fin);

        BigDecimal ca = financeService.calculerCA(d, f);
        BigDecimal totalEntrees = financeService.calculerTotalEntrees(d, f);
        BigDecimal totalSorties = financeService.calculerTotalSorties(d, f);
        BigDecimal benefice = financeService.calculerBenefice(d, f);
        BigDecimal solde = financeService.calculerSolde();

        try (org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Bilan Financier");

            // Header Style
            org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 14);
            headerStyle.setFont(headerFont);

            // Title
            org.apache.poi.ss.usermodel.Row titleRow = sheet.createRow(0);
            org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Bilan Financier");
            titleCell.setCellStyle(headerStyle);

            // Period
            org.apache.poi.ss.usermodel.Row periodRow = sheet.createRow(1);
            periodRow.createCell(0).setCellValue("Période : du " + d.toLocalDate() + " au " + f.toLocalDate());

            // Header Row
            org.apache.poi.ss.usermodel.Row tableHeader = sheet.createRow(3);
            org.apache.poi.ss.usermodel.CellStyle boldStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);

            org.apache.poi.ss.usermodel.Cell h1 = tableHeader.createCell(0);
            h1.setCellValue("Indicateur");
            h1.setCellStyle(boldStyle);

            org.apache.poi.ss.usermodel.Cell h2 = tableHeader.createCell(1);
            h2.setCellValue("Montant (MGA)");
            h2.setCellStyle(boldStyle);

            // Data
            String[][] data = {
                {"Chiffre d'affaires", ca.toString()},
                {"Total entrées", totalEntrees.toString()},
                {"Total sorties", totalSorties.toString()},
                {"Bénéfice net", benefice.toString()},
                {"Solde trésorerie", solde.toString()}
            };

            int rowNum = 4;
            for (String[] rowData : data) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rowData[0]);

                org.apache.poi.ss.usermodel.Cell cellVal = row.createCell(1);
                cellVal.setCellValue(Double.parseDouble(rowData[1]));

                org.apache.poi.ss.usermodel.CellStyle numStyle = workbook.createCellStyle();
                numStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
                cellVal.setCellStyle(numStyle);
            }

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            byte[] bytes = out.toByteArray();

            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=bilan.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération de l'export Excel", e);
        }
    }
}