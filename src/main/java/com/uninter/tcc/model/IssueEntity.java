package com.uninter.tcc.model;



import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.persistence.Id;

@Document(collection = "issues")
public class IssueEntity {

    @Id
    private String id;
    private String description;
    private int severity;
    private String assignee;

    public IssueEntity() {

    }

    public IssueEntity(String description, int severity, String assignee) {
        this.description = description;
        this.severity = severity;
        this.assignee = assignee;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public String getAssignee() {
        return assignee;
    }

    public int getSeverity() {
        return severity;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return this.description + " " + this.severity + " " + this.assignee;
    }
}
