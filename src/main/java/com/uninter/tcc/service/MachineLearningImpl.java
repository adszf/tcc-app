package com.uninter.tcc.service;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uninter.tcc.share.Utilities;

import org.apache.commons.collections4.ListUtils;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.meta.Vote;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

@Service
public class MachineLearningImpl implements MachineLearning {

	@Autowired
	Utilities util = new Utilities();

	// Usado para utilizar Threads
	// @Autowired
	// @Qualifier("taskExecutorMachineLearning")
	// private ExecutorService executorService;
	// final ReentrantLock counterLock = new ReentrantLock(true);

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MachineLearningImpl.class);

	public Evaluation cross(List<Instance> instanceList, int numFolds, FilteredClassifier filteredClassifier,
			Evaluation evaluation, Instances instancesModel) throws Exception {
		Instances data = new Instances(instancesModel, 0);
		data.addAll(instanceList);
		/*
		  IntStream.range(0, numFolds).forEach(currentOfFolder -> {
		  Classifier classifier;
		  try {
		  classifier = build(numFolds, data, currentOfFolder, filteredClassifier,
		  instancesModel);
		  Instances test = data.testCV(numFolds, currentOfFolder);
		  evaluation.evaluateModel(classifier, test);
		  } catch (Exception e) {
		  e.printStackTrace();
		  new RuntimeException(e);
		  }
		  });
		 */

		for (int i = 0; i < numFolds; i++) {
			// Se fosse executar em Threads, devesse ter o counterLock para trancar o
			// processo, pois o mesmo precisa esta finalizado para continuar as proximas Steps.
			// counterLock.lock();
			Classifier classifier = build(numFolds, data, i, filteredClassifier,
					instancesModel);
			// counterLock.unlock();
			Instances test = data.testCV(numFolds, i);
			evaluation.evaluateModel(classifier, test);
		}

		return evaluation;
	}

	@Override
	public Instances instances(String jsonStringObject, String className, String classIndex, boolean onlyModel,
			String id)
			throws Exception {
		Instances instancesActual;
		DataSource loader = new DataSource(!onlyModel ? util.csvToArff(jsonStringObject, className, id)
				: util.generateModelInstances(jsonStringObject));
		instancesActual = loader.getDataSet();
		instancesActual.setClassIndex(instancesActual.attribute(classIndex).index());
		return instancesActual;

	}

	@Override
	public FilteredClassifier filter(String classMl, String optionsClassifier, List<String> optionsFilter)
			throws Exception {
		FilteredClassifier filteredClassifierActual;
		AbstractClassifier abstractClassifierActual;
		Class<?> clazz = Class.forName(classMl);
		Constructor<?> constructor = clazz.getConstructor();
		Object instance = constructor.newInstance();
		abstractClassifierActual = (AbstractClassifier) instance;
		filteredClassifierActual = new FilteredClassifier();
		Pattern pattern = Pattern.compile("([\\w.]+)\\s+(-.*)");
		if (!Arrays.toString(optionsFilter.toArray()).isBlank()) {
			IntStream.range(0, optionsFilter.size()).forEach(index -> {
				Matcher matcher = pattern.matcher(optionsFilter.get(index));
				if (matcher.matches()) {
					String filterClass = matcher.group(1);
					String options = matcher.group(2);
					Class<?> className;
					try {
						className = Class.forName(filterClass);
						Constructor<?> constructorFilter = className.getConstructor();
						Filter filter = (Filter) constructorFilter.newInstance();
						filter.setOptions(Utils.splitOptions(options));
						filteredClassifierActual.setFilter(filter);
					} catch (Exception e) {
						logger.error("An error occurred:", e);
						throw new RuntimeException(e);
					}
				}
			});
		}
		if (!optionsClassifier.isBlank()) {
			Method findMethod = instance.getClass().getMethod("setOptions", String[].class);
			String[] options = Utils.splitOptions(optionsClassifier);
			findMethod.invoke(instance, (Object) options);

		}
		filteredClassifierActual.setClassifier(abstractClassifierActual);
		return filteredClassifierActual;
	}

	@Override
	public List<List<Instance>> partitions(Integer partitionSize, Instances instances) throws Exception {
		return ListUtils.partition(instances, partitionSize);
	}

	@Override
	public StringBuilder train(List<List<Instance>> partitions, FilteredClassifier filteredClassifier,
			Instances instancesModel) throws Exception {
		Evaluation evaluationActual = new Evaluation(instancesModel);
		StringBuilder evaluationStatus = new StringBuilder();
		// Executando em Threads não obteve um aumento significativo na performace.
		// No processo de train, o mesmo foi usado em forma sequencial e paralelo a fim de verificar  aumento de performace, entretanto, não obtevesse desempenho.
		/*
		  CompletableFuture<Void> execute;
		  List<CompletableFuture> allfutures = new ArrayList<>();
		  AtomicInteger count = new AtomicInteger(0);
		  
		  for (List<Instance> actualInstance : partitions) {
		  execute = CompletableFuture.runAsync(() -> {
		  try {
		  logger.info("______START______");
		  List<Instance> instances = actualInstance;
		  Integer numFolds = instances.size() <= 10 ? instances.size() : 10;
		  logger.info("{}", partitions.get(count.get()).size());
		  logger.info("{}", count.get());
		  // evaluation.crossValidateModel(fc, instances, 5, new Random(1));
		  cross(instances, numFolds, filteredClassifier, evaluationActual,
		  instancesModel);
		  chooseLoggerType(instancesModel.classAttribute(), evaluationActual);
		  count.getAndIncrement();
		  logger.info("______END______");
		  } catch (Exception e) {
		  throw new RuntimeException(e);
		  }
		  
		  }, executorService);
		  allfutures.add(execute);
		  }
		  CompletableFuture.allOf(allfutures.toArray(new
		  CompletableFuture[allfutures.size()])).join();
		 */
		for (int i = 0; i < partitions.size(); i++) {
			logger.info("______START______");
			List<Instance> instances = partitions.get(i);
			Integer numFolds = instances.size() <= 10 ? instances.size() : 10;
			logger.info("{}", partitions.get(i).size());
			logger.info("{}", i);
			// evaluation.crossValidateModel(fc, instances, 5, new Random(1));
			cross(instances, numFolds, filteredClassifier, evaluationActual,
					instancesModel);
			chooseLoggerType(instancesModel.classAttribute(), evaluationActual);
			logger.info("______END______");
		}
		evaluationStatus.append(evaluationActual.toSummaryString(true));
		if (instancesModel.classAttribute().isNominal()) {
			evaluationStatus.append(evaluationActual.toClassDetailsString());
			evaluationStatus.append(evaluationActual.toMatrixString("=== Confusion Matrix ==="));
		}
		return evaluationStatus;
	}

	private void chooseLoggerType(Attribute attribute, Evaluation evaluationActual) throws Exception {
		if (attribute.isNominal()) {
			logger.info("Correct: {}", evaluationActual.correct());
			logger.info("Incorrect: {}", evaluationActual.incorrect());
			logger.info("Unclassified: {}", evaluationActual.unclassified());
			logger.info("Pct.Correct: {}", evaluationActual.pctCorrect());
			logger.info("Pct.Incorrect: {}", evaluationActual.pctIncorrect());
			logger.info("Pct.Unclassified: {}", evaluationActual.pctUnclassified());
		} else {
			logger.info("Correlation coefficient: {}", evaluationActual.correlationCoefficient());
			logger.info("Mean absolute error : {}", evaluationActual.meanAbsoluteError());
			logger.info("Root mean squared error: {}", evaluationActual.rootMeanSquaredError());
			logger.info("Relative absolute error : {}", evaluationActual.relativeAbsoluteError());
			logger.info("Root relative squared error: {}", evaluationActual.rootRelativeSquaredError());
		}
	}

	@Override
	public Classifier build(Integer numFolds, List<Instance> instance, Integer sequence,
			FilteredClassifier filteredClassifier, Instances instanceToUse) throws Exception {
		logger.info("SEQUENCE: {}", sequence);
		Instances data = new Instances(instanceToUse, 0);
		data.addAll(instance);
		data.randomize(new Random(1));
		if (data.classAttribute().isNominal()) {
			data.stratify(numFolds);
		}
		Instances train = data.trainCV(numFolds, sequence, new Random(1));
		filteredClassifier.buildClassifier(train);
		return AbstractClassifier.makeCopy(filteredClassifier);
	}

	@Override
	public Instance createAnalysis(Instances instances, Object client, String classForPrediction) throws Exception {
		Instance newIstanceToAnalyze = new DenseInstance(instances.numAttributes());
		newIstanceToAnalyze.setDataset(instances);
		for (int i = 0; i < newIstanceToAnalyze.numAttributes(); i++) {
			String name = instances.attribute(i).name();
			Field field = client.getClass().getDeclaredField(name);
			field.setAccessible(true);
			Object value = field.get(client);
			Boolean flagFieldExists = name.equalsIgnoreCase(field.getName());
			if (Boolean.TRUE.equals(flagFieldExists)) {
				if (!classForPrediction.equalsIgnoreCase(name)) {
					if (instances.attribute(i).isNominal()) {
						newIstanceToAnalyze.setValue(instances.attribute(i), value.toString());
					} else if (instances.attribute(i).isNumeric()) {
						newIstanceToAnalyze.setValue(instances.attribute(i),
								Double.parseDouble(value.toString()));
					} else {
						newIstanceToAnalyze.setMissing(instances.attribute(i));
					}
				} else {
					newIstanceToAnalyze.setMissing(instances.attribute(i));
				}
			} else {
				newIstanceToAnalyze.setMissing(instances.attribute(i));
			}
		}
		return newIstanceToAnalyze;

	}

	@Override
	public String analyzeSet(Instance newIstanceToAnalyze, List<Classifier> populateClassifier) throws Exception {
		NumberFormat nf = new DecimalFormat("0.#");
		Vote ensembleClassifier = new Vote();
		StringBuilder result = new StringBuilder();
		ensembleClassifier.setClassifiers(populateClassifier.toArray(Classifier[]::new));
		double[] predicts = ensembleClassifier.distributionForInstance(newIstanceToAnalyze);
		int count = 0;
		for (double predict : predicts) {
			result.append("\n" + newIstanceToAnalyze.attribute(newIstanceToAnalyze.classIndex()).value(count)
					+ ":" + nf.format(predict));
			count++;
		}
		return result.toString();
	}

	@Override
	public String requestTrain(String idContext, Integer partitionSize, String classnameClassifier,
			String optClassifier, List<String> optFilter, String classIndex) throws Exception {
		Instances instances = util.mergeInstances(idContext, classIndex);
		StringBuilder response = new StringBuilder();
		List<List<Instance>> list = partitions(partitionSize, instances);
		FilteredClassifier filteredClassifier = filter(classnameClassifier, optClassifier, optFilter);
		response = train(list, filteredClassifier, instances);
		return response.toString();
	}
}
