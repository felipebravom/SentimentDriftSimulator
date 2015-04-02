package moa.tasks;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import lexexpand.LexiconEvaluator;
import cmu.arktweetnlp.Twokenize;
import moa.classifiers.Classifier;
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
import weka.core.SparseInstance;
import weka.core.Utils;

public class VSMCreator extends MainTask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FlagOption resetCountsOption= new FlagOption("resetCount",'r',"resets counts");

	public ClassOption streamOption = new ClassOption("stream", 's',
			"Stream to learn from.", InstanceStream.class,
			"ArffFileStream");

	public IntOption textAttributeIndexOption = new IntOption("textAttribute", 'j',
			"Text attribute in the stream.",
			1, 0, Integer.MAX_VALUE);



	public IntOption instanceLimitOption = new IntOption("instanceLimit", 'i',
			"Maximum number of instances to test/train on  (-1 = no limit).",
			100000000, -1, Integer.MAX_VALUE);


	public IntOption sampleFrequencyOption = new IntOption("sampleFrequency",
			'k',
			"How many instances between samples of the learning performance.",
			100000, 0, Integer.MAX_VALUE);

	public IntOption mindDocsOption = new IntOption("minDocs",
			'm',
			"Minimum number of documents of a word to be included as an attribute.",
			0, 0, Integer.MAX_VALUE);
	
	
	public IntOption attMaxOption = new IntOption("attMax",
			'n',
			"Maximum number of attributes.",
			100000, 0, Integer.MAX_VALUE);
	
	

	public FileOption lexFileOption = new FileOption("lexFile", 'a',
			"Input lexicon file.", "lexicons/metaLexEmo.csv", "csv", false);


	/** the vocabulary and the WordRep */
	protected Object2ObjectMap<String, WordRep> wordInfo; 

	/** Contains a mapping of valid words to attribute indexes. */
	private Object2IntMap<String> m_Dictionary;




	// This class contains all the information of the word to compute the centroid
	class WordRep{
		String word; // the word
		double polarity; // the polarity
		int numDoc; // number of documents where the word occurs
		Object2IntMap<String> wordSpace; // the vector space model of the document



		public WordRep(String word){
			this.word=word;
			this.numDoc=0;
			this.wordSpace=new Object2IntOpenHashMap<String>();
		}

		public void addDoc(Object2IntMap<String> docVector){
			this.numDoc++;
			for(String vecWord:docVector.keySet()){
				int vecWordFreq=docVector.getInt(vecWord);
				// if the word was seen before we add the current frequency
				this.wordSpace.put(vecWord,vecWordFreq+this.wordSpace.getInt(vecWord));
			}	

		}
		
		
		public double getPolarity(){
			return this.polarity;
		}
		
		public void setPolarity(double polarity){
			this.polarity=polarity;
		}

	}



	public VSMCreator() {
	}

	public VSMCreator(InstanceStream stream,
			int instanceLimit, int timeLimit) {
		this.streamOption.setCurrentObject(stream);
		this.instanceLimitOption.setValue(instanceLimit);

	}

	@Override
	public Class<?> getTaskResultType() {
		return Classifier.class;
	}



	public Object2IntMap<String> calculateTermFreq(List<String> tokens) {
		Object2IntMap<String> termFreq = new Object2IntOpenHashMap<String>();

		// Traverse the strings and increments the counter when the token was
		// already seen before
		for (String token : tokens) {
			termFreq.put(token, termFreq.getInt(token) + 1);			
		}

		return termFreq;
	}

	
	
	// creates a SparseInstance from a WordRep Object
	public Instance getInstFromWord(WordRep wordRep, int size){
		double[] values = new double[size];
		
		for(String innerWord:wordRep.wordSpace.keySet()){
			// only include frequent attributes
			WordRep innerWordRep=this.wordInfo.get(innerWord);
			if(innerWordRep!=null){
				if(innerWordRep.numDoc>this.mindDocsOption.getValue()){
					int attIndex=this.m_Dictionary.getInt(innerWord);
					// we normalise the value by the number of documents
					values[attIndex]=((double)wordRep.wordSpace.getInt(innerWord))/wordRep.numDoc;					
				}
				
			}
			
			
		
		}
		
		// I assign the polarity		
		values[size-1]=wordRep.getPolarity()>=0?wordRep.getPolarity():Utils.missingValue();
		
		
		return (new SparseInstance(1, values));		
		
	}
	

	@Override
	public Object doMainTask(TaskMonitor monitor, ObjectRepository repository) {


		InstanceStream stream = (InstanceStream) getPreparedClassOption(this.streamOption);
		int maxInstances = this.instanceLimitOption.getValue();
		long instancesProcessed = 0;
		int indexCounter=0; // counter of attributes indexes of new words
		this.wordInfo = new Object2ObjectOpenHashMap<String, WordRep>();


		// the dictionary of words and attribute indexes
		this.m_Dictionary=new Object2IntOpenHashMap<String>();
		
		LexiconEvaluator seedLex = new LexiconEvaluator(lexFileOption.getFile());
		seedLex.processDict();

		

		ArrayList<Attribute> att = new ArrayList<Attribute>();
		for(int i=0;i<attMaxOption.getValue();i++){
			Attribute a = new Attribute("att"+i);
			att.add(a);		
		}
		
		// The target label
		ArrayList<String> label = new ArrayList<String>();

		label.add("negative");
		label.add("neutral");
		label.add("positive");
		
		att.add(new Attribute("Class", label));


		InstancesHeader wordStream = new InstancesHeader(new Instances("la", att, 0));
		
		
		
		
		while (stream.hasMoreInstances() && ((maxInstances < 0) || (instancesProcessed < maxInstances))) {

			Instance inst=stream.nextInstance();
			String content = inst.stringValue(textAttributeIndexOption.getValue());

			content=content.toLowerCase();

			// tokenises the content 
			List<String> tokens=Twokenize.tokenizeRawTweetText(content); 

			// calculates the wordVector
			Object2IntMap<String> wordFreqs = this.calculateTermFreq(tokens);



			// if the word is new we add it to the vocabulary, otherwise we
			// add the document to the vector
			for (String word : wordFreqs.keySet()) {
				WordRep wordRep;

				if (this.wordInfo.containsKey(word)) {
					wordRep=this.wordInfo.get(word);
					wordRep.addDoc(wordFreqs); // add the document

				} else {
					wordRep=new WordRep(word);
					wordRep.addDoc(wordFreqs); // add the document
					this.wordInfo.put(word, wordRep);	
					
					// add the new word to the dictionary of attributes
					this.m_Dictionary.put(word, indexCounter);					
					indexCounter++; // increment the counter
					
					String wordPol=seedLex.retrieveValue(word);
					if(wordPol.equals("negative"))
						wordRep.setPolarity(0);
					else if(wordPol.equals("neutral"))
						wordRep.setPolarity(1);
					else if(wordPol.equals("positive"))
						wordRep.setPolarity(2);
					else
						wordRep.setPolarity(-1);							
					
				}
				
				// if the 
				if(wordRep.getPolarity()>-1 && wordRep.numDoc > this.mindDocsOption.getValue()){
					Instance inst2=this.getInstFromWord(wordRep, wordStream.numAttributes());
					System.out.println(wordRep.word+":"+wordRep.getPolarity()+":"+inst2);
					
				}
				


			}
			
			instancesProcessed++;

		}
		
		
		

		return null;


	}


	public double logOfBase(int base, double num) {
		return Math.log(num) / Math.log(base);
	}


}
