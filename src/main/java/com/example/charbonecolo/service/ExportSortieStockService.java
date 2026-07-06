package com.example.charbonecolo.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.charbonecolo.dto.SortieCriteriaWrapper;
import com.example.charbonecolo.dto.SortieDto;

@Service
public class ExportSortieStockService {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    @Autowired
    private MouvementStockService mouvementStockService;

    public ExportSortieStockService(MouvementStockService mouvementStockService) {
        this.mouvementStockService = mouvementStockService;
    }

    public ExportSortieStockService() {
    }

    public byte[] genererExcelSorties(SortieCriteriaWrapper wrapper, Pageable pageable) throws IOException {
        List<SortieDto> sorties = mouvementStockService.listSortiesPourExport(wrapper, pageable);
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sorties de stock");
            CellStyle headerStyle = creerStyleEntete(workbook);

            Row headerRow = sheet.createRow(0);
            String[] colonnes = { "Référence", "Produit", "Quantité", "Lots consommés (FIFO)", "Motif", "Date" };
            for (int i = 0; i < colonnes.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(colonnes[i]);
                cell.setCellStyle(headerStyle);
            }

            int ligneIndex = 1;
            for (SortieDto s : sorties) {
                Row row = sheet.createRow(ligneIndex++);
                row.createCell(0).setCellValue("SOR-" + s.getId());
                row.createCell(1).setCellValue(s.getProduitNom());
                row.createCell(2).setCellValue(s.getQuantite());
                row.createCell(3).setCellValue(s.getLotsConsommes());
                row.createCell(4).setCellValue(s.getMotifLibelle() != null ? s.getMotifLibelle() : "—");
                row.createCell(5).setCellValue(s.getDateMouvement().format(DATE_FORMAT));
            }

            for (int i = 0; i < colonnes.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private CellStyle creerStyleEntete(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
}
