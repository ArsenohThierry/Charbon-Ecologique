package com.example.charbonecolo.dto;

import java.time.LocalDate;

public class EntreeCriteriaWrapper {

    private Integer idProduit;
    private LocalDate dateMin;
    private LocalDate dateMax;
    private Integer page = 1;
    private Integer limit = 10;
    private String currentSort;
    private String currentDir;

    public Integer getIdProduit() { return idProduit; }
    public void setIdProduit(Integer idProduit) { this.idProduit = idProduit; }

    public LocalDate getDateMin() { return dateMin; }
    public void setDateMin(LocalDate dateMin) { this.dateMin = dateMin; }

    public LocalDate getDateMax() { return dateMax; }
    public void setDateMax(LocalDate dateMax) { this.dateMax = dateMax; }

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }

    public Integer getLimit() { return limit; }
    public void setLimit(Integer limit) { this.limit = limit; }

    public String getCurrentSort() { return currentSort; }
    public void setCurrentSort(String currentSort) { this.currentSort = currentSort; }

    public String getCurrentDir() { return currentDir; }
    public void setCurrentDir(String currentDir) { this.currentDir = currentDir; }
}