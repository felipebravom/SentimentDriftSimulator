package simulator;

import java.io.IOException;

import weka.core.Instance;
import moa.streams.ArffFileStream;
import moa.tasks.VSMCreator;



public class Testing {

	static public void main(String args[]) throws IOException{

		ArffFileStream stream= new ArffFileStream("/Users/admin/edinHead.arff", 2 ); 
		//stream.prepareForUse();
		
		VSMCreator vsm=new VSMCreator();
		vsm.streamOption.setCurrentObject(stream);
		
		vsm.textAttributeIndexOption.setValue(2);
		
		vsm.prepareForUse();
		
		vsm.doTask();
		
		
//		while(stream.hasMoreInstances()){
//			Instance tweet = stream.nextInstance();
//			System.out.println(tweet.toString());
//			
//		}

		



	}

}
