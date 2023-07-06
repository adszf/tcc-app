package com.uninter.tcc.service;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.internal.build.AllowSysOut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uninter.tcc.domain.entity.CreditScoreEntity;
import com.uninter.tcc.domain.entity.CreditScoreFinalEntity;
import com.uninter.tcc.domain.entity.CustomCreditScoreEntity;
import com.uninter.tcc.repository.CreditScoreFinalRepository;
import com.uninter.tcc.repository.CreditScoreRepository;
import com.uninter.tcc.repository.CustomCreditScoreRepository;
import com.uninter.tcc.utils.MongoWekaIntegration;
import com.uninter.tcc.utils.RacedIncrementalLogitBoost;
import com.uninter.tcc.utils.Utilities;
import org.apache.commons.collections4.ListUtils;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.evaluation.output.prediction.PlainText;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SGD;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.SMOreg;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.misc.SerializedClassifier;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.trees.RandomTree;
import weka.classifiers.trees.j48.ClassifierSplitModel;
import weka.classifiers.trees.j48.ModelSelection;
import weka.classifiers.trees.j48.PruneableClassifierTree;
import weka.core.Attribute;
import weka.core.Debug;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.ArffLoader;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.Filter;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
/* import weka.gui.visualize.PlotData2D;
import weka.gui.visualize.ThresholdVisualizePanel;
import weka.gui.visualize.VisualizePanel; */

@Service
public class CreditAnalysisImpl implements CreditAnalysis {

	@Autowired
	private CreditScoreRepository creditScoreRepository;

	@Autowired
	private CreditScoreFinalRepository creditScoreFinalRepository;

	@Autowired
	private CustomCreditScoreRepository customCreditScoreRepository;

	@Autowired
	private MongoTemplate mongoTemplate;

	private final Utilities custom = new Utilities();

	private final MongoWekaIntegration integration = new MongoWekaIntegration();

	private final ObjectMapper mapper = new ObjectMapper();

	private Classifier classifier;
	private Evaluation evaluation;
	private FilteredClassifier filteredClassifier;
	private AbstractClassifier abstractClassifier;

	@Override
	public void creditScoreAnalysis(Long cpf) throws Exception {
		// integration.processarDados(1000, 0,
		// customCreditScoreRepository,mongoTemplate);

//		  CustomCreditScoreEntity client =
//		  customCreditScoreRepository.findByFakeIdCpf(cpf);

//		  List<CustomCreditScoreEntity> clientsWithParams =
//		  customCreditScoreRepository.findByRegiao(client.regiao);

		try {
//			 List<CustomCreditScoreEntity> clientsWithParams =
//			 customCreditScoreRepository.findAll();
//			 CustomCreditScoreEntity entity = new CustomCreditScoreEntity();
//			List<CreditScoreEntity> clientsWithParams = creditScoreRepository.findAll();
			List<CreditScoreFinalEntity> creditFinalEntity = creditScoreFinalRepository.findAll();
			CreditScoreFinalEntity entity = new CreditScoreFinalEntity();
//			CreditScoreEntity entity = new CreditScoreEntity();
			String jsonStringObject = mapper.writeValueAsString(creditFinalEntity);
			String className = entity.getClass().getSimpleName();

//			 classifierJ48(jsonStringObject, className);
			classifierGeneric(jsonStringObject, className);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return;
		}
		System.out.println("SUCESSO!");
	}

	private void classifierJ48(String jsonStringObject, String className) {
		// Try block to check for exceptions
		try {
			/* Instâncias - dados */
			DataSource loader = new DataSource(custom.csvToArff(jsonStringObject, className));
			Instances train = loader.getDataSet();
			train.setClassIndex(train.attribute("tipoEmprestimo").index());
			Instance test = new DenseInstance(train.numAttributes());

			test.setDataset(train);
			test.setValue(train.attribute(1), Double.parseDouble("1234567891"));
			test.setValue(train.attribute(3), Double.parseDouble("25"));
			test.setValue(train.attribute(4), "Casado");
			test.setValue(train.attribute(5), "bossoroca");
			test.setValue(train.attribute(6), "RS");
			test.setValue(train.attribute(7), Double.parseDouble("5"));
			test.setValue(train.attribute(8), Double.parseDouble("5"));
			test.setValue(train.attribute(9), Double.parseDouble("1"));
			test.setValue(train.attribute(11), Double.parseDouble("1234"));
			test.setValue(train.attribute(14), Double.parseDouble("0"));
			test.setValue(train.attribute(15), Double.parseDouble("1"));
			test.setMissing(train.attribute(0));
			test.setMissing(train.attribute(2));
			test.setMissing(train.attribute(10));
			test.setMissing(train.attribute(12));
			List<Instances> lista = new ArrayList<>();

			int aux = 0;
			int atualLista = 0;
			Instances instanceAux = null;
			for (Iterator<Instance> iterator = train.iterator(); iterator.hasNext();) {
				Instance instance = (Instance) iterator.next();
				if (aux == 0) {
					instanceAux = (Instances) new Instances(train, 0);
					lista.add(atualLista, instanceAux);
				} else {
					instanceAux = (Instances) instanceAux;
				}
				if (instanceAux.size() == 1000) {
					lista.add(atualLista, instanceAux);
					aux = 0;
					atualLista++;
				} else {
					instanceAux.add(instance);
					aux++;
				}
			}
			System.out.println(lista.size());
			/* Filtro - para pré-processamento dos dados */
			Remove remove = new Remove();
			remove.setAttributeIndices("1,2,3,6,11");
			remove.setInvertSelection(false);
			J48 j48Classifier = new J48();
			FilteredClassifier fc = new FilteredClassifier();
			fc.setFilter(remove);
			fc.setClassifier(j48Classifier);
			System.out.println(fc);
			/* Evaluation evaluation = new Evaluation(train); */
			System.out.println(fc);
			System.out.println("=================================");
			/* System.out.println(j48Classifier.toSummaryString()); */
			System.out.println("=================================");
			System.out.println(evaluation.toSummaryString());
			System.out.println(evaluation.toClassDetailsString());
			System.out.println("=================================");
			System.out.println(evaluation.toCumulativeMarginDistributionString());
			System.out.println("=================================");
			System.out.println(evaluation.toMatrixString("=== Confusion Matrix ==="));
			// Catch block to handle the exceptions
		} catch (Exception e) {
			// Print message on the console
			System.out.println("Error Occurred!!!! \n" + e.getMessage());
		}
	}

	private void classifierGeneric(String jsonStringObject, String className) throws Exception {

		/* Instâncias - dados */
		/**/
		DataSource loader = new DataSource(custom.csvToArff(jsonStringObject, className));
		ArffLoader loaderArff = new ArffLoader();
		Instances train = loader.getDataSet();
		train.setClassIndex(train.attribute("creditScore").index());
		/**/

		/* Filtro - para pré-processamento dos dados */
		/**/
		abstractClassifier = new Logistic();
		// Remove remove = new Remove();
		// remove.setAttributeIndices("1,2,3");
		// remove.setInvertSelection(false);
		// abstractClassifier.setOptions(Utils.splitOptions("-F 1 -L 0.01 -R 1.0E-4 -E
		// 100 -C 0.001 -S 1 -output-debug-info"));
		// --MultilayerPerceptron -->
		// abstractClassifier.setOptions(Utils.splitOptions("-L 0.1 -M 0.2 -N 10 -V 0 -S
		// 0 -E 20 -H 3"));
		filteredClassifier = new FilteredClassifier();
		// filteredClassifier.setFilter(remove);
		filteredClassifier.setClassifier(abstractClassifier);
		/**/

		/* TEST ! */
		/**/
		/*
		 * RacedIncrementalLogitBoost incremental = new RacedIncrementalLogitBoost();
		 * incremental.setClassifier(filteredClassifier); String[] test = {};
		 * //incremental.main(Utils.
		 * splitOptions("-t C:\\Users\\adson\\Desktop\\output\\" + className +".arff -l
		 * C:\\Users\\adson\\Desktop\\output\\" + className +".arff"));
		 * incremental.buildClassifier(train); Instance current; for (Iterator<Instance>
		 * iterator = train.iterator(); iterator.hasNext();) {
		 * System.out.println("START"); Instance instance = (Instance) iterator.next();
		 * incremental.updateClassifier(instance); System.out.println("END"); }
		 */
		// System.out.println(incremental);
		/**/

		/* Divide Instancias */
		/**/

//		List<Instances> listaTest = new ArrayList<>();
		List<Instance> collection = train.stream().collect(Collectors.toList());
		int partitionSize = 10000;
		List<List<Instance>> partitions = ListUtils.partition(collection, partitionSize);
//		List<List<Instance>> partitions = partition(collection, partitionSize);
	    System.out.println(partitions);
	    

//		List<Instances> lista = new ArrayList<>();
//		int aux = 0;
//		int atualLista = 0;
//		Instances instanceAux = null;
//		int countInstancesCreated = 0;
//		for (int i = 0; i < train.size(); i++) {
//			Instance instance = train.get(i);
//			if (aux == 0) {
//				instanceAux = (Instances) new Instances(train, 0); // lista.add(atualLista, instanceAux);
//				System.out.println(countInstancesCreated++);
//			} else {
//				instanceAux = (Instances) instanceAux;
//			}
//			if (instanceAux.size() == 5000 || i == (train.size() - 1)) {
//				if (i == (train.size() - 1)) {
//					instanceAux.add(instance);
//				}
//				lista.add(atualLista, instanceAux);
//				aux = 0;
//				atualLista++;
//			} else {
//				instanceAux.add(instance);
//				aux++;
//			}
//		}

		// for (Instances instance : lista) {
		// System.out.println(instance.size());
		// }

		/**/

		/* Treina e faz predições */
		/**/
		// Instances merged = null;
		// evaluation = new Evaluation(train);
		for (int i = 0; i < partitions.size(); i++) {
			System.out.println("______INICIO______");
			List<Instance> instances = partitions.get(i);
			System.out.println((partitions.get(i)).size());
			System.out.println(i);
			// evaluation.crossValidateModel(fc, instances, 5, new Random(1));
			// --
//			instances.trainCV(lista.size(), i, new Random(1));
//			classifier = AbstractClassifier.makeCopy(abstractClassifier);
//			classifier.buildClassifier(instances);
//			Instances test = instances.testCV(lista.size(), i);
//			evaluation.evaluateModel(classifier, test);
			cross(instances, 10);
			// --
			// System.out.println("CORRETO: " + evaluation.correct());
			// System.out.println("INCORRETO: " + evaluation.incorrect());
			// System.out.println("NAO CLASSIFICADO: " + evaluation.unclassified());
			// System.out.println("PORCENTAGEM CORRETA: " + evaluation.pctCorrect());
			// System.out.println("PORCENTAGEM INCORRETA: " + evaluation.pctIncorrect());
			// System.out.println("PORCENTAGEM NAO CLASSIFICADA: " +
			// evaluation.pctUnclassified());
			System.out.println("______FIM______");
		}
		// System.out.println(copiedClassifier);
		// System.out.println("=================================");
		// System.out.println(evaluation.toSummaryString());
		// System.out.println("=================================");
		// System.out.println(evaluation.toClassDetailsString());
		// System.out.println("=================================");
		// System.out.println(evaluation.toMatrixString("=== Confusion Matrix ==="));
		/**/

		/* TEST PARA CLASSIFICAR UMA NOVA INSTANCIA DEPOIS DE TREINADA */
		/**/
		Instance test = new DenseInstance(train.numAttributes());
		test.setDataset(train);
		test.setMissing(train.attribute(0));
		test.setValue(train.attribute(1), Double.parseDouble("4"));
		test.setValue(train.attribute(2), "Solteiro");
		test.setValue(train.attribute(3), Double.parseDouble("3"));
		test.setValue(train.attribute(4), Double.parseDouble("2"));
		test.setValue(train.attribute(5), Double.parseDouble("3"));
		test.setValue(train.attribute(6), "humanas");
		test.setValue(train.attribute(7), Double.parseDouble("2000"));
		test.setValue(train.attribute(8), "N");
		test.setValue(train.attribute(9), Double.parseDouble("2"));
		test.setValue(train.attribute(10), Double.parseDouble("1"));
		test.setValue(train.attribute(11), "pessoal");
		test.setValue(train.attribute(12), "S");
		test.setValue(train.attribute(13), Double.parseDouble("1"));
		test.setValue(train.attribute(14), Double.parseDouble("4"));
		test.setValue(train.attribute(15), Double.parseDouble("2"));
		test.setValue(train.attribute(16), Double.parseDouble("3"));
		test.setMissing(train.attribute(17));
		// 4 Solteiro PR 3 1 3 saude 18533 N 3 1 N especial 1 2 1 3 REGULAR 0,8
		// 1 Casado PR 1 1 3 engenharias 8710 S 3 2 S automotivo 3 2 1 2 BOM 1,2
		// 4 Solteiro PR 2 2 3 humanas 9765 N 2 1 S pessoal 1 4 2 3 RUIM 0,5
		NumberFormat nf = new DecimalFormat("0.#");

		double[] predicts = classifier.distributionForInstance(test);
		for (double predict : predicts) {
			System.out.println(nf.format(predict));
		}
		/**/

	}

	public static Instances merge(Instances data1, Instances data2) throws Exception {
		// Check where are the string attributes
		int asize = data1.numAttributes();
		boolean strings_pos[] = new boolean[asize];
		for (int i = 0; i < asize; i++) {
			Attribute att = data1.attribute(i);
			strings_pos[i] = ((att.type() == Attribute.STRING) || (att.type() == Attribute.NOMINAL));
		}

		// Create a new dataset
		Instances dest = new Instances(data1);
		dest.setRelationName(data1.relationName() + "+" + data2.relationName());

		DataSource source = new DataSource(data2);
		Instances instances = source.getStructure();
		Instance instance = null;
		while (source.hasMoreElements(instances)) {
			instance = source.nextElement(instances);
			dest.add(instance);

			// Copy string attributes
			for (int i = 0; i < asize; i++) {
				if (strings_pos[i]) {
					dest.instance(dest.numInstances() - 1).setValue(i, instance.stringValue(i));
				}
			}
		}

		return dest;
	}

	public void cross(List<Instance> instances, int numFolds) throws Exception {
		// Make a copy of the data we can reorder
		Instances data =  new Instances("", new ArrayList<>(), 0);
		data=(Instances) instances.get(0);
		data.randomize(new Random(1));
		if (data.classAttribute().isNominal()) {
			data.stratify(numFolds);
		}
		for (int i = 0; i < numFolds; i++) {
			Instances train = data.trainCV(numFolds, i, new Random(1));
			// setPriors(train);
			filteredClassifier.buildClassifier(train);
			classifier = AbstractClassifier.makeCopy(filteredClassifier);
			// classifier.buildClassifier(train);
			/*
			 * TEST CLASSIFIER AND EXECUTE EVALUATION Instances test =
			 * data.testCV(numFolds,i); evaluation.evaluateModel(classifier, test);
			 */

		}

	}

	private static <T> List<List<T>> partition(List<T> collection, int partitionSize) {
		return IntStream.rangeClosed(0, (collection.size() - 1) / partitionSize).mapToObj(
				i -> collection.subList(i * partitionSize, Math.min((i + 1) * partitionSize, collection.size())))
				.collect(Collectors.toList());
	}
}
