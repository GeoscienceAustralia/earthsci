package au.gov.ga.earthsci.application.tree;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;

import au.gov.ga.earthsci.core.tree.AbstractLazyTreeNode;
import au.gov.ga.earthsci.core.tree.ILazyTreeNode;
import au.gov.ga.earthsci.core.tree.LazyTreeJob;
import au.gov.ga.earthsci.core.util.ILabeled;

/**
 * An implementation of {@link ILazyTreeContentProvider} that provides content from
 * models based on a tree of {@link ILazyTreeNode}s
 * <p/>
 * Note that the root node of the tree <em>will not be displayed</em> in the tree.
 * <p/>
 * This class may be subclassed as required.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class LazyTreeNodeContentProvider implements ITreeContentProvider
{
	
	private static final ILazyTreeNode<String> DEFAULT_PENDING_NODE = new SimpleLazyTreeLeafNode(Messages.LazyTreeNodeContentProvider_LoadingMessage);

	private Object loadingNode = DEFAULT_PENDING_NODE;
	
	private TreeViewer viewer;
	
	public LazyTreeNodeContentProvider(TreeViewer viewer)
	{
		this.viewer = viewer;
	}

	@Override
	public void dispose()
	{
		// Do nothing
		
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{
		// Do nothing
	}

	@Override
	public Object[] getElements(Object inputElement)
	{
		// TODO: This means the root node will not be displayed
		return getChildren(inputElement);
	}

	/**
	 * Can be overridden by subclasses to provide support for non-{@link ILazyTreeNode} 
	 * instances that might be present in the model.
	 */
	@Override
	public Object[] getChildren(final Object parentElement)
	{
		if (!(parentElement instanceof ILazyTreeNode<?>))
		{
			return new Object[0];
		}
		
		ILazyTreeNode<?> parent = (ILazyTreeNode<?>)parentElement;
		return getChildrenFromLazyTreeNode(parent);
	}
	
	/**
	 * Load the children from the provided {@link ILazyTreeNode} if available, or trigger a load of it's children.
	 * <p/>
	 * If children are unavailable, a dummy object is returned as specified by {@link #setLoadingNode(Object)}
	 */
	protected Object[] getChildrenFromLazyTreeNode(final ILazyTreeNode<?> parent)
	{
		if (parent.isLoaded())
		{
			if (parent.hasError())
			{
				return new Object[] {new SimpleLazyTreeLeafNode(parent.getStatus().getMessage())};
			}
			return parent.getChildren();
		}
		
		LazyTreeJob job = parent.load();
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event)
			{
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run()
					{
						viewer.refresh(parent);
					}
				});
			}
		});
		
		return new Object[] {loadingNode};
	}

	@Override
	public Object getParent(Object element)
	{
		if (!(element instanceof ILazyTreeNode<?>))
		{
			return null;
		}
		
		return ((ILazyTreeNode<?>)element).getParent();
	}

	@Override
	public boolean hasChildren(Object element)
	{
		return ((ILazyTreeNode<?>)element).hasChildren();
	}

	/**
	 * Set the element to use as a placeholder when loading children from a {@link ILazyTreeNode}. 
	 */
	public void setLoadingNode(Object element)
	{
		loadingNode = element;
	}
	
	private static class SimpleLazyTreeLeafNode extends AbstractLazyTreeNode<String> implements ILabeled
	{

		private String name;
		
		public SimpleLazyTreeLeafNode(String name)
		{
			this.name = name;
		}

		@Override
		public String getName()
		{
			return name;
		}

		@Override
		public String getLabel()
		{
			return name;
		}
		
		@Override
		public String getLabelOrName()
		{
			return name;
		}
		
		@Override
		public boolean isLoaded() {return true; };
		
		@Override
		protected IStatus doLoad(IProgressMonitor monitor)
		{
			return Status.OK_STATUS;
		}
		
	}
	
}
