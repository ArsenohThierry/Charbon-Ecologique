package com.example.charbonecolo.config;

import com.example.charbonecolo.controller.BilanController;
import com.example.charbonecolo.controller.JournalController;
import com.example.charbonecolo.controller.KpiController;
import com.example.charbonecolo.controller.TresorerieController;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice(assignableTypes = {
        JournalController.class,
        BilanController.class,
        KpiController.class,
        TresorerieController.class
})
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FinanceExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(FinanceExceptionHandler.class);

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                     HttpServletRequest request,
                                     RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", "Parametre invalide : veuillez verifier les valeurs saisies.");
        return "redirect:" + pageFinance(request);
    }

    @ExceptionHandler(Exception.class)
    public String handleFinanceException(Exception ex,
                                         HttpServletRequest request,
                                         RedirectAttributes redirectAttributes) {
        log.error("Erreur dans le module financier", ex);
        redirectAttributes.addFlashAttribute("error", "Une erreur est survenue dans le module financier.");
        return "redirect:" + pageFinance(request);
    }

    private String pageFinance(HttpServletRequest request) {
        String uri = request.getRequestURI();

        if (uri.startsWith("/finance/journal")) {
            return "/finance/journal";
        }
        if (uri.startsWith("/finance/bilan")) {
            return "/finance/bilan";
        }
        if (uri.startsWith("/finance/tresorerie")) {
            return "/finance/tresorerie";
        }

        return "/finance/kpi";
    }
}
