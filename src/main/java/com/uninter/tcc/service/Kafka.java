package com.uninter.tcc.service;

public interface Kafka {

	String send(String type,Integer quantity) throws Exception;
}
