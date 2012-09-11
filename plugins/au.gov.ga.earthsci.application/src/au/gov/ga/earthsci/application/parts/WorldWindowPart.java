package au.gov.ga.earthsci.application.parts;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.avlist.AVKey;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;

import au.gov.ga.earthsci.newt.awt.NewtInputHandlerAWT;
import au.gov.ga.earthsci.newt.awt.WorldWindowNewtAutoDrawableAWT;
import au.gov.ga.earthsci.newt.awt.WorldWindowNewtCanvasAWT;

public class WorldWindowPart
{
	@Inject
	private IEclipseContext context;

	@Inject
	public void init(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.EMBEDDED);
		Frame frame = SWT_AWT.new_Frame(composite);
		frame.setLayout(new BorderLayout());

		Configuration.setValue(AVKey.INPUT_HANDLER_CLASS_NAME, NewtInputHandlerAWT.class.getName());
		Configuration.setValue(AVKey.WORLD_WINDOW_CLASS_NAME, WorldWindowNewtAutoDrawableAWT.class.getName());
		WorldWindowNewtCanvasAWT wwd = new WorldWindowNewtCanvasAWT();
		frame.add(wwd, BorderLayout.CENTER);

		wwd.setModel(context.get(Model.class));
	}
}
