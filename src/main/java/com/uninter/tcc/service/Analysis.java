package com.uninter.tcc.service;

public interface Analysis {

    String creditScoreAnalysis(String cpfNumber, String idContext, String sizeOfEachPage, String classPredict)
            throws Exception;

    String behaviorScoreAnalysis(String cpfNumber, String idContext, String quantityOfClassifiers,
            String classPredict) throws Exception;
}