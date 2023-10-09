package com.uninter.tcc.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uninter.tcc.dto.kafka.send.SendRequestDto;
import com.uninter.tcc.model.ClassifierEntity;
import com.uninter.tcc.repository.ClassifierRepository;
import com.uninter.tcc.share.ClassifierIdGenerator;
import com.uninter.tcc.share.Utilities;

import lombok.Data;
import weka.classifiers.Classifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instance;
import weka.core.Instances;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.modelmapper.ModelMapper;

import java.util.stream.IntStream;

@Service
@Data
public class KafkaImpl implements Kafka {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private ClassifierRepository classifierRepository;

	@Autowired
	MachineLearning machineLearning;

	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;

	@Autowired
	@Qualifier("taskExecutorKafka")
	private ExecutorService executorService;

	@Autowired
	ClassifierIdGenerator generator;

	private final Utilities utils = new Utilities();
	private final ObjectMapper mapper = new ObjectMapper();
	private final ModelMapper modelMapper = new ModelMapper();
	private static List<CompletableFuture<?>> allfuturesPopulate = new ArrayList<>();
	private static List<CompletableFuture<?>> allFuturesMountClassifier = new ArrayList<>();
	private static final AtomicInteger number = new AtomicInteger(0); // Número da página inicial
	private boolean hasMoreDocuments = true;
	private String context;
	private List<List<Instance>> lists = new ArrayList<>();
	private Instances instanceToUse;
	private List<Object> populate = new ArrayList<>();
	private boolean status = true;
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(KafkaImpl.class);
	private volatile int count = 0;

	@Override
	public String send(SendRequestDto request) throws Exception {
		try {
			Class<?> clazzClassname = Class.forName("com.uninter.tcc.model." + request.getType() + "Entity");
			Constructor<?> constructor = clazzClassname.getConstructor();
			var createClassname = constructor.newInstance();
			Class<?> repositoryClass = Class.forName("com.uninter.tcc.repository." + request.getType() + "Repository");
			int size = request.getQuantity(); // Define o tamanho da página (quantidade de documentos por página)
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
			String strDate = sdf.format(date);
			if (!hasMoreDocuments) {
				status = false;

			} else {
				context = generator.generateId(request.getContext());
			}
			while (hasMoreDocuments) {
				populateObjects(repositoryClass, size);
			}
			allfuturesPopulate.forEach(CompletableFuture::join);
			if (status) {
				logger.info("OK POPULATE");
				mountObjects(createClassname, request, strDate);
			}
		} catch (Exception e) {
			Thread.currentThread().interrupt();
			logger.error("Unable to send message:", e);
			clear();
		}
		return Boolean.TRUE.equals(status)
				? "Enviado para fila do Kafka, esse é o id gerado: " + context
				: "Em processo no MS, aguarde...";
	}

	private Void mountObjects(Object createClassname, SendRequestDto request, String strDate) throws Exception {
		try {

			instanceToUse = machineLearning.instances(
					mapper.writeValueAsString(populate.toArray()),
					createClassname.getClass().getSimpleName(),
					request.getClassIndex(), false, context);
			lists = machineLearning.partitions(request.getQuantity(), instanceToUse);
			IntStream.range(0, lists.size()).forEach(indexOfList -> {
				CompletableFuture<Void> listClassifierMount;
				listClassifierMount = CompletableFuture.runAsync(() -> {
					try {
						classifierMount(lists.get(indexOfList), request, strDate, context,
								indexOfList);
					} catch (Exception e) {
						Thread.currentThread().interrupt();
						logger.error("An error occurred:", e);
						throw new RuntimeException(e);
					}

				}, executorService);
				allFuturesMountClassifier.add(listClassifierMount);
			});
			CompletableFuture
					.allOf(allFuturesMountClassifier
							.toArray(new CompletableFuture[allFuturesMountClassifier.size()]))
					.whenComplete((resultMountClassifier, exMountClassifier) -> {
						logger.info("OK MOUNT");
						clear();
					});
		} catch (Exception e) {
			Thread.currentThread().interrupt();
			logger.error("An error occurred:", e);
			throw new RuntimeException(e);
		}
		return null;
	}

	private void classifierMount(List<Instance> list, SendRequestDto request, String strDate, String context,
			int indexOfList) throws Exception {
		FilteredClassifier filteredClassifier = machineLearning.filter(request.getClassnameClassifier(),
				request.getOptClassifier(), request.getOptFilter());
		Integer numFolds = list.size() <= 10 ? list.size() : 10;
		IntStream.range(0, numFolds).forEach(currentOfFolder -> {
			try {
				logger.info("INDEX_OF_LIST: {}", String.valueOf(indexOfList));
				Classifier classifier = machineLearning.build(numFolds, list, currentOfFolder, filteredClassifier,
						instanceToUse);
				ClassifierEntity entity = modelMapper.map(request, ClassifierEntity.class);
				entity.setDate(strDate);
				entity.setClassifierCompact(utils.wekaSaveModel(classifier, context, count++));
				entity.setContext(context);
				kafkaTemplate.send("classifiers", mapper.writeValueAsString(entity));
				if (executorService.awaitTermination(5, TimeUnit.SECONDS)) {
					executorService.shutdownNow();
				}
			} catch (Exception e) {
				logger.error("An error occurred:", e);
				throw new RuntimeException(e);
			}
		});

	}

	private Void populateObjects(Class<?> repositoryClass, int size) throws Exception {
		CompletableFuture<Void> listPagesMongoFuture;
		MongoRepositoryFactory factory = new MongoRepositoryFactory(mongoTemplate);
		var repository = factory.getRepository(repositoryClass);
		Pageable pageable = PageRequest.of(number.get(), size);
		Method findMethod = repositoryClass.getMethod("findAll", Pageable.class);
		Page<?> page = (Page<?>) findMethod.invoke(repository, pageable);
		listPagesMongoFuture = CompletableFuture.runAsync(() -> {
			try {
				populate.addAll(page.getContent());
			} catch (Exception exception) {
				logger.error(exception.getMessage());
				Thread.currentThread().interrupt();
			}
		}, executorService);
		number.incrementAndGet();
		hasMoreDocuments = page.hasNext();
		allfuturesPopulate.add(listPagesMongoFuture);
		return null;
	}

	void clear() {
		logger.info("Cleaning up!");
		status = true;
		hasMoreDocuments = true;
		number.getAndSet(0);
		instanceToUse = null;
		populate.clear();
		allFuturesMountClassifier.clear();
		allfuturesPopulate.clear();
		count = 0;

	}

	@KafkaListener(topics = "classifiers")
	private void listen(
			@Payload String message,
			@Header(KafkaHeaders.OFFSET) int offset) throws Exception {
		ClassifierEntity entity = mapper.readValue(message, ClassifierEntity.class);
		classifierRepository.save(entity);
		logger.info(entity.getId());
	}

}
