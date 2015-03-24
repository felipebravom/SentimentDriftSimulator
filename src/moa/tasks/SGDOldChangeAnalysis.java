package moa.tasks;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;

import moa.classifiers.Classifier;
import moa.classifiers.functions.SGDOld;
import moa.core.InstancesHeader;
import moa.core.ObjectRepository;
import moa.options.ClassOption;
import moa.options.FileOption;
import moa.options.FloatOption;
import moa.options.IntOption;
import moa.options.MultiChoiceOption;
import moa.streams.InstanceStream;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.Instance;
import weka.core.Attribute;

public class SGDOldChangeAnalysis extends MainTask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected SGDOld learner;

	public ClassOption streamOption = new ClassOption("stream", 's',
			"Stream to learn from.", InstanceStream.class,
			"generators.RandomTreeGenerator");

	public IntOption instanceLimitOption = new IntOption("instanceLimit", 'i',
			"Maximum number of instances to test/train on  (-1 = no limit).",
			100000000, -1, Integer.MAX_VALUE);


	public IntOption sampleFrequencyOption = new IntOption("sampleFrequency",
			'k',
			"How many instances between samples of the learning performance.",
			100000, 0, Integer.MAX_VALUE);


	public FloatOption lambdaRegularizationOption = new FloatOption("lambdaRegularization",
			'l', "Lambda regularization parameter .",
			0.0001, 0.00, Integer.MAX_VALUE);

	public MultiChoiceOption lossFunctionOption = new MultiChoiceOption(
			"lossFunction", 'o', "The loss function to use.", new String[]{
					"HINGE", "LOGLOSS", "SQUAREDLOSS"}, new String[]{
					"Hinge loss (SVM)",
					"Log loss (logistic regression)",
			"Squared loss (regression)"}, 0);


	public FloatOption learningRateOption = new FloatOption("learningRate",
			'r', "Learning rate parameter.",
			0.1, 0.00, Integer.MAX_VALUE);


	public FileOption arffFileOption = new FileOption("arffFile", 'a',
			"Destination arff file.", null, "arff", true);
	
	public FileOption outputFileOption = new FileOption("outputFile", 'f',
			"Destination arff file.", null, "csv", true);

	public SGDOldChangeAnalysis() {
	}

	public SGDOldChangeAnalysis(SGDOld learner, InstanceStream stream,
			int instanceLimit, int timeLimit) {
		this.learner=learner;
		this.streamOption.setCurrentObject(stream);
		this.instanceLimitOption.setValue(instanceLimit);

	}

	@Override
	public Class<?> getTaskResultType() {
		return Classifier.class;
	}

	@Override
	public Object doMainTask(TaskMonitor monitor, ObjectRepository repository) {
		this.learner = new SGDOld();
		// Logistic Regression
		learner.lossFunctionOption.setChosenIndex(this.lossFunctionOption.getChosenIndex());
		learner.lambdaRegularizationOption.setValue(this.lambdaRegularizationOption.getValue());
		learner.learningRateOption.setValue(this.learningRateOption.getValue());

		InstanceStream stream = (InstanceStream) getPreparedClassOption(this.streamOption);
		learner.setModelContext(stream.getHeader());
		learner.prepareForUse();


		int maxInstances = this.instanceLimitOption.getValue();
		long instancesProcessed = 0;


		File destFile = this.outputFileOption.getFile();
		File arffFile = this.arffFileOption.getFile();

		// copy the names of the attributes
		ArrayList<Attribute> atts=new ArrayList<Attribute>();
		for(int i=0;i<stream.getHeader().numAttributes();i++){
			if(stream.getHeader().classIndex()!=i){
				atts.add(new Attribute(stream.getHeader().attribute(i).name()));	
			}

		}
		atts.add(new Attribute("SGD-bias"));

		InstancesHeader ih=new InstancesHeader(new Instances("SGD",atts,0)); 


		try {
		
			Writer wcsv = new BufferedWriter(new FileWriter(destFile));
			Writer warff = new BufferedWriter(new FileWriter(arffFile));

			
			wcsv.write(learner.csvHeader());
			
			warff.write(ih.toString());
			warff.write("\n");



			while (stream.hasMoreInstances() && ((maxInstances < 0) || (instancesProcessed < maxInstances))) {

				learner.trainOnInstance(stream.nextInstance());

				if (instancesProcessed % this.sampleFrequencyOption.getValue() == 0 
						|| stream.hasMoreInstances() == false){
				

					wcsv.write(learner.csvLine());
					
					Instance inst=new DenseInstance(1,learner.getWeights());
					System.out.println("Instances header att "+ih.numAttributes());
					System.out.println("Instance att "+inst.numAttributes());			

					warff.write(inst.toString());
					warff.write("\n");

					//inst.

				}

				instancesProcessed++;

			}


			warff.close();
			wcsv.close();
			learner.setModelContext(stream.getHeader());			

		
			
			return learner;


		} catch (Exception ex) {
			throw new RuntimeException(
					"Failed writing to file " + arffFile, ex);
		}

	}

}
