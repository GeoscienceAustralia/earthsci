package au.gov.ga.earthsci.core.tree;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

/**
 * An abstract implementation of {@link ILazyTreeNode} that provides threadsafe
 * @author James Navin (james.navin@ga.gov.au)
 */
public abstract class AbstractLazyTreeNode<E> extends AbstractTreeNode<E> implements ILazyTreeNode<E>
{

	private AtomicReference<LazyTreeJob> lastLoadJob = new AtomicReference<LazyTreeJob>(null);
	private AtomicBoolean loaded = new AtomicBoolean(false);
	
	@Override
	public final LazyTreeJob load()
	{
		System.out.println("Load called");
		if (lastLoadJob.get() == null)
		{
			LazyTreeJob loadJob = new LazyTreeJob(this) {
				@Override
				protected IStatus doRun(IProgressMonitor monitor)
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
				System.out.println("Scheduling job");
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

	@Override
	public int getChildCount()
	{
		if (isLoaded())
		{
			return super.getChildCount();
		}
		return 1;
	}
	
	@Override
	public String toString()
	{
		return getName() + " (" + depth() + ", " + index() + ")";
	}
	
}
