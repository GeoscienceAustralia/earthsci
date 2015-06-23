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
package au.gov.ga.earthsci.application.parts.globe;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;

import au.gov.ga.earthsci.worldwind.common.exaggeration.VerticalExaggerationListener;
import au.gov.ga.earthsci.worldwind.common.exaggeration.VerticalExaggerationService;

/**
 * Tool control used to change globe exaggeration.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class GlobeExaggerationToolControl implements VerticalExaggerationListener
{
	private final static double SCALE_MIN = 0.1; //must be a power of 10
	private final static double SCALE_MAX = 100; //must be a power of 10
	private final static double SCALE_LOG_MIN = Math.log10(SCALE_MIN);
	private final static double SCALE_LOG_MAX = Math.log10(SCALE_MAX);
	private final static int INCREMENTS_PER_POWER = 1000;

	private final static int SCALE_WIDTH = 120; //width of the scale control
	private final static int SCALE_HEIGHT = 21; //height of the scale control (to fit in the toolbar)
	private final static int TICK_HEIGHT = 3; //height of the ticks
	private final static int SCALE_MARGIN = 14; //margin on the left/right of the scale
	private final static int GRAB_WIDTH = 8; //width of the scale position indicator

	private final static int KEY_DELAY = 1500; //ms

	private StyledText scaleText;
	private Scale scale;
	private Color tickForeground;
	private String keyString = ""; //$NON-NLS-1$
	private long lastKeyTime;

	@PostConstruct
	public void createControls(Composite parent, IEclipseContext context)
	{
		VerticalExaggerationService.INSTANCE.addListener(this);
		parent.setBackgroundMode(SWT.INHERIT_FORCE);
		RowLayout layout = new RowLayout(SWT.HORIZONTAL);
		layout.wrap = false;
		layout.spacing = layout.marginBottom = layout.marginTop = layout.marginLeft = layout.marginRight = 0;
		parent.setLayout(layout);

		Composite labelParent = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 4;
		labelParent.setLayout(gridLayout);
		labelParent.setSize(labelParent.computeSize(SWT.DEFAULT, SCALE_HEIGHT));


		scaleText = new StyledText(labelParent, SWT.NONE);
		scaleText.setText("1.000x");

		ScaleEditListener clickListener = new ScaleEditListener();
		scaleText.addFocusListener(clickListener);
		scaleText.addKeyListener(clickListener);
		scaleText.addVerifyKeyListener(clickListener);
		scaleText.setSize(50, scaleText.computeSize(SCALE_WIDTH, SWT.DEFAULT).y);

		Composite child = new Composite(parent, SWT.NONE);
		child.setSize(child.computeSize(SCALE_WIDTH, SWT.DEFAULT));

		scale = new Scale(child, SWT.HORIZONTAL);
		Point size = scale.computeSize(SCALE_WIDTH, SWT.DEFAULT);
		scale.setSize(size);
		scale.setMinimum(exaggerationToScale(0));
		scale.setMaximum(exaggerationToScale(SCALE_MAX));
		scale.setLocation(0, (SCALE_HEIGHT - size.y) / 2);
		scale.setToolTipText(Messages.GlobeExaggerationToolControl_ToolTip0);
		scale.setIncrement(INCREMENTS_PER_POWER / 100);

		scale.setSelection(exaggerationToScale(VerticalExaggerationService.INSTANCE.get()));
		updateSelection(false);

		tickForeground = Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW);
		scale.addPaintListener(new PaintListener()
		{
			@Override
			public void paintControl(PaintEvent e)
			{
				GlobeExaggerationToolControl.this.paintControl(e);
			}
		});

		scale.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				updateSelection(true);
			}
		});

		scale.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				handleKey(e);
			}
		});
	}

	@PreDestroy
	public void preDestroy()
	{
		VerticalExaggerationService.INSTANCE.removeListener(this);
	}

	private double scaleToExaggeration(int value)
	{
		if (value <= 0)
		{
			return 0;
		}

		double exponent = (value / (double) INCREMENTS_PER_POWER) + SCALE_LOG_MIN;
		return Math.pow(10, exponent);
	}

	private int exaggerationToScale(double value)
	{
		if (value <= 0)
		{
			return 0;
		}


		double log10 = Math.max(SCALE_LOG_MIN, Math.min(SCALE_LOG_MAX, Math.log10(value)));
		return (int) Math.round((log10 - SCALE_LOG_MIN) * INCREMENTS_PER_POWER);
	}

	private void paintControl(PaintEvent e)
	{
		GC gc = e.gc;
		Color foreground = gc.getForeground();
		Color background = gc.getBackground();

		try
		{
			gc.setForeground(tickForeground);

			Point size = scale.getSize();
			float scalePercent =
					(scale.getSelection() - scale.getMinimum()) / (float) (scale.getMaximum() - scale.getMinimum());
			int grabCenter = Math.round(scalePercent * (size.x - SCALE_MARGIN * 2)) + SCALE_MARGIN;
			int y = (size.y - SCALE_HEIGHT) / 2;

			double value = SCALE_MIN;
			while (value <= SCALE_MAX)
			{
				double log10 = Math.log10(value);
				double percent = (log10 - SCALE_LOG_MIN) / (SCALE_LOG_MAX - SCALE_LOG_MIN);
				double increment = Math.pow(10, Math.floor(log10 + Double.MIN_VALUE));
				long count = Math.round(value / increment);

				int x = (int) Math.round(percent * (size.x + 1 - SCALE_MARGIN * 2) + SCALE_MARGIN - 1);
				if (x < grabCenter - GRAB_WIDTH / 2 || x > grabCenter + GRAB_WIDTH / 2)
				{
					gc.drawLine(x, y, x, y + TICK_HEIGHT);
					gc.drawLine(x, y + SCALE_HEIGHT, x, y + SCALE_HEIGHT - TICK_HEIGHT);
				}

				value = (count + 1) * increment;
			}
		}
		finally
		{
			gc.setForeground(foreground);
			gc.setBackground(background);
		}
	}

	private void updateSelection(boolean setService)
	{
		double exaggeration = scaleToExaggeration(scale.getSelection());

		if (setService)
		{
			VerticalExaggerationService.INSTANCE.set(exaggeration);
		}

		int decimalPlaces = 2 - (exaggeration <= 0 ? 0 : (int) Math.log10(exaggeration));
		DecimalFormat format = new DecimalFormat();
		format.setMinimumFractionDigits(decimalPlaces);
		format.setMaximumFractionDigits(decimalPlaces);
		scaleText.setText(format.format(exaggeration) + "x"); //$NON-NLS-1$
	}

	private void handleKey(KeyEvent e)
	{
		long thisKeyTime = System.currentTimeMillis();
		if (thisKeyTime - lastKeyTime > KEY_DELAY)
		{
			keyString = ""; //$NON-NLS-1$
		}

		if (e.character == SWT.CR || e.character == SWT.KEYPAD_CR || e.character == SWT.LF)
		{
			lastKeyTime = 0;
		}
		else if (e.character == SWT.DEL)
		{
			keyString = ""; //$NON-NLS-1$
		}
		else if (e.character == SWT.BS)
		{
			if (keyString.length() > 0)
			{
				keyString = keyString.substring(0, keyString.length() - 1);
			}
			lastKeyTime = thisKeyTime;
		}
		else
		{
			String newString = keyString + e.character;
			Double value = null;
			try
			{
				value = Double.parseDouble(newString);
			}
			catch (NumberFormatException nfe)
			{
			}

			if (value != null)
			{
				scale.setSelection(exaggerationToScale(value));
				updateSelection(true);
				keyString = newString;
				lastKeyTime = thisKeyTime;
			}
		}
	}

	@Override
	public void verticalExaggerationChanged(double oldValue, final double newValue)
	{
		if (scale != null && !scale.isDisposed())
		{
			scale.getDisplay().asyncExec(new Runnable()
			{
				@Override
				public void run()
				{
					scale.setSelection(exaggerationToScale(newValue));
					updateSelection(false);
				}
			});
		}
	}

	private class ScaleEditListener extends MouseAdapter implements FocusListener, KeyListener, VerifyKeyListener
	{
		@Inject
		@Named(IServiceConstants.ACTIVE_SHELL)
		private Shell shell;
		IInputValidator validator;

		/**
		 * 
		 */
		public ScaleEditListener()
		{
			validator = new IInputValidator()
			{

				@Override
				public String isValid(String inputString)
				{
					Pattern matingPattern = Pattern.compile("\\d+(\\.\\d+)?x?"); // \d+\.\d+ //$NON-NLS-1$
					Matcher matcher = matingPattern.matcher(inputString);

					if (!matcher.matches())
					{
						return Messages.GlobeExaggerationSet_PatternNotMatched;
					}

					try
					{
						double value = Double.parseDouble(inputString.replace("x", ""));
						//TODO: Specifies a minimum at  the top of the file, yet to see it enforced.
						//if (value < SCALE_MIN)
						//{
						//	return "Must be at least 0.1";
						//}
						if (value > 100)
						{
							return Messages.GlobeExaggerationSet_RangeExceeded;
						}
					}
					catch (NumberFormatException ex)
					{
						return ex.getLocalizedMessage();
					}
					return null;

				}
			};

		}



		String originalValue;

		@Override
		public void focusGained(FocusEvent arg0)
		{
			if (validator.isValid(scaleText.getText()) == null)
			{
				originalValue = scaleText.getText();
			}

		}

		@Override
		public void focusLost(FocusEvent arg0)
		{
			scaleText.setMarginColor(null);
			if (validator.isValid(scaleText.getText()) == null)
			{
				double value = Double.parseDouble(scaleText.getText().replace("x", ""));
				scale.setSelection(exaggerationToScale(value));
				updateSelection(true);
			}
			else
			{
				scaleText.setText(originalValue);
			}
			showValidNess(validator.isValid(scaleText.getText()));
		}

		@Override
		public void keyPressed(KeyEvent event)
		{
			if (event.keyCode == SWT.CR || event.keyCode == SWT.LF)
			{
				event.doit = false;
			}
		}


		@Override
		public void keyReleased(KeyEvent event)
		{
			scaleText.setMarginColor(null);

			showValidNess(validator.isValid(scaleText.getText()));
			if (event.keyCode == SWT.CR || event.keyCode == SWT.LF)
			{
				event.doit = false;
				focusLost(null);
			}
		}

		private void showValidNess(String message)
		{
			Color background = message == null ? null : Display.getDefault().getSystemColor(SWT.COLOR_RED);
			scaleText.setForeground(background);
			scaleText.setToolTipText(message);
			//			scaleError
		}

		@Override
		public void verifyKey(VerifyEvent event)
		{
			if (event.keyCode == SWT.CR || event.keyCode == SWT.LF)
			{
				// The user pressed Enter 
				event.doit = false;
			}

		}
	}
}
