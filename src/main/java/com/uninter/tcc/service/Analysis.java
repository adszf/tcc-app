package com.uninter.tcc.service;

public interface Analysis {

    String creditScoreAnalysis(Long cpfNumber, String idContext, String sizeOfEachPage, String classPredict)
            throws Exception;

    String behaviorScoreAnalysis(Long cpfNumber, String idContext, String quantityOfClassifiers,
            String classPredict) throws Exception;
}