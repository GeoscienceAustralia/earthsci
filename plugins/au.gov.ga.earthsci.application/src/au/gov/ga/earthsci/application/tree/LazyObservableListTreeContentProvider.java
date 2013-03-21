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
package au.gov.ga.earthsci.application.tree;

import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.databinding.viewers.TreeStructureAdvisor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.Viewer;

import au.gov.ga.earthsci.core.tree.ILazyTreeNode;
import au.gov.ga.earthsci.core.tree.ILazyTreeNodeCallback;

/**
 * {@link ObservableListTreeContentProvider} subclass that supports
 * {@link ILazyTreeNode}s. Loads the lazy tree node's children in the
 * {@link #getChildren(Object)} method.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class LazyObservableListTreeContentProvider extends ObservableListTreeContentProvider
{
	private ColumnViewer viewer;

	public LazyObservableListTreeContentProvider(IObservableFactory listFactory, TreeStructureAdvisor structureAdvisor)
	{
		super(listFactory, structureAdvisor);
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{
		super.inputChanged(viewer, oldInput, newInput);
		if (!(viewer instanceof ColumnViewer))
		{
			throw new IllegalArgumentException(getClass() + " only works with viewers of type " + ColumnViewer.class); //$NON-NLS-1$
		}
		this.viewer = (ColumnViewer) viewer;
	}

	@Override
	public Object[] getChildren(final Object parentElement)
	{
		if (!(parentElement instanceof ILazyTreeNode<?>))
		{
			return super.getChildren(parentElement);
		}
		final ILazyTreeNode<?> lazyTreeNode = (ILazyTreeNode<?>) parentElement;
		if (!lazyTreeNode.isLoaded())
		{
			lazyTreeNode.load(new ILazyTreeNodeCallback()
			{
				@Override
				public void loaded()
				{
					if (!viewer.getControl().isDisposed())
					{
						viewer.getControl().getDisplay().asyncExec(new Runnable()
						{
							@Override
							public void run()
							{
								viewer.refresh(lazyTreeNode);
							}
						});
					}
				}
			});
		}
		return lazyTreeNode.getDisplayChildren();
	}

	@Override
	public boolean hasChildren(Object element)
	{
		if (!(element instanceof ILazyTreeNode<?>))
		{
			return super.hasChildren(element);
		}
		ILazyTreeNode<?> lazyTreeNode = (ILazyTreeNode<?>) element;
		if (lazyTreeNode.isLoaded())
		{
			return super.hasChildren(element);
		}
		//assume an non-loaded lazy tree node has children so the user can expand it and attempt the load:
		return true;
	}
}
