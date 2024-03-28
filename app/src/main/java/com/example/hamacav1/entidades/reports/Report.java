package com.example.hamacav1.entidades.reports;

public class Report {
    private String title;
    private String description;
    private String state;
    private String fullComment;
    private String creationDate;
    private String createdBy; //idUsuario deberia ir aqui


    public Report() {
    }

    public Report(String title, String description, String state, String fullComment, String creationDate, String createdBy) {
        this.title = title;
        this.description = description;
        this.state = state;
        this.fullComment = fullComment;
        this.creationDate = creationDate;
        this.createdBy = createdBy;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getFullComment() {
        return fullComment;
    }

    public void setFullComment(String fullComment) {
        this.fullComment = fullComment;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
