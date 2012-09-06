package au.gov.ga.earthsci.ui.application;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory
{
	public static final String ID = "au.gov.ga.earthsci.ui.application.perspective"; //$NON-NLS-1$
	
	@Override
	public void createInitialLayout(IPageLayout layout)
	{
		layout.setEditorAreaVisible(false);
		layout.setFixed(true);
	}
}
