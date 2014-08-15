package simulator;

import moa.classifiers.Classifier;
import moa.classifiers.functions.SGD;
import moa.streams.ArffFileStream;
import moa.streams.generators.RandomRBFGenerator;
import weka.core.Instance;

public class MoaTesting {
	

	static public void main(String args[]){
		// defines the classifier
		Classifier learner = new SGD();

		// defines the stream
		RandomRBFGenerator stream= new RandomRBFGenerator();
		ArffFileStream stream2= new ArffFileStream("/Users/admin/moa-release-2014.04a/data/airlines.arff", -1 ); 


		//		EvaluatePrequential eq=new EvaluatePrequential();
		//		eq.prepareForUse();
		//		eq.doTask();



		stream2.prepareForUse();
		//stream.getHeader().setClassIndex(classIndex);

		learner.setModelContext(stream2.getHeader());
		learner.prepareForUse();

		int numberSamplesCorrect=0;
		int numberSamples=0;

		boolean isTesting=true;

		int numInstances=10000;

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


	}




}

