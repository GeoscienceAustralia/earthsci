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
package au.gov.ga.earthsci.catalog;

import java.net.URI;
import java.net.URL;

import org.eclipse.core.runtime.content.IContentType;

import au.gov.ga.earthsci.common.util.ExceptionFormatter;

/**
 * {@link ICatalogTreeNode} that represents an error node as a child of a node
 * that has failed to load its children lazily.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ErrorCatalogTreeNode extends AbstractCatalogTreeNode
{
	/** The exception that caused the error */
	private final Throwable error;

	/** A localised, human readable message to associate with the error */
	private final String message;

	private boolean removeable = false;

	public ErrorCatalogTreeNode(Throwable error)
	{
		this(null, error);
	}

	public ErrorCatalogTreeNode(URI nodeURI, Throwable error)
	{
		this(nodeURI, null, error);
	}

	public ErrorCatalogTreeNode(URI nodeURI, String message, Throwable error)
	{
		super(nodeURI);
		this.error = error;
		this.message = message == null ? error.getLocalizedMessage() : message;
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
		return message;
	}

	public Throwable getError()
	{
		return error;
	}

	public String getMessage()
	{
		return message;
	}

	@Override
	public URL getInformationURL()
	{
		return null;
	}

	@Override
	public String getInformationString()
	{
		return ExceptionFormatter.toHTML(error);
	}

	@Override
	public URL getIconURL()
	{
		return Icons.ERROR;
	}
}
