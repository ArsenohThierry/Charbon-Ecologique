package com.example.charbonecolo.config;

import org.springframework.web.servlet.HandlerInterceptor;

import com.example.charbonecolo.exception.AccessDeniedException;
import com.example.charbonecolo.model.UtilisateurModel;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        UtilisateurModel user = (UtilisateurModel) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        String role = user.getRole().getLibelle();
        String path = request.getRequestURI();

        if ("ADMIN".equals(role)) {
            return true;
        }

        if (path.startsWith("/finance/") && "STOCK_MANAGER".equals(role)) {
            throw new AccessDeniedException("Vous n'avez pas accès à cette fonctionnalité.");
        }

        if ("FINANCE_MANAGER".equals(role)
                && (path.startsWith("/matiere/") || path.startsWith("/fournisseur/")
                        || path.startsWith("/produits") || path.startsWith("/stock/"))) {
            throw new AccessDeniedException("Vous n'avez pas accès à cette fonctionnalité.");
        }

        if ("STOCK_MANAGER".equals(role)
                && (path.startsWith("/cmd") || path.startsWith("/livraisons") || path.startsWith("/factures"))) {
            throw new AccessDeniedException("Vous n'avez pas accès à cette fonctionnalité.");
        }

        return true;
    }
}
