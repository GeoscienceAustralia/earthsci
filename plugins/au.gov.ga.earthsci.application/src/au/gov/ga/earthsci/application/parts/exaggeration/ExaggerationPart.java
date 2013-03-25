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
package au.gov.ga.earthsci.application.parts.exaggeration;

import javax.inject.Inject;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Scale;

/**
 * Part that allows control of the globe's vertical exaggeration.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ExaggerationPart
{
	private final static int SCALE_HEIGHT = 22;
	private final static int TICK_HEIGHT = 5;
	private final static int SCALE_MARGIN = 14;
	private final static int INCREMENTS_PER_POWER = 1000;

	//min and max must be powers of 10:
	private final static double SCALE_MIN = 0.1;
	private final static double SCALE_MAX = 100;
	private final static double SCALE_LOG_MIN = Math.log10(SCALE_MIN);
	private final static double SCALE_LOG_MAX = Math.log10(SCALE_MAX);

	@Inject
	public void init(Composite parent)
	{
		/*Composite child = new Composite(parent, SWT.NONE);
		child.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_CYAN));*/

		LogScale logScale = new LogScale(parent, SWT.NONE);
		logScale.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_CYAN));
	}

	private class LogScale extends Composite
	{
		private final Scale scale;

		public LogScale(Composite parent, int style)
		{
			super(parent, style);
			setLayout(new GridLayout());

			final Composite scaleWrapper = new Composite(this, SWT.NONE);
			GridData gridData = new GridData();
			gridData.heightHint = SCALE_HEIGHT;
			gridData.grabExcessHorizontalSpace = true;
			gridData.horizontalAlignment = SWT.FILL;
			scaleWrapper.setLayoutData(gridData);

			scale = new Scale(scaleWrapper, SWT.HORIZONTAL);
			scaleWrapper.addControlListener(new ControlAdapter()
			{
				@Override
				public void controlResized(ControlEvent e)
				{
					scale.setSize(scale.computeSize(scaleWrapper.getSize().x, SWT.DEFAULT));
					scale.setLocation(0, (scaleWrapper.getSize().y - scale.getSize().y) / 2);
				}
			});
			scale.setMinimum(0);
			scale.setMaximum((int) Math.round(SCALE_LOG_MAX - SCALE_LOG_MIN) * INCREMENTS_PER_POWER);
			setValue(1);
			
			scale.addSelectionListener(new SelectionAdapter()
			{
				@Override
				public void widgetSelected(SelectionEvent e)
				{
					System.out.println(getValue());
				}
			});

			LogTicks ticks = new LogTicks(this, SWT.NONE);
			gridData = new GridData();
			gridData.grabExcessHorizontalSpace = true;
			gridData.horizontalAlignment = SWT.FILL;
			ticks.setLayoutData(gridData);
		}

		public double getValue()
		{
			if (scale.getSelection() == 0)
			{
				return 0;
			}

			double log10 = scale.getSelection() / (double) INCREMENTS_PER_POWER + SCALE_LOG_MIN;
			return Math.pow(10, log10);
		}

		public void setValue(double value)
		{
			if (value <= 0)
			{
				scale.setSelection(0);
				return;
			}

			double log10 = Math.log10(value);
			int selection = (int) Math.round((log10 - SCALE_LOG_MIN) * INCREMENTS_PER_POWER);
			scale.setSelection(selection);
		}
	}

	private class LogTicks extends Canvas
	{
		private int height = TICK_HEIGHT;

		public LogTicks(Composite parent, int style)
		{
			super(parent, style);

			addDisposeListener(new DisposeListener()
			{
				@Override
				public void widgetDisposed(DisposeEvent e)
				{
					LogTicks.this.widgetDisposed(e);
				}
			});

			addPaintListener(new PaintListener()
			{
				@Override
				public void paintControl(PaintEvent e)
				{
					LogTicks.this.paintControl(e);
				}
			});
		}

		protected void paintControl(PaintEvent e)
		{
			GC gc = e.gc;

			double value = SCALE_MIN;
			while (value <= SCALE_MAX)
			{
				double log10 = Math.log10(value);
				double percent = (log10 - SCALE_LOG_MIN) / (SCALE_LOG_MAX - SCALE_LOG_MIN);
				double increment = Math.pow(10, Math.floor(log10 + Double.MIN_VALUE));
				long count = Math.round(value / increment);

				int x = (int) Math.round(percent * (getSize().x + 1 - SCALE_MARGIN * 2) + SCALE_MARGIN - 1);
				gc.drawLine(x, 0, x, TICK_HEIGHT);

				if (count == 1)
				{
					String label = Long.toString(Math.round(value));
					Point labelSize = gc.textExtent(label);
					gc.drawString(label, x - labelSize.x / 2, TICK_HEIGHT);

					height = TICK_HEIGHT + labelSize.y;
				}

				value = (count + 1) * increment;
			}
		}

		protected void widgetDisposed(DisposeEvent e)
		{
		}

		@Override
		public Point computeSize(int wHint, int hHint, boolean changed)
		{
			return super.computeSize(wHint, height, changed);
		}
	}
}
