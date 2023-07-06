package com.uninter.tcc.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;

import com.uninter.tcc.domain.entity.CustomCreditScoreEntity;
import com.uninter.tcc.repository.CustomCreditScoreRepository;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class MongoWekaIntegration {

	public void processarDados(int pageSize, int pageNumber, CustomCreditScoreRepository repository,
			MongoTemplate mongoTemplate) throws Exception {
		int Size = 1000; // Define o tamanho da página (quantidade de documentos por página)
		int Number = 0; // Número da página inicial
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy_hhmmss");
		String strDate = sdf.format(date);
		File currentDir = new File("").getAbsoluteFile();
		String path = OSValidator.getOS().equals("win") ? currentDir.getAbsolutePath()
				: OSValidator.getOS().equals("uni") ? "/opt/app" : "";

		boolean hasMoreDocuments = true;

		while (hasMoreDocuments) {
			// Obter a página atual de documentos do MongoDB
			Page<CustomCreditScoreEntity> page = getDocumentPage(pageNumber, pageSize, repository);

			// Verificar se há mais documentos a serem processados
			hasMoreDocuments = page.hasNext();

			// Processar documentos da página atual
			List<Instance> instances = carregarDados(page.getContent(), mongoTemplate);

			// Criar conjunto de dados Weka
			Instances data = converterParaARFF(instances);

			// Salvar conjunto de dados em um arquivo ARFF
			salvarARFF(data, path + "/output/" + "document" + "-" + strDate + ".arff");

			pageNumber++; // Avançar para a próxima página
		}
	}

	private Page<CustomCreditScoreEntity> getDocumentPage(int pageNumber, int pageSize,
			CustomCreditScoreRepository repository) {

		return repository.findAll(PageRequest.of(pageNumber, pageSize));
	}

	private List<Instance> carregarDados(List<CustomCreditScoreEntity> list, MongoTemplate mongoTemplate) {
		List<Instance> instances = new ArrayList<>();

		for (CustomCreditScoreEntity documento : list) {
			Document document = (Document) mongoTemplate.getConverter().convertToMongoType(documento);
			Instance instance = criarInstancia(document);
			instances.add(instance);
		}

		return instances;
	}

	private Instance criarInstancia(Document documento) {
		Set<String> campos = documento.keySet();
		ArrayList<Attribute> attributes = new ArrayList<>();

		for (String campo : campos) {
			Object valor = documento.get(campo);

			if (valor instanceof Number) {
				attributes.add(new Attribute(campo));
			} else {
				// Caso seja uma string, criar um atributo nominal
				//List<String> valoresNominais = obterValoresNominais(documento, campo);
				attributes.add(new Attribute(campo, (List<String>) null));
			}
		}

		DenseInstance instance = new DenseInstance(attributes.size());
		instance.setDataset(new Instances("Dataset", attributes, 0));
		int count = 0;
		for (String campo : campos) {
			
			Object valor = documento.get(campo);

			if (valor instanceof Number) {
				instance.setValue(attributes.get(attributes.indexOf(new Attribute(campo))),
						((Number) valor).doubleValue());
			} else if (valor instanceof String || valor instanceof Object) {
				instance.setValue(attributes.get(count), String.valueOf(valor));
			}
			count++;
		}
		count = 0;

		return instance;
	}

	private List<String> obterValoresNominais(Document documento, String campo) {
		List<String> valoresNominais = new ArrayList<>();

		// Verifica se o campo existe no documento
		if (documento.containsKey(campo)) {
			// Obtém o valor do campo no documento
			Object valorCampo = documento.get(campo);
			// Verifica se o valor do campo é uma string
			if (valorCampo instanceof String) {
				String valorNominal = (String) valorCampo;
				// Adiciona o valor nominal à lista
				valoresNominais.add(valorNominal);
			} else {
				// Verifica se o valor do campo é um documento (objeto)
				if (valorCampo instanceof Object) {
//	            Document documentoInterno = (Document) valorCampo;
					String valorNominal = String.valueOf(valorCampo);
					// Adiciona o valor nominal à lista
					valoresNominais.add(valorNominal);
					// Itera sobre as chaves do documento interno
//	            for (String chaveInterna : documentoInterno.keySet()) {
//	                Object valorInterno = documentoInterno.get(chaveInterna);
//	                // Verifica se o valor interno é uma string
//	                if (valorInterno instanceof String) {
//	                    String valorNominal = (String) valorInterno;
//	                    // Adiciona o valor nominal à lista
//	                    valoresNominais.add(valorNominal);
//	                }
//	            }
				}
			}
			// Verifica se o valor do campo é uma lista
			if (valorCampo instanceof List<?>) {
				// Itera sobre os elementos da lista
				List<?> listaValores = (List<?>) valorCampo;
				for (Object valor : listaValores) {
					// Verifica se o valor é uma string
					if (valor instanceof String) {
						String valorNominal = (String) valor;
						// Adiciona o valor nominal à lista, se ainda não estiver presente
						if (!valoresNominais.contains(valorNominal)) {
							valoresNominais.add(valorNominal);
						}
					}
				}
			}
		}

		return valoresNominais;
	}

	private void salvarARFF(Instances data, String filePath) throws Exception {
		ArffSaver saver = new ArffSaver();
		saver.setInstances(data);
		saver.setFile(new File(filePath));
		saver.writeBatch();
	}

	private Instances converterParaARFF(List<Instance> instances) {
		int numAttributes = instances.get(0).numAttributes();
		Instances data = new Instances("Dataset", new ArrayList<>(), instances.size());

		for (int i = 0; i < numAttributes; i++) {
			Attribute attribute = instances.get(0).attribute(i);
			data.insertAttributeAt(attribute, i);
		}

		for (Instance instance : instances) {
			data.add(instance);
		}

		return data;
	}
}