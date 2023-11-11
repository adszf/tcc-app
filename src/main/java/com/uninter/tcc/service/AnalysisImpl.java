package com.uninter.tcc.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uninter.tcc.model.BehaviorScoreEntity;
import com.uninter.tcc.model.ClassifierEntity;
import com.uninter.tcc.model.ClientBehaviorScoreEntity;
import com.uninter.tcc.model.ClientCreditScoreEntity;
import com.uninter.tcc.model.CreditScoreEntity;
import com.uninter.tcc.repository.BehaviorScoreRepository;
import com.uninter.tcc.repository.ClassifierRepository;
import com.uninter.tcc.repository.ClientBehaviorScoreRepository;
import com.uninter.tcc.repository.ClientCreditScoreRepository;
import com.uninter.tcc.repository.CreditScoreRepository;
import com.uninter.tcc.share.Utilities;

import lombok.Data;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;

@Service
@Data
public class AnalysisImpl implements Analysis {

	@Autowired
	private CreditScoreRepository creditScoreRepository;

	@Autowired
	private BehaviorScoreRepository behaviorScoreRepository;

	@Autowired
	private ClassifierRepository classifierRepository;

	@Autowired
	private MachineLearning machineLearning;

	@Autowired
	private ClientCreditScoreRepository clientCreditScoreRepository;

	@Autowired
	private ClientBehaviorScoreRepository clientBehaviorScoreRepository;

	@Autowired
	@Qualifier("taskExecutorAnalysis")
	private ExecutorService executorService;

	private final Utilities utils = new Utilities();
	private final ObjectMapper mapper = new ObjectMapper();
	private final ModelMapper modelMapper = new ModelMapper();
	private List<Map<String, Classifier>> populateClassifier = new ArrayList<>();
	private static List<CompletableFuture<?>> allfutures = new ArrayList<>();
	private static final AtomicInteger number = new AtomicInteger(0); // Número da página inicial
	private boolean hasMoreDocuments = true;
	private CompletableFuture<Void> listPagesMongoFuture;
	private static final String CREDIT = "creditScore";
	private static final String BEHAVIOR = "behaviorScore";

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AnalysisImpl.class);

	@Override
	public String creditScoreAnalysis(String cpfNumber, String idContext, String quantityOfClassifiers,
			String classPredict) throws Exception {
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

			ClientCreditScoreEntity modeltest = clientCreditScoreRepository.findByCpfAndTipo(cpfNumber,
					CREDIT);

			CreditScoreEntity client = modelMapper.map(modeltest, CreditScoreEntity.class);

			logger.info("Dados para predição: {}", client);

			Instances mergedInstances = utils.mergeInstances(idContext, classPredict);

			Instance instanceToAnalyze = machineLearning.createAnalysis(mergedInstances,
					client, classPredict);

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
	public String behaviorScoreAnalysis(String cpfNumber, String idContext, String quantityOfClassifiers,
			String classPredict) throws Exception {
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

			ClientBehaviorScoreEntity modeltest = clientBehaviorScoreRepository.findByCpfAndTipo(cpfNumber,
					BEHAVIOR);

			BehaviorScoreEntity client = modelMapper.map(modeltest, BehaviorScoreEntity.class);

			logger.info("Dados para predição: {}", client);

			Instances mergedInstances = utils.mergeInstances(idContext, classPredict);

			Instance instanceToAnalyze = machineLearning.createAnalysis(mergedInstances,
					client, classPredict);

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
			// byte[] serializedClassifierBytesConvert =
			// Base64.decodeBase64(current.getClassifierCompact());
			Classifier classifier;
			try {
				/*
				 * File modelFile = new File(current.getClassifierCompact());
				 * FileInputStream fileInputStream = new FileInputStream(modelFile);
				 * ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
				 */
				classifier = (Classifier) SerializationHelper
						.read(current.getClassifierCompact());
				/* classifier = (Classifier) objectInputStream; */
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
