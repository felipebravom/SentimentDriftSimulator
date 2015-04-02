package lexexpand;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cmu.arktweetnlp.Tagger;
import cmu.arktweetnlp.Tagger.TaggedToken;


/**
 * 
 * @author fbravo Evaluates a generic lexicon where the first column is the word and 
 * 	the other are features of the lexicon
 *  
 */
public class ExpandLexEvaluator {

	protected String path;
	protected Map<String, Map<String,String>> dict; // entry and a metadata map

	public ExpandLexEvaluator(String file) {
		this.dict = new HashMap<String, Map<String,String>>();
		this.path = file;

	}

	public void processDict() throws IOException {
		// first, we open the file
		BufferedReader bf = new BufferedReader(new FileReader(this.path));
		
		String firstLine=bf.readLine();
		String fieldNames[] = firstLine.split("\t");
		
		String line;
		while ((line = bf.readLine()) != null) {
			Map<String,String> entry=new HashMap<String,String>();
			
			String pair[] = line.split("\t");
			String word=pair[0];
			for(int i=1;i<pair.length;i++)
				entry.put(fieldNames[i],pair[i]);
				
			
			this.dict.put(word, entry);

		}
		bf.close();

	}

	// returns the score associated to a word
	public Map<String,String> retrieveValue(String word) {
		if (!this.dict.containsKey(word)) {
			return null;
		} else {
			return this.dict.get(word);
		}

	}

	public Map<String, Map<String,String>> getDict() {
		return this.dict;
	}
	
	
	// returns a positive and negative score for a tagged list of Strings
	public Map<String, Double> evaluatePolarity(List<String> tagTokens) {

		Map<String, Double> sentCount = new HashMap<String, Double>();

		double negScore = 0.0;
		double posScore = 0.0;
		
		double negCount = 0.0;
		double posCount = 0.0;

		for (String w : tagTokens) {
			if(this.dict.containsKey(w)){
				Map<String,String> pol = this.retrieveValue(w);
				if (pol.get("label").equals("positive")) {
					posScore += Double.parseDouble(pol.get("positive")) ;
					posCount++;
				} else if (pol.get("label").equals("negative")) {
					negScore += Double.parseDouble(pol.get("negative")) ;
					negCount++;
				}
				
			}
		
		}

		sentCount.put("posScore", posScore);
		sentCount.put("negScore", negScore);

		sentCount.put("posCount", posCount);
		sentCount.put("negCount", negCount);

		
		return sentCount;
	}
	


	
	
	
	

	static public void main(String args[]) throws IOException {
		
		
		
		ExpandLexEvaluator consLex=new ExpandLexEvaluator("lexGen.csv");
		consLex.processDict();
		
//		ExpandLexEvaluator edLex=new ExpandLexEvaluator("lexicons/edinVSMemot.csv");
//		edLex.processDict();
//		
//		ExpandLexEvaluator s140Lex=new ExpandLexEvaluator("lexicons/edinSampleVSMLex.csv");
//		s140Lex.processDict();
		
		
		
		String content="Can't say I like the cared omg by my new facebook layout yum ah . But just posted pics from my Super Bowl week. =)";
		Tagger tagger = new Tagger();
		tagger.loadModel("models/model.20120919");
		
		
		List<String> tagWords=new ArrayList<String>();

		List<TaggedToken> tagTokens=tagger.tokenizeAndTag(content.toLowerCase());
		for(TaggedToken tt:tagTokens){
			tagWords.add(tt.tag+"-"+tt.token);
		}
		
		Map<String,Double> scores=consLex.evaluatePolarity(tagWords);
		
		System.out.println(scores.get("posScore")+"  neg="+scores.get("posScore"));
		System.out.println(scores.get("posCount")+"  neg="+scores.get("posCount"));
		
//		Map<String,String> emos= consLex.retrieveValue("N-interview");
//
//		for(String word:emos.keySet()){
//			System.out.println(word+" "+emos.get(word));
//		}
//		
//		emos= edLex.retrieveValue("N-interview");
//
//		for(String word:emos.keySet()){
//			System.out.println(word+" "+emos.get(word));
//		}
//		
//		emos= s140Lex.retrieveValue("N-interview");
//
//		for(String word:emos.keySet()){
//			System.out.println(word+" "+emos.get(word));
//		}
//		
		
//		LexiconEvaluator l = new LexiconEvaluator("lexicons/opinion-finder.txt");
//		l.processDict();
//		System.out.println(l.retrieveValue("wrong"));
//		System.out.println(l.retrieveValue("happy"));
//		System.out.println(l.retrieveValue("good"));
//
//		LexiconEvaluator l2 = new LexiconEvaluator("lexicons/AFINN-111.txt");
//		l2.processDict();
//		System.out.println(l2.retrieveValue("wrong"));
//		System.out.println(l2.retrieveValue("happy"));
//		System.out.println(l2.retrieveValue("good"));
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
