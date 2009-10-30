package ch.elexis.actions;

import java.util.LinkedList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

public class ElexisEventDispatcher {
	private final LinkedList<ElexisEventListener> listeners;
	private static ElexisEventDispatcher theInstance;
	private final LinkedList<DispatchJob> jobsWaiting;
	
	public static ElexisEventDispatcher getInstance(){
		if(theInstance==null){
			theInstance=new ElexisEventDispatcher();
		}
		return theInstance;
	}
	
	private ElexisEventDispatcher(){
		listeners=new LinkedList<ElexisEventListener>();
		jobsWaiting=new LinkedList<DispatchJob>();
	}
	
	public void addListener(ElexisEventListener el){
		listeners.add(el);
	}
	
	public void removeListener(ElexisEventListener el){
		listeners.remove(el);
	}
	
	public void fire(ElexisEvent ee){
		DispatchJob job=null;
		if(jobsWaiting.size()==0){
			job=new DispatchJob();
			job.addJobChangeListener(new JobChangeAdapter(){
				@Override
				public void done(IJobChangeEvent event){
					jobsWaiting.add((DispatchJob) event.getJob());
					super.done(event);
				}
				
			});
		}else{
			job=jobsWaiting.remove();
		}
		job.setEvent(ee);
		job.schedule();
	}
	
	private class DispatchJob extends Job{
		private ElexisEvent event;
		public DispatchJob(){
			super("Dispatch Elexis events");
			setSystem(true);
			setUser(false);
			setPriority(Job.DECORATE);
		}
		
		public void setEvent(ElexisEvent event){
			this.event=event;
		}
		@Override
		protected IStatus run(IProgressMonitor monitor){
			for(ElexisEventListener l:listeners){
				if(event.matches(l.getElexisEventFilter())){
					l.catchElexisEvent(event);
				}
			}
			return Status.OK_STATUS;
		}
		
	}
}
