package com.uninter.tcc.service;

import java.util.List;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instance;
import weka.core.Instances;

public interface MachineLearning {

	Instances instances(String jsonStringObject, String className,String classIndex,boolean onlyModel,String id) throws Exception;

	FilteredClassifier filter(String classMl, String optionsClassifier, List<String> optionsFilter) throws Exception;

	List<List<Instance>> partitions(Integer partitionSize, Instances instances) throws Exception;

	Evaluation train(List<List<Instance>> partitions,FilteredClassifier filteredClassifier, Instances instancesModel) throws Exception;

	Classifier build(Integer numFolds, List<Instance> instances, Integer sequence, FilteredClassifier filteredClassifier, Instances instanceToUse)throws Exception;

	Instance createAnalysis(Instances instances,Object client,String classForPrediction) throws Exception;
	
	String analyzeSet(Instance newIstanceToAnalyze,List<Classifier> populateClassifier) throws Exception;

    String requestTrain(String idContext, Integer partitionSize, String classnameClassifier, String optClassifier, List<String> optFilter, String classIndex) throws Exception;
}