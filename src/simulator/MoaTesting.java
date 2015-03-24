package simulator;

import moa.classifiers.Classifier;
import moa.classifiers.functions.SGD;
import moa.classifiers.functions.SGDExport;
import moa.options.Options;
import moa.streams.ArffFileStream;
import moa.streams.generators.RandomRBFGenerator;
import moa.tasks.EvaluatePrequential;
import weka.core.Instance;

public class MoaTesting {


	static public void main(String args[]){

//		EvaluatePrequential eq=new EvaluatePrequential();
//		eq.prepareForUse();
//		System.out.println("lalA"+eq.getOptions().getAsCLIString());
//		eq.doTask();


		// defines the classifier
		SGDExport learner = new SGDExport();
		// Logistic Regression
		learner.lossFunctionOption.setChosenIndex(1);
		
		
		

		


		// defines the stream

		ArffFileStream stream2= new ArffFileStream("/Users/admin/moa-release-2014.04a/data/airlines.arff", 2 ); 

		//	ArffFileStream stream2= new ArffFileStream("/Users/admin/TwitterStreamExp/procSample.arff", 1 ); 



		//	

		//		EvaluatePrequential eq=new EvaluatePrequential();
		//		eq.prepareForUse();
		//		eq.doTask();



		//stream2.classIndexOption.setValue(1);
		stream2.prepareForUse();
		//stream.getHeader().setClassIndex(classIndex);

		learner.setModelContext(stream2.getHeader());
		learner.prepareForUse();

		
		int numberSamplesCorrect=0;
		int numberSamples=0;

		boolean isTesting=true;

		int numInstances=10;

		while(stream2.hasMoreInstances() && numberSamples < numInstances){
			Instance trainInst = stream2.nextInstance();
						
			if(isTesting){
				double[] pred=learner.getVotesForInstance(trainInst);

				for(int i=0;i<pred.length;i++){
					System.out.print(pred[i]+" ");

				}
				System.out.println(trainInst.classValue());

				if(learner.correctlyClassifies(trainInst)){
					numberSamplesCorrect++;
					System.out.println("CORRECT");
				}
				else{
					System.out.println("INCORRECT");					
				}

				//	learner.get

			}
			numberSamples++;
			learner.trainOnInstance(trainInst);
			

			
		}
		double accuracy = 100 * (double) numberSamplesCorrect / (double) numberSamples;

		System.out.println(numberSamples +" instances processed with " + accuracy + "%accuracy");


		//learner.
		System.out.println(learner.toString());
		
		
		System.out.println(learner.attHeader());
		System.out.println(learner.printHeader());
		System.out.println(learner.printLine());
		


	}




}

