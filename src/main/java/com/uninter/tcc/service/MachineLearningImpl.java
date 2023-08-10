package com.uninter.tcc.service;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uninter.tcc.utility.Utilities;
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
import weka.filters.unsupervised.attribute.Remove;
import java.util.Random;

@Service
public class MachineLearningImpl implements MachineLearning {

	private final ObjectMapper mapper = new ObjectMapper();
	@Autowired 
	Utilities util = new Utilities();

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
	public Instances instances(String jsonStringObject, String className, String classIndex) throws Exception {
		Instances instancesActual;
		DataSource loader = new DataSource(util.csvToArff(jsonStringObject, className));
		instancesActual = loader.getDataSet();
		instancesActual.setClassIndex(instancesActual.attribute(classIndex).index());
		return instancesActual;

	}

	@Override
	public FilteredClassifier filter(Object classMl, String options, String remove) throws Exception {
		Remove removeActual;
		FilteredClassifier filteredClassifierActual;
		AbstractClassifier abstractClassifierActual;
		abstractClassifierActual = (AbstractClassifier) classMl;
		filteredClassifierActual = new FilteredClassifier();
		// new Logistic();
		// abstractClassifier = new MultilayerPerceptron();
		removeActual = new Remove();
		if (!remove.isBlank()) {
			removeActual.setOptions(Utils.splitOptions(remove));
			filteredClassifierActual.setFilter(removeActual);
		}
		if (!options.isBlank()) {
			abstractClassifierActual.setOptions(Utils.splitOptions(options));
		}
		// removeActual.setAttributeIndices("first");
		// removeActual.setInvertSelection(false);
		// "-F 1 -L 0.01 -R 1.0E-4 -E 100 -C 0.001 -S 1 -output-debug-info"
		// --MultilayerPerceptron -->
		// abstractClassifier.setOptions(Utils.splitOptions("-L 0.1 -M 0.2 -N 10 -V 0 -S
		// 0 -E 20 -H 3"));

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
		for (int i = 0; i < partitions.size(); i++) {
			System.out.println("______INICIO______");
			List<Instance> instances = partitions.get(i);
			System.out.println((partitions.get(i)).size());
			System.out.println(i);
			// evaluation.crossValidateModel(fc, instances, 5, new Random(1));
			cross(instances, 10, filteredClassifier, evaluationActual, instancesModel);
			System.out.println("CORRETO: " + evaluationActual.correct());
			System.out.println("INCORRETO: " + evaluationActual.incorrect());
			System.out.println("NAO CLASSIFICADO: " + evaluationActual.unclassified());
			System.out.println("PORCENTAGEM CORRETA: " + evaluationActual.pctCorrect());
			System.out.println("PORCENTAGEM INCORRETA: " + evaluationActual.pctIncorrect());
			System.out.println("PORCENTAGEM NAO CLASSIFICADA: " + evaluationActual.pctUnclassified());
			System.out.println("______FIM______");
		}
		System.out.println("=================================");
		System.out.println(evaluationActual.toSummaryString());
		System.out.println("=================================");
		System.out.println(evaluationActual.toClassDetailsString());
		System.out.println("=================================");
		System.out.println(evaluationActual.toMatrixString("=== Confusion Matrix ==="));

		return evaluationActual;
	}

	@Override
	public Classifier build(Integer numFolds, List<Instance> instance, Integer sequence,
			FilteredClassifier filteredClassifier, Instances instanceToUse) throws Exception {
		Instances data = new Instances(instanceToUse, 0);
		data.addAll(instance);
		data.randomize(new Random(1));
		if (data.classAttribute().isNominal()) {
			data.stratify(numFolds);
		}
		Instances train = data.trainCV(numFolds, sequence, new Random(1));
		filteredClassifier.buildClassifier(train);
		Classifier classifier = AbstractClassifier.makeCopy(filteredClassifier);
		return classifier;

	}

	@Override
	public Instance createAnalysis(Instances instances, Object client, String classForPrediction) throws Exception {

		Instance newIstanceToAnalyze = new DenseInstance(instances.numAttributes());
		newIstanceToAnalyze.setDataset(instances);

		for (int i = 0; i < instances.numAttributes(); i++) {
			String name = newIstanceToAnalyze.attribute(i).name();
			Field field = client.getClass().getDeclaredField(name);
			Object value = field.get(client);
			Boolean flagFieldExists = name.equalsIgnoreCase(field.getName());
			if (Boolean.TRUE.equals(flagFieldExists)) {
				if (!classForPrediction.equalsIgnoreCase(name)) {
					if (newIstanceToAnalyze.attribute(i).isNominal()) {
						newIstanceToAnalyze.setValue(newIstanceToAnalyze.attribute(i), value.toString());
					} else if (newIstanceToAnalyze.attribute(i).isNumeric()) {
						newIstanceToAnalyze.setValue(newIstanceToAnalyze.attribute(i),
								Double.parseDouble(value.toString()));
					} else {
						newIstanceToAnalyze.setMissing(newIstanceToAnalyze.attribute(i));
					}
				} else {
					newIstanceToAnalyze.setMissing(newIstanceToAnalyze.attribute(i));
				}
			} else {
				newIstanceToAnalyze.setMissing(newIstanceToAnalyze.attribute(i));
			}
		}
		System.out.println(mapper.writeValueAsString(
				newIstanceToAnalyze.toString(newIstanceToAnalyze.attribute(instances.numAttributes() - 1))));
		return newIstanceToAnalyze;

	}

	@Override
	public String analyzeSet(Instance newIstanceToAnalyze, List<Classifier> populateClassifier) throws Exception {
		NumberFormat nf = new DecimalFormat("0.#");
		Vote ensembleClassifier = new Vote();
		StringBuilder result = new StringBuilder();
		ensembleClassifier.setClassifiers(populateClassifier.toArray(Classifier[]::new));
		double[] predicts = ensembleClassifier.distributionForInstance(newIstanceToAnalyze);
		for (double predict : predicts) {
			System.out.println("\n CLASSIFICADO:" + nf.format(predict));
			result.append("\n" + nf.format(predict));
		}
		return result.toString();
	}

}
