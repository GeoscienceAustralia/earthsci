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
package au.gov.ga.earthsci.newt.swt;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.WorldWindowGLAutoDrawable;
import gov.nasa.worldwind.render.ScreenCreditController;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.dashboard.DashboardController;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;

import javax.media.opengl.GLAutoDrawable;
import javax.swing.Timer;

import org.eclipse.swt.widgets.Control;

import com.jogamp.newt.opengl.GLWindow;

/**
 * {@link WorldWindowGLAutoDrawable} subclass used when using a NEWT canvas for
 * rendering. This is required because, in a few methods, the
 * {@link WorldWindowGLAutoDrawable} assumes the {@link GLAutoDrawable} passed
 * to the {@link #initDrawable(GLAutoDrawable)} function is an instanceof
 * {@link java.awt.Component}, which isn't the case for NEWT canvas'.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class WorldWindowNewtAutoDrawableSWT extends WorldWindowGLAutoDrawable implements WorldWindowNewtDrawableSWT
{
	protected Control swtControl;

	protected DashboardController dashboard; //wish this wasn't private in the superclass
	protected Timer redrawTimer; //wish this wasn't private in the superclass

	@Deprecated
	@Override
	public void initDrawable(GLAutoDrawable glAutoDrawable)
	{
		throw new IllegalStateException("WorldWindowNewtDrawable.initDrawable(GLAutoDrawable) should not be invoked"); //$NON-NLS-1$
	}

	@Override
	public void initDrawable(GLWindow window, Control swtControl)
	{
		super.initDrawable(window);
		this.swtControl = swtControl;
	}

	@Override
	public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int w, int h)
	{
		// This is apparently necessary to enable the WWJ canvas to resize correctly with JSplitPane.
		//swtControl.setMinimumSize(new Dimension(0, 0));
	}

	@Override
	public void redraw()
	{
		if (swtControl != null)
			swtControl.redraw();
	}

	@Override
	public void endInitialization()
	{
		initializeCreditsController();
		//this.dashboard = new DashboardController(this, awtComponent);
	}

	@Override
	protected void initializeCreditsController()
	{
		new ScreenCreditController((WorldWindow) swtControl);
	}

	@Override
	protected void doShutdown()
	{
		//have to override this method because the superclass' dashboard field is private
		super.doShutdown();
		if (this.dashboard != null)
			this.dashboard.dispose();
	}

	@Override
	public void propertyChange(PropertyChangeEvent propertyChangeEvent)
	{
		if (propertyChangeEvent == null)
		{
			String msg = Logging.getMessage("nullValue.PropertyChangeEventIsNull"); //$NON-NLS-1$
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}

		redraw(); //this is how the super class should be implemented (instead of calling repaint directly)
	}

	@Override
	protected int doDisplay()
	{
		int redrawDelay = super.doDisplay();
		if (redrawDelay > 0)
		{
			if (redrawTimer == null)
			{
				redrawTimer = new Timer(redrawDelay, new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent actionEvent)
					{
						redrawTimer = null;
						redraw(); //this is how the super class should be implemented (instead of calling repaint directly)
					}
				});
				redrawTimer.setRepeats(false);
				redrawTimer.start();
			}
		}
		return 0;
	}
}
