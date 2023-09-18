package com.uninter.tcc.dto.kafka.send;

import lombok.Data;

@Data
public class SendDto {
    private String type; 
    private Integer quantity;
    private String classnameClassifier;
    private String optRemove;
    private String optClassifier;
    private String classIndex;
    private String classifierCompact;
}
