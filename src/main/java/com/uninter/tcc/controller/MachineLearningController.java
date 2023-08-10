package com.uninter.tcc.controller;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.uninter.tcc.service.Analysis;
import com.uninter.tcc.service.MachineLearning;

import weka.classifiers.meta.FilteredClassifier;

@RestController
@RequestMapping( "/ml")
public class MachineLearningController {
    @Autowired
    private MachineLearning machineLearning;
    
//    @GetMapping("/train")
//	public ResponseEntity<String> train(
//			@RequestParam(name = "cpf", required = true/* defaultValue = "World" */) String cpf) {
//		ResponseEntity<String> result = null;
//		try {
//			
//			result = new ResponseEntity<String>(machineLearning.train(null, null, null).predictions().toArray().toString(), new HttpHeaders(),HttpStatus.OK);
//
//		} catch (Exception e) {
//			System.out.println(e.toString());
//			result = new ResponseEntity<String>(e.getMessage(), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
//		}
//		return result;
//
//	}

}
