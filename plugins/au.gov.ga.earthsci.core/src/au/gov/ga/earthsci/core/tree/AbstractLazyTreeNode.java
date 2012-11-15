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
 * An abstract implementation of {@link ILazyTreeNode} that provides background loading via the {@link Job} API.
 * <p/>
 * Subclasses should implement {@link #doLoad(IProgressMonitor)} to perform the actual loading of children, and progress should
 * be reported to the provided {@link IProgressMonitor}.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public abstract class AbstractLazyTreeNode<E> extends AbstractTreeNode<E> implements ILazyTreeNode<E>
{
	private static final String UNKNOWN_PLUGIN_ID = "unknown"; //$NON-NLS-1$
	
	final private AtomicReference<LazyTreeJob> lastLoadJob = new AtomicReference<LazyTreeJob>(null);
	final private AtomicBoolean loaded = new AtomicBoolean(false);
	final private AtomicReference<IStatus> status = new AtomicReference<IStatus>(null);
	
	@Override
	public final LazyTreeJob load()
	{
		if (lastLoadJob.get() == null)
		{
			final LazyTreeJob loadJob = new LazyTreeJob(this) {
				@Override
				protected IStatus doRun(IProgressMonitor monitor)
				{
					try
					{
						return doLoad(monitor);
					}
					catch (Exception e)
					{
						return createErrorStatus(Messages.AbstractLazyTreeNode_UnkownExceptionDuringLoadMessage, e);
					}
				}
			};
			
			if (lastLoadJob.compareAndSet(null, loadJob))
			{
				// Remove the last load job when loading is complete
				loadJob.addJobChangeListener(new JobChangeAdapter() {
					@Override
					public void done(final IJobChangeEvent event)
					{
						setLoaded(event.getResult().getCode() != Status.CANCEL);
						setStatus(event.getResult());
						lastLoadJob.set(null);
					}
				});
				
				loadJob.schedule();
				return loadJob;
			}
		}
		
		return lastLoadJob.get();
	}

	/**
	 * Perform loading of child nodes here.
	 * <p/>
	 * Progress should be reported to the provided {@link IProgressMonitor} as appropriate.
	 * 
	 * @param monitor The monitor to use for reporting load progress
	 */
	protected abstract IStatus doLoad(IProgressMonitor monitor);
	
	@Override
	public boolean isLoaded()
	{
		return loaded.get();
	}

	protected final void setLoaded(boolean loaded)
	{
		boolean old = this.loaded.getAndSet(loaded);
		
		if (old != loaded)
		{
			firePropertyChange("loaded", old, loaded); //$NON-NLS-1$
		}
	}
	
	@Override
	public IStatus getStatus()
	{
		return status.get();
	}
	
	protected final void setStatus(IStatus status)
	{
		IStatus old = this.status.getAndSet(status);
		
		if (old == null || old.getCode() != status.getCode())
		{
			firePropertyChange("status", old, status); //$NON-NLS-1$
		}
	}
	
	@Override
	public boolean hasError()
	{
		return isLoaded() && getStatus() != null && getStatus().getCode() == IStatus.ERROR;
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
	
	@SuppressWarnings("nls")
	@Override
	public String toString()
	{
		return getName() + " (" + depth() + ", " + index() + ")";
	}
	
	/**
	 * Return a new {@link IStatus} instance representing an ERROR state with the
	 * given message and (optional) exception
	 */
	protected IStatus createErrorStatus(String message, Throwable error)
	{
		return new Status(Status.ERROR, UNKNOWN_PLUGIN_ID, Status.ERROR, message, error);
	}
	
}
