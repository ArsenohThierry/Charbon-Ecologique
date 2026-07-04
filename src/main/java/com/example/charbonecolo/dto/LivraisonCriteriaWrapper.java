package com.example.charbonecolo.dto;

import java.time.LocalDate;

public class LivraisonCriteriaWrapper {

    private LocalDate dateLivMin;
    private LocalDate dateLivMax;
    private String reference;
    private Integer statut;
    private String currentDir;
    private String currentSort;
    private Integer limit = 10;
    private Integer page = 1;

    public LocalDate getDateLivMin() { return dateLivMin; }
    public void setDateLivMin(LocalDate dateLivMin) { this.dateLivMin = dateLivMin; }

    public LocalDate getDateLivMax() { return dateLivMax; }
    public void setDateLivMax(LocalDate dateLivMax) { this.dateLivMax = dateLivMax; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public Integer getStatut() { return statut; }
    public void setStatut(Integer statut) { this.statut = statut; }

    public String getCurrentDir() { return currentDir; }
    public void setCurrentDir(String currentDir) { this.currentDir = currentDir; }

    public String getCurrentSort() { return currentSort; }
    public void setCurrentSort(String currentSort) { this.currentSort = currentSort; }

    public Integer getLimit() { return limit; }
    public void setLimit(Integer limit) { this.limit = limit; }

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }
}