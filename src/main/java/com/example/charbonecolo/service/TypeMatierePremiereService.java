package com.example.charbonecolo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.charbonecolo.repository.TypeMatierePremiereRepository;

@Service
public class TypeMatierePremiereService {

    @Autowired
    private TypeMatierePremiereRepository typeMatierePremiereRepository;
}
