package moa.streams.generators;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import simulator.LexiconEvaluator;
import simulator.MyUtils;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import moa.core.InstancesHeader;
import moa.core.ObjectRepository;
import moa.options.AbstractOptionHandler;
import moa.options.FileOption;
import moa.options.FloatOption;
import moa.options.IntOption;
import moa.streams.ArffFileStream;
import moa.streams.InstanceStream;
import moa.tasks.TaskMonitor;

public class SentimentDriftGenerator extends AbstractOptionHandler implements
InstanceStream {



	private static final long serialVersionUID = 1L;

	public FileOption arffFileOption = new FileOption("arffFile", 'f',
			"ARFF file to load.", null, "arff", false);


	public IntOption instanceRandomSeedOption = new IntOption(
			"instanceRandomSeed", 'i',
			"Seed for random generation of instances.", 1);
	
	
    public FloatOption driftProbOption = new FloatOption("driftProb",
            'd', "Probability of Drift.",
            0.001, 0.00, 1.00);

	protected InstancesHeader streamHeader;

	protected Random instanceRandom;

	protected ArffFileStream arffFileStream;
	//= new ArffFileStream("/Users/admin/workspace/SentimentSimulator/data/twitter.arff", 0);

	protected LexiconEvaluator le;


	public SentimentDriftGenerator() {
	}

	public SentimentDriftGenerator(String arffFileName){
		this.arffFileOption.setValue(arffFileName);

		//this.arffFileStream=new ArffFileStream(arffFileName,0);


		restart();
	}




	@Override
	public String getPurposeString() {
		return "Generates drifting sentiment labels for text data.";
	}


	@Override
	public void getDescription(StringBuilder sb, int indent) {
		// TODO Auto-generated method stub

	}

	@Override
	public InstancesHeader getHeader() {
		return this.streamHeader;

	}

	@Override
	public long estimatedRemainingInstances() {
		return this.arffFileStream.estimatedRemainingInstances();
	}

	@Override
	public boolean hasMoreInstances() {
		return this.arffFileStream.hasMoreInstances();
	}

	@Override
	protected void prepareForUseImpl(TaskMonitor monitor,
			ObjectRepository repository) {
		// generate header

		this.arffFileStream=new ArffFileStream(this.arffFileOption.getValue(),0);

		//this.arffFileStream.prepareForUse();
		
		this.le = new LexiconEvaluator("lexicons/AFINN-111.txt");
		try {
			le.processDict();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		InstancesHeader inputHeader=this.arffFileStream.getHeader();



		ArrayList<Attribute> attributes = new ArrayList<Attribute>();

		// Adds all attributes of the inputformat
		for (int i = 0; i < this.arffFileStream.getHeader().numAttributes(); i++) {
			attributes.add(inputHeader.attribute(i));
		}

		//	ArrayList<Attribute> attributes = new ArrayList<Attribute>();

		// The content of the message
		//	attributes.add(new Attribute("content", (ArrayList<String>) null));

		// The target label
		ArrayList<String> label = new ArrayList<String>();
		label.add("positive");
		label.add("neutral");
		label.add("negative");

		attributes.add(new Attribute("class", label));

		this.streamHeader = new InstancesHeader(new Instances(
				getCLICreationString(InstanceStream.class), attributes, 0));
		this.streamHeader.setClassIndex(this.streamHeader.numAttributes() - 1);



		restart();


	}

	@Override
	public Instance nextInstance() {

		Instance inputInst= this.arffFileStream.nextInstance();


		// construct instance
		InstancesHeader header = getHeader();

		double values[] = new double[header.numAttributes()];

		String content=inputInst.stringValue(0);

		values[0] = header.attribute(0).addStringValue(content);

		List<String> tokens=MyUtils.cleanTokenize(content);


		// the change
		double change=this.instanceRandom.nextDouble();
		if(change<this.driftProbOption.getValue()){
			System.out.println("CHANGE \n \n");
			this.le.createUniformNoise(this.instanceRandom, .6, -5, 5);
		}

		double score=le.evaluateStrengthLexicon(tokens);

		if(score>1){
			values[1] = header.attribute(1).indexOfValue("positive");			
		}
		else if(score < 1){
			values[1] = header.attribute(1).indexOfValue("negative");			
		}
		else{
			values[1] = header.attribute(1).indexOfValue("neutral");					
		}



		Instance inst = new DenseInstance(1,values);	
		inst.setDataset(header);

		return inst;
	}

	@Override
	public boolean isRestartable() {
		return true;
	}

	@Override
	public void restart() {
		this.instanceRandom = new Random(this.instanceRandomSeedOption.getValue());
		this.arffFileStream.restart();
	}



}
