/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.earthsci.worldwind.common.downloader;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.WWObjectImpl;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.retrieve.RetrievalFuture;
import gov.nasa.worldwind.retrieve.RetrievalService;
import gov.nasa.worldwind.retrieve.Retriever;
import gov.nasa.worldwind.util.Logging;

import java.net.SocketTimeoutException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.net.ssl.SSLHandshakeException;

/**
 * {@link RetrievalService} used by the {@link Downloader}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DownloaderRetrievalService extends WWObjectImpl implements RetrievalService,
		Thread.UncaughtExceptionHandler
{
	// These constants are last-ditch values in case Configuration lacks defaults
	private static final int DEFAULT_POOL_SIZE = 5;
	private static final int DEFAULT_TIME_PRIORITY_GRANULARITY = 500; // milliseconds

	private static final String RUNNING_THREAD_NAME_PREFIX = "Active downloader thread: ";
	private static final String IDLE_THREAD_NAME_PREFIX = "Idle downloader thread";

	private RetrievalExecutor executor; // thread pool for running retrievers
	private ConcurrentLinkedQueue<RetrievalTask> activeTasks; // tasks currently allocated a thread
	
	protected SSLExceptionListener sslExceptionListener;

	/**
	 * Encapsulates a single threaded retrieval as a
	 * {@link java.util.concurrent.FutureTask}.
	 */
	private static class RetrievalTask extends FutureTask<Retriever> implements RetrievalFuture,
			Comparable<RetrievalTask>
	{
		private Retriever retriever;
		private double priority; // retrieval secondary priority (primary priority is submit time)

		private RetrievalTask(Retriever retriever, double priority)
		{
			super(retriever);
			this.retriever = retriever;
			this.priority = priority;
		}

		@Override
		public Retriever getRetriever()
		{
			return this.retriever;
		}

		@Override
		public void run()
		{
			if (this.isDone() || this.isCancelled())
				return;

			super.run();
		}

		@Override
		public int compareTo(RetrievalTask that)
		{
			if (that == null)
			{
				String msg = Logging.getMessage("nullValue.RetrieverIsNull");
				Logging.logger().fine(msg);
				throw new IllegalArgumentException(msg);
			}

			if (this.priority > 0 && that.priority > 0) // only secondary priority used if either is negative
			{
				// Requests submitted within different time-granularity periods are ordered exclusive of their
				// client-specified priority.
				long now = System.currentTimeMillis();
				long thisElapsedTime = now - this.retriever.getSubmitTime();
				long thatElapsedTime = now - that.retriever.getSubmitTime();
				if (((thisElapsedTime - thatElapsedTime) / DEFAULT_TIME_PRIORITY_GRANULARITY) != 0)
					return thisElapsedTime < thatElapsedTime ? -1 : 1;
			}

			// The client-pecified priority is compared for requests submitted within the same granularity period.
			return this.priority == that.priority ? 0 : this.priority < that.priority ? -1 : 1;
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			final RetrievalTask that = (RetrievalTask) o;

			// Tasks are equal if their retrievers are equivalent
			return this.retriever.equals(that.retriever);
			// Priority and submint time are not factors in equality
		}

		@Override
		public int hashCode()
		{
			return this.retriever.getName().hashCode();
		}
	}

	@Override
	public void uncaughtException(Thread thread, Throwable throwable)
	{
		Logging.logger().fine(
				Logging.getMessage("BasicRetrievalService.UncaughtExceptionDuringRetrieval", thread.getName()));
		throwable.printStackTrace();
	}

	private class RetrievalExecutor extends ThreadPoolExecutor
	{
		private static final long THREAD_TIMEOUT = 2; // keep idle threads alive this many seconds

		private RetrievalExecutor(int poolSize)
		{
			super(poolSize, poolSize, THREAD_TIMEOUT, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),
					new ThreadFactory()
					{
						@Override
						public Thread newThread(Runnable runnable)
						{
							Thread thread = new Thread(runnable);
							thread.setDaemon(true);
							thread.setPriority(Thread.MIN_PRIORITY);
							thread.setUncaughtExceptionHandler(DownloaderRetrievalService.this);
							return thread;
						}
					});
		}

		/**
		 * @param thread
		 *            the thread the task is running on
		 * @param runnable
		 *            the <code>Retriever</code> running on the thread
		 * @throws IllegalArgumentException
		 *             if either <code>thread</code> or <code>runnable</code> is
		 *             null
		 */
		@Override
		protected void beforeExecute(Thread thread, Runnable runnable)
		{
			if (thread == null)
			{
				String msg = Logging.getMessage("nullValue.ThreadIsNull");
				Logging.logger().fine(msg);
				throw new IllegalArgumentException(msg);
			}
			if (runnable == null)
			{
				String msg = Logging.getMessage("nullValue.RunnableIsNull");
				Logging.logger().fine(msg);
				throw new IllegalArgumentException(msg);
			}

			RetrievalTask task = (RetrievalTask) runnable;

			task.retriever.setBeginTime(System.currentTimeMillis());

			if (DownloaderRetrievalService.this.activeTasks.contains(task))
			{
				// Task is a duplicate
				Logging.logger().finer(
						Logging.getMessage("BasicRetrievalService.CancellingDuplicateRetrieval", task.getRetriever()
								.getName()));
				task.cancel(true);
			}

			DownloaderRetrievalService.this.activeTasks.add(task);

			thread.setName(RUNNING_THREAD_NAME_PREFIX + task.getRetriever().getName());
			thread.setPriority(Thread.MIN_PRIORITY); // Subordinate thread priority to rendering
			thread.setUncaughtExceptionHandler(DownloaderRetrievalService.this);

			super.beforeExecute(thread, runnable);
		}

		/**
		 * @param runnable
		 *            the <code>Retriever</code> running on the thread
		 * @param throwable
		 *            an exception thrown during retrieval, will be null if no
		 *            exception occurred
		 * @throws IllegalArgumentException
		 *             if <code>runnable</code> is null
		 */
		@Override
		protected void afterExecute(Runnable runnable, Throwable throwable)
		{
			if (runnable == null)
			{
				String msg = Logging.getMessage("nullValue.RunnableIsNull");
				Logging.logger().fine(msg);
				throw new IllegalArgumentException(msg);
			}

			super.afterExecute(runnable, throwable);

			RetrievalTask task = (RetrievalTask) runnable;
			DownloaderRetrievalService.this.activeTasks.remove(task);
			task.retriever.setEndTime(System.currentTimeMillis());

			try
			{
				if (throwable != null)
				{
					Logging.logger().log(
							Level.FINE,
							Logging.getMessage("BasicRetrievalService.ExceptionDuringRetrieval", task.getRetriever()
									.getName()), throwable);
				}

				task.get(); // Wait for task to finish, cancel or break
			}
			catch (java.util.concurrent.ExecutionException e)
			{
				String message =
						Logging.getMessage("BasicRetrievalService.ExecutionExceptionDuringRetrieval", task
								.getRetriever().getName());
				if (e.getCause() instanceof SocketTimeoutException)
				{
					Logging.logger().fine(message + " " + e.getCause().getLocalizedMessage());
				}
				else if (e.getCause() instanceof SSLHandshakeException)
                {
                    if (sslExceptionListener != null)
                        sslExceptionListener.onException(e.getCause(), task.getRetriever().getName());
                    else
                        Logging.logger().fine(message + " " + e.getCause().getLocalizedMessage());
                }
				else
				{
					Logging.logger().log(Level.FINE, message, e);
				}
			}
			catch (InterruptedException e)
			{
				Logging.logger()
						.log(Level.FINE,
								Logging.getMessage("BasicRetrievalService.RetrievalInterrupted", task.getRetriever()
										.getName()), e);
			}
			catch (java.util.concurrent.CancellationException e)
			{
				Logging.logger().fine(
						Logging.getMessage("BasicRetrievalService.RetrievalCancelled", task.getRetriever().getName()));
			}
			finally
			{
				Thread.currentThread().setName(IDLE_THREAD_NAME_PREFIX);
			}
		}
	}

	public DownloaderRetrievalService()
	{
		Integer poolSize = Configuration.getIntegerValue(AVKey.RETRIEVAL_POOL_SIZE, DEFAULT_POOL_SIZE);

		// this.executor runs the retrievers, each in their own thread
		this.executor = new RetrievalExecutor(poolSize);

		// this.activeTasks holds the list of currently executing tasks (*not* those pending on the queue)
		this.activeTasks = new ConcurrentLinkedQueue<RetrievalTask>();
	}

	@Override
	public void shutdown(boolean immediately)
	{
		if (immediately)
			this.executor.shutdownNow();
		else
			this.executor.shutdown();

		this.activeTasks.clear();
	}

	/**
	 * @param retriever
	 *            the retriever to run
	 * @return a future object that can be used to query the request status of
	 *         cancel the request.
	 * @throws IllegalArgumentException
	 *             if <code>retrieer</code> is null or has no name
	 */
	@Override
	public RetrievalFuture runRetriever(Retriever retriever)
	{
		if (retriever == null)
		{
			String msg = Logging.getMessage("nullValue.RetrieverIsNull");
			Logging.logger().fine(msg);
			throw new IllegalArgumentException(msg);
		}
		if (retriever.getName() == null)
		{
			String message = Logging.getMessage("nullValue.RetrieverNameIsNull");
			Logging.logger().fine(message);
			throw new IllegalArgumentException(message);
		}

		// Add with secondary priority that removes most recently added requests first.
		return this.runRetriever(retriever, Long.MAX_VALUE - System.currentTimeMillis());
	}

	/**
	 * @param retriever
	 *            the retriever to run
	 * @param priority
	 *            the secondary priority of the retriever, or negative if it is
	 *            to be the primary priority
	 * @return a future object that can be used to query the request status of
	 *         cancel the request.
	 * @throws IllegalArgumentException
	 *             if <code>retriever</code> is null or has no name
	 */
	@Override
	public synchronized RetrievalFuture runRetriever(Retriever retriever, double priority)
	{
		if (retriever == null)
		{
			String message = Logging.getMessage("nullValue.RetrieverIsNull");
			Logging.logger().fine(message);
			throw new IllegalArgumentException(message);
		}

		if (retriever.getName() == null)
		{
			String message = Logging.getMessage("nullValue.RetrieverNameIsNull");
			Logging.logger().fine(message);
			throw new IllegalArgumentException(message);
		}

		RetrievalTask task = new RetrievalTask(retriever, priority);
		retriever.setSubmitTime(System.currentTimeMillis());

		// Do not queue duplicates.
		if (this.activeTasks.contains(task) || this.executor.getQueue().contains(task))
			return null;

		this.executor.execute(task);

		return task;
	}

	/**
	 * @param poolSize
	 *            the number of threads in the thread pool
	 * @throws IllegalArgumentException
	 *             if <code>poolSize</code> is non-positive
	 */
	@Override
	public void setRetrieverPoolSize(int poolSize)
	{
		if (poolSize < 1)
		{
			String message = Logging.getMessage("BasicRetrievalService.RetrieverPoolSizeIsLessThanOne");
			Logging.logger().fine(message);
			throw new IllegalArgumentException(message);
		}

		this.executor.setCorePoolSize(poolSize);
		this.executor.setMaximumPoolSize(poolSize);
	}

	@Override
	public int getRetrieverPoolSize()
	{
		return this.executor.getCorePoolSize();
	}

	private boolean hasRetrievers()
	{
		Thread[] threads = new Thread[Thread.activeCount()];
		int numThreads = Thread.enumerate(threads);
		for (int i = 0; i < numThreads; i++)
		{
			if (threads[i].getName().startsWith(RUNNING_THREAD_NAME_PREFIX))
				return true;
		}
		return false;
	}

	@Override
	public boolean hasActiveTasks()
	{
		return this.hasRetrievers();
	}

	@Override
	public boolean isAvailable()
	{
		return true;
	}

	@Override
	public int getNumRetrieversPending()
	{
		// Could use same method to determine active tasks as hasRetrievers() above, but this method only advisory.
		return this.activeTasks.size() + this.executor.getQueue().size();
	}

	/**
	 * @param retriever
	 *            the retriever to check
	 * @return <code>true</code> if the retriever is being run or pending
	 *         execution
	 * @throws IllegalArgumentException
	 *             if <code>retriever</code> is null
	 */
	@Override
	public boolean contains(Retriever retriever)
	{
		if (retriever == null)
		{
			String msg = Logging.getMessage("nullValue.RetrieverIsNull");
			Logging.logger().fine(msg);
			throw new IllegalArgumentException(msg);
		}
		RetrievalTask task = new RetrievalTask(retriever, 0d);
		return (this.activeTasks.contains(task) || this.executor.getQueue().contains(task));
	}

	public double getProgress()
	{
		int totalContentLength = 0;
		int totalBytesRead = 0;

		for (RetrievalTask task : this.activeTasks)
		{
			if (task.isDone())
				continue;

			Retriever retriever = task.getRetriever();
			try
			{
				double tcl = retriever.getContentLength();
				if (tcl > 0)
				{
					totalContentLength += tcl;
					totalBytesRead += retriever.getContentLengthRead();
				}
			}
			catch (Exception e)
			{
				Logging.logger().log(
						Level.FINE,
						Logging.getMessage("BasicRetrievalService.ExceptionRetrievingContentSizes",
								retriever.getName() != null ? retriever.getName() : ""), e);
			}
		}

		for (Runnable runnable : this.executor.getQueue())
		{
			RetrievalTask task = (RetrievalTask) runnable;

			Retriever retriever = task.getRetriever();
			try
			{
				double tcl = retriever.getContentLength();
				if (tcl > 0)
				{
					totalContentLength += tcl;
					totalBytesRead += retriever.getContentLengthRead();
				}
			}
			catch (Exception e)
			{
				String message =
						Logging.getMessage("BasicRetrievalService.ExceptionRetrievingContentSizes")
								+ (retriever.getName() != null ? retriever.getName() : "");
				Logging.logger().log(Level.FINE, message, e);
			}
		}

		// Compute an aggregated progress notification.

		double progress;

		if (totalContentLength < 1)
			progress = 0;
		else
			progress = Math.min(100.0, 100.0 * totalBytesRead / totalContentLength);

		return progress;
	}

    @Override
	public SSLExceptionListener getSSLExceptionListener()
    {
        return sslExceptionListener;
    }

    @Override
	public void setSSLExceptionListener(SSLExceptionListener sslExceptionListener)
    {
        this.sslExceptionListener = sslExceptionListener;
    }
}
