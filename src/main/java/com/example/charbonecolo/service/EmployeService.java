package com.example.charbonecolo.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.charbonecolo.model.EmploiModel;
import com.example.charbonecolo.model.EmployeModel;
import com.example.charbonecolo.model.SalaireHistoriqueModel;
import com.example.charbonecolo.repository.EmploiRepository;
import com.example.charbonecolo.repository.EmployeRepository;
import com.example.charbonecolo.repository.SalaireHistoriqueRepository;

@Service
public class EmployeService {

    private final EmployeRepository employeRepository;
    private final EmploiRepository emploiRepository;
    private final SalaireHistoriqueRepository historiqueRepository;

    public EmployeService(EmployeRepository employeRepository, EmploiRepository emploiRepository,
                          SalaireHistoriqueRepository historiqueRepository) {
        this.employeRepository = employeRepository;
        this.emploiRepository = emploiRepository;
        this.historiqueRepository = historiqueRepository;
    }

    public List<EmploiModel> getAllEmplois() {
        return emploiRepository.findAll();
    }

    public Page<EmployeModel> searchEmployes(String nom, String reference, Integer idEmploi, Pageable pageable) {
        return employeRepository.findByCriteria(nom, reference, idEmploi, pageable);
    }

    public EmployeModel getById(Integer id) {
        return employeRepository.findById(id).orElseThrow();
    }

    @Transactional
    public EmployeModel saveEmploye(EmployeModel employe) {
        if (employe.getId() == null) {
            employe.setReference(generateReference());
        }
        if (employe.getDateEmbauche() == null) {
            employe.setDateEmbauche(LocalDate.now());
        }
        return employeRepository.save(employe);
    }

    @Transactional
    public void deleteById(Integer id) {
        employeRepository.deleteById(id);
    }

    public EmploiModel getEmploiById(Integer id) {
        return emploiRepository.findById(id).orElseThrow();
    }

    @Transactional
    public EmploiModel saveEmploi(EmploiModel emploi) {
        return emploiRepository.save(emploi);
    }

    @Transactional
    public void deleteEmploiById(Integer id) {
        if (employeRepository.countByEmploiId(id) > 0) {
            throw new RuntimeException("Ce poste est attribué à un ou plusieurs employés.");
        }
        emploiRepository.deleteById(id);
    }

    public List<EmployeModel> getAllEmployes() {
        return employeRepository.findAll();
    }

    public Page<SalaireHistoriqueModel> getHistoriqueByEmployeId(Integer employeId, Pageable pageable) {
        return historiqueRepository.findByEmployeIdOrderByDateEffetDesc(employeId, pageable);
    }

    @Transactional
    public SalaireHistoriqueModel salarier(Integer employeId, Integer emploiId, BigDecimal salaireBase,
                                            BigDecimal prime, BigDecimal indemnite, LocalDate dateEffet) {
        EmployeModel employe = getById(employeId);

        if (emploiId != null) {
            EmploiModel emploi = emploiRepository.findById(emploiId).orElseThrow();
            employe.setEmploi(emploi);
        }

        SalaireHistoriqueModel histo = new SalaireHistoriqueModel();
        histo.setEmploye(employe);
        histo.setSalaireBase(salaireBase);
        histo.setPrime(prime);
        histo.setIndemnite(indemnite);
        histo.setTotal(salaireBase.add(prime).add(indemnite));
        histo.setDateEffet(dateEffet);
        historiqueRepository.save(histo);

        employe.setPrime(prime);
        employe.setIndemnite(indemnite);
        employeRepository.save(employe);

        return histo;
    }

    private String generateReference() {
        long count = employeRepository.count() + 1;
        return "EMP-" + String.format("%03d", count);
    }
}
