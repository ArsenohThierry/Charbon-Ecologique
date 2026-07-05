package com.example.charbonecolo.service;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

@Service
public class ExportBilanService {
    private final JournalFinancierService journalService;

    public ExportBilanService(JournalFinancierService journalService) {
        this.journalService = journalService;
    }

    /**
     * Export Excel
     */
    public byte[] exportBilanExcel(LocalDateTime debut,
        LocalDateTime fin) throws Exception {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Bilan Financier");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Indicateur");
        header.createCell(1).setCellValue("Montant (MGA)");

        Row row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue("Chiffre d'affaires");
        row1.createCell(1).setCellValue(
                journalService.calculerCA(debut, fin).doubleValue());

        Row row2 = sheet.createRow(2);
        row2.createCell(0).setCellValue("Bénéfice");
        row2.createCell(1).setCellValue(
                journalService.calculerBenefice(debut, fin).doubleValue());

        Row row3 = sheet.createRow(3);
        row3.createCell(0).setCellValue("Entrées");
        row3.createCell(1).setCellValue(
                journalService.calculerTotalEntrees(debut, fin).doubleValue());

        Row row4 = sheet.createRow(4);
        row4.createCell(0).setCellValue("Sorties");
        row4.createCell(1).setCellValue(
                journalService.calculerTotalSorties(debut, fin).doubleValue());

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return out.toByteArray();
    }

    /**
     * Export PDF
     */
    public byte[] exportBilanPdf(LocalDateTime debut, 
        LocalDateTime fin) throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Document document = new Document();
        PdfWriter.getInstance(document, out);

        document.open();

        document.add(new Paragraph("BILAN FINANCIER"));
        document.add(new Paragraph(" "));
        document.add(new Paragraph(
                "Chiffre d'affaires : "
                        + journalService.calculerCA(debut, fin)
                        + " MGA"));

        document.add(new Paragraph(
                "Bénéfice : "
                        + journalService.calculerBenefice(debut, fin)
                        + " MGA"));

        document.add(new Paragraph(
                "Entrées : "
                        + journalService.calculerTotalEntrees(debut, fin)
                        + " MGA"));

        document.add(new Paragraph(
                "Sorties : "
                        + journalService.calculerTotalSorties(debut, fin)
                        + " MGA"));

        document.close();

        return out.toByteArray();
    }
}
