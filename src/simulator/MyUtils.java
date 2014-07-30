package simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cmu.arktweetnlp.Tagger;
import cmu.arktweetnlp.Twokenize;
import cmu.arktweetnlp.impl.ModelSentence;
import cmu.arktweetnlp.impl.Sentence;


public class MyUtils {

	// Tokenize a the tweet using TwitterNLP and cleans repeated letters, URLS,
	// and user mentions
	static public List<String> cleanTokenize(String content) {
		content = content.toLowerCase();

		// if a letters appears two or more times it is replaced by only two
		// occurrences of it
		content = content.replaceAll("([a-z])\\1+", "$1$1");

		List<String> tokens = new ArrayList<String>();

		for (String word : Twokenize.tokenizeRawTweetText(content)) {
			String cleanWord = word;

			// Replace URLs to a generic URL
			if (word.matches("http.*|ww\\..*")) {
				cleanWord = "http://www.url.com";
			}

			// Replaces user mentions to a generic user
			else if (word.matches("@.*")) {
				cleanWord = "@user";
			}

			tokens.add(cleanWord);
		}
		return tokens;
	}

	// calculates the frequency of each different token in a list of strings
	static public Map<String, Integer> calculateTermFreq(List<String> tokens) {
		Map<String, Integer> termFreq = new HashMap<String, Integer>();

		// Traverse the strings and increments the counter when the token was
		// already seen before
		for (String token : tokens) {
			if (termFreq.containsKey(token))
				termFreq.put(token, termFreq.get(token) + 1);
			else
				termFreq.put(token, 1);
		}

		return termFreq;
	}

	// Returns POS tags from a List of tokens using TwitterNLP
	static public List<String> getPOStags(List<String> tokens, Tagger tagger) {

		Sentence sentence = new Sentence();
		sentence.tokens = tokens;
		ModelSentence ms = new ModelSentence(sentence.T());
		tagger.featureExtractor.computeFeatures(sentence, ms);
		tagger.model.greedyDecode(ms, false);

		ArrayList<String> tags = new ArrayList<String>();

		for (int t = 0; t < sentence.T(); t++) {
			String tag = tagger.model.labelVocab.name(ms.labels[t]);
			tags.add(tag);
		}

		return tags;

	}

	

}
