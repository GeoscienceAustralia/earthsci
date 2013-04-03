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

import gov.nasa.worldwind.geom.Vec4;

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
 * A widget that can be used to edit the fields of a {@link Vec4} instance
 * <p/>
 * If style specifies {@code READ_ONLY}, the widget will present the
 * {@link Vec4} fields as read-only labels. Otherwise the editor will present
 * editable text fields.
 * 
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>READ_ONLY</dd>
 * <dt><b>Events:</b></dt>
 * <dd>SWT.Modify ({@code data} is a 4 element array of {@code String} with
 * [x,y,z,w])</dd>
 * </dl>
 * 
 * A high-level event/listener API is also provided. See
 * {@link #addVec4EditorListener()}.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class Vec4Editor extends Composite
{

	private final Text xText;
	private final Text yText;
	private final Text zText;
	private final Text wText;

	private final AtomicBoolean quiet = new AtomicBoolean(false);
	private final Vector<Vec4EditorListener> listeners = new Vector<Vec4EditorListener>();

	private int numDecimalPlaces = 8;

	/**
	 * Create a new editor instance
	 */
	public Vec4Editor(Composite parent, int style)
	{
		super(parent, style);

		GridLayout layout = new GridLayout(2, false);
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

		addLabel(style, Messages.Vec4Editor_XLabel);
		xText = addText(fieldStyle, fieldLayoutData, modifyListener);

		addLabel(style, Messages.Vec4Editor_YLabel);
		yText = addText(fieldStyle, fieldLayoutData, modifyListener);

		addLabel(style, Messages.Vec4Editor_ZLabel);
		zText = addText(fieldStyle, fieldLayoutData, modifyListener);

		addLabel(style, Messages.Vec4Editor_WLabel);
		wText = addText(fieldStyle, fieldLayoutData, modifyListener);
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
	 * Set the {@link Vec4} value on this editor.
	 * <p/>
	 * This will update the fields of the editor to reflect those contained in
	 * the provided {@link Vec4}. If the provided {@link Vec4} is
	 * <code>null</code>, the fields of the editor will be cleared.
	 */
	public void setVec4Value(Vec4 vec)
	{
		quiet.set(true);
		if (vec == null)
		{
			xText.setText(""); //$NON-NLS-1$
			yText.setText(""); //$NON-NLS-1$
			zText.setText(""); //$NON-NLS-1$
			wText.setText(""); //$NON-NLS-1$
		}
		else
		{
			NumberFormat f = getNumberFormat();
			xText.setText(f.format(vec.x));
			yText.setText(f.format(vec.y));
			zText.setText(f.format(vec.z));
			wText.setText(f.format(vec.w));
		}
		quiet.set(false);
		notifyModify();
	}

	/**
	 * Return the {@link Vec4} value represented by this editor, or
	 * <code>null</code> if it is invalid.
	 */
	public Vec4 getVec4Value()
	{
		try
		{
			double x = Double.parseDouble(xText.getText().trim());
			double y = Double.parseDouble(yText.getText().trim());
			double z = Double.parseDouble(zText.getText().trim());
			double w = Double.parseDouble(wText.getText().trim());

			return new Vec4(x, y, z, w);
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/**
	 * Set the number of decimal places to use when representing vector values
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
	 * Register a {@link Vec4EditorListener} on this editor.
	 */
	public void addVec4EditorListener(Vec4EditorListener l)
	{
		if (l == null)
		{
			return;
		}
		listeners.add(l);
	}

	/**
	 * Remove the provided {@link Vec4EditorListener} from this editor.
	 */
	public void removeVec4EditorListener(Vec4EditorListener l)
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

		Event e = new Event();
		e.widget = this;
		e.item = this;
		e.type = SWT.Modify;
		e.display = Display.getCurrent();
		e.data = new String[] { xText.getText(), yText.getText(), zText.getText(), wText.getText() };
		notifyListeners(SWT.Modify, e);

		if (listeners.isEmpty())
		{
			return;
		}

		Vec4ChangedEvent vce =
				new Vec4ChangedEvent(this, getVec4Value(), xText.getText(), yText.getText(), zText.getText(),
						wText.getText());
		for (Vec4EditorListener l : listeners)
		{
			l.vec4Changed(vce);
		}
	}

	/**
	 * An event that is fired when the {@link Vec4} value is changed within a
	 * {@link Vec4Editor}
	 */
	public static class Vec4ChangedEvent extends EventObject
	{
		private final Vec4 vec;
		private final String xText;
		private final String yText;
		private final String zText;
		private final String wText;

		private Vec4ChangedEvent(Object source, Vec4 vec, String xText, String yText, String zText, String wText)
		{
			super(source);
			this.vec = vec;
			this.xText = xText;
			this.yText = yText;
			this.zText = zText;
			this.wText = wText;
		}

		public boolean isValid()
		{
			return vec != null;
		}

		public Vec4 getVec4()
		{
			return vec;
		}

		public String getXText()
		{
			return xText;
		}

		public String getYText()
		{
			return yText;
		}

		public String getZText()
		{
			return zText;
		}

		public String getWText()
		{
			return wText;
		}
	}

	/**
	 * A listener interface for classes that wish to be notified of changes to
	 * the fields of a {@link Vec4Editor}
	 */
	public static interface Vec4EditorListener extends EventListener
	{
		/**
		 * Invoked when changes are made to the fields of the source
		 * {@link Vec4Editor}
		 */
		void vec4Changed(Vec4ChangedEvent e);
	}
}
