/*******************************************************************************
 * Copyright 2013 Geoscience Australia
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
package au.gov.ga.earthsci.core.tree.lazy;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import au.gov.ga.earthsci.core.tree.ILazyTreeNode;
import au.gov.ga.earthsci.core.tree.ILazyTreeNodeCallback;
import au.gov.ga.earthsci.core.tree.ITreeNode;

/**
 * Helper class for the {@link IAsynchronousLazyTreeNode}; uses a {@link Job} to
 * load the lazily loaded nodes.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class AsynchronousLazyTreeNodeHelper<E>
{
	private final IAsynchronousLazyTreeNode<E> node;
	private final AtomicBoolean loaded = new AtomicBoolean(false);
	private final AtomicBoolean loading = new AtomicBoolean(false);
	private Throwable error;

	public AsynchronousLazyTreeNodeHelper(IAsynchronousLazyTreeNode<E> node)
	{
		this.node = node;
	}

	public final void load(String jobName, final ILazyTreeNodeCallback callback)
	{
		if (!isLoaded() && loading.compareAndSet(false, true))
		{
			final Job loadJob = new Job(jobName)
			{
				@Override
				protected IStatus run(IProgressMonitor monitor)
				{
					return node.doLoad(monitor);
				}
			};

			loadJob.addJobChangeListener(new JobChangeAdapter()
			{
				@Override
				public void done(final IJobChangeEvent event)
				{
					setLoaded(event.getResult().getCode() != Status.CANCEL);
					error = event.getResult().getException();
					loading.set(false);
					callback.loaded();
				}
			});
			loadJob.schedule();
		}
	}

	/**
	 * @see ILazyTreeNode#isLoaded()
	 */
	public boolean isLoaded()
	{
		return loaded.get();
	}

	protected void setLoaded(boolean loaded)
	{
		node.firePropertyChange("loaded", this.loaded.getAndSet(loaded), loaded); //$NON-NLS-1$
	}

	/**
	 * @see ILazyTreeNode#getDisplayChildren()
	 */
	public ITreeNode<E>[] getDisplayChildren()
	{
		ITreeNode<E>[] children = node.getChildren();
		ITreeNode<E> firstNode = null;
		if (error != null)
		{
			firstNode = node.getErrorNode(error);
		}
		else if (loading.get())
		{
			firstNode = node.getLoadingNode();
		}
		if (firstNode != null)
		{
			@SuppressWarnings("unchecked")
			ITreeNode<E>[] newChildren = new ITreeNode[children.length + 1];
			newChildren[0] = firstNode;
			System.arraycopy(children, 0, newChildren, 1, children.length);
			children = newChildren;
		}
		return children;
	}
}
