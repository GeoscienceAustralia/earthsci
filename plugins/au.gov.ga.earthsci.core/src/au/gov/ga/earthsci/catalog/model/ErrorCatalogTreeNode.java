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
package au.gov.ga.earthsci.catalog.model;

import java.net.URI;

import org.eclipse.core.runtime.content.IContentType;

/**
 * {@link ICatalogTreeNode} that represents an error node as a child of a node
 * that has failed to load its children lazily.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ErrorCatalogTreeNode extends AbstractCatalogTreeNode
{
	private final Throwable error;
	private boolean removeable = false;

	public ErrorCatalogTreeNode(Throwable error)
	{
		this(null, error);
	}
	
	public ErrorCatalogTreeNode(URI nodeURI, Throwable error)
	{
		super(nodeURI);
		this.error = error;
	}

	@Override
	public boolean isRemoveable()
	{
		return removeable;
	}

	public void setRemoveable(boolean removeable)
	{
		this.removeable = removeable;
	}

	@Override
	public boolean isLayerNode()
	{
		return false;
	}

	@Override
	public URI getLayerURI()
	{
		return null;
	}

	@Override
	public IContentType getLayerContentType()
	{
		return null;
	}

	@Override
	public String getName()
	{
		return error.getLocalizedMessage();
	}

	public Throwable getError()
	{
		return error;
	}
}
