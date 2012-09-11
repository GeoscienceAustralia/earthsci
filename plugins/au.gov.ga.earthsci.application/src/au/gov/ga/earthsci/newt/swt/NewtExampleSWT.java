package au.gov.ga.earthsci.newt.swt;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;

import java.awt.BorderLayout;
import java.awt.Frame;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import au.gov.ga.earthsci.newt.awt.NewtInputHandlerAWT;
import au.gov.ga.earthsci.newt.awt.WorldWindowNewtAutoDrawableAWT;
import au.gov.ga.earthsci.newt.awt.WorldWindowNewtCanvasAWT;

public class NewtExampleSWT
{
	public static void main(String[] args)
	{
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());

		/*System.setProperty("newt.debug.Window", "true");
		Configuration.setValue(AVKey.INPUT_HANDLER_CLASS_NAME, NewtInputHandlerSWT.class.getName());
		Configuration.setValue(AVKey.WORLD_WINDOW_CLASS_NAME, WorldWindowNewtAutoDrawableSWT.class.getName());
		WorldWindowNewtCanvasSWT wwd = new WorldWindowNewtCanvasSWT(shell, SWT.NONE, null);*/

		Composite composite = new Composite(shell, SWT.EMBEDDED);
		Frame frame = SWT_AWT.new_Frame(composite);
		frame.setLayout(new BorderLayout());
		Configuration.setValue(AVKey.INPUT_HANDLER_CLASS_NAME, NewtInputHandlerAWT.class.getName());
		Configuration.setValue(AVKey.WORLD_WINDOW_CLASS_NAME, WorldWindowNewtAutoDrawableAWT.class.getName());
		WorldWindowNewtCanvasAWT wwd = new WorldWindowNewtCanvasAWT();
		frame.add(wwd, BorderLayout.CENTER);

		Model m = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
		wwd.setModel(m);

		shell.setSize(1024, 768);
		shell.open();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
}
