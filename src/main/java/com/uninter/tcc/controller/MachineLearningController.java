package com.uninter.tcc.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.uninter.tcc.service.MachineLearning;

@RestController
@RequestMapping("/ml")
public class MachineLearningController {
    @Autowired
    private MachineLearning machineLearning;

    @GetMapping("/train")
    public ResponseEntity<String> train(
            @RequestParam(name = "idContext", required = true) String idContext,
            @RequestParam(name = "partitionSize", required = true) Integer partitionSize,
            @RequestParam(name = "classnameClassifier", required = true) String classnameClassifier,
            @RequestParam(name = "optClassifier", required = true) String optClassifier,
            @RequestParam(name = "optFilter", required = true) List<String> optFilter,
            @RequestParam(name = "classIndex", required = true) String classIndex) {
        ResponseEntity<String> result = null;
        try {
            result = new ResponseEntity<>(machineLearning.requestTrain(idContext, partitionSize,
                    classnameClassifier, optClassifier, optFilter, classIndex), new HttpHeaders(), HttpStatus.OK);
        } catch (Exception e) {
            result = new ResponseEntity<>(e.getMessage(), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return result;

    }

}
