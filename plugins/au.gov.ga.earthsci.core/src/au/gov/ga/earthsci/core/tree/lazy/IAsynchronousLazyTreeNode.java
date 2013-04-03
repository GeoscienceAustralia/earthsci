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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

import au.gov.ga.earthsci.core.tree.ILazyTreeNode;
import au.gov.ga.earthsci.core.tree.ITreeNode;

/**
 * {@link ILazyTreeNode} extension that uses a {@link Job} to load the node's
 * children asynchronously. Implementors should delegate the
 * {@link ILazyTreeNode} methods to an local instance of
 * {@link AsynchronousLazyTreeNodeHelper}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IAsynchronousLazyTreeNode<E extends ITreeNode<E>> extends ILazyTreeNode<E>
{
	/**
	 * Perform the actual loading of lazy children on the calling thread. Notify
	 * the monitor of progress.
	 * 
	 * @param monitor
	 *            Monitor to notify of load progress and completion
	 * @return Status of the load
	 */
	IStatus doLoad(IProgressMonitor monitor);

	/**
	 * Return a tree node that represents the current loading state of this lazy
	 * node. Null can be returned if no node should be displayed during this
	 * node's loading, or if this node already has children loaded (eg from a
	 * cached retrieval).
	 * 
	 * @return A node that displays the current loading state of this lazy node
	 */
	E getLoadingNode();

	/**
	 * Return a tree node that represents the given loading error. Null can be
	 * returned if this node already has children loaded (eg from a cached
	 * retrieval).
	 * 
	 * @param error
	 *            Error to create the node for
	 * @return A node that displays the given error
	 */
	E getErrorNode(Throwable error);
}
