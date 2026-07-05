package com.example.charbonecolo.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class KpiDto {
    public KpiDto() {
    }
    private BigDecimal ca;
    private BigDecimal caPrecedent;
    private BigDecimal benefice;
    private BigDecimal entrees;
    private BigDecimal sorties;
    private BigDecimal solde;
    private List<Map<String, Object>> evolutionCA;

    public KpiDto(BigDecimal ca, BigDecimal caPrecedent, BigDecimal benefice, BigDecimal entrees, BigDecimal sorties, BigDecimal solde, List<Map<String, Object>> evolutionCA) {
        this.ca = ca;
        this.caPrecedent = caPrecedent;
        this.benefice = benefice;
        this.entrees = entrees;
        this.sorties = sorties;
        this.solde = solde;
        this.evolutionCA = evolutionCA;
    }

    public BigDecimal getCa() {
        return ca;
    }

    public void setCa(BigDecimal ca) {
        this.ca = ca;
    }

    public BigDecimal getCaPrecedent() {
        return caPrecedent;
    }

    public void setCaPrecedent(BigDecimal caPrecedent) {
        this.caPrecedent = caPrecedent;
    }

    public BigDecimal getBenefice() {
        return benefice;
    }

    public void setBenefice(BigDecimal benefice) {
        this.benefice = benefice;
    }

    public BigDecimal getEntrees() {
        return entrees;
    }

    public void setEntrees(BigDecimal entrees) {
        this.entrees = entrees;
    }

    public BigDecimal getSorties() {
        return sorties;
    }

    public void setSorties(BigDecimal sorties) {
        this.sorties = sorties;
    }

    public BigDecimal getSolde() {
        return solde;
    }

    public void setSolde(BigDecimal solde) {
        this.solde = solde;
    }

    public List<Map<String, Object>> getEvolutionCA() {
        return evolutionCA;
    }

    public void setEvolutionCA(List<Map<String, Object>> evolutionCA) {
        this.evolutionCA = evolutionCA;
    }
}