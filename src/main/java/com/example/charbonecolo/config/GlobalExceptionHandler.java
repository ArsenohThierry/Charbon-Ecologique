package com.example.charbonecolo.config;

import com.example.charbonecolo.exception.BusinessException;
import com.example.charbonecolo.exception.ResourceNotFoundException;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

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

    // @ExceptionHandler(Exception.class)
    // public String handleGeneric(Exception e, Model model) {
    //     model.addAttribute("error", "Une erreur inattendue est survenue");
    //     return "error";
    // }

    @ExceptionHandler(Exception.class)
    public String handleAllExceptions(Exception ex, Model model) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String stackTraceAsString = sw.toString();

        model.addAttribute("status", 500);
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("trace", stackTraceAsString); // <-- La variable "trace" est envoyée ici !

        return "error/500";
    }
}
