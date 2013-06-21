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
package au.gov.ga.earthsci.catalog.ui;

import java.io.File;
import java.net.URI;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.catalog.ICatalogModel;
import au.gov.ga.earthsci.catalog.ICatalogTreeNode;
import au.gov.ga.earthsci.catalog.IntentCatalogLoader;
import au.gov.ga.earthsci.catalog.LoadingCatalogTreeNode;
import au.gov.ga.earthsci.core.tree.ITreeNode;

/**
 * A {@link ViewerDropAdapter} that provides drag-and-drop support for the
 * catalog browser part.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
class CatalogTreeDropAdapter extends ViewerDropAdapter
{
	private static final Logger logger = LoggerFactory.getLogger(CatalogTreeDropAdapter.class);

	private final ICatalogModel model;
	private final IEclipseContext context;

	protected CatalogTreeDropAdapter(Viewer viewer, ICatalogModel model, IEclipseContext context)
	{
		super(viewer);
		this.model = model;
		this.context = context;
	}

	@Override
	public boolean validateDrop(Object target, int operation, TransferData transferType)
	{
		// Only allow drops at the top level
		if (target != null
				&& (((ICatalogTreeNode) target).getParent() != model.getRoot() || getCurrentLocation() == LOCATION_ON))
		{
			return false;
		}
		return FileTransfer.getInstance().isSupportedType(transferType);
	}

	@Override
	public boolean performDrop(Object data)
	{
		logger.trace("Received drop: {}", data); //$NON-NLS-1$

		if (data == null)
		{
			return false;
		}

		if (isFileDrop())
		{
			return doFileDrop(data);
		}

		return false;
	}

	private boolean isFileDrop()
	{
		return FileTransfer.getInstance().isSupportedType(getCurrentEvent().currentDataType);
	}

	private boolean doFileDrop(Object data)
	{
		logger.trace("Processing drop as file data", data); //$NON-NLS-1$

		String[] filenames = (String[]) data;

		int index = getDropIndex();
		for (String filename : filenames)
		{
			File file = new File(filename);
			URI uri = file.toURI();
			LoadingCatalogTreeNode loadingNode = new LoadingCatalogTreeNode(uri);
			model.addTopLevelCatalog(index++, loadingNode);
			IntentCatalogLoader.load(uri, loadingNode, context);
		}
		return true;
	}

	private int getDropIndex()
	{
		ITreeNode<?> target = (ITreeNode<?>) getCurrentTarget();
		if (target == null)
		{
			return model.getRoot().getChildCount();
		}

		int location = getCurrentLocation();
		if (location == LOCATION_NONE)
		{
			return model.getRoot().getChildCount();
		}

		return location == LOCATION_BEFORE ? target.index() : target.index() + 1;
	}

	@Override
	public void dropAccept(DropTargetEvent event)
	{
		event.detail = DND.DROP_COPY;
		super.dropAccept(event);
	}

	@Override
	public void dragOperationChanged(DropTargetEvent event)
	{
		event.detail = DND.DROP_COPY;
		super.dragOperationChanged(event);
	}

	@Override
	public void dragEnter(DropTargetEvent event)
	{
		// Convert all operations to copy ops
		event.detail = DND.DROP_COPY;
		super.dragEnter(event);
	}
}
