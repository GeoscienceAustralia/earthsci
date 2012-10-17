package au.gov.ga.earthsci.core.tree;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

/**
 * An abstract implementation of {@link ILazyTreeNode} that provides threadsafe
 * @author James Navin (james.navin@ga.gov.au)
 */
public abstract class AbstractLazyTreeNode<E> extends AbstractTreeNode<E> implements ILazyTreeNode<E>
{

	private AtomicReference<Job> lastLoadJob;
	private AtomicBoolean loaded = new AtomicBoolean(false);
	
	@Override
	public final Job load()
	{
		if (lastLoadJob.get() == null)
		{
			Job loadJob = new Job(getName()) {
				@Override
				protected IStatus run(IProgressMonitor monitor)
				{
					return doLoad(monitor);
				}
			};
			
			// Remove the last load job when loading is complete
			loadJob.addJobChangeListener(new JobChangeAdapter() {
				@Override
				public void done(IJobChangeEvent event)
				{
					loaded.set(event.getResult() == Status.OK_STATUS);
					lastLoadJob.set(null);
				}
			});
			
			if (lastLoadJob.compareAndSet(null, loadJob))
			{
				loadJob.schedule();
			}
			
			return loadJob;
		}
		
		return lastLoadJob.get();
	}

	/**
	 * Perform loading of child nodes here
	 * 
	 * @param monitor The monitor to use for reporting load progress
	 */
	protected abstract IStatus doLoad(IProgressMonitor monitor);
	
	@Override
	public boolean isLoaded()
	{
		return loaded.get();
	}

}
