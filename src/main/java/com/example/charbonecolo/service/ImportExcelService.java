package com.example.charbonecolo.service;

import com.example.charbonecolo.config.Constantes;
import com.example.charbonecolo.exception.ImportException;
import com.example.charbonecolo.exception.InvalidFileException;
import com.example.charbonecolo.model.ImportExcelModel;
import com.example.charbonecolo.repository.ImportExcelRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Service pour l'import de données financières à partir de fichiers Excel.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ImportExcelService {

    private final ImportExcelRepository importExcelRepo;
    private final JournalFinancierService journalService;

    public ImportExcelService(ImportExcelRepository importExcelRepo, JournalFinancierService journalService) {
        this.importExcelRepo = importExcelRepo;
        this.journalService = journalService;
    }

    /**
     * Importe les lignes d'un fichier Excel dans le journal financier.
     *
     * @param file le fichier Excel importé
     * @throws InvalidFileException si le fichier est invalide
     * @throws ImportException si une erreur survient pendant l'import
     */
    public void importer(MultipartFile file) {
        validerFichier(file);

        ImportExcelModel importModel = new ImportExcelModel();
        importModel.setNomFichier(file.getOriginalFilename());
        importModel.setDateImport(LocalDateTime.now());
        importModel.setStatut("EN_COURS");
        importModel.setNbLignes(0);
        importModel = importExcelRepo.save(importModel);

        int linesImported = 0;
        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new ImportException("Le fichier Excel ne contient pas de ligne d'en-tête.");
            }

            MappingColonnes mapping = trouverColonnes(headerRow);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                importerLigne(row, mapping, i + 1);
                linesImported++;
            }

            mettreAJourStatutImport(importModel, "TERMINE", linesImported, null);

        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Erreur lors de l'import";
            mettreAJourStatutImport(importModel, "ERREUR", linesImported, msg);
            throw new ImportException("Erreur d'import : " + msg, e);
        }
    }

    /**
     * Valide la structure physique du fichier (existence, extension, taille).
     */
    private void validerFichier(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Le fichier Excel est vide ou inexistant.");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            throw new InvalidFileException("Le nom du fichier est invalide.");
        }

        int dotIdx = filename.lastIndexOf(".");
        if (dotIdx == -1) {
            throw new InvalidFileException("Format de fichier non supporté. L'extension doit être .xlsx ou .xls.");
        }

        String extension = filename.substring(dotIdx).toLowerCase();
        if (!".xlsx".equals(extension) && !".xls".equals(extension)) {
            throw new InvalidFileException("Format de fichier non supporté. Seuls les formats .xlsx et .xls sont acceptés.");
        }

        // Limite à 5 Mo
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new InvalidFileException("Le fichier dépasse la taille maximale autorisée de 5 Mo.");
        }
    }

    /**
     * Détermine les index de colonnes en fonction des noms d'en-tête.
     */
    private MappingColonnes trouverColonnes(Row headerRow) {
        MappingColonnes mapping = new MappingColonnes();

        for (Cell cell : headerRow) {
            if (cell.getCellType() == CellType.STRING) {
                String header = cell.getStringCellValue().trim().toLowerCase();
                if (header.contains("date")) mapping.dateIdx = cell.getColumnIndex();
                else if (header.contains("type")) mapping.typeIdx = cell.getColumnIndex();
                else if (header.contains("ref")) mapping.refIdx = cell.getColumnIndex();
                else if (header.contains("desc")) mapping.descIdx = cell.getColumnIndex();
                else if (header.contains("deb")) mapping.debitIdx = cell.getColumnIndex();
                else if (header.contains("cred")) mapping.creditIdx = cell.getColumnIndex();
                else if (header.contains("orig")) mapping.origineIdx = cell.getColumnIndex();
            }
        }

        // Configuration par défaut si aucun en-tête correspondant n'est trouvé
        if (mapping.typeIdx == -1) mapping.typeIdx = 1;
        if (mapping.refIdx == -1) mapping.refIdx = 2;
        if (mapping.descIdx == -1) mapping.descIdx = 3;
        if (mapping.debitIdx == -1) mapping.debitIdx = 4;
        if (mapping.creditIdx == -1) mapping.creditIdx = 5;

        return mapping;
    }

    /**
     * Importe une seule ligne du fichier Excel.
     */
    private void importerLigne(Row row, MappingColonnes mapping, int lineNum) {
        LocalDateTime dateOp = lireDate(row.getCell(mapping.dateIdx));

        String type = lireTexte(row.getCell(mapping.typeIdx));
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Type de journal manquant à la ligne " + lineNum);
        }

        String reference = lireTexte(row.getCell(mapping.refIdx));
        if (reference == null || reference.isBlank()) {
            throw new IllegalArgumentException("Référence manquante à la ligne " + lineNum);
        }

        String description = lireTexte(row.getCell(mapping.descIdx));
        BigDecimal debit = lireMontant(row.getCell(mapping.debitIdx));
        BigDecimal credit = lireMontant(row.getCell(mapping.creditIdx));

        // Déduction de l'origine
        String origine = Constantes.ORIGINE_PAIEMENT; // Valeur par défaut
        if (mapping.origineIdx != -1) {
            String origVal = lireTexte(row.getCell(mapping.origineIdx));
            if (origVal != null && !origVal.isBlank()) {
                origine = origVal;
            }
        } else {
            if (Constantes.JOURNAL_ACHAT.equalsIgnoreCase(type)) {
                origine = Constantes.ORIGINE_ACHAT;
            }
        }

        journalService.enregistrerAvecDate(type, origine, reference, debit, credit, description, dateOp);
    }

    /**
     * Met à jour le statut et les métadonnées de l'import en base de données.
     */
    private void mettreAJourStatutImport(ImportExcelModel importModel, String statut, int nbLignes, String messageErreur) {
        importModel.setStatut(statut);
        importModel.setNbLignes(nbLignes);
        if (messageErreur != null) {
            if (messageErreur.length() > 500) {
                importModel.setMessageErreur(messageErreur.substring(0, 500) + "...");
            } else {
                importModel.setMessageErreur(messageErreur);
            }
        }
        importExcelRepo.save(importModel);
    }

    /**
     * Lit et convertit la cellule de date.
     */
    private LocalDateTime lireDate(Cell cell) {
        if (cell != null) {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                Date d = cell.getDateCellValue();
                return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            } else if (cell.getCellType() == CellType.STRING) {
                try {
                    return LocalDateTime.parse(cell.getStringCellValue().trim());
                } catch (Exception e) {
                    // Ignorer et retourner la date courante
                }
            }
        }
        return LocalDateTime.now();
    }

    /**
     * Lit et extrait la valeur texte d'une cellule.
     */
    private String lireTexte(Cell cell) {
        if (cell == null) {
            return "";
        }
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((int) cell.getNumericCellValue()).trim();
        }
        return "";
    }

    /**
     * Lit et extrait la valeur numérique (montant) d'une cellule.
     */
    private BigDecimal lireMontant(Cell cell) {
        if (cell == null) {
            return BigDecimal.ZERO;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return new BigDecimal(cell.getStringCellValue().trim());
            } catch (Exception e) {
                return BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * Vérifie si une ligne Excel est entièrement vide.
     */
    private boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    /**
     * Classe interne pour stocker la correspondance des colonnes.
     */
    private static class MappingColonnes {
        int dateIdx = -1;
        int typeIdx = -1;
        int refIdx = -1;
        int descIdx = -1;
        int debitIdx = -1;
        int creditIdx = -1;
        int origineIdx = -1;
    }
}
