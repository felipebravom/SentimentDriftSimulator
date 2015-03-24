package moa.classifiers.functions;

import java.util.ArrayList;

import weka.core.Utils;

public class SGDExport extends SGD {

	/**
	 *  Extension of the SGD algorithm that allows to export the weights at different time spans
	 */
	private static final long serialVersionUID = 1L;

	public String printHeader(){
		StringBuffer buff = new StringBuffer();

		for(int i=0;i<this.getModelContext().numAttributes();i++){

		}

		for (int i = 0; i < m_weights.numValues(); i++) {
			buff.append(this.modelContext.attribute(i).name());
			buff.append("\t");		
		}
		buff.append("bias\n");
		return buff.toString();
	}


	public String attHeader(){
		StringBuffer buff = new StringBuffer();

		for(int i=0;i<this.getModelContext().numAttributes();i++){
			if(i!=this.getModelContext().classIndex()){
				buff.append(this.modelContext.attribute(i).name());
				buff.append("\t");	
			}

		}
		buff.append("bias\n");
		return buff.toString();
	}


	public String printLine(){
		StringBuffer buff = new StringBuffer();
		for (int i = 0; i < m_weights.numValues(); i++) {
			if (i != this.getModelContext().classIndex()) {            
				buff.append(Utils.doubleToString(m_weights.getValue(i), 12, 4).trim());    
				buff.append("\t");		
			}
		}
		buff.append(Utils.doubleToString(this.m_bias, 12, 4).trim()+"\n");

		return buff.toString();	

	}
	
	
	public double[] getWeights(){
		
		ArrayList<Double> values=new ArrayList<Double>();
		for (int i = 0; i < m_weights.numValues(); i++) {
			if (i != this.getModelContext().classIndex()) {            
				values.add(m_weights.getValue(i));    				
			}
		}
		
		values.add(this.m_bias);
		
		
		System.out.println("BIAS+ "+this.m_bias);
		System.out.println("m_weight Length: "+m_weights.numValues());
		System.out.println("values Length: "+values.size());
		
		
		double[] result=new double[values.size()];
		for(int i=0;i<result.length;i++){
			result[i]=values.get(i);
		}
		

		return result;
	}

}
