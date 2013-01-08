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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
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
 * A widget that allows for the viewing / editing of a {@link Position} object.
 * <p/>
 * If style specifies {@code READ_ONLY}, the widget will present the {@link Position}
 * fields as read-only labels. Otherwise the editor will present editable text fields.
 * 
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>READ_ONLY</dd>
 * <dt><b>Events:</b></dt>
 * <dd>SWT.Modify ({@code data} is a 3 element array of {@code String} with [lat, lon, elevation])</dd>
 * </dl>
 * 
 * @author James Navin (james.navin@ga.gov.au)
 *
 */
public class PositionEditor extends Composite
{

	private final Label latLabel;
	private final Text latitude;
	private final Label latUnit;
	
	private final Label lonLabel;
	private final Text longitude;
	private final Label lonUnit;
	
	private final Label elLabel;
	private final Text elevation;
	private final Label elUnit;
	
	private int numDecimalPlaces = 8;
	
	private final Vector<PositionEditorListener> listeners = new Vector<PositionEditorListener>();
	
	/**
	 * Create a new {@link PositionEditor}
	 * 
	 * @param parent
	 * @param style
	 */
	public PositionEditor(Composite parent, int style)
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
		
		latLabel = new Label(this, style);
		latLabel.setText("Latitude:");
		latitude = new Text(this, fieldStyle);
		latitude.setLayoutData(fieldLayoutData);
		latitude.addModifyListener(modifyListener);
		latUnit = new Label(this, style);
		latUnit.setText("\u00B0");
		
		lonLabel = new Label(this, style);
		lonLabel.setText("Longitude:");
		longitude = new Text(this, fieldStyle);
		longitude.setLayoutData(fieldLayoutData);
		longitude.addModifyListener(modifyListener);
		lonUnit = new Label(this, style);
		lonUnit.setText("\u00B0");
		
		elLabel = new Label(this, style);
		elLabel.setText("Elevation:");
		elevation = new Text(this, fieldStyle);
		elevation.setLayoutData(fieldLayoutData);
		elevation.addModifyListener(modifyListener);
		elUnit = new Label(this, style);
		elUnit.setText("m");
		
		addDisposeListener(new DisposeListener()
		{
			@Override
			public void widgetDisposed(DisposeEvent e)
			{
			}
		});
	}

	/**
	 * Returns the current {@link Position} value this editor reflects, or <code>null</code>
	 * if it is invalid.
	 */
	public Position getPositionValue()
	{
		try
		{
			double lat = Double.parseDouble(latitude.getText().trim());
			double lon = Double.parseDouble(longitude.getText().trim());
			double el = Double.parseDouble(elevation.getText().trim());
			
			return Position.fromDegrees(lat, lon, el);
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	/**
	 * Set the current {@link Position} value on this editor.
	 * <p/>
	 * This will reset the fields on the editor to reflect those of the provided {@link Position}.
	 * <p/>
	 * If <code>null</code> is provided, the fields of this editor will be cleared.
	 */
	public void setPositionValue(Position p)
	{
		if (p != null)
		{
			NumberFormat numberFormat = getNumberFormat();
			
			latitude.setText(numberFormat.format(p.latitude.degrees));
			longitude.setText(numberFormat.format(p.longitude.degrees));
			elevation.setText(numberFormat.format(p.elevation));
		}
		else
		{
			latitude.setText(""); //$NON-NLS-1$
			longitude.setText(""); //$NON-NLS-1$
			elevation.setText(""); //$NON-NLS-1$
		}
		layout(true, true);
	}

	/**
	 * Set the number of decimal places to use when 
	 * representing position values
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
	 * Register a {@link PositionEditorListener} on this editor
	 */
	public void addPositionEditorListener(PositionEditorListener l)
	{
		if (l == null)
		{
			return;
		}
		listeners.add(l);
	}
	
	/**
	 * Remove the {@link PositionEditorListener} from this editor
	 */
	public void removePositionEditorListener(PositionEditorListener l)
	{
		if (l == null)
		{
			return;
		}
		listeners.remove(l);
	}
	
	private void notifyModify()
	{
		// Send a low-level event
		Event e = new Event();
		e.item = this;
		e.type = SWT.Modify;
		e.display = Display.getCurrent();
		e.data = new String[] {latitude.getText(), longitude.getText(), elevation.getText()};
		notifyListeners(SWT.Modify, e);
		
		// Then a high-level event
		if (listeners.isEmpty())
		{
			return;
		}
		PositionChangedEvent pce = new PositionChangedEvent(this,
															getPositionValue(),
														    latitude.getText(), 
														    longitude.getText(), 
															elevation.getText());
		for (PositionEditorListener l : listeners)
		{
			l.positionChanged(pce);
		}
	}
	
	/**
	 * An event that is fired when a user's text change alters the value of the {@link Position}
	 * this editor reflects.
	 */
	public final static class PositionChangedEvent extends EventObject
	{
		private final boolean valid;
		private final Position p;
		private final String lat;
		private final String lon;
		private final String el;
		
		private PositionChangedEvent(Object source, Position p, String lat, String lon, String el)
		{
			super(source);
			this.p = p;
			this.valid = p != null;
			this.lat = lat;
			this.lon = lon;
			this.el = el;
		}
		
		public boolean isValid() {return valid;}
		public Position getPosition() {return p;}
		public String getLatitudeText() {return lat;}
		public String getLongitudeText() {return lon;}
		public String getElevationText() {return el;}
	}
	
	/**
	 * An interface for classes that wish to be notified of changes to the {@link Position} value
	 * backing a {@link PositionEditor}.
	 */
	public static interface PositionEditorListener extends EventListener
	{

		/**
		 * Invoked when changes to the editor have resulted in a changed position
		 */
		void positionChanged(PositionChangedEvent e);
	}
}
