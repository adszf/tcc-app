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
import com.uninter.tcc.service.Kafka;

@RestController
@RequestMapping("/kafka")
public class KafkaController {
	@Autowired
	private Kafka kafka;

	@GetMapping("/send")
	public ResponseEntity<?> send(@RequestParam(name = "type", required = true) String type,
			@RequestParam(name = "quantity", required = true) Integer quantity) {
		ResponseEntity<String> result = null;
		try {
			result = new ResponseEntity<>(kafka.send(type, quantity), new HttpHeaders(), HttpStatus.OK);
		} catch (Exception e) {
			result = new ResponseEntity<>(e.getMessage(), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return result;
	}
}
