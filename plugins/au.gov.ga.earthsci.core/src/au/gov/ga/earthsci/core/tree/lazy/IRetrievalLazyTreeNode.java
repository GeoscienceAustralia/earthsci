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

import java.net.URL;

import au.gov.ga.earthsci.core.retrieve.IRetrievalData;
import au.gov.ga.earthsci.core.retrieve.IRetrievalService;
import au.gov.ga.earthsci.core.tree.ILazyTreeNode;
import au.gov.ga.earthsci.core.tree.ITreeNode;

/**
 * {@link ILazyTreeNode} extension that uses the {@link IRetrievalService} to
 * retrieve the node's data. Implementors should delegate the
 * {@link ILazyTreeNode} methods to an local instance of
 * {@link RetrievalLazyTreeNodeHelper}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface IRetrievalLazyTreeNode<E> extends ILazyTreeNode<E>
{
	/**
	 * @return URL to retrieve the lazy data from
	 */
	URL getRetrievalURL();

	/**
	 * Handle the retrieved lazy loaded data, and return the loaded children.
	 * <p/>
	 * Note: it is possible that this method can be called twice, once for a
	 * cached retrieval and once for an updated retrieval.
	 * <p/>
	 * An exception should be thrown if the data is in an unrecognized format.
	 * 
	 * @param data
	 *            Retrieved data
	 * @param url
	 *            URL the data was retrieved from
	 * @throws Exception
	 *             If the data could not be read
	 * @return Children nodes loaded from the retrieved data
	 */
	ITreeNode<E>[] handleRetrieval(IRetrievalData data, URL url) throws Exception;

	/**
	 * Return a tree node that represents the current loading state of this lazy
	 * node. Null can be returned if no node should be displayed during this
	 * node's loading, or if this node already has children loaded (eg from a
	 * cached retrieval).
	 * 
	 * @return A node that displays the current loading state of this lazy node
	 */
	ITreeNode<E> getLoadingNode();

	/**
	 * Return a tree node that represents the given loading error. Null can be
	 * returned if this node already has children loaded (eg from a cached
	 * retrieval).
	 * 
	 * @param error
	 *            Error to create the node for
	 * @return A node that displays the given error
	 */
	ITreeNode<E> getErrorNode(Throwable error);
}
