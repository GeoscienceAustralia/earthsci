package au.gov.ga.earthsci.common.util;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * An {@link IProgressMonitor} that maintains a list of registered delegate
 * monitors.
 * <p/>
 * Each event will be posted to all registered delegates.
 * <p/>
 * This provides an easy mechanism to have multiple interested parties listen
 * for progress updates from a job.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class DelegatingProgressMonitor extends NullProgressMonitor implements IProgressMonitor
{

	private Set<IProgressMonitor> additionalMonitors = new LinkedHashSet<IProgressMonitor>();
	private ReadWriteLock monitorsLock = new ReentrantReadWriteLock();


	@Override
	public void beginTask(String name, int totalWork)
	{
		monitorsLock.readLock().lock();
		try
		{
			for (IProgressMonitor m : additionalMonitors)
			{
				m.beginTask(name, totalWork);
			}
		}
		finally
		{
			monitorsLock.readLock().unlock();
		}
	}

	@Override
	public void done()
	{
		monitorsLock.readLock().lock();
		try
		{
			for (IProgressMonitor m : additionalMonitors)
			{
				m.done();
			}
		}
		finally
		{
			monitorsLock.readLock().unlock();
		}
	}

	@Override
	public void internalWorked(double work)
	{
		monitorsLock.readLock().lock();
		try
		{
			for (IProgressMonitor m : additionalMonitors)
			{
				m.internalWorked(work);
			}
		}
		finally
		{
			monitorsLock.readLock().unlock();
		}
	}

	@Override
	public void setCanceled(boolean value)
	{
		super.setCanceled(value);

		monitorsLock.readLock().lock();
		try
		{
			for (IProgressMonitor m : additionalMonitors)
			{
				m.setCanceled(value);
			}
		}
		finally
		{
			monitorsLock.readLock().unlock();
		}
	}

	@Override
	public void setTaskName(String name)
	{
		monitorsLock.readLock().lock();
		try
		{
			for (IProgressMonitor m : additionalMonitors)
			{
				m.setTaskName(name);
			}
		}
		finally
		{
			monitorsLock.readLock().unlock();
		}

	}

	@Override
	public void subTask(String name)
	{
		monitorsLock.readLock().lock();
		try
		{
			for (IProgressMonitor m : additionalMonitors)
			{
				m.subTask(name);
			}
		}
		finally
		{
			monitorsLock.readLock().unlock();
		}
	}

	@Override
	public void worked(int work)
	{
		monitorsLock.readLock().lock();
		try
		{
			for (IProgressMonitor m : additionalMonitors)
			{
				m.worked(work);
			}
		}
		finally
		{
			monitorsLock.readLock().unlock();
		}
	}

	/**
	 * Add an additional monitor delegate
	 */
	public void addMonitor(IProgressMonitor monitor)
	{
		monitorsLock.writeLock().lock();
		try
		{
			additionalMonitors.add(monitor);
		}
		finally
		{
			monitorsLock.writeLock().unlock();
		}
	}

	/**
	 * Add additional monitor delegates
	 */
	public void addMonitors(Collection<IProgressMonitor> monitors)
	{
		monitorsLock.writeLock().lock();
		try
		{
			additionalMonitors.addAll(monitors);
		}
		finally
		{
			monitorsLock.writeLock().unlock();
		}
	}

	/**
	 * Remove an additional monitor delegate
	 */
	public void removeMonitor(IProgressMonitor monitor)
	{
		monitorsLock.writeLock().lock();
		try
		{
			additionalMonitors.remove(monitor);
		}
		finally
		{
			monitorsLock.writeLock().unlock();
		}
	}

	/**
	 * Remove all registered monitor delegates
	 */
	public void clear()
	{
		monitorsLock.writeLock().lock();
		try
		{
			additionalMonitors.clear();
		}
		finally
		{
			monitorsLock.writeLock().unlock();
		}
	}

}
