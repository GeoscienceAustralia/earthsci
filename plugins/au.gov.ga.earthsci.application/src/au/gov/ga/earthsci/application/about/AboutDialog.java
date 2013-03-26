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
package au.gov.ga.earthsci.application.about;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.branding.IProductConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An about dialog for the application.
 * <p/>
 * Customisation is possible through the org.eclipse.core.runtime.products
 * extension point using constants defined in {@link IProductConstants}.
 * Additional information is obtained via the provided {@link IProduct}
 * instance.
 * <p/>
 * In particular, the following are used:
 * <ul>
 * <li> {@link IProduct#getName()}
 * <li> {@link IProductConstants#ABOUT_IMAGE}
 * <li> {@link IProductConstants#ABOUT_TEXT}
 * </ul>
 * <p/>
 * Based loosely on the AboutDialog available in the Eclipse 3.1 workbench.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class AboutDialog extends TrayDialog
{
	private static final Logger logger = LoggerFactory.getLogger(AboutDialog.class);

	private static final String DEFAULT_ABOUT_IMAGE =
			"platform:/plugin/au.gov.ga.earthsci.application/icons/application_icon.png"; //$NON-NLS-1$
	private IProduct product;

	private Image aboutImage = null;

	/**
	 * Create a new about dialog that displays information for the given
	 * product.
	 * 
	 * @param shell
	 *            The parent shell to attach the dialog to
	 * @param product
	 *            The product to display information for
	 */
	public AboutDialog(Shell shell, IProduct product)
	{
		super(shell);
		this.product = product;
	}

	@Override
	protected void configureShell(Shell newShell)
	{
		super.configureShell(newShell);
		newShell.setText(NLS.bind(Messages.AboutDialog_Title, product.getName()));
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Color background = JFaceColors.getBannerBackground(parent.getDisplay());
		Color foreground = JFaceColors.getBannerForeground(parent.getDisplay());

		final Composite topContainer = new Composite(parent, SWT.NONE);
		topContainer.setBackground(background);
		topContainer.setForeground(foreground);

		topContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		topContainer.setLayout(layout);

		aboutImage = openAboutImage();

		Label imageLabel = new Label(topContainer, SWT.NONE);
		imageLabel.setBackground(background);
		imageLabel.setForeground(foreground);

		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.BEGINNING;
		data.grabExcessHorizontalSpace = false;
		imageLabel.setLayoutData(data);
		imageLabel.setImage(aboutImage);

		String aboutText = getAboutText();
		if (aboutText != null)
		{
			final int minWidth = 400;

			final Composite textComposite = new Composite(topContainer, SWT.NONE);
			data = new GridData(GridData.FILL_BOTH);
			data.widthHint = minWidth;
			textComposite.setLayoutData(data);

			textComposite.setLayout(new FillLayout());

			Browser browser = new Browser(textComposite, SWT.NONE);
			browser.setText(aboutText);
			browser.addLocationListener(new LocationListener()
			{

				@Override
				public void changing(LocationEvent event)
				{
					// Intercept links and launch in the platform default
					event.doit = false;
					Program.launch(event.location);
				}

				@Override
				public void changed(LocationEvent event)
				{
				}
			});
		}

		Label bar = new Label(topContainer, SWT.HORIZONTAL | SWT.SEPARATOR);
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		bar.setLayoutData(data);

		return topContainer;
	}

	@Override
	protected boolean isResizable()
	{
		return true;
	}

	@Override
	public boolean close()
	{
		if (aboutImage != null)
		{
			aboutImage.dispose();
			aboutImage = null;
		}

		return super.close();
	}

	/**
	 * Retrieve the "About" image specified in the current product under the key
	 * {@link IProductConstants#ABOUT_IMAGE}, or the default image if none is
	 * specified.
	 */
	private Image openAboutImage()
	{
		String aboutImagePath = product.getProperty(IProductConstants.ABOUT_IMAGE);
		if (aboutImagePath == null)
		{
			aboutImagePath = DEFAULT_ABOUT_IMAGE;
		}

		URL aboutImageURL = null;
		try
		{
			aboutImageURL = new URL(aboutImagePath);
		}
		catch (MalformedURLException e)
		{
			logger.error("Unable to find image " + aboutImagePath, e); //$NON-NLS-1$
			try
			{
				aboutImageURL = new URL(DEFAULT_ABOUT_IMAGE);
			}
			catch (MalformedURLException e1)
			{
			}
		}

		ImageDescriptor descriptor = ImageDescriptor.createFromURL(aboutImageURL);

		return descriptor.createImage();
	}

	/**
	 * Retrieve the html-formatted "About" text specified in the current product
	 * under the key {@link IProductConstants#ABOUT_TEXT}, along with a default
	 * platform descriptor appended.
	 */
	private String getAboutText()
	{
		StringBuffer htmlBlock = new StringBuffer();

		htmlBlock.append("<html><head>"); //$NON-NLS-1$
		htmlBlock.append("<style type=\"text/css\">a:link{color:blue;}a:visited{color:blue;}</style>"); //$NON-NLS-1$
		htmlBlock.append("</head><body>"); //$NON-NLS-1$
		htmlBlock.append(product.getProperty(IProductConstants.ABOUT_TEXT));
		htmlBlock.append(Messages.AboutDialog_StandardPostamble);
		htmlBlock.append("</body></html>"); //$NON-NLS-1$

		return htmlBlock.toString();
	}
}
