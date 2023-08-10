package com.uninter.tcc.controller;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.uninter.tcc.service.Analysis;

@RestController
@RequestMapping( "/utils" )
public class UtilsController {
    @GetMapping("memory-status")
    public ArrayList<String> getMemoryStatistics() {
        ArrayList<String> parametrosMemory = new ArrayList<String>();
        parametrosMemory.add("TOTAL MEMORY: "+String.valueOf(Runtime.getRuntime().totalMemory()));
        parametrosMemory.add("MAX MEMORY: "+String.valueOf(Runtime.getRuntime().maxMemory()));
        parametrosMemory.add("FREE MEMORY: "+String.valueOf(Runtime.getRuntime().freeMemory()));
        return parametrosMemory;
    }
}
