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
package au.gov.ga.earthsci.common.ui.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ExpandEvent;
import org.eclipse.swt.events.ExpandListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * {@link SashForm} that contains two {@link ExpandBar}s with a sash between
 * them. Ensures that at least one of the bars is expanded at all times, such
 * that the SashForm is filled.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DoubleExpandSashForm extends SashForm
{
	private final ExpandBar expandBar1, expandBar2;
	private final ExpandItem expandItem1, expandItem2;
	private final ExpandListener expandListener1, expandListener2;

	private ExpandBar expandingBar;

	private int[] weights;

	public DoubleExpandSashForm(Composite parent, int style)
	{
		super(parent, style | SWT.VERTICAL | SWT.SMOOTH);

		expandBar1 = new ExpandBar(this, SWT.V_SCROLL);
		expandBar1.setBackground(getBackground());
		expandBar1.setSpacing(0);

		expandBar2 = new ExpandBar(this, SWT.V_SCROLL);
		expandBar2.setBackground(getBackground());
		expandBar2.setSpacing(0);

		expandItem1 = new ExpandItem(expandBar1, SWT.NONE);
		expandItem1.setExpanded(true);
		expandItem2 = new ExpandItem(expandBar2, SWT.NONE);
		expandItem2.setExpanded(true);

		expandListener1 = setupListeners(expandBar1, expandItem1);
		expandListener2 = setupListeners(expandBar2, expandItem2);

		addControlListener(new ControlAdapter()
		{
			@Override
			public void controlResized(ControlEvent e)
			{
				expandingBar = expandBar1;
				if (!expandItem1.getExpanded())
				{
					setWeights(weightsForOneCollapsed(true));
				}
				else if (!expandItem2.getExpanded())
				{
					setWeights(weightsForOneCollapsed(false));
				}
				expandingBar = null;
			}
		});
	}

	protected ExpandListener setupListeners(final ExpandBar expandBar, final ExpandItem expandItem)
	{
		final boolean lastItem = expandBar == expandBar2;

		expandBar.addControlListener(new ControlAdapter()
		{
			@Override
			public void controlResized(ControlEvent e)
			{
				int height = expandBar.getBounds().height - expandItem.getHeaderHeight() - expandBar.getSpacing() * 2;
				boolean expand = height > 0;
				if (expandingBar != expandBar && expandItem.getExpanded() != expand)
				{
					expandItem.setExpanded(expand);
				}
				if (expand)
				{
					expandItem.setHeight(height);
				}
			}
		});

		ExpandListener expandListener = new ExpandListener()
		{
			@Override
			public void itemExpanded(ExpandEvent e)
			{
				expand(true);
			}

			@Override
			public void itemCollapsed(ExpandEvent e)
			{
				expand(false);
			}

			private void expand(boolean expand)
			{
				try
				{
					expandingBar = expandBar;

					ExpandItem otherExpandItem = lastItem ? expandItem1 : expandItem2;

					int[] weights;
					if (expand && otherExpandItem.getExpanded())
					{
						if (DoubleExpandSashForm.this.weights != null)
						{
							weights = DoubleExpandSashForm.this.weights;
						}
						else
						{
							weights = new int[] { 1, 1 };
						}
					}
					else
					{
						weights = weightsForOneCollapsed(lastItem == expand);
					}

					if (weights[0] > 0 && weights[1] > 0)
					{
						setWeights(weights);
					}
				}
				finally
				{
					expandingBar = null;
				}
			}
		};
		expandBar.addExpandListener(expandListener);
		return expandListener;
	}

	private int[] weightsForOneCollapsed(boolean firstCollapsed)
	{
		int[] weights = new int[2];
		int availableHeight = getBounds().height - getSashWidth();
		if (firstCollapsed)
		{
			weights[0] = expandBar1.getSpacing() * 2 + expandItem1.getHeaderHeight();
			weights[1] = availableHeight - weights[0];
		}
		else
		{
			weights[1] = expandBar2.getSpacing() * 2 + expandItem2.getHeaderHeight();
			weights[0] = availableHeight - weights[1];
		}
		weights[1]--; //bugfix
		return weights;
	}

	@Override
	public void setWeights(int[] weights)
	{
		super.setWeights(weights);
		if (expandingBar == null)
		{
			this.weights = weights;
		}
	}

	public ExpandBar getExpandBar1()
	{
		return expandBar1;
	}

	public ExpandBar getExpandBar2()
	{
		return expandBar2;
	}

	public ExpandItem getExpandItem1()
	{
		return expandItem1;
	}

	public ExpandItem getExpandItem2()
	{
		return expandItem2;
	}

	public void setExpandItem1Expanded(boolean expand)
	{
		expandItem(expandItem1, expandListener1, expand);
	}

	public void setExpandItem2Expanded(boolean expand)
	{
		expandItem(expandItem2, expandListener2, expand);
	}

	private void expandItem(ExpandItem expandItem, ExpandListener expandListener, boolean expand)
	{
		if (expand != expandItem.getExpanded())
		{
			expandItem.setExpanded(expand);
			if (expand)
			{
				expandListener.itemExpanded(null);
			}
			else
			{
				expandListener.itemCollapsed(null);
			}
		}
	}

	public static void main(String[] args)
	{
		final Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());

		DoubleExpandSashForm form = new DoubleExpandSashForm(shell, SWT.NONE);
		form.setLayout(new FillLayout());


		ExpandBar expandBar1 = form.getExpandBar1();
		Composite child1 = new Composite(expandBar1, SWT.NONE);
		child1.setLayout(new FillLayout());
		new Label(child1, SWT.NONE).setText("Label in pane 1"); //$NON-NLS-1$
		form.getExpandItem1().setControl(child1);

		ExpandBar expandBar2 = form.getExpandBar2();
		Composite child2 = new Composite(expandBar2, SWT.NONE);
		child2.setLayout(new FillLayout());
		new Button(child2, SWT.PUSH).setText("Button in pane2"); //$NON-NLS-1$
		form.getExpandItem2().setControl(child2);


		form.setWeights(new int[] { 70, 30 });
		shell.open();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch())
			{
				display.sleep();
			}
		}
		display.dispose();
	}
}
