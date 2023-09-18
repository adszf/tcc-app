package com.uninter.tcc.model;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.persistence.Id;
import lombok.Data;

@Data
@Document(collection = "Classifiers")
public class ClassifierEntity {
    @Id
    private String id;
    private String context;
    private String type;
    private String date; 
    private Integer quantity;
    private String classnameClassifier;
    private List<String> optFilter;
    private String optClassifier;
    private String classIndex;
    private String classifierCompact;
}
