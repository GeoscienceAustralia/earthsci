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
package au.gov.ga.earthsci.viewers;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;

import au.gov.ga.earthsci.application.ImageRegistry;

/**
 * {@link CheckboxTreeViewer} that implements {@link IControlViewer}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ControlCheckboxTreeViewer extends CheckboxTreeViewer implements IControlViewer
{
	private final ControlTreeViewerHelper helper = new ControlTreeViewerHelper(this);

	public ControlCheckboxTreeViewer(Composite parent, int style)
	{
		super(parent, style);

		// XXX: This is a workaround to reduce the flicker that occurs when tree item controls are redrawn
		getTree().setBackgroundImage(ImageRegistry.getInstance().get(ImageRegistry.ICON_TRANSPARENT));
	}

	public ControlCheckboxTreeViewer(Composite parent)
	{
		super(parent);
		getTree().setBackgroundImage(ImageRegistry.getInstance().get(ImageRegistry.ICON_TRANSPARENT));
	}

	public ControlCheckboxTreeViewer(Tree tree)
	{
		super(tree);
		getTree().setBackgroundImage(ImageRegistry.getInstance().get(ImageRegistry.ICON_TRANSPARENT));
	}

	@Override
	public IControlProvider getControlProvider()
	{
		return helper.getControlProvider();
	}

	@Override
	public void setControlProvider(IControlProvider controlProvider)
	{
		helper.setControlProvider(controlProvider);
	}

	@Override
	protected void associate(Object element, Item item)
	{
		super.associate(element, item);
		helper.associate(element, item);
	}

	@Override
	protected void disassociate(Item item)
	{
		super.disassociate(item);
		helper.disassociate(item);
	}

	@Override
	public Composite getControlForItem(Item item)
	{
		return helper.getControlForItem(item);
	}
}
