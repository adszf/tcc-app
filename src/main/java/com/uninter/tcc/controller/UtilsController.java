package com.uninter.tcc.controller;

import java.util.ArrayList;
import java.util.Set;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/utils")
public class UtilsController {
    @GetMapping("memory-status")
    public ArrayList<String> getMemoryStatistics() {
        ArrayList<String> parametrosMemory = new ArrayList<String>();
        parametrosMemory.add("TOTAL MEMORY: " + String.valueOf(Runtime.getRuntime().totalMemory()));
        parametrosMemory.add("MAX MEMORY: " + String.valueOf(Runtime.getRuntime().maxMemory()));
        parametrosMemory.add("FREE MEMORY: " + String.valueOf(Runtime.getRuntime().freeMemory()));
        return parametrosMemory;
    }

    @GetMapping("running-threads")
    public String getAllRunningThreads() {
        StringBuilder response = new StringBuilder();
        Set<Thread> threads = Thread.getAllStackTraces().keySet();
        response.append(String.format("%-15s \t %-15s \t %-15s \t %s\n", "Name", "State", "Priority", "isDaemon"));
        for (Thread t : threads) {
            response.append(
                    String.format("%-15s \t %-15s \t %-15d \t %s\n", t.getName(), t.getState(), t.getPriority(),
                            t.isDaemon()));
        }
        return response.toString();
    }
}
