package com.uninter.tcc.controller;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.uninter.tcc.service.CreditAnalysis;

@RestController
public class AnalysisController {
    @Autowired
    private CreditAnalysis creditAnalysis;

    @GetMapping("memory-status")
    public ArrayList<String> getMemoryStatistics() {
        ArrayList<String> parametrosMemory = new ArrayList<String>();
        parametrosMemory.add("TOTAL MEMORY: "+String.valueOf(Runtime.getRuntime().totalMemory()));
        parametrosMemory.add("MAX MEMORY: "+String.valueOf(Runtime.getRuntime().maxMemory()));
        parametrosMemory.add("FREE MEMORY: "+String.valueOf(Runtime.getRuntime().freeMemory()));
        return parametrosMemory;
    }

    @GetMapping("/creditScore")
    public String credit(@RequestParam(name = "cpf", required = true/* defaultValue = "World" */) String cpf) {
        try {
            Long cpfNumber = Long.valueOf(cpf);
            creditAnalysis.creditScoreAnalysis(cpfNumber);

        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return cpf;

    }
}
