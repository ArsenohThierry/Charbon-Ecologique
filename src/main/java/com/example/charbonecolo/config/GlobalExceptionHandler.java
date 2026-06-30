package com.example.charbonecolo.config;

import com.example.charbonecolo.exception.BusinessException;
import com.example.charbonecolo.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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
}
