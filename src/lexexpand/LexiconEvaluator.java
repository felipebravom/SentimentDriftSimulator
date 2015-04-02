package lexexpand;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 
 * @author fbravo Evaluates a lexicon from a csv file
 */
public class LexiconEvaluator {

	protected File file;
	protected Map<String, String> dict;

	public LexiconEvaluator(File file) {
		this.dict = new HashMap<String, String>();
		this.file = file;

	}

	public void processDict()  {
		// first, we open the file
		
		try {
			BufferedReader bf;
			bf = new BufferedReader(new FileReader(this.file));
			String line;
			while ((line = bf.readLine()) != null) {
				String pair[] = line.split("\t");
				this.dict.put(pair[0], pair[1]);

			}
			bf.close();		
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	// returns the score associated to a word
	public String retrieveValue(String word) {
		if (!this.dict.containsKey(word)) {
			return "not_found";
		} else {
			return this.dict.get(word);
		}

	}

	public Map<String, String> getDict() {
		return this.dict;
	}

	// counts positive and negative words from a polarity-oriented lexicon
	public Map<String, Integer> evaluatePolarityLexicon(List<String> tokens) {

		Map<String, Integer> sentCount = new HashMap<String, Integer>();

		int negCount = 0;
		int posCount = 0;

		for (String w : tokens) {
			String pol = this.retrieveValue(w);
			if (pol.equals("positive")) {
				posCount++;
			} else if (pol.equals("negative")) {
				negCount++;
			}
		}

		sentCount.put("posCount", posCount);
		sentCount.put("negCount", negCount);

		return sentCount;
	}

	// computes scores from strength-oriented lexicons
	public Map<String, Double> evaluateStrengthLexicon(List<String> tokens) {

		Map<String, Double> strengthScores = new HashMap<String, Double>();
		double posScore = 0;
		double negScore = 0;
		for (String w : tokens) {
			String pol = this.retrieveValue(w);
			if (!pol.equals("not_found")) {
				double value = Double.parseDouble(pol);
				if (value > 0) {
					posScore += value;
				} else {
					negScore += value;
				}
			}
		}
		strengthScores.put("posScore", posScore);
		strengthScores.put("negScore", negScore);

		return strengthScores;
	}
	
	
	
	// counts positive, neutral and negative emoticons from an emoticon-oriented lexicon
	// positive emoticons are markes as 1, neutral as 0, and negative as -1
	public Map<String, Integer> evaluateEmoticonLexicon(List<String> tokens) {

		Map<String, Integer> sentCount = new HashMap<String, Integer>();

		int negCount = 0;
		int neuCount = 0;
		int posCount = 0;

		for (String w : tokens) {
			String pol = this.retrieveValue(w);
			if (pol.equals("1")) {
				posCount++;
			} else if (pol.equals("0")) {
				neuCount++;
			}
			else if (pol.equals("-1"))
				negCount++;
		}

		sentCount.put("posCount", posCount);
		sentCount.put("neuCount", neuCount);
		sentCount.put("negCount", negCount);

		return sentCount;
	}
	
	
	
	
}
