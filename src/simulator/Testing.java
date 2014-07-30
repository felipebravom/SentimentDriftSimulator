package simulator;

import java.io.IOException;
import java.util.Random;

import weka.core.Instance;
import moa.streams.generators.SentimentDriftGenerator;


public class Testing {

	static public void main(String args[]) throws IOException{
		
	
	
		SentimentDriftGenerator sg=new SentimentDriftGenerator("/Users/admin/workspace/SentimentSimulator/data/twitter.arff");
		sg.prepareForUse();

		Instance inta=sg.nextInstance();
		for(int i=0;i<100;i++){
			inta=sg.nextInstance();
			if(inta.numAttributes()>0)
				System.out.println(inta.toString());


		}



	}

}