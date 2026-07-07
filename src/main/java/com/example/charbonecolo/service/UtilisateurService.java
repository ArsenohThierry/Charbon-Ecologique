package com.example.charbonecolo.service;

import com.example.charbonecolo.exception.BusinessException;
import com.example.charbonecolo.model.UtilisateurModel;
import com.example.charbonecolo.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;

    public UtilisateurService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    public UtilisateurModel authenticate(String username, String password) {
        Optional<UtilisateurModel> optUser = utilisateurRepository.findByUsername(username);
        if (optUser.isEmpty()
                || !optUser.get().getMotPasse().equals(password)
                || !optUser.get().getActif()) {
            throw new BusinessException("Identifiants invalides ou compte désactivé");
        }
        return optUser.get();
    }
}
