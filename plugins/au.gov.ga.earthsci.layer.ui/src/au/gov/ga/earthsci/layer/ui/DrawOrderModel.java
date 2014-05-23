/*******************************************************************************
 * Copyright 2014 Geoscience Australia
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
package au.gov.ga.earthsci.layer.ui;

import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import au.gov.ga.earthsci.common.util.AbstractPropertyChangeBean;
import au.gov.ga.earthsci.common.util.IPropertyChangeBean;
import au.gov.ga.earthsci.layer.DrawOrder;
import au.gov.ga.earthsci.layer.tree.ILayerNode;
import au.gov.ga.earthsci.layer.tree.ILayerTreeNode;

/**
 * Model that represents the draw order of the layers. Automatically updates
 * when the layers change, using {@link PropertyChangeListener}s.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DrawOrderModel
{
	private ILayerTreeNode input;
	private final RootDrawOrderModelElement root = new RootDrawOrderModelElement();
	private final Map<Integer, DrawOrderDrawOrderModelElement> drawOrderElements =
			new HashMap<Integer, DrawOrderDrawOrderModelElement>();
	private final DrawOrderDrawOrderModelElement nullDrawOrderElement = new DrawOrderDrawOrderModelElement(root, null);

	private PropertyChangeListener listener = new PropertyChangeListener()
	{
		@Override
		public void propertyChange(PropertyChangeEvent evt)
		{
			rebuildTree();
		}
	};

	public RootDrawOrderModelElement getRoot()
	{
		return root;
	}

	public synchronized void setInput(ILayerTreeNode input)
	{
		if (this.input != null)
		{
			this.input.removePropertyChangeListener("layers", listener); //$NON-NLS-1$
		}

		this.input = input;

		if (input != null)
		{
			input.addPropertyChangeListener("layers", listener); //$NON-NLS-1$
		}

		rebuildTree();
	}

	public void dispose()
	{
		setInput(null);
	}

	protected synchronized void rebuildTree()
	{
		//first clear out the child lists
		root.getChildren().clear();
		for (DrawOrderDrawOrderModelElement drawOrderElement : drawOrderElements.values())
		{
			drawOrderElement.getChildren().clear();
		}

		//if no input, then don't add anything
		if (input == null)
		{
			return;
		}

		//sorted set of draw orders
		SortedSet<DrawOrderDrawOrderModelElement> drawOrders = new TreeSet<DrawOrderDrawOrderModelElement>();

		//ensure all pre-defined draw orders are added as elements
		for (DrawOrder drawOrderValue : DrawOrder.values())
		{
			DrawOrderDrawOrderModelElement element = getDrawOrderElement(drawOrderValue.value);
			drawOrders.add(element);
		}

		//add the layers to their draw order element
		LayerList layers = input.getLayers();
		for (Layer layer : layers)
		{
			if (layer instanceof ILayerTreeNode)
			{
				Integer value = null;
				if (layer instanceof ILayerNode)
				{
					value = ((ILayerNode) layer).getDrawOrder();
				}
				DrawOrderDrawOrderModelElement parent = getDrawOrderElement(value);
				parent.getChildren().add(new LayerDrawOrderModelElement(parent, (ILayerTreeNode) layer));
			}
		}

		//add all used draw orders to the root
		for (DrawOrderDrawOrderModelElement element : drawOrders)
		{
			root.getChildren().add(element);
		}
		if (!nullDrawOrderElement.getChildren().isEmpty())
		{
			root.getChildren().add(nullDrawOrderElement);
		}

		//fire property changes
		root.fireChildrenPropertyChange();
		for (IDrawOrderModelElement child : root.getChildren())
		{
			child.fireChildrenPropertyChange();
		}
	}

	protected DrawOrderDrawOrderModelElement getDrawOrderElement(Integer value)
	{
		if (value == null)
		{
			return nullDrawOrderElement;
		}
		DrawOrderDrawOrderModelElement element = drawOrderElements.get(value);
		if (element == null)
		{
			element = new DrawOrderDrawOrderModelElement(root, value);
			drawOrderElements.put(value, element);
		}
		return element;
	}

	/**
	 * Node in the draw order model tree hierarchy.
	 */
	public interface IDrawOrderModelElement extends IPropertyChangeBean
	{
		/**
		 * @return Parent of this node, <code>null</code> if root
		 */
		IDrawOrderModelElement getParent();

		/**
		 * @return List of this node's children
		 */
		List<IDrawOrderModelElement> getChildren();

		/**
		 * Call {@link #firePropertyChange(String, Object, Object)} with the
		 * "children" property
		 */
		void fireChildrenPropertyChange();
	}

	/**
	 * Abstract implementation of {@link IDrawOrderModelElement}.
	 */
	public abstract class AbstractDrawOrderModelElement extends AbstractPropertyChangeBean implements
			IDrawOrderModelElement
	{
		public final IDrawOrderModelElement parent;
		public final List<IDrawOrderModelElement> children = new ArrayList<IDrawOrderModelElement>();

		public AbstractDrawOrderModelElement(IDrawOrderModelElement parent)
		{
			this.parent = parent;
		}

		@Override
		public IDrawOrderModelElement getParent()
		{
			return parent;
		}

		@Override
		public List<IDrawOrderModelElement> getChildren()
		{
			return children;
		}

		@Override
		public void fireChildrenPropertyChange()
		{
			firePropertyChange("children", null, getChildren()); //$NON-NLS-1$
		}
	}

	/**
	 * {@link IDrawOrderModelElement} for the root node.
	 */
	public class RootDrawOrderModelElement extends AbstractDrawOrderModelElement
	{
		public RootDrawOrderModelElement()
		{
			super(null);
		}
	}

	/**
	 * {@link IDrawOrderModelElement} for the draw order nodes.
	 */
	public class DrawOrderDrawOrderModelElement extends AbstractDrawOrderModelElement implements
			Comparable<DrawOrderDrawOrderModelElement>
	{
		public final Integer drawOrder;

		public DrawOrderDrawOrderModelElement(RootDrawOrderModelElement root, Integer drawOrder)
		{
			super(root);
			this.drawOrder = drawOrder;
		}

		@Override
		public int compareTo(DrawOrderDrawOrderModelElement o)
		{
			if (drawOrder == null || o.drawOrder == null)
			{
				return 0;
			}
			return drawOrder.compareTo(o.drawOrder);
		}
	}

	/**
	 * {@link IDrawOrderModelElement} for the layer leaf nodes.
	 */
	public class LayerDrawOrderModelElement extends AbstractDrawOrderModelElement
	{
		public final ILayerTreeNode node;

		public LayerDrawOrderModelElement(DrawOrderDrawOrderModelElement parent, ILayerTreeNode node)
		{
			super(parent);
			this.node = node;
		}

		@Override
		public DrawOrderDrawOrderModelElement getParent()
		{
			return (DrawOrderDrawOrderModelElement) super.getParent();
		}
	}
}
