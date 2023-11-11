package com.uninter.tcc.controller;

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
			@RequestParam(name = "cpf", required = true) String cpf,
			@RequestParam(name = "idContext", required = true) String idContext,
			@RequestParam(name = "sizeOfEachPage", required = false, defaultValue = "5") String sizeOfEachPage,
			@RequestParam(name = "classForPredict", required = true) String classPredict) {
		ResponseEntity<String> result = null;
		try {
			result = new ResponseEntity<>(
					creditAnalysis.creditScoreAnalysis(cpf, idContext, sizeOfEachPage, classPredict),
					new HttpHeaders(),
					HttpStatus.OK);
		} catch (Exception e) {
			result = new ResponseEntity<>(e.getMessage(), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return result;

	}

	@GetMapping("/behaviorScore")
	public ResponseEntity<String> behavior(
			@RequestParam(name = "cpf", required = true) String cpf,
			@RequestParam(name = "idContext", required = true) String idContext,
			@RequestParam(name = "sizeOfEachPage", required = false, defaultValue = "5") String sizeOfEachPage,
			@RequestParam(name = "classForPredict", required = true) String classPredict) {
		ResponseEntity<String> result = null;
		try {
			result = new ResponseEntity<>(
					creditAnalysis.behaviorScoreAnalysis(cpf, idContext, sizeOfEachPage, classPredict),
					new HttpHeaders(),
					HttpStatus.OK);
		} catch (Exception e) {
			result = new ResponseEntity<>(e.getMessage(), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return result;

	}
}
