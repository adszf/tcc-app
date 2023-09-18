package com.uninter.tcc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uninter.tcc.dto.kafka.send.SendRequestDto;
import com.uninter.tcc.service.Kafka;

@RestController
@RequestMapping("/kafka")
public class KafkaController {
	@Autowired
	private Kafka kafka;

	@PostMapping("/send")
	public ResponseEntity<?> send(@RequestBody SendRequestDto request) {
		ResponseEntity<String> result = null;
		try {
			result = new ResponseEntity<>(
					kafka.send(request),
					new HttpHeaders(), HttpStatus.OK);
		} catch (Exception e) {
			result = new ResponseEntity<>(e.getMessage(), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return result;
	}
}
