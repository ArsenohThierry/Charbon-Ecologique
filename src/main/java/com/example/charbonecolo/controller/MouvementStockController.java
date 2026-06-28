package com.example.charbonecolo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for Stock Movement module
 */
@Controller
public class MouvementStockController {

    /**
     * Redirects to the entrée stock page
     * @return view name for entrée stock page
     */
    @GetMapping("stock/entree")
    public String entreeStock() {
        return "stitch/module_stock/entree_stock";
    }

    /**
     * Redirects to the sortie stock page
     * @return view name for sortie stock page
     */
    @GetMapping("stock/sortie")
    public String sortieStock() {
        return "stitch/module_stock/sortie_stock";
    }
}
