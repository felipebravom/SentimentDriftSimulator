package simulator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


/**
 * 
 * @author fbravo Evaluates a lexicon from a csv file
 */
public class LexiconEvaluator {

	protected String path;
	protected Map<String, Double> dict;

	public LexiconEvaluator(String file) {
		this.dict = new HashMap<String, Double>();
		this.path = file;

	}

	public void processDict() throws IOException {
		// first, we open the file
		BufferedReader bf = new BufferedReader(new FileReader(this.path));
		String line;
		while ((line = bf.readLine()) != null) {
			String pair[] = line.split("\t");
			this.dict.put(pair[0], Double.parseDouble(pair[1]));

		}
		bf.close();

	}

	// returns the score associated to a word
	public double retrieveValue(String word) {
		if (!this.dict.containsKey(word)) {
			return 0.0;
		} else {
			return this.dict.get(word);
		}

	}

	public Map<String, Double> getDict() {
		return this.dict;
	}



	// computes scores from strength-oriented lexicons
	public double evaluateStrengthLexicon(List<String> tokens) {
		double score=0.0;
		for (String w : tokens) {
			score += this.retrieveValue(w);
		}

		return score;
	}
	
	
	static public int getRandom(Random r,int min, int max){
		int i1 = r.nextInt(max - min + 1) + min;
		return  i1;
	}

	public void createUniformNoise(Random r, double lambda,int min, int max){

		Map<String,Double> dict=this.getDict();
		for(String word:dict.keySet()){
			double actScore=dict.get(word);
			double newScore=lambda*getRandom(r,min,max)+(1-lambda)*actScore;
			dict.put(word, new Double(Math.round(newScore)));			
		}

	}
	
	
	
	
	
	

	static public void main(String args[]) throws IOException {
		
		

		LexiconEvaluator l2 = new LexiconEvaluator("lexicons/AFINN-111.txt");
		l2.processDict();
		System.out.println(l2.retrieveValue("wrong"));
		System.out.println(l2.retrieveValue("happy"));
		System.out.println(l2.retrieveValue("good"));
		
		Random r=new Random();
			
		
		for(int i=0;i<1000;i++){
			
			
			System.out.println("CHANGE\n");
			
			l2.createUniformNoise(r,0.6, -5, 5);
			System.out.println(l2.retrieveValue("wrong"));
			System.out.println(l2.retrieveValue("happy"));
			System.out.println(l2.retrieveValue("good"));
	
		}
				
		
		
//
//		LexiconEvaluator l5 = new LexiconEvaluator(
//				"lexicons/Sentiment140-Lexicon-v0.1/unigrams-pmilexicon.txt");
//		l5.processDict();
//		System.out.println(l5.retrieveValue("wath"));
//		System.out.println(l5.retrieveValue("hate"));
//		System.out.println(l5.retrieveValue("good"));
//
//		LexiconEvaluator l3 = new LexiconEvaluator(
//				"lexicons/NRC-Hashtag-Sentiment-Lexicon-v0.1/unigrams-pmilexicon.txt");
//		l3.processDict();
//		System.out.println(l3.retrieveValue("love"));
//		System.out.println(l3.retrieveValue("sad"));
//		System.out.println(l3.retrieveValue("sick"));
//
//		LexiconEvaluator l4 = new LexiconEvaluator("lexicons/BingLiu.csv");
//		l4.processDict();
//		System.out.println(l4.retrieveValue("love"));
//		System.out.println(l4.retrieveValue("hate"));
//		System.out.println(l4.retrieveValue("sick"));

	}
}
