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

import javax.annotation.PostConstruct;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Scale;

/**
 * Tool control used to change globe exaggeration.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class GlobeExaggerationToolControl
{
	private boolean settingScale = false;
	private Scale scale;

	private static IEclipseContext context;

	/**
	 * e4 bug workaround, don't call.
	 */
	public static void setPartContext(IEclipseContext context)
	{
		GlobeExaggerationToolControl.context = context;
	}

	@PostConstruct
	public void createControls(Composite parent, IEclipseContext context)
	{
		//XXX BUG: for some reason, after the layer part is closed and reopened, the
		//injected context is "anonymous", and doesn't contain the TreeViewer
		//the following is a workaround; the LayerTreePart's context is passed
		//to this class and stored in a static variable, and then this context
		//is used to create the scale on instead of the injected one

		if (context.get(MPart.class) == null)
		{
			context = GlobeExaggerationToolControl.context.createChild();
			context.set(Composite.class, parent);
			ContextInjectionFactory.inject(this, context);
		}
		else
		{
			int width = 80;
			int height = 21;

			Composite child = new Composite(parent, SWT.NONE);
			child.setSize(child.computeSize(width, SWT.DEFAULT));

			scale = new Scale(child, SWT.HORIZONTAL);
			Point size = scale.computeSize(width, SWT.DEFAULT);
			scale.setSize(size);
			scale.setMinimum(0);
			scale.setMaximum(100);
			scale.setSelection(100);
			scale.setLocation(0, (height - size.y) / 2);
			scale.setToolTipText("Set vertical exaggeration");

			scale.addSelectionListener(new SelectionAdapter()
			{
				@Override
				public void widgetSelected(SelectionEvent e)
				{
					if (settingScale)
					{
						return;
					}
					//TODO
				}
			});
		}
	}
}