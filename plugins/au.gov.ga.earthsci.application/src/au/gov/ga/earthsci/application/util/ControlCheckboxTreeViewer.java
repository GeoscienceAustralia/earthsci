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
package au.gov.ga.earthsci.application.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * {@link CheckboxTreeViewer} that supports an {@link IControlProvider}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ControlCheckboxTreeViewer extends CheckboxTreeViewer
{
	private IControlProvider controlProvider;
	private Map<Item, ControlItem> controls = new HashMap<Item, ControlItem>();

	public ControlCheckboxTreeViewer(Composite parent, int style)
	{
		super(parent, style);
	}

	public ControlCheckboxTreeViewer(Composite parent)
	{
		super(parent);
	}

	public ControlCheckboxTreeViewer(Tree tree)
	{
		super(tree);
	}

	/**
	 * @return The {@link IControlProvider} associated with this viewer.
	 */
	public IControlProvider getControlProvider()
	{
		return controlProvider;
	}

	/**
	 * Set the {@link IControlProvider} used by this viewer to create custom
	 * controls for items.
	 * 
	 * @param controlProvider
	 */
	public void setControlProvider(IControlProvider controlProvider)
	{
		this.controlProvider = controlProvider;
	}

	@Override
	protected void associate(Object element, Item item)
	{
		super.associate(element, item);

		//create an associate a control with the item/element
		if (controlProvider != null && item instanceof TreeItem)
		{
			TreeItem treeItem = (TreeItem) item;
			ControlItem controlItem = controls.get(item);

			if (controlItem == null)
			{
				setupControl(element, treeItem);
			}
			else
			{
				boolean reuseControl =
						controlProvider.updateControl(controlItem.control, element, item, controlItem.editor);
				if (reuseControl)
				{
					//ensure the control is layout correctly
					controlItem.editor.layout();
				}
				else
				{
					//don't reuse the old control; pack it up and create a new one
					packupControl(item);
					setupControl(element, treeItem);
				}
			}
		}
		else
		{
			packupControl(item);
		}
	}

	@Override
	protected void disassociate(Item item)
	{
		super.disassociate(item);
		packupControl(item);
	}

	private void setupControl(Object element, TreeItem item)
	{
		controls.put(item, new ControlItem(element, item));
	}

	private void packupControl(Item item)
	{
		ControlItem controlItem = controls.remove(item);
		if (controlItem != null)
		{
			controlItem.dispose();
		}
	}

	public void setControlVisibleForItem(Item item, boolean visible)
	{
		if (controlProvider != null && item != null)
		{
			ControlItem controlItem = controls.get(item);
			if (controlItem != null)
			{
				controlItem.composite.setVisible(visible);
				if (visible)
				{
					controlItem.editor.layout();
				}
			}
		}
	}

	/**
	 * Helper class which stores required objects for each tree item.
	 */
	private class ControlItem
	{
		public final Object element;
		public final TreeItem item;
		public final CustomTreeEditor editor;
		public final BoundedComposite composite;
		public final Control control;

		public ControlItem(Object element, TreeItem item)
		{
			Tree tree = getTree();
			this.element = element;
			this.item = item;
			this.editor = new CustomTreeEditor(tree, this);
			this.composite = new BoundedComposite(tree, SWT.NONE);
			this.composite.setBackground(tree.getBackground());
			this.control =
					controlProvider == null ? null : controlProvider.getControl(composite, element, item, editor);
			editor.setEditor(composite, item);
		}

		public void dispose()
		{
			if (control != null)
			{
				control.dispose();
			}
			composite.dispose();
			editor.dispose();
		}
	}

	/**
	 * Custom {@link TreeEditor} subclass which provides the ability to override
	 * the bounds of the editor control.
	 */
	private class CustomTreeEditor extends TreeEditor
	{
		private final ControlItem controlItem;

		public CustomTreeEditor(Tree tree, ControlItem controlItem)
		{
			super(tree);
			this.controlItem = controlItem;
		}

		@Override
		public void layout()
		{
			if (controlProvider != null)
			{
				//prevent the composite's bounds from being set
				controlItem.composite.ignoreBoundsSet = true;
				super.layout();
				controlItem.composite.ignoreBoundsSet = false;

				//if the composite's bounds were attempted to be set
				if (controlItem.composite.ignoredBounds != null)
				{
					//ask the control provider if it wants to override the bounds
					Rectangle bounds =
							controlProvider.overrideBounds(controlItem.composite.ignoredBounds, controlItem.control,
									controlItem.element, controlItem.item);
					bounds = bounds == null ? controlItem.composite.ignoredBounds : bounds;
					controlItem.composite.ignoredBounds = null;
					//now finally set the bounds
					controlItem.composite.setBounds(bounds);
				}
			}
			else
			{
				super.layout();
			}
		}
	}

	/**
	 * {@link Composite} subclass which provides the ability to prevent its
	 * bounds being set, storing the requested bounds instead.
	 */
	private static class BoundedComposite extends Composite
	{
		public boolean ignoreBoundsSet = false;
		public Rectangle ignoredBounds;

		public BoundedComposite(Composite parent, int style)
		{
			super(parent, style);
			setLayout(new FillLayout());
		}

		@Override
		public void setBounds(int x, int y, int width, int height)
		{
			if (ignoreBoundsSet)
			{
				ignoredBounds = new Rectangle(x, y, width, height);
			}
			else
			{
				super.setBounds(x, y, width, height);
			}
		}
	}
}
