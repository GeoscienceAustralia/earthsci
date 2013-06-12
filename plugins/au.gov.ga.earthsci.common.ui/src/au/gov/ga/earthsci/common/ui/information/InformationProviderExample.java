package au.gov.ga.earthsci.common.ui.information;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Example pulled shamelessly from here: <a href=
 * "http://www.subshell.com/en/subshell/blog/jface-tooltips-via-information-controls100.html"
 * >http://www.subshell.com/en/subshell/blog/jface-tooltips-via-information-
 * controls100.html</a>
 * 
 * @author Torsten Witte
 */
public class InformationProviderExample
{
	InformationProviderExample(Shell shell) throws Exception
	{
		// Create a table viewer
		TableViewer viewer = new TableViewer(shell);

		// Create the label provider
		viewer.setLabelProvider(new LabelProvider()
		{
			@Override
			public String getText(Object element)
			{
				if (element == null)
				{
					return ""; //$NON-NLS-1$
				}
				return element.getClass().getSimpleName() + ": " + element.toString(); //$NON-NLS-1$
			}
		});

		// Create the content provider
		viewer.setContentProvider(new ArrayContentProvider());

		// Create the table input with different domain model objects
		List<Object> tableInput = new ArrayList<Object>();
		tableInput.add("Just a String"); //$NON-NLS-1$
		tableInput.add("Just another String"); //$NON-NLS-1$
		tableInput.add(new URL("http://www.google.de")); //$NON-NLS-1$
		tableInput.add(new URL("http://www.subshell.com")); //$NON-NLS-1$
		tableInput.add(new URL("http://www.tagesschau.de")); //$NON-NLS-1$

		viewer.setInput(tableInput);

		// Hook tooltips
		hookTooltips(viewer);
	}

	private void hookTooltips(TableViewer viewer)
	{
		IInformationProvider provider = new TableViewerInformationProvider(viewer);
		InformationProviderHoverInformationControlManager.install(viewer.getControl(), provider,
				new IInformationControlCreator()
				{
					@Override
					public IInformationControl createInformationControl(Shell parent)
					{
						return new DefaultInformationControl(parent, false);
					}
				});
	}

	public static void main(String[] args) throws Exception
	{
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new InformationProviderExample(shell);
		shell.open();

		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch())
			{
				display.sleep();
			}
		}

		display.dispose();
	}
}