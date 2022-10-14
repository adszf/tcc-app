package com.uninter.tcc.utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.csv.CsvSchema.Builder;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

public class Utilities {
    /*
     * private static final String STRING_ARRAY_SAMPLE =
     * "./string-array-sample.arff";
     */

    /* String fileName = "src/main/resources/items.arff"; */

    public Instances csvToArff(String jsonStringObject, String className) throws IOException {
        Object data = new Object();
        Date date = new Date();
        // format date in mm-dd-yyyy hh:mm:ss format
        SimpleDateFormat  sdf = new SimpleDateFormat("MM-dd-yyyy_hhmmss");
        String strDate = sdf.format(date);
        System.out.println("formatted date in MM-dd-yyyy_hhmmss : " + strDate);
        try {
            File fileInputCsv = new File("C:\\Users\\adson\\Desktop\\input\\" + className +"-"+ strDate +".csv");
            if (!fileInputCsv.exists()) {
                JsonNode jsonTree = new ObjectMapper().readTree(jsonStringObject);
                Builder csvSchemaBuilder = CsvSchema.builder();
                JsonNode firstObject = jsonTree.elements().next();
                firstObject.fieldNames().forEachRemaining(fieldName -> {
                    csvSchemaBuilder.addColumn(fieldName);
                });
                CsvSchema csvSchema = csvSchemaBuilder.build().withHeader();
                CsvMapper csvMapper = new CsvMapper();
                csvMapper.writerFor(JsonNode.class)
                        .with(csvSchema)
                        .writeValue(fileInputCsv, jsonTree);
            }
            // load CSV
            CSVLoader loader = new CSVLoader();
            loader.setSource(fileInputCsv);
            data = loader.getDataSet();
            // save ARFF
            ArffSaver saver = new ArffSaver();
            saver.setInstances((Instances) data);
            saver.setFile(new File("C:\\Users\\adson\\Desktop\\output\\" + className + ".arff"));
            saver.writeBatch();
            // .arff file will be created in the output location
        } catch (Exception e) {
            // TODO: handle exception
        }
        return (Instances) data;
    }
}
