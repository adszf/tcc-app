package com.uninter.tcc.dto.kafka.send;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendRequestDto {
    @NotNull
    private String type;
    @NotNull
    private Integer quantity;
    @NotNull
    private String classnameClassifier;
    @NotNull
    private List<String> optFilter;
    @NotNull
    String optClassifier;
    @NotNull
    private String classIndex;
    @NotNull
    private String context;
}
