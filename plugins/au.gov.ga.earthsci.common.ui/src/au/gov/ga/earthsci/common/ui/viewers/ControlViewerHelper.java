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
package au.gov.ga.earthsci.common.ui.viewers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Abstract implementation of the {@link IControlViewerHelper} interface.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * 
 * @param <T>
 *            {@link ControlEditor} type
 */
public abstract class ControlViewerHelper<T extends ControlEditor> implements IControlViewerHelper
{
	private IControlProvider controlProvider;
	private final Map<Item, ControlItem> controls = new HashMap<Item, ControlItem>();

	/**
	 * @return A new {@link ControlEditor} for editing an item in this Viewer
	 */
	public abstract T createEditor();

	/**
	 * Set the given editor's control and item.
	 * <p/>
	 * For a tree, this should call
	 * {@link TreeEditor#setEditor(Control, TreeItem)}.
	 * 
	 * @param editor
	 *            Editor to call
	 * @param control
	 *            Control used for editing
	 * @param item
	 *            Item being edited
	 */
	public abstract void setEditor(T editor, Control control, Item item);

	/**
	 * @return The control used to render this Viewer's items
	 */
	public abstract Composite getViewerControl();

	@Override
	public IControlProvider getControlProvider()
	{
		return controlProvider;
	}

	@Override
	public void setControlProvider(IControlProvider controlProvider)
	{
		this.controlProvider = controlProvider;
	}

	@Override
	public void associate(Object element, Item item)
	{
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
	public void disassociate(Item item)
	{
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

	@Override
	public Composite getControlForItem(Item item)
	{
		if (controlProvider != null && item != null)
		{
			ControlItem controlItem = controls.get(item);
			if (controlItem != null)
			{
				return controlItem.composite;
			}
		}
		return null;
	}

	/**
	 * Helper class which stores required objects for each tree item.
	 */
	private class ControlItem
	{
		public final Object element;
		public final TreeItem item;
		public final T editor;
		public final BoundedComposite composite;
		public final Control control;

		public ControlItem(Object element, TreeItem item)
		{
			this.element = element;
			this.item = item;
			this.editor = createEditor();
			Composite viewerControl = getViewerControl();
			this.composite = new BoundedComposite(viewerControl, SWT.NONE, this);
			this.composite.setBackground(viewerControl.getBackground());
			this.control =
					controlProvider == null ? null : controlProvider.getControl(composite, element, item, editor);
			setEditor(editor, composite, item);
		}

		public void dispose()
		{
			if (control != null)
			{
				controlProvider.disposeControl(control, element, item);
			}
			composite.dispose();
			editor.dispose();
		}
	}

	/**
	 * {@link Composite} subclass which provides the ability to prevent its
	 * bounds being set, storing the requested bounds instead.
	 */
	private class BoundedComposite extends Composite
	{
		private final ControlItem controlItem;

		public BoundedComposite(Composite parent, int style, ControlItem controlItem)
		{
			super(parent, style);
			this.controlItem = controlItem;
			setLayout(new FillLayout());
		}

		@Override
		public void setBounds(int x, int y, int width, int height)
		{
			if (controlProvider != null)
			{
				Rectangle bounds = new Rectangle(x, y, width, height);
				Rectangle overriddenBounds =
						controlProvider.overrideBounds(bounds, controlItem.control, controlItem.element,
								controlItem.item);
				bounds = overriddenBounds != null ? overriddenBounds : bounds;
				x = bounds.x;
				y = bounds.y;
				width = bounds.width;
				height = bounds.height;
			}
			super.setBounds(x, y, width, height);
		}
	}
}
