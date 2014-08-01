package moa.streams.generators;



import moa.core.InstancesHeader;
import moa.core.ObjectRepository;
import moa.options.AbstractOptionHandler;
import moa.options.FileOption;
import moa.options.IntOption;
import moa.streams.ArffFileStream;
import moa.streams.InstanceStream;
import moa.tasks.TaskMonitor;
import weka.core.Instance;


/**
 * Stream reader of ARFF files.
 *
 * @author Richard Kirkby (rkirkby@cs.waikato.ac.nz)
 * @version $Revision: 7 $
 */
public class MyArffFileStream extends AbstractOptionHandler implements
InstanceStream {

	@Override
	public String getPurposeString() {
		return "A stream read from an ARFF file.";
	}

	private static final long serialVersionUID = 1L;

	public FileOption arffFileOption = new FileOption("arffFile", 'f',
			"ARFF file to load.", null, "arff", false);

	public IntOption classIndexOption = new IntOption(
			"classIndex",
			'c',
			"Class index of data. 0 for none or -1 for last attribute in file.",
			-1, -1, Integer.MAX_VALUE);


	protected ArffFileStream arffFileStream;

	public MyArffFileStream() {
	
	}

	public MyArffFileStream(String arffFileName, int classIndex) {
        this.arffFileOption.setValue(arffFileName);
        this.classIndexOption.setValue(classIndex);
        restart();
	}

	@Override
	public void prepareForUseImpl(TaskMonitor monitor,
			ObjectRepository repository) {
		
		this.arffFileStream=new ArffFileStream(this.arffFileOption.getValue(),this.classIndexOption.getValue());
		
		restart();
	}

	@Override
	public InstancesHeader getHeader() {
		return this.arffFileStream.getHeader();
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
	public Instance nextInstance() {
		return this.arffFileStream.nextInstance();
	}

	@Override
	public boolean isRestartable() {
		return this.arffFileStream.isRestartable();
	}

	@Override
	public void restart() {
		this.arffFileStream.restart();    	
	}



	@Override
	public void getDescription(StringBuilder sb, int indent) {
		// TODO Auto-generated method stub
	}
}
