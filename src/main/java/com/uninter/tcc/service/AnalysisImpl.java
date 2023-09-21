package com.uninter.tcc.service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uninter.tcc.model.BehaviorScoreEntity;
import com.uninter.tcc.model.ClassifierEntity;
import com.uninter.tcc.model.CreditScoreFinalEntity;
import com.uninter.tcc.repository.BehaviorScoreRepository;
import com.uninter.tcc.repository.ClassifierRepository;
import com.uninter.tcc.repository.CreditScoreFinalRepository;
import com.uninter.tcc.utility.Utilities;

import lombok.Data;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;

@Service
@Data
public class AnalysisImpl implements Analysis {

	@Autowired
	private CreditScoreFinalRepository creditScoreFinalRepository;

	@Autowired
	private BehaviorScoreRepository behaviorScoreRepository;

	@Autowired
	private ClassifierRepository classifierRepository;

	@Autowired
	MachineLearning machineLearning;

	@Autowired
	@Qualifier("taskExecutorAnalysis")
	private ExecutorService executorService;

	private final Utilities utils = new Utilities();
	private final ObjectMapper mapper = new ObjectMapper();
	private List<Map<String, Classifier>> populateClassifier = new ArrayList<>();
	private static List<CompletableFuture<?>> allfutures = new ArrayList<>();
	private static final AtomicInteger number = new AtomicInteger(0); // Número da página inicial
	private boolean hasMoreDocuments = true;
	private CompletableFuture<Void> listPagesMongoFuture;
	private static final String CREDIT = "creditScore";
	private static final String BEHAVIOR = "behaviorScore";

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AnalysisImpl.class);

	@Override
	public String creditScoreAnalysis(Long cpfNumber, String idContext, String quantityOfClassifiers,
			String classPredict) throws Exception {

		Pageable pageableCreditScore = PageRequest.of(0, 2000);
		String result = null;
		try {

			while (hasMoreDocuments) {

				Pageable pageableClassifier = PageRequest.of(number.get(), Integer.parseInt(quantityOfClassifiers));

				Page<ClassifierEntity> page = classifierRepository.findByContext(idContext, pageableClassifier);

				listPagesMongoFuture = CompletableFuture.runAsync(() -> populate(page, CREDIT), executorService);

				number.incrementAndGet();

				hasMoreDocuments = page.hasNext();

				allfutures.add(listPagesMongoFuture);

			}

			CompletableFuture.allOf(allfutures.toArray(new CompletableFuture[allfutures.size()])).join();

			Page<CreditScoreFinalEntity> modeltest = creditScoreFinalRepository.findAll(pageableCreditScore);

			logger.info("Dados para predição: {}",modeltest.getContent().get(cpfNumber.intValue()));

			Instances mergedInstances = utils.mergeInstances(idContext, classPredict);

			Instance instanceToAnalyze = machineLearning.createAnalysis(mergedInstances,
					modeltest.getContent().get(cpfNumber.intValue()), classPredict);

			List<Classifier> list = populateClassifier.stream()
					.filter(map -> map.containsKey(CREDIT))
					.map(map -> map.get(CREDIT))
					.collect(Collectors.toList());

			result = machineLearning.analyzeSet(instanceToAnalyze, list);

			clear();

		} catch (Exception e) {
			logger.error("An error occurred:", e);
			clear();
			throw e;
		}
		return result;
	}

	@Override
	public String behaviorScoreAnalysis(Long cpfNumber, String idContext, String quantityOfClassifiers,
			String classPredict) throws Exception {

		Pageable pageableBehaviorScore = PageRequest.of(0, 2000);
		String result = null;
		try {

			while (hasMoreDocuments) {

				Pageable pageableClassifier = PageRequest.of(number.get(), Integer.parseInt(quantityOfClassifiers));

				Page<ClassifierEntity> page = classifierRepository.findByContext(idContext, pageableClassifier);

				listPagesMongoFuture = CompletableFuture.runAsync(() -> populate(page, BEHAVIOR), executorService);

				number.incrementAndGet();

				hasMoreDocuments = page.hasNext();

				allfutures.add(listPagesMongoFuture);

			}

			CompletableFuture.allOf(allfutures.toArray(new CompletableFuture[allfutures.size()])).join();

			Page<BehaviorScoreEntity> modeltest = behaviorScoreRepository.findAll(pageableBehaviorScore);

			logger.info("Dados para predição: {}",modeltest.getContent().get(cpfNumber.intValue()));

			Instances mergedInstances = utils.mergeInstances(idContext, classPredict);

			Instance instanceToAnalyze = machineLearning.createAnalysis(mergedInstances,
					modeltest.getContent().get(cpfNumber.intValue()), classPredict);

			List<Classifier> list = populateClassifier.stream()
					.filter(map -> map.containsKey(BEHAVIOR))
					.map(map -> map.get(BEHAVIOR))
					.collect(Collectors.toList());

			result = machineLearning.analyzeSet(instanceToAnalyze, list);

			clear();

		} catch (Exception e) {
			logger.error("An error occurred:", e);
			clear();
			throw e;
		}
		return result;
	}

	private Void populate(Page<ClassifierEntity> page, String type) {
		page.getContent().parallelStream().forEach(current -> {
			byte[] serializedClassifierBytesConvert = Base64.decodeBase64(current.getClassifierCompact());
			Classifier classifier;
			try {
				classifier = (Classifier) SerializationHelper
						.read(new ByteArrayInputStream(serializedClassifierBytesConvert));
				Map<String, Classifier> mapClassifiers = new HashMap<>();
				mapClassifiers.put(type, classifier);
				populateClassifier.add(mapClassifiers);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		return null;

	}

	void clear() {
		hasMoreDocuments = true;
		number.getAndSet(0);
		populateClassifier.clear();
	}
}
