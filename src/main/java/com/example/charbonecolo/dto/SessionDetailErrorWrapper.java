package com.example.charbonecolo.dto;

public class SessionDetailErrorWrapper {
    private Integer index;
    private String message;
    private String level; // WARNING na DANGER
    public Integer getIndex() {
        return index;
    }
    public void setIndex(Integer index) {
        this.index = index;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getLevel() {
        return level;
    }
    public void setLevel(String level) {
        this.level = level;
    }
}
