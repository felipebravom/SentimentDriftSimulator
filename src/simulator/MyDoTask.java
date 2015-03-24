package simulator;

import moa.core.Globals;
import moa.core.Measurement;
import moa.core.StringUtils;
import moa.core.TimingUtils;
import moa.options.ClassOption;
import moa.options.FlagOption;
import moa.options.IntOption;
import moa.options.Option;
import moa.tasks.FailedTaskReport;
import moa.tasks.Task;
import moa.tasks.TaskThread;

public class MyDoTask {
	
    /** Array of characters to use to animate the progress of tasks running. */
    public static final char[] progressAnimSequence = new char[]{'-', '\\',
        '|', '/'};

    /** Maximum length of the status string that shows the progress of tasks running.  */
    public static final int MAX_STATUS_STRING_LENGTH = 79;
	
	
	
	static public void runMoa(String command) throws Exception{

		// create standard options
		FlagOption suppressStatusOutputOption = new FlagOption(
				"suppressStatusOutput", 'S',
				"Suppress the task status output that is normally send to stderr.");
		FlagOption suppressResultOutputOption = new FlagOption(
				"suppressResultOutput", 'R',
				"Suppress the task result output that is normally send to stdout.");
		IntOption statusUpdateFrequencyOption = new IntOption(
				"statusUpdateFrequency",
				'F',
				"How many milliseconds to wait between status updates.",
				1000, 0, Integer.MAX_VALUE);
		Option[] extraOptions = new Option[]{
				suppressStatusOutputOption, suppressResultOutputOption,
				statusUpdateFrequencyOption};

		// parse options
		Task task = (Task) ClassOption.cliStringToObject(command, Task.class, extraOptions);
		Object result = null;
		if (suppressStatusOutputOption.isSet()) {
			result = task.doTask();
		} else {
			System.err.println();
			System.err.println(Globals.getWorkbenchInfoString());
			System.err.println();
			boolean preciseTiming = TimingUtils.enablePreciseTiming();
			// start the task thread
			TaskThread taskThread = new TaskThread(task);
			taskThread.start();
			int progressAnimIndex = 0;
			// inform user of progress
			while (!taskThread.isComplete()) {
				StringBuilder progressLine = new StringBuilder();
				progressLine.append(progressAnimSequence[progressAnimIndex]);
				progressLine.append(' ');
				progressLine.append(StringUtils.secondsToDHMSString(taskThread.getCPUSecondsElapsed()));
				progressLine.append(" [");
				progressLine.append(taskThread.getCurrentStatusString());
				progressLine.append("] ");
				double fracComplete = taskThread.getCurrentActivityFracComplete();
				if (fracComplete >= 0.0) {
					progressLine.append(StringUtils.doubleToString(
							fracComplete * 100.0, 2, 2));
					progressLine.append("% ");
				}
				progressLine.append(taskThread.getCurrentActivityString());
				while (progressLine.length() < MAX_STATUS_STRING_LENGTH) {
					progressLine.append(" ");
				}
				if (progressLine.length() > MAX_STATUS_STRING_LENGTH) {
					progressLine.setLength(MAX_STATUS_STRING_LENGTH);
					progressLine.setCharAt(
							MAX_STATUS_STRING_LENGTH - 1, '~');
				}
				System.err.print(progressLine.toString());
				System.err.print('\r');
				if (++progressAnimIndex >= progressAnimSequence.length) {
					progressAnimIndex = 0;
				}
				try {
					Thread.sleep(statusUpdateFrequencyOption.getValue());
				} catch (InterruptedException ignored) {
					// wake up
				}
			}
			StringBuilder cleanupString = new StringBuilder();
			for (int i = 0; i < MAX_STATUS_STRING_LENGTH; i++) {
				cleanupString.append(' ');
			}
			System.err.println(cleanupString);
			result = taskThread.getFinalResult();
			if (!(result instanceof FailedTaskReport)) {
				System.err.print("Task completed in "
						+ StringUtils.secondsToDHMSString(taskThread.getCPUSecondsElapsed()));
				if (preciseTiming) {
					System.err.print(" (CPU time)");
				}
				System.err.println();
				System.err.println();
			}
		}
		if (result instanceof FailedTaskReport) {
			System.err.println("Task failed. Reason: ");
			((FailedTaskReport) result).getFailureReason().printStackTrace();
		} else {
			if (!suppressResultOutputOption.isSet()) {
				if (result instanceof Measurement[]) {
					StringBuilder sb = new StringBuilder();
					Measurement.getMeasurementsDescription(
							(Measurement[]) result, sb, 0);
					System.out.println(sb.toString());
				} else {
					System.out.println(result);
				}
				System.out.flush();
			}
		}
	}
	
	static public void main(String args[]) throws Exception{
		
		// create standard options
		FlagOption suppressStatusOutputOption = new FlagOption(
				"suppressStatusOutput", 'S',
				"Suppress the task status output that is normally send to stderr.");
		FlagOption suppressResultOutputOption = new FlagOption(
				"suppressResultOutput", 'R',
				"Suppress the task result output that is normally send to stdout.");
		IntOption statusUpdateFrequencyOption = new IntOption(
				"statusUpdateFrequency",
				'F',
				"How many milliseconds to wait between status updates.",
				1000, 0, Integer.MAX_VALUE);
		Option[] extraOptions = new Option[]{
				suppressStatusOutputOption, suppressResultOutputOption,
				statusUpdateFrequencyOption};
		
		Task task = (Task) ClassOption.cliStringToObject("EvaluatePrequential -l trees.HoeffdingTree", Task.class, extraOptions); 
		Object result=task.doTask(); 
		System.out.println(result.toString());
		
//		try {
//			runMoa("EvaluatePrequential -l trees.HoeffdingTree");
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
	}

}
