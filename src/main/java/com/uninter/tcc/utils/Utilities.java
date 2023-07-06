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

    public Instances csvToArff(String jsonStringObject, String className) throws Exception {
        Object data = new Object();
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy_hhmmss");
        String strDate = sdf.format(date);
        File currentDir = new File("").getAbsoluteFile();
        /*
         * System.out.println( new File("").getAbsolutePath());
         * System.out.println( new File("").getAbsoluteFile());
         * System.out.println( new File("").getCanonicalPath());
         */
        /*
         * String s = "name: " + System.getProperty("os.name");
         * s += ", version: " + System.getProperty("os.version");
         * s += ", arch: " + System.getProperty("os.arch");
         * System.out.println("OS=" + s);
         * System.out.println("Working Directory = " + System.getProperty("user.dir"));
         */

        /*
         * Path currentRelativePath = Paths.get("");
         * String path = currentRelativePath.toAbsolutePath().toString();
         * System.out.println("Current absolute path is: " + path);
         */
        String path = OSValidator.getOS().equals("win") ? currentDir.getAbsolutePath()
                : OSValidator.getOS().equals("uni") ? "/opt/app" : "";
        try {
            if (!path.isBlank()) {
                File fileInputCsv = new File(path + "/input/" + className + "-" + strDate + ".csv");
                if (fileInputCsv.createNewFile()) {
                    JsonNode jsonTree = new ObjectMapper().readTree(jsonStringObject);
                    Builder csvSchemaBuilder = CsvSchema.builder();
                    JsonNode firstObject = jsonTree.elements().next();
                    firstObject.fieldNames().forEachRemaining(csvSchemaBuilder::addColumn);
                    CsvSchema csvSchema = csvSchemaBuilder.build().withHeader();
                    CsvMapper csvMapper = new CsvMapper();
                    csvMapper.writerFor(JsonNode.class)
                            .with(csvSchema)
                            .writeValue(fileInputCsv, jsonTree);
                } else {
                    throw new IOException("ERRO fileInputCsv");
                }
                // load CSV
                CSVLoader loader = new CSVLoader();
                loader.setSource(fileInputCsv);
                data = loader.getDataSet();
                // save ARFF
                ArffSaver saver = new ArffSaver();
                File fileOutputArff = new File(path + "/output/" + className + "-" + strDate + ".arff");
                if (fileOutputArff.createNewFile()) {
                    saver.setInstances((Instances) data);
                    saver.setFile(fileOutputArff);
                    saver.writeBatch();
                } else {
                    throw new IOException("ERRO fileOutputArff");
                }
            } else {
                throw new IOException("ERRO Path - Caminho de geração de arquivos não reconhecido!");
            } // .arff file will be created in the output location
        } catch (Exception e) {
            throw e;
        }
        return (Instances) data;
    }

    
}
