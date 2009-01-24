package ch.elexis.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

public class DelayableJob extends Job {
	private IWorker worker;
	public static final int DELAY_ADAPTIVE = -1;
	private long lastCall = 0L;
	private int lastDelay = 200;
	
	public DelayableJob(String name, IWorker worker){
		super(name);
		this.worker = worker;
	}
	
	/**
	 * Launch the job after a specified delay. If a re-launch occurs within the delay time, the
	 * counter is reset and will wait for the specified time again. If the Job was already launched
	 * when a re-launch occurs, the Job will be stopped if possible.
	 * 
	 * If the delay is DELAY_ADAPTIVE, the JOb will try to find the optimal delay by analyzing
	 * earlier calls. So different typing speeds of different users can be handled.
	 * 
	 * @param delayMillis
	 */
	public void launch(int delayMillis){
		this.cancel();
		if (delayMillis == DELAY_ADAPTIVE) {
			if (lastCall == 0) {
				delayMillis = lastDelay; // this is the first call; we start with predefined value
			} else {
				int delay = (int) (System.currentTimeMillis() - lastCall);
				if ((delay > 20) && (delay < 1000)) { // we do not consider delays <20 or > 1000ms
					int diff = delay - lastDelay;
					lastDelay = lastDelay + (diff / 2);
				}
				delayMillis = lastDelay;
			}
			lastCall = System.currentTimeMillis();
			System.out.println("Delay: " + delayMillis);
		}
		this.schedule(delayMillis);
	}
	
	@Override
	protected IStatus run(IProgressMonitor monitor){
		return worker.work(monitor);
	}
	
	public interface IWorker {
		public IStatus work(IProgressMonitor monitor);
	}
}
