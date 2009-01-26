package ch.elexis.actions;

import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

/**
 * A job that does not execute immediately on launch but waits if there comes anothar call - e.g a
 * key press of the user. The time the job waits is configurable but can also be adaptive - it
 * remembers the time between earlier calls and decides accordingly, how long it should wait next
 * time.
 * 
 * @author gerry
 * 
 */
public class DelayableJob extends Job {
	private IWorker worker;
	public static final int DELAY_ADAPTIVE = -1;
	private long lastCall = 0L;
	private int lastDelay = 200;
	private HashMap<String, Object> privdata = new HashMap<String, Object>();
	
	public DelayableJob(String name, IWorker worker){
		super(name);
		this.worker = worker;
	}
	
	/**
	 * Launch the job after a specified delay. If a re-launch occurs within the delay time, the
	 * counter is reset and will wait for the specified time again. If the Job was already launched
	 * when a re-launch occurs, the Job will be stopped if possible.
	 * 
	 * If the delay is DELAY_ADAPTIVE, the Job will try to find the optimal delay by analyzing
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
			// System.out.println("Delay: " + delayMillis);
		}
		this.schedule(delayMillis);
	}
	
	/**
	 * set arbitrary data that can be retrieved at run time
	 * 
	 * @param key
	 *            a unique key
	 * @param value
	 *            n arbitrary object
	 */
	public void setRuntimeData(String key, Object value){
		privdata.put(key, value);
	}
	
	public Object getRuntimeData(String key){
		return privdata.get(key);
	}
	
	@Override
	protected IStatus run(IProgressMonitor monitor){
		return worker.work(monitor, privdata);
	}
	
	public interface IWorker {
		public IStatus work(IProgressMonitor monitor, HashMap<String, Object> params);
	}
}
