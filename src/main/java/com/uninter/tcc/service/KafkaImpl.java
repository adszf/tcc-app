package com.uninter.tcc.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.uninter.tcc.model.CreditScoreFinalEntity;
import com.uninter.tcc.model.CustomCreditScoreEntity;
import com.uninter.tcc.repository.CreditScoreFinalRepository;
import com.uninter.tcc.repository.CreditScoreRepository;
import com.uninter.tcc.repository.CustomCreditScoreRepository;
import com.uninter.tcc.utility.Utilities;

import lombok.extern.slf4j.Slf4j;
import weka.classifiers.Classifier;
import weka.classifiers.functions.Logistic;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.logging.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;

import org.apache.commons.codec.binary.Base64;

import java.util.stream.IntStream;

@Service
public class KafkaImpl implements Kafka {

	@Autowired
	private CreditScoreFinalRepository creditScoreFinalRepository;

	@Autowired
	MachineLearning machineLearning;

	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;

	@Autowired
	@Qualifier("taskExecutor")
	private ExecutorService executorService;

	private final Utilities custom = new Utilities();
	private final ObjectMapper mapper = new ObjectMapper();
	private static CompletableFuture<Void> execute;
	private static List<CompletableFuture<?>> allfutures = new ArrayList<>();
	private static final AtomicInteger number = new AtomicInteger(0); // Número da página inicial
	private boolean hasMoreDocuments = true;
	//static final ReentrantLock counterLock = new ReentrantLock(true); // enable fairness policy

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(KafkaImpl.class);

	@Override
	public String send(String type, Integer quantity) throws Exception {
		int size = quantity; // Define o tamanho da página (quantidade de documentos por página)
		Boolean status = true;
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy_hhmmss");
		String strDate = sdf.format(date);
		if (!hasMoreDocuments) {
			status = false;
		}
		while (hasMoreDocuments) {

			Page<CreditScoreFinalEntity> creditFinalEntity = creditScoreFinalRepository
					.findAll(PageRequest.of(number.get(), size));
			log.info("NUMERO DA PAGINA: " + number.get());
			log.info(String.valueOf(creditFinalEntity.getTotalPages()));
			log.info(String.valueOf(creditFinalEntity.getPageable().getPageNumber()));
			execute = CompletableFuture.runAsync(() -> {
				try {
					String jsonStringObject = mapper.writeValueAsString(creditFinalEntity.getContent());
					// log.info("LISTA DO OBJETO: "+ jsonStringObject);
					String className = new CreditScoreFinalEntity().getClass().getSimpleName();
					Logistic logistic = new Logistic();
					Instances instanceToUse = machineLearning.instances(jsonStringObject, className, "creditScore");
					FilteredClassifier filteredClassifier = machineLearning.filter(logistic, "", "-R first");
					IntStream.range(0, 10).forEach(currentOfFolder -> {
						try {

							Classifier classifier = machineLearning.build(10, instanceToUse, currentOfFolder,
									filteredClassifier, instanceToUse);
							// Serializar o classificador para um array de bytes
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							SerializationHelper.write(baos, classifier);
							byte[] serializedClassifierBytes = baos.toByteArray();
							// Converter o array de bytes para uma representação Base64 (string)
							String serializedClassifier = Base64.encodeBase64String(serializedClassifierBytes);

							// Desserializar o classificador a partir do array de bytes
							// Converter a representação Base64 de volta para um array de bytes
							// byte[] serializedClassifierBytesConvert =
							// Base64.decodeBase64(serializedClassifier);
							// Classifier classifierToByte = (Classifier) SerializationHelper.read( new
							// ByteArrayInputStream(serializedClassifierBytesConvert));

							// log.info("CURRENT IN NUMBER OF FOLDERS: " + currentOfFolder + "\n THREAD: "+
							// Thread.currentThread().getName());
							kafkaTemplate.send("classifiers", serializedClassifier);
							if (executorService.awaitTermination(5, TimeUnit.SECONDS)) {
								executorService.shutdown();
							}
						} catch (Exception e) {
							e.printStackTrace();
							return;
						}
					});

				} catch (InterruptedException | ExecutionException InterruptedExecutionException) {
					log.info(InterruptedExecutionException.getMessage());
					throw new RuntimeException(InterruptedExecutionException);
				} catch (JsonProcessingException jsonException) {
					log.info(jsonException.getMessage());
					// jsonException.printStackTrace();
				} catch (Exception exception) {
					log.info(exception.getMessage());
					// exception.printStackTrace();
				}

			}, executorService);

			number.incrementAndGet();
			hasMoreDocuments = creditFinalEntity.hasNext();
			// log.info(String.valueOf(hasMoreDocuments));
			allfutures.add(execute);
		}
		CompletableFuture.allOf(allfutures.toArray(new CompletableFuture[allfutures.size()]))
				.whenComplete((result, ex) -> {
					if (ex == null) {
						log.info("OK");
						hasMoreDocuments = true;
						// executorService.shutdownNow();
						number.getAndSet(0);

					} else {
						log.info("Unable to send message: " + ex.getMessage());
						hasMoreDocuments = true;
						number.getAndSet(0);
						// executorService.shutdownNow();
					}
				});

		return status == true ? "Enviado para fila do Kafka" : "Em processo no MS, aguarde...";
	}

}
