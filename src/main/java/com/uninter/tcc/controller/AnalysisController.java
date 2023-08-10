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

@RestController
@RequestMapping("/analysis")
public class AnalysisController {
	@Autowired
	private Analysis creditAnalysis;

	@GetMapping("/creditScore")
	public ResponseEntity<String> credit(
			@RequestParam(name = "cpf", required = true) String cpf) {
		ResponseEntity<String> result = null;
		try {
			Long cpfNumber = Long.valueOf(cpf);
			result = new ResponseEntity<>(creditAnalysis.creditScoreAnalysis(cpfNumber), new HttpHeaders(),HttpStatus.OK);
		} catch (Exception e) {
			result = new ResponseEntity<>(e.getMessage(), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return result;

	}
}
