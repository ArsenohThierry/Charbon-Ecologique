package com.example.charbonecolo.controller;

import com.example.charbonecolo.service.ImportExcelService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/finance")
public class ImportController {

    private final ImportExcelService importService;

    public ImportController(ImportExcelService importService) {
        this.importService = importService;
    }

    @GetMapping("/import")
    public String afficher() {
        return "stitch/module_finance/import";
    }

    @PostMapping("/import")
    public String importer(@RequestParam("fichier") MultipartFile fichier,
                           RedirectAttributes ra) {
        try {
            importService.importer(fichier);
            ra.addFlashAttribute("succes", "Import réussi");
        } catch (Exception e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/finance/import";
    }
}