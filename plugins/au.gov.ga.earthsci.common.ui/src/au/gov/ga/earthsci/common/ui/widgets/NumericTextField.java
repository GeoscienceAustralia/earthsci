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
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.DragDetectListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.GestureListener;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SegmentListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TouchListener;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * A text field that accepts only numeric input.
 * <p/>
 * The field can be configured to accept decimal or integer values, and
 * optionally restrict values to non-negatives.
 * <p/>
 * <em>Important</em>: This class extends {@link Composite} in order to be
 * consistent with the SWT class hierarchy. However, it is not intended to have
 * child elements. Adding children to this widget is considered programmer error
 * and may result in undesired behaviour.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 * 
 */
public class NumericTextField extends Composite
{

	private final boolean decimal;
	private final boolean negative;

	private Number maxValue;
	private Number minValue;

	private final Text textField;

	/**
	 * Create a new numeric text field with the given parent and style.
	 * <p/>
	 * The created field will support decimals and allow negative values.
	 * 
	 * @param parent
	 *            The parent to create the field in
	 * @param style
	 *            The style to apply to the field
	 */
	public NumericTextField(Composite parent, int style)
	{
		this(parent, style, true, true);
	}

	/**
	 * Create a new numeric text field that optionally allows decimal and/or
	 * negative values
	 * 
	 * @param parent
	 *            The parent to create the field in
	 * @param style
	 *            The style to apply to the field
	 * @param allowDecimals
	 *            Whether decimal values should be allowed
	 * @param allowNegative
	 *            Whether negative values should be allowed
	 */
	public NumericTextField(Composite parent, int style, boolean allowDecimals, boolean allowNegative)
	{
		super(parent, SWT.NONE);
		setLayout(new FillLayout());

		this.decimal = allowDecimals;
		this.negative = allowNegative;
		textField = new Text(this, style);

		textField.addVerifyListener(new VerifyListener()
		{
			@SuppressWarnings("nls")
			@Override
			public void verifyText(VerifyEvent e)
			{
				final String oldText = textField.getText();
				String newText = oldText.substring(0, e.start) + e.text + oldText.substring(e.end);

				// Allow empty fields
				if (newText == null || newText.isEmpty())
				{
					e.doit = true;
					return;
				}

				// Don't want negatives if not allowed
				if (newText.startsWith("-") && !negative)
				{
					e.doit = false;
					return;
				}

				// Don't want leading/trailing spaces
				if (!newText.equals(newText.trim()))
				{
					e.doit = false;
					return;
				}

				Number n = toNumber(newText);
				if (n == null)
				{
					e.doit = false;
					return;
				}

				// Value cannot be negative if negative not allowed
				if (n.doubleValue() < 0 && !negative)
				{
					e.doit = false;
					return;
				}

				// Value must be inside optional [min, max] range
				if ((minValue != null && n.doubleValue() < minValue.doubleValue()) ||
						(maxValue != null && n.doubleValue() > maxValue.doubleValue()))
				{
					e.doit = false;
					return;
				}
			}
		});
	}

	/**
	 * Set the maximum value this field can accept
	 */
	public void setMaxValue(Number value)
	{
		maxValue = value;
	}

	/**
	 * Set the minimum value this field can accept
	 */
	public void setMinValue(Number value)
	{
		minValue = value;
	}

	/**
	 * Return the current value of this numeric field, or <code>null</code> if
	 * the field is empty or non-numeric.
	 * 
	 * @return The current value of this field, or <code>null</code> if the
	 *         field is empty or non-numeric.
	 */
	public Number getNumber()
	{
		return toNumber(getText());
	}

	@SuppressWarnings("nls")
	private Number toNumber(String text)
	{
		Number result = null;
		if (decimal)
		{
			// Handle the edge cases first
			if (text.equals("."))
			{
				result = Double.valueOf(0.0);
			}
			else if (text.equals("-"))
			{
				result = Double.valueOf(-0);
			}
			else if (text.equals("-."))
			{
				result = Double.valueOf(-0.0);
			}
			else
			{
				try
				{
					result = Double.parseDouble(text);
				}
				catch (NumberFormatException e)
				{
					// Do nothing
				}
			}
		}
		else
		{
			if (text.equals("-"))
			{
				result = Long.valueOf(-0);
			}
			else
			{
				try
				{
					result = Long.parseLong(text);
				}
				catch (NumberFormatException e)
				{
					// Do nothing
				}
			}
		}
		return result;
	}

	@SuppressWarnings("nls")
	public void setNumber(Number n)
	{
		if (n == null)
		{
			textField.setText("");
		}
		else if (decimal)
		{
			textField.setText("" + n.doubleValue());
		}
		else
		{
			textField.setText("" + n.longValue());
		}
	}

	/**
	 * Return the current text value of this field, exactly as it appears in the
	 * field.
	 * 
	 * @return Return the current text value of this field, exactly as it
	 *         appears in the field.
	 */
	public String getText()
	{
		return textField.getText();
	}

	/**
	 * Add a modify listener to this field to listen for changes in its value
	 * 
	 * @param listener
	 *            The listener to add
	 */
	public void addModifyListener(ModifyListener listener)
	{
		textField.addModifyListener(listener);
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		textField.setEnabled(enabled);
	}

	/**
	 * Set the editable state
	 * 
	 * @param editable
	 */
	public void setEditable(boolean editable)
	{
		textField.setEditable(editable);
	}

	/**
	 * @return The editable state
	 */
	public boolean getEditable()
	{
		return textField.getEditable();
	}

	@Override
	public void setVisible(boolean visible)
	{
		super.setVisible(visible);
		textField.setVisible(visible);
	}

	@Override
	public void addControlListener(ControlListener listener)
	{
		textField.addControlListener(listener);
	}

	@Override
	public void addDragDetectListener(DragDetectListener listener)
	{
		textField.addDragDetectListener(listener);
	}

	@Override
	public void addListener(int eventType, Listener listener)
	{
		textField.addListener(eventType, listener);
	}

	@Override
	public void addFocusListener(FocusListener listener)
	{
		textField.addFocusListener(listener);
	}

	@Override
	public void addDisposeListener(DisposeListener listener)
	{
		textField.addDisposeListener(listener);
	}

	@Override
	public void addGestureListener(GestureListener listener)
	{
		textField.addGestureListener(listener);
	}

	@Override
	public void addHelpListener(HelpListener listener)
	{
		textField.addHelpListener(listener);
	}

	@Override
	public void addKeyListener(KeyListener listener)
	{
		textField.addKeyListener(listener);
	}

	@Override
	public void addMenuDetectListener(MenuDetectListener listener)
	{
		textField.addMenuDetectListener(listener);
	}

	public void addSegmentListener(SegmentListener listener)
	{
		textField.addSegmentListener(listener);
	}

	@Override
	public void addMouseListener(MouseListener listener)
	{
		textField.addMouseListener(listener);
	}

	public void addSelectionListener(SelectionListener listener)
	{
		textField.addSelectionListener(listener);
	}

	@Override
	public void addMouseTrackListener(MouseTrackListener listener)
	{
		textField.addMouseTrackListener(listener);
	}

	@Override
	public void addMouseMoveListener(MouseMoveListener listener)
	{
		textField.addMouseMoveListener(listener);
	}

	@Override
	public void addMouseWheelListener(MouseWheelListener listener)
	{
		textField.addMouseWheelListener(listener);
	}

	@Override
	public void addPaintListener(PaintListener listener)
	{
		textField.addPaintListener(listener);
	}

	@Override
	public void addTouchListener(TouchListener listener)
	{
		textField.addTouchListener(listener);
	}

	@Override
	public void addTraverseListener(TraverseListener listener)
	{
		textField.addTraverseListener(listener);
	}

	@Override
	public void removeListener(int eventType, Listener listener)
	{
		textField.removeListener(eventType, listener);
	}

	@Override
	public void removeDisposeListener(DisposeListener listener)
	{
		textField.removeDisposeListener(listener);
	}

	public void removeModifyListener(ModifyListener listener)
	{
		textField.removeModifyListener(listener);
	}

	public void removeSegmentListener(SegmentListener listener)
	{
		textField.removeSegmentListener(listener);
	}

	public void removeSelectionListener(SelectionListener listener)
	{
		textField.removeSelectionListener(listener);
	}

	@Override
	public void removeControlListener(ControlListener listener)
	{
		textField.removeControlListener(listener);
	}

	@Override
	public void removeDragDetectListener(DragDetectListener listener)
	{
		textField.removeDragDetectListener(listener);
	}

	@Override
	public void removeFocusListener(FocusListener listener)
	{
		textField.removeFocusListener(listener);
	}

	@Override
	public void removeGestureListener(GestureListener listener)
	{
		textField.removeGestureListener(listener);
	}

	@Override
	public void removeHelpListener(HelpListener listener)
	{
		textField.removeHelpListener(listener);
	}

	@Override
	public void removeKeyListener(KeyListener listener)
	{
		textField.removeKeyListener(listener);
	}

	@Override
	public void removeMenuDetectListener(MenuDetectListener listener)
	{
		textField.removeMenuDetectListener(listener);
	}

	@Override
	public void removeMouseTrackListener(MouseTrackListener listener)
	{
		textField.removeMouseTrackListener(listener);
	}

	@Override
	public void removeMouseListener(MouseListener listener)
	{
		textField.removeMouseListener(listener);
	}

	@Override
	public void removeMouseMoveListener(MouseMoveListener listener)
	{
		textField.removeMouseMoveListener(listener);
	}

	@Override
	public void removeMouseWheelListener(MouseWheelListener listener)
	{
		textField.removeMouseWheelListener(listener);
	}

	@Override
	public void removePaintListener(PaintListener listener)
	{
		textField.removePaintListener(listener);
	}

	@Override
	public void removeTouchListener(TouchListener listener)
	{
		textField.removeTouchListener(listener);
	}

	@Override
	public void removeTraverseListener(TraverseListener listener)
	{
		textField.removeTraverseListener(listener);
	}

}
