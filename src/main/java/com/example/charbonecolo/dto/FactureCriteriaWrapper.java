package com.example.charbonecolo.dto;

public class FactureCriteriaWrapper {

    private String filtre;
    private String currentDir;
    private String currentSort;
    private Integer limit = 10;
    private Integer page = 1;

    public String getFiltre() { return filtre; }
    public void setFiltre(String filtre) { this.filtre = filtre; }

    public String getCurrentDir() { return currentDir; }
    public void setCurrentDir(String currentDir) { this.currentDir = currentDir; }

    public String getCurrentSort() { return currentSort; }
    public void setCurrentSort(String currentSort) { this.currentSort = currentSort; }

    public Integer getLimit() { return limit; }
    public void setLimit(Integer limit) { this.limit = limit; }

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }
}
