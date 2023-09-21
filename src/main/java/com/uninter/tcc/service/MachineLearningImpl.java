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

import com.uninter.tcc.shared.Utilities;

import org.apache.commons.collections4.ListUtils;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.meta.Vote;
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

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MachineLearningImpl.class);

	public Evaluation cross(List<Instance> instanceList, int numFolds, FilteredClassifier filteredClassifier,
			Evaluation evaluation, Instances instancesModel) throws Exception {
		Instances data = new Instances(instancesModel, 0);
		data.addAll(instanceList);
		for (int i = 0; i < numFolds; i++) {
			Classifier classifier = build(numFolds, data, i, filteredClassifier, instancesModel);
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
	public Evaluation train(List<List<Instance>> partitions, FilteredClassifier filteredClassifier,
			Instances instancesModel) throws Exception {
		Evaluation evaluationActual = new Evaluation(instancesModel);
		StringBuilder evaluationStatus = new StringBuilder();
		for (int i = 0; i < partitions.size(); i++) {
			logger.info("______INICIO______");
			List<Instance> instances = partitions.get(i);
			Integer numFolds = instances.size() <= 10 ? instances.size() : 10;
			logger.info("{}", partitions.get(i).size());
			logger.info("{}", String.valueOf(i));
			// evaluation.crossValidateModel(fc, instances, 5, new Random(1));
			cross(instances, numFolds, filteredClassifier, evaluationActual, instancesModel);
			logger.info("CORRETO: {}", evaluationActual.correct());
			logger.info("INCORRETO: {}", evaluationActual.incorrect());
			logger.info("NAO CLASSIFICADO: {}", evaluationActual.unclassified());
			logger.info("PORCENTAGEM CORRETA: {}", evaluationActual.pctCorrect());
			logger.info("PORCENTAGEM INCORRETA: {}", evaluationActual.pctIncorrect());
			logger.info("PORCENTAGEM NAO CLASSIFICADA: {}", evaluationActual.pctUnclassified());
			logger.info("______FIM______");
		}
		evaluationStatus.append(evaluationActual.toSummaryString(true)).append(evaluationActual.toClassDetailsString())
				.append(evaluationActual.toMatrixString("=== Confusion Matrix ==="));
		logger.info("{}",evaluationStatus.toString());
		return evaluationActual;
	}

	@Override
	public Classifier build(Integer numFolds, List<Instance> instance, Integer sequence,
			FilteredClassifier filteredClassifier, Instances instanceToUse) throws Exception {
		logger.info("SEQUENCE: {}",sequence);
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
			result.append("\n" + newIstanceToAnalyze.attribute(newIstanceToAnalyze.numAttributes() - 1).value(count)
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
		Evaluation evaluation = train(list, filteredClassifier, instances);
		response.append(evaluation.toSummaryString(true)).append(evaluation.toClassDetailsString())
				.append(evaluation.toMatrixString("=== Confusion Matrix ==="));
		return response.toString();
	}
}
