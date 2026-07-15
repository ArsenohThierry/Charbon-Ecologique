package com.example.charbonecolo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.charbonecolo.model.FactureDetailModel;
import com.example.charbonecolo.model.FactureModel;
import com.example.charbonecolo.repository.FactureDetailRepository;
import com.example.charbonecolo.repository.FactureRepository;
import com.example.charbonecolo.util.FactureToPdf;

@Service
public class FactureService {
    private final FactureDetailRepository factureDetailRepository;
    private final FactureRepository factureRepository;
    public FactureService(FactureDetailRepository factureDetailRepository, FactureRepository factureRepository) {
        this.factureDetailRepository = factureDetailRepository;
        this.factureRepository = factureRepository;
    }

    public byte[] exportToPdf(Integer id) throws Exception {
        FactureModel found = factureRepository.findById(id).orElse(null);
        List<FactureDetailModel> details = factureDetailRepository.findByFactureId(id);
        return FactureToPdf.exportFactureToPdf(found, details);
    }

}
