package ch.elexis.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

public class DelayableJob extends Job {
	private IWorker worker;
	
	public DelayableJob(String name, IWorker worker){
		super(name);
		this.worker=worker;
	}
	
	public void launch(long delayMillis){
		this.cancel();
		System.out.println("launch");
		this.schedule(delayMillis);
	}
	@Override
	protected IStatus run(IProgressMonitor monitor){
		return worker.work(monitor);
	}
	
	public interface IWorker{
		public IStatus work(IProgressMonitor monitor);
	}
}
