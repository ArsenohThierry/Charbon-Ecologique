package com.example.charbonecolo.config;

import com.example.charbonecolo.exception.AccessDeniedException;
import com.example.charbonecolo.exception.BusinessException;
import com.example.charbonecolo.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(AccessDeniedException e, Model model) {
        model.addAttribute("error", e.getMessage());
        return "error";
    }

    @ExceptionHandler(BusinessException.class)
    public String handleBusiness(BusinessException e, Model model) {
        log.warn("Business error: {}", e.getMessage());
        model.addAttribute("error", e.getMessage());
        return "error";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(ResourceNotFoundException e, Model model) {
        log.warn("Resource not found: {}", e.getMessage());
        model.addAttribute("error", e.getMessage());
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneric(Exception e, Model model) {
        log.error("Unexpected error", e);
        model.addAttribute("error", "Une erreur inattendue est survenue");
        return "error";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String handleTypeMismatch(MethodArgumentTypeMismatchException ex, RedirectAttributes rad) {
        String rawValue = ex.getValue() != null ? ex.getValue().toString() : "null";
        rad.addFlashAttribute("error", "La donnee envoyé n'est pas un entier valide -> " + rawValue);
        return "redirect:/matiere/home";
    }
}
