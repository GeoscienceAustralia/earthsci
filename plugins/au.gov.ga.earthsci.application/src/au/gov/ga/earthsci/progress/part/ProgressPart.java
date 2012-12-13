package au.gov.ga.earthsci.progress.part;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.core.runtime.jobs.ProgressProvider;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * A simple progress part inspired by the legacy Eclipse 3.x
 * <code>DetailedProgressView</code>
 * <p/>
 * Used to show the progress of currently running jobs.
 * <p/>
 * This part also injects an {@link ProgressProvider} onto the current
 * {@link IJobManager} to capture progress information. Other plugins should not
 * override this provider.
 * <hr/>
 * <b>Important:</b> This part should be replaced with the inbuilt one when it
 * becomes available in the Eclipse 4.x release train.
 * <hr/>
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class ProgressPart
{

	private static final Map<Integer, String> JOB_STATE_LABEL = new HashMap<Integer, String>();
	static
	{
		JOB_STATE_LABEL.put(Job.WAITING, Messages.ProgressPart_WaitingStatusLabel);
		JOB_STATE_LABEL.put(Job.SLEEPING, Messages.ProgressPart_SleepingStatusLabel);
		JOB_STATE_LABEL.put(Job.RUNNING, Messages.ProgressPart_RunningStatusLabel);
	}

	@Inject
	private EModelService modelService;

	@Inject
	private IJobManager jobManager;

	private TableViewer viewer;

	private List<JobInfo> jobList = new ArrayList<JobInfo>();

	@Inject
	public void init(Composite parent, MPart part)
	{

		viewer = new TableViewer(parent, SWT.V_SCROLL);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setInput(jobList);
		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);
		viewer.setSorter(new ViewerSorter()
		{
			@Override
			public int category(Object element)
			{
				return ((JobInfo) element).job.getState();
			}

			@Override
			public int compare(Viewer viewer, Object e1, Object e2)
			{
				return ((JobInfo) e1).job.getPriority() - ((JobInfo) e2).job.getPriority();
			}
		});
		viewer.addFilter(new ViewerFilter()
		{
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element)
			{
				return ((JobInfo) element).job.getState() != Job.SLEEPING;
			}

		});

		createColumns();

		jobManager.addJobChangeListener(new JobChangeAdapter()
		{

			@Override
			public void scheduled(IJobChangeEvent event)
			{
				JobInfo jobInfo = new JobInfo(event.getJob(), ProgressPart.this);

				synchronized (jobList)
				{
					jobList.add(jobInfo);
				}
				asyncRefresh();

				super.scheduled(event);
			}

			@Override
			public void done(final IJobChangeEvent event)
			{
				JobInfo jobInfo = findInfoForJob(event.getJob());

				synchronized (jobList)
				{
					jobList.remove(jobInfo);
				}
				asyncRefresh();
			}
		});

		jobManager.setProgressProvider(new ProgressProvider()
		{

			@Override
			public IProgressMonitor createMonitor(final Job job)
			{
				JobInfo jobInfo = findInfoForJob(job);
				if (jobInfo == null)
				{
					jobInfo = new JobInfo(job, ProgressPart.this);

					synchronized (jobList)
					{
						jobList.add(jobInfo);
					}
					asyncRefresh();
				}

				return jobInfo;
			}
		});
	}

	private JobInfo findInfoForJob(Job job)
	{
		synchronized (jobList)
		{
			for (JobInfo info : jobList)
			{
				if (info.job == job)
				{
					return info;
				}
			}
		}
		return null;
	}

	private void createColumns()
	{
		TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setText(Messages.ProgressPart_JobNameColumnLabel);
		column.getColumn().setWidth(500);

		column.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				return ((JobInfo) element).job.getName();
			}
		});

		column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setText(Messages.ProgressPart_JobStateColumnLabel);
		column.getColumn().setWidth(100);

		column.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				return JOB_STATE_LABEL.get(((JobInfo) element).job.getState());
			}
		});

		column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setText(Messages.ProgressPart_JobProgressColumnLabel);
		column.getColumn().setWidth(100);

		column.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				return ((JobInfo) element).getProgress();
			}
		});

		column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setText(Messages.ProgressPart_JobTaskColumnLabel);
		column.getColumn().setWidth(500);

		column.setLabelProvider(new ColumnLabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				return ((JobInfo) element).currentTask;
			}
		});
	}

	private void asyncRefresh()
	{
		Display.getDefault().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				if (!viewer.getTable().isDisposed())
				{
					viewer.refresh();
				}
			}
		});
	}

	private void asyncUpdate(final JobInfo job)
	{
		Display.getDefault().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				if (!viewer.getTable().isDisposed())
				{
					viewer.update(job, null);
				}
			}
		});
	}

	private static class JobInfo extends NullProgressMonitor
	{
		ProgressPart view;

		String currentTask;
		int totalWork;
		int work;

		Job job;

		public JobInfo(Job job, final ProgressPart view)
		{
			this.view = view;
			this.job = job;

			this.job.addJobChangeListener(new JobChangeAdapter()
			{
				@Override
				public void aboutToRun(IJobChangeEvent event)
				{
					view.asyncUpdate(JobInfo.this);
				}

				@Override
				public void running(IJobChangeEvent event)
				{
					view.asyncUpdate(JobInfo.this);
				}
			});
		}

		String getProgress()
		{
			if (totalWork == IProgressMonitor.UNKNOWN)
			{
				return Messages.ProgressPart_UnknownProgress;
			}
			return (int) (work * 100d / totalWork) + "%"; //$NON-NLS-1$
		}

		@Override
		public void worked(int work)
		{
			this.work += work;

			view.asyncUpdate(this);
		}

		@Override
		public void beginTask(String name, int totalWork)
		{
			this.currentTask = name;
			this.totalWork = totalWork;

			view.asyncUpdate(this);
		}

		@Override
		public void subTask(String name)
		{
			this.currentTask = name;

			view.asyncUpdate(this);
		}
	}
}
