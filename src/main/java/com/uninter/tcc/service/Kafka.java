package com.uninter.tcc.service;

import com.uninter.tcc.dto.kafka.send.SendRequestDto;

public interface Kafka {

	String send(SendRequestDto request) throws Exception;
}
