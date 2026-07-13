package com.example.charbonecolo.util;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.awt.Color; // OpenPDF utilise les couleurs standard de Java !

// Vos packages OpenPDF officiels
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;

import com.example.charbonecolo.model.FactureDetailModel;
import com.example.charbonecolo.model.FactureModel;

public class FactureToPdf {

    public static byte[] exportFactureToPdf(FactureModel facture, List<FactureDetailModel> details) throws Exception {
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, baos);
            document.open();
            Color grisFonce = new Color(60, 60, 60);
            Color grisLigne = new Color(220, 220, 220);
            Color bleuEntete = new Color(240, 244, 248);
            Font fontTitre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, grisFonce);
            Font fontSectionHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, grisFonce);
            Font fontHeaderTableau = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, grisFonce);
            Font fontCorps = FontFactory.getFont(FontFactory.HELVETICA, 10, grisFonce);
            Font fontCorpsBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, grisFonce);
            PdfPTable enteteTable = new PdfPTable(2);
            enteteTable.setWidthPercentage(100);
            enteteTable.setSpacingAfter(24);
            PdfPCell cellGauche = new PdfPCell(new Paragraph("Charbon Vert", fontSectionHeader));
            cellGauche.setBorder(PdfPCell.NO_BORDER);
            enteteTable.addCell(cellGauche);
            PdfPCell cellDroite = new PdfPCell(new Paragraph("FACTURE\nN° " + facture.getReference(), fontTitre));
            cellDroite.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellDroite.setBorder(PdfPCell.NO_BORDER);
            enteteTable.addCell(cellDroite);
            document.add(enteteTable);
            PdfPTable blocInfos = new PdfPTable(2);
            blocInfos.setWidthPercentage(100);
            blocInfos.setSpacingAfter(35);
            String dateStr = "N/A";
            if (facture.getDateFacture() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                dateStr = facture.getDateFacture().format(formatter);
            }
            Paragraph pFacture = new Paragraph();
            pFacture.add(new Phrase("Détails de la facture :\n", fontSectionHeader));
            pFacture.add(new Phrase("Date : ", fontCorpsBold));
            pFacture.add(new Phrase(dateStr + "\n", fontCorps));
            pFacture.add(new Phrase("Commande : ", fontCorpsBold));
            pFacture.add(new Phrase(facture.getPaiement().getCommande().getReference() + "\n", fontCorps));
            pFacture.add(new Phrase("Réf. Paiement : ", fontCorpsBold));
            pFacture.add(new Phrase(facture.getPaiement().getReference(), fontCorps));
            PdfPCell cellInfoFacture = new PdfPCell(pFacture);
            cellInfoFacture.setBorder(PdfPCell.NO_BORDER);
            cellInfoFacture.setLeading(14f, 0f);
            blocInfos.addCell(cellInfoFacture);
            Paragraph pClient = new Paragraph();
            pClient.add(new Phrase("Facturé à :\n", fontSectionHeader));
            pClient.add(new Phrase(facture.getPaiement().getCommande().getClient().getNom() + "\n", fontCorpsBold));
            PdfPCell cellInfoClient = new PdfPCell(pClient);
            cellInfoClient.setBorder(PdfPCell.NO_BORDER);
            cellInfoClient.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellInfoClient.setLeading(14f, 0f);
            blocInfos.addCell(cellInfoClient);
            document.add(blocInfos);
            float[] columnsRation = { 4.5f, 1.5f, 2f, 2f };
            PdfPTable table = new PdfPTable(columnsRation);
            table.setWidthPercentage(100);
            table.setSpacingAfter(20);
            addHeaderCell(table, "Description", fontHeaderTableau, Element.ALIGN_LEFT, bleuEntete);
            addHeaderCell(table, "Qte", fontHeaderTableau, Element.ALIGN_RIGHT, bleuEntete);
            addHeaderCell(table, "PU (Ar)", fontHeaderTableau, Element.ALIGN_RIGHT, bleuEntete);
            addHeaderCell(table, "Total (Ar)", fontHeaderTableau, Element.ALIGN_RIGHT, bleuEntete);
            for (FactureDetailModel detail : details) {
                table.addCell(addGivenCell(detail.getLibelle(), fontCorps, Element.ALIGN_LEFT, grisLigne));
                table.addCell(addGivenCell(detail.getQuantite().toString(), fontCorps, Element.ALIGN_RIGHT, grisLigne));
                String puStr = String.format("%,.2f", detail.getPu().doubleValue()).replace(",", " ");
                table.addCell(addGivenCell(puStr, fontCorps, Element.ALIGN_RIGHT, grisLigne));
                double totalLigne = detail.getPu() * detail.getQuantite();
                String totalLigneStr = String.format("%,.2f", totalLigne).replace(",", " ");
                table.addCell(addGivenCell(totalLigneStr, fontCorps, Element.ALIGN_RIGHT, grisLigne));
            }
            document.add(table);
            PdfPTable totalTable = new PdfPTable(2);
            totalTable.setWidthPercentage(35);
            totalTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            PdfPCell cellTotalLabel = new PdfPCell(new Phrase("Montant Total", fontSectionHeader));
            cellTotalLabel.setBorder(PdfPCell.NO_BORDER);
            cellTotalLabel.setPadding(8);
            totalTable.addCell(cellTotalLabel);
            String totalGeneralStr = String.format("%,.2f", getTotal(details)).replace(",", " ") + " Ar";
            PdfPCell cellTotalValeur = new PdfPCell(new Phrase(totalGeneralStr, fontSectionHeader));
            cellTotalValeur.setBorder(PdfPCell.NO_BORDER);
            cellTotalValeur.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellTotalValeur.setPadding(8);
            totalTable.addCell(cellTotalValeur);
            document.add(totalTable);
            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw e;
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
    }

    private static void addHeaderCell(PdfPTable table, String texte, Font font, int alignement, Color fondCouleur) {
        PdfPCell cellule = new PdfPCell(new Phrase(texte, font));
        cellule.setHorizontalAlignment(alignement);
        cellule.setBackgroundColor(fondCouleur);
        cellule.setPaddingTop(8);
        cellule.setPaddingBottom(8);
        cellule.setPaddingLeft(6);
        cellule.setPaddingRight(6);
        cellule.setBorder(PdfPCell.BOTTOM);
        cellule.setBorderColor(Color.DARK_GRAY); 
        table.addCell(cellule);
    }

    private static PdfPCell addGivenCell(String texte, Font font, int alignement, Color couleurLigne) {
        PdfPCell cellule = new PdfPCell(new Phrase(texte, font));
        cellule.setHorizontalAlignment(alignement);
        cellule.setPaddingTop(8);
        cellule.setPaddingBottom(8);
        cellule.setPaddingLeft(6);
        cellule.setPaddingRight(6);
        cellule.setBorder(PdfPCell.BOTTOM);
        cellule.setBorderColor(couleurLigne); 
        return cellule;
    }

    private static double getTotal(List<FactureDetailModel> details) {
        return details.stream().mapToDouble(d -> d.getPu() * d.getQuantite()).sum();
    }
}