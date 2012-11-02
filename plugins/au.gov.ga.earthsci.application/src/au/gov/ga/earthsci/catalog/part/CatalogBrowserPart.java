package au.gov.ga.earthsci.catalog.part;

import java.io.File;

import javax.annotation.PostConstruct;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import au.gov.ga.earthsci.core.model.catalog.ICatalogModel;
import au.gov.ga.earthsci.core.model.catalog.ICatalogTreeNode;
import au.gov.ga.earthsci.core.model.catalog.dataset.DatasetReader;
import au.gov.ga.earthsci.core.tree.AbstractLazyTreeNode;
import au.gov.ga.earthsci.core.tree.ILazyTreeNode;
import au.gov.ga.earthsci.core.tree.LazyTreeJob;

/**
 * A part that renders a tree-view of the current {@link ICatalogModel} and allows
 * the user to browse and interact with elements in the model.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class CatalogBrowserPart
{

	private TreeViewer viewer;
	
	// TODO: Inject this!
	private ICatalogModel model;
	
	@PostConstruct
	public void init(Composite parent, MPart part)
	{
		
		initTree();
		initViewer(parent);
		
	}
	
	@Deprecated
	private void initTree()
	{
		model = new ICatalogModel()
		{
			ICatalogTreeNode root;
			
			@Override
			public ICatalogTreeNode getRoot()
			{
				if (root != null)
				{
					return root;
				}
				
				File f = new File("V:/projects/data/12-6205 - IGC Common Earth Model/Viewer - Full version/data/Dataset/dataset.xml");
				try
				{
					root = DatasetReader.read(f, f.toURL());
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				
				return root;
			}
		};
	}

	private void initViewer(Composite parent)
	{
		viewer = new TreeViewer(parent, SWT.VIRTUAL);
		viewer.setContentProvider(new CatalogContentProvider(viewer));
		viewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element)
			{
				return ((ILazyTreeNode<?>)element).getName();
			}
		});
		viewer.setSorter(null);
		viewer.setInput(model);
		viewer.getTree().setItemCount(1);
	}
	
	@Deprecated
	private static class DummyNode extends AbstractLazyTreeNode<ICatalogTreeNode> implements ICatalogTreeNode
	{

		@Override
		public ICatalogTreeNode getValue()
		{
			return null;
		}

		@Override
		public String getName()
		{
			return "Node " + depth() + "." + index();
		}

		@Override
		protected IStatus doLoad(IProgressMonitor monitor)
		{
			monitor.beginTask("Loading children", 5);
			for (int i = 0; i < 5; i++)
			{
				monitor.subTask("Child " + i);
				add(new DummyNode());
				try
				{
					Thread.sleep(1000);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				monitor.worked(1);
			}
			monitor.done();
			return Status.OK_STATUS;
		}

		@Override
		public boolean isRemoveable()
		{
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isReloadable()
		{
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public LazyTreeJob reload()
		{
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
}
