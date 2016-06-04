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
package au.gov.ga.earthsci.application.widgets;

import gov.nasa.worldwind.geom.Position;

import java.text.NumberFormat;
import java.util.EventListener;
import java.util.EventObject;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * A widget that allows for the viewing / editing of a {@link Double} value.
 * <p/>
 * If style specifies {@code READ_ONLY}, the widget will present the
 * {@link Double} value as a read-only label. Otherwise the editor will present
 * an editable text field.
 * 
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>READ_ONLY</dd>
 * <dt><b>Events:</b></dt>
 * <dd>SWT.Modify ({@code data} is a {@code String})</dd>
 * </dl>
 * 
 * @author Michael de Hoog
 */
public class DoubleEditor extends Composite
{
	private final Text text;

	private int numDecimalPlaces = 8;

	private final AtomicBoolean quiet = new AtomicBoolean(false);
	private final Vector<DoubleEditorListener> listeners = new Vector<DoubleEditorListener>();

	/**
	 * Create a new {@link DoubleEditor}
	 */
	public DoubleEditor(Composite parent, int style)
	{
		super(parent, style);

		GridLayout layout = new GridLayout(3, false);
		layout.horizontalSpacing = 2;
		setLayout(layout);

		int fieldStyle = style | SWT.BORDER;
		GridData fieldLayoutData = new GridData(GridData.FILL_HORIZONTAL);

		ModifyListener modifyListener = new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				notifyModify();
			}
		};

		addLabel(style, Messages.DoubleEditor_Label);
		text = addText(fieldStyle, fieldLayoutData, modifyListener);
	}

	private Label addLabel(int style, String text)
	{
		Label result = new Label(this, style);
		result.setText(text);
		return result;
	}

	private Text addText(int style, Object layoutData, ModifyListener listener)
	{
		Text result = new Text(this, style);
		result.setLayoutData(layoutData);
		result.addModifyListener(listener);
		return result;
	}

	/**
	 * Returns the current {@link Position} value this editor reflects, or
	 * <code>null</code> if it is invalid.
	 */
	public Double getDoubleValue()
	{
		try
		{
			return Double.parseDouble(text.getText().trim());
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/**
	 * Set the current {@link Position} value on this editor.
	 * <p/>
	 * This will reset the fields on the editor to reflect those of the provided
	 * {@link Position}.
	 * <p/>
	 * If <code>null</code> is provided, the fields of this editor will be
	 * cleared.
	 */
	public void setDoubleValue(Double d)
	{
		quiet.set(true);
		if (d != null)
		{
			NumberFormat numberFormat = getNumberFormat();
			text.setText(numberFormat.format(d));
		}
		else
		{
			text.setText(""); //$NON-NLS-1$
		}
		quiet.set(false);
		notifyModify();
	}

	/**
	 * Set the number of decimal places to use when representing position values
	 */
	public void setNumDecimalPlaces(int numDecimalPlaces)
	{
		this.numDecimalPlaces = numDecimalPlaces;
	}

	private NumberFormat getNumberFormat()
	{
		NumberFormat numberFormat = NumberFormat.getNumberInstance();
		numberFormat.setMaximumFractionDigits(numDecimalPlaces);
		numberFormat.setMinimumFractionDigits(numDecimalPlaces);
		numberFormat.setGroupingUsed(false);
		return numberFormat;
	}

	/**
	 * Register a {@link DoubleEditorListener} on this editor
	 */
	public void addDoubleEditorListener(DoubleEditorListener l)
	{
		if (l == null)
		{
			return;
		}
		listeners.add(l);
	}

	/**
	 * Remove the {@link DoubleEditorListener} from this editor
	 */
	public void removeDoubleEditorListener(DoubleEditorListener l)
	{
		if (l == null)
		{
			return;
		}
		listeners.remove(l);
	}

	private void notifyModify()
	{
		if (quiet.get())
		{
			return;
		}

		// Send a low-level event
		Event e = new Event();
		e.item = this;
		e.widget = this;
		e.type = SWT.Modify;
		e.display = Display.getCurrent();
		e.data = text.getText();
		notifyListeners(SWT.Modify, e);

		// Then a high-level event
		if (listeners.isEmpty())
		{
			return;
		}
		DoubleChangedEvent dce =
				new DoubleChangedEvent(this, getDoubleValue(), text.getText());
		for (DoubleEditorListener l : listeners)
		{
			l.doubleChanged(dce);
		}
	}

	/**
	 * An event that is fired when a user's text change alters the value of a
	 * {@link Double} within a {@link DoubleEditor}
	 */
	public final static class DoubleChangedEvent extends EventObject
	{
		private final boolean valid;
		private final double d;
		private final String val;

		private DoubleChangedEvent(Object source, Double d, String val)
		{
			super(source);
			this.d = d;
			this.valid = d != null;
			this.val = val;
		}

		public boolean isValid()
		{
			return valid;
		}

		public Double getDouble()
		{
			return d;
		}

		public String getDoubleText()
		{
			return val;
		}
	}

	/**
	 * An interface for classes that wish to be notified of changes to the
	 * {@link Double} value backing a {@link DoubleEditor}.
	 */
	public static interface DoubleEditorListener extends EventListener
	{

		/**
		 * Invoked when changes to the editor have resulted in a changed
		 * {@link Double}
		 */
		void doubleChanged(DoubleChangedEvent e);
	}
}
