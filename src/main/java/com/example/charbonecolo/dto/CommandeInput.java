package com.example.charbonecolo.dto;

import jakarta.validation.constraints.NotBlank;

public class CommandeInput {
    @NotBlank(message = "Le nom du client est requis.")
    private String clientNom;

    private Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getClientNom() {
        return clientNom;
    }

    public void setClientNom(String clientNom) {
        this.clientNom = clientNom;
    }
}
