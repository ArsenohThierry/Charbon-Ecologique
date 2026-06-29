package com.example.charbonecolo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.charbonecolo.model.LotStatutsModel;
import com.example.charbonecolo.repository.LotStatutsRepository;

@Service
public class LotStatutsService {
    @Autowired
    private LotStatutsRepository lotStatutsRepository;

    public List<LotStatutsModel> getAllLotsStatuts(){
        return lotStatutsRepository.findAll();
    }

}
