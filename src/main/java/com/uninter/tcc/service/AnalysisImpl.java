package com.uninter.tcc.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uninter.tcc.model.CreditScoreFinalEntity;
import com.uninter.tcc.repository.CreditScoreFinalRepository;
import com.uninter.tcc.repository.CreditScoreRepository;
import com.uninter.tcc.repository.CustomCreditScoreRepository;
import com.uninter.tcc.utility.Utilities;

import weka.classifiers.Classifier;
import weka.classifiers.functions.Logistic;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instance;
import weka.core.Instances;
import java.util.stream.IntStream;

@Service
public class AnalysisImpl implements Analysis {

	@Autowired
	private CreditScoreFinalRepository creditScoreFinalRepository;

	@Autowired
	MachineLearning machineLearning;

	private final Utilities custom = new Utilities();
	private final ObjectMapper mapper = new ObjectMapper();


	@Override
	public String creditScoreAnalysis(Long cpf) throws Exception {
		String result = null;
		try {
			List<CreditScoreFinalEntity> creditFinalEntity = creditScoreFinalRepository.findAll();
			String jsonStringObject = mapper.writeValueAsString(creditFinalEntity);
			String className = new CreditScoreFinalEntity().getClass().getSimpleName();
			Logistic log = new Logistic();
			List<Classifier> populateClassifier = new ArrayList<>();
			Instances instanceToUse = machineLearning.instances(jsonStringObject, className, "creditScore");
			FilteredClassifier filteredClassifier = machineLearning.filter(log, "", "-R first");
			List<List<Instance>> lists = machineLearning.partitions(10000, instanceToUse);
			IntStream.range(0, lists.size()).forEach(indexOfList -> IntStream.range(0, 10).forEach(currentOfFolder -> {
				try {
					populateClassifier.add(machineLearning.build(10, lists.get(indexOfList), currentOfFolder,filteredClassifier, instanceToUse));
					System.out.println("CURRENT IN NUMBER OF FOLDERS: " + currentOfFolder + "\n THREAD: "+ Thread.currentThread().getName());
					System.out.println("INDEX OF LIST: " + indexOfList + "\n THREAD: " + Thread.currentThread().getName());
				} catch (Exception e) {
					//e.printStackTrace();
					return;
				}
			}));
//			for (int i = 0; i < lists.size(); i++) {
//				List<Instance> instances = lists.get(i);
//			for (int y = 0; y < 10; y++) {
//				//populateClassifier.add(machineLearning.build(10, instances, i, filteredClassifier,instanceToUse));
//				populateClassifier.add(machineLearning.build(10, instances, i, filteredClassifier,instanceToUse));
//			}
//			}
//			 machineLearning.train(lists, filteredClassifier, instanceToUse);
			Instance IstanceToAnalyze = machineLearning.createAnalysis(instanceToUse, creditFinalEntity.get(20),
					"creditScore");
			System.out.println(mapper.writeValueAsString(creditFinalEntity.get(20)));
			result = machineLearning.analyzeSet(IstanceToAnalyze, populateClassifier);
		} catch (Exception e) {
			return result;
		}
		return result;
	}

}
