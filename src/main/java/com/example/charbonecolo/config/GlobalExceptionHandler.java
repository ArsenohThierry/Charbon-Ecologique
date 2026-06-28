package com.example.charbonecolo.config;

import com.example.charbonecolo.exception.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ImportException.class)
    public String handleImport(ImportException e, Model model) {
        model.addAttribute("error", e.getMessage());
        return "error";
    }

    @ExceptionHandler(InvalidFileException.class)
    public String handleInvalidFile(InvalidFileException e, Model model) {
        model.addAttribute("error", e.getMessage());
        return "error";
    }

    @ExceptionHandler(BusinessException.class)
    public String handleBusiness(BusinessException e, Model model) {
        model.addAttribute("error", e.getMessage());
        return "error";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(ResourceNotFoundException e, Model model) {
        model.addAttribute("error", e.getMessage());
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneric(Exception e, Model model) {
        model.addAttribute("error", "Une erreur inattendue est survenue : " + e.getMessage());
        return "error";
    }
}
