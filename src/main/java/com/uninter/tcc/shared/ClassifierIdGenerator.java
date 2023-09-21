package com.uninter.tcc.shared;

import java.util.Random;

import org.springframework.stereotype.Component;

@Component
public class ClassifierIdGenerator {

    private final Random random;

    public ClassifierIdGenerator() {
        this.random = new Random();
    }

    public String generateId(String context) {
        StringBuilder key = new StringBuilder(context);
        int id = Math.abs(random.nextInt());
        key.append("-");
        key.append(id);
        return key.toString();
    }
}
