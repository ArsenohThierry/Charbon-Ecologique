package com.example.charbonecolo.dto;

import java.time.LocalDate;

public class CriteriaWrapper {
    private LocalDate dateMin;
    private LocalDate dateMax;
    private Integer montantMin;
    private Integer montantMax;
    private String currentDir;
    private String currentSort;
    private String keyword;
    private Integer limit = 10;
    private Integer page = 1;
    private Integer statut;

    public Integer getStatut() {
        return statut;
    }

    public void setStatut(Integer statut) {
        this.statut = statut;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public String getCurrentDir() {
        return currentDir;
    }

    public void setCurrentDir(String currentDir) {
        this.currentDir = currentDir;
    }

    public String getCurrentSort() {
        return currentSort;
    }

    public void setCurrentSort(String currentSort) {
        this.currentSort = currentSort;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public LocalDate getDateMin() {
        return dateMin;
    }

    public void setDateMin(LocalDate dateMin) {
        this.dateMin = dateMin;
    }

    public LocalDate getDateMax() {
        return dateMax;
    }

    public void setDateMax(LocalDate dateMax) {
        this.dateMax = dateMax;
    }

    public Integer getMontantMin() {
        return montantMin;
    }

    public void setMontantMin(Integer montantMin) {
        this.montantMin = montantMin;
    }

    public Integer getMontantMax() {
        return montantMax;
    }

    public void setMontantMax(Integer montantMax) {
        this.montantMax = montantMax;
    }
}
