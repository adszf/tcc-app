package com.uninter.tcc.utility;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.csv.CsvSchema.Builder;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.core.converters.ConverterUtils.DataSource;

@Component
public class Utilities {

	private final ObjectMapper mapper = new ObjectMapper();

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Utilities.class);

	public Instances csvToArff(String jsonStringObject, String className, String id) throws Exception {
		Object data = new Object();
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy_hhmmss");
		String strDate = sdf.format(date);
		File currentDir = new File("").getAbsoluteFile();
		mapper.getFactory()
				.setStreamReadConstraints(StreamReadConstraints.builder().maxStringLength(100_000_000).build());

		/*
		 * System.out.println( new File("").getAbsolutePath()); System.out.println( new
		 * File("").getAbsoluteFile()); System.out.println( new
		 * File("").getCanonicalPath());
		 */
		/*
		 * String s = "name: " + System.getProperty("os.name"); s += ", version: " +
		 * System.getProperty("os.version"); s += ", arch: " +
		 * System.getProperty("os.arch"); System.out.println("OS=" + s);
		 * System.out.println("Working Directory = " + System.getProperty("user.dir"));
		 */
		/*
		 * Path currentRelativePath = Paths.get(""); String path =
		 * currentRelativePath.toAbsolutePath().toString();
		 * System.out.println("Current absolute path is: " + path);
		 */
		String path = OSValidator.getOS().equals("win") ? currentDir.getAbsolutePath()
				: OSValidator.getOS().equals("uni") ? "/opt/app" : "";
		try {
			if (!path.isBlank()) {
				UUID uuid = UUID.randomUUID();
				File fileInputCsv = new File(
						path + "/input/" + id + "-" + strDate + "-" + uuid.toString() + ".csv");
				if (fileInputCsv.createNewFile()) {
					JsonNode jsonTree = mapper.readTree(jsonStringObject);
					Builder csvSchemaBuilder = CsvSchema.builder();
					JsonNode firstObject = jsonTree.elements().next();
					firstObject.fieldNames().forEachRemaining(csvSchemaBuilder::addColumn);
					CsvSchema csvSchema = csvSchemaBuilder.build().withHeader();
					CsvMapper csvMapper = new CsvMapper();
					csvMapper.writerFor(JsonNode.class).with(csvSchema).writeValue(fileInputCsv, jsonTree);
				} else {
					throw new IOException("ERRO fileInputCsv");
				}
				// load CSV
				CSVLoader loader = new CSVLoader();
				loader.setSource(fileInputCsv);
				data = loader.getDataSet();
				// save ARFF
				ArffSaver saver = new ArffSaver();
				File fileOutputArff = new File(
						path + "/output/" + id + "-" + strDate + "-" + uuid.toString() + ".arff");
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
			logger.error("ERROR: ", e);
			throw e;
		}
		return (Instances) data;
	}

	public Instances generateModelInstances(String jsonStringObject)
			throws Exception {
		Object data = new Object();
		mapper.getFactory()
				.setStreamReadConstraints(StreamReadConstraints.builder().maxStringLength(100_000_000).build());
		try {
			CSVLoader csvLoader = new CSVLoader();
			JsonNode jsonTree = mapper.readTree(jsonStringObject);
			Builder csvSchemaBuilder = CsvSchema.builder();
			jsonTree.elements().next().fieldNames().forEachRemaining(csvSchemaBuilder::addColumn);
			CsvSchema csvSchema = csvSchemaBuilder.build().withHeader();
			CsvMapper csvMapper = new CsvMapper();
			InputStream in = new ByteArrayInputStream(
					csvMapper.writerFor(JsonNode.class).with(csvSchema).writeValueAsBytes(jsonTree));
			csvLoader.setSource(in);
			data = csvLoader.getDataSet();
		} catch (Exception e) {
			logger.error("ERROR: ", e);
			throw e;
		}
		return (Instances) data;

	}

	public ArrayList<File> getFilesOutputArff(String context) throws Exception {
		ArrayList<File> result = new ArrayList<>();
		File currentDir = new File("").getAbsoluteFile();
		// Caminho da pasta onde os arquivos estão
		String folderPath = OSValidator.getOS().equals("win") ? currentDir.getAbsolutePath() + "/output/"
				: OSValidator.getOS().equals("uni") ? "/opt/app/output/" : "";

		File folder = new File(folderPath);
		// Listando os arquivos na pasta
		File[] allFiles = folder.listFiles();
		// Procurando os arquivos que começam com "credit--381170476"
		if (allFiles != null) {
			for (File file : allFiles) {
				if (file.getName().startsWith(context)) {
					result.add(file);
				}
			}
		}
		return result;
	}

	public Instances mergeInstances(String idContext, String classIndex ) throws Exception {
		List<Instances> instancesList = new ArrayList<>();
		ArrayList<File> files = getFilesOutputArff(idContext);
		// Carregar cada arquivo de instâncias em uma lista
		for (int i = 0; i <= files.size() - 1; i++) {
			String filePath = files.get(i).getAbsolutePath();
			DataSource source = new DataSource(filePath);
			Instances dataset = source.getDataSet();
			instancesList.add(dataset);
		}
		// Mesclar as listas de instâncias
		Instances mergedInstances = new Instances(instancesList.get(0)); // Usar o primeiro como base
		mergedInstances.setClassIndex(mergedInstances.attribute(classIndex).index());
		for (int i = 1; i < instancesList.size(); i++) {
			for (int j = 0; j < instancesList.get(i).numInstances(); j++) {
				mergedInstances.add(instancesList.get(i).instance(j));
			}
		}
		return mergedInstances;
	}
}
