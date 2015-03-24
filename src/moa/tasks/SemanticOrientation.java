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
import moa.options.FlagOption;
import moa.options.FloatOption;
import moa.options.IntOption;
import moa.options.MultiChoiceOption;
import moa.streams.InstanceStream;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.Instance;
import weka.core.Attribute;

public class SemanticOrientation extends MainTask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FlagOption resetCountsOption= new FlagOption("resetCount",'r',"resets counts");

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



	public FileOption arffFileOption = new FileOption("arffFile", 'a',
			"Destination arff file.", null, "arff", true);



	public SemanticOrientation() {
	}

	public SemanticOrientation(InstanceStream stream,
			int instanceLimit, int timeLimit) {
		this.streamOption.setCurrentObject(stream);
		this.instanceLimitOption.setValue(instanceLimit);

	}

	@Override
	public Class<?> getTaskResultType() {
		return Classifier.class;
	}

	@Override
	public Object doMainTask(TaskMonitor monitor, ObjectRepository repository) {


		InstanceStream stream = (InstanceStream) getPreparedClassOption(this.streamOption);


		// the classIndex
		int classIndex=stream.getHeader().classIndex();

		Attribute classAttribute=stream.getHeader().classAttribute();

		int numClasses=stream.getHeader().classAttribute().numValues();



		int maxInstances = this.instanceLimitOption.getValue();
		long instancesProcessed = 0;


		File arffFile = this.arffFileOption.getFile();

		int streamNumAttributes=stream.getHeader().numAttributes();

		// copy the names of the attributes
		ArrayList<Attribute> atts=new ArrayList<Attribute>();
		for(int i=0;i<streamNumAttributes;i++){
			if(stream.getHeader().classIndex()!=i){
				atts.add(new Attribute(stream.getHeader().attribute(i).name()));	
			}

		}


		InstancesHeader ih=new InstancesHeader(new Instances("Semantic Orientation",atts,0)); 


		try {

			Writer warff = new BufferedWriter(new FileWriter(arffFile));



			warff.write(ih.toString());
			warff.write("\n");



			// Starts with one assuming a Laplace correction
			double posCount=1.0;
			double negCount=1.0;



			double[] wordPosCount=new double[streamNumAttributes-1];	
			double[] wordNegCount=new double[streamNumAttributes-1];

			// Initialize both arrays with values equal to one for Laplace correction
			for(int i=0;i<wordPosCount.length;i++){
				wordPosCount[i]=1.0;
				wordNegCount[i]=1.0;
			}




			while (stream.hasMoreInstances() && ((maxInstances < 0) || (instancesProcessed < maxInstances))) {

				Instance insta=stream.nextInstance();

				Double classValue=insta.value(classIndex);


				if(classValue==0){					
					negCount++;

					for(int i=0,j=0; i<streamNumAttributes && j< streamNumAttributes-1; ){					
						if(i==classIndex)
							i++;
						else{
							wordNegCount[j] += insta.value(i); 
						}						
						i++;
						j++;					

					}				

				}

				else{
					posCount++;

					for(int i=0,j=0; i<streamNumAttributes && j< streamNumAttributes-1; ){					
						if(i==classIndex)
							i++;
						else{
							wordPosCount[j] += insta.value(i); 
						}						
						i++;
						j++;					

					}			



				}





				System.out.println(numClasses);

				//insta.value(classIndex)
				//insta.classAttribute().


				if (instancesProcessed % this.sampleFrequencyOption.getValue() == 0 
						|| stream.hasMoreInstances() == false){

					double[] semanticOrientation=new double[streamNumAttributes-1];
					for(int i=0;i<semanticOrientation.length;i++){
						double posProb=wordPosCount[i]/posCount;
						double negProb=wordNegCount[i]/negCount;
						semanticOrientation[i]=logOfBase(2,posProb)-logOfBase(2,negProb);
											
					}



					Instance inst=new DenseInstance(1,semanticOrientation);




					warff.write(inst.toString());
					warff.write("\n");


					if(this.resetCountsOption.isSet()){
						// resets the window
						posCount=1.0;
						negCount=1.0;
						for(int i=0;i<wordPosCount.length;i++){
							wordPosCount[i]=1.0;
							wordNegCount[i]=1.0;
						}
						
					}
	



				}

				instancesProcessed++;

			}


			warff.close();




			return null;


		} catch (Exception ex) {
			throw new RuntimeException(
					"Failed writing to file " + arffFile, ex);
		}

	}


	public double logOfBase(int base, double num) {
		return Math.log(num) / Math.log(base);
	}


}
