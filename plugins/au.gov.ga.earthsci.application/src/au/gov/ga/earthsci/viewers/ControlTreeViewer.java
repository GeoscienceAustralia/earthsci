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

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;

/**
 * A {@link TreeViewer} that implements {@link IControlViewer}
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class ControlTreeViewer extends TreeViewer implements IControlViewer
{
	private final ControlTreeViewerHelper helper = new ControlTreeViewerHelper(this);
	
	public ControlTreeViewer(Composite parent, int style)
	{
		super(parent, style);
	}

	public ControlTreeViewer(Composite parent)
	{
		super(parent);
	}

	public ControlTreeViewer(Tree tree)
	{
		super(tree);
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
	public Composite getControlForItem(Item item)
	{
		return helper.getControlForItem(item);
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
}
