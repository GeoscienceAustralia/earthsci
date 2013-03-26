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
package au.gov.ga.earthsci.application.parts.info;

import java.net.URL;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;

import au.gov.ga.earthsci.application.parts.info.handlers.LinkHandler;
import au.gov.ga.earthsci.common.util.IInformationed;

/**
 * Part that shows information about the currently selected item.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class InfoPart
{
	public static final String PART_ID = "au.gov.ga.earthsci.application.information.part"; //$NON-NLS-1$
	public static final String INPUT_NAME = PART_ID + ".input"; //$NON-NLS-1$

	private Browser browser;
	private IInformationed informationed;
	private boolean link;

	@Inject
	private IEclipseContext context;

	@Inject
	public void init(Composite parent)
	{
		browser = new Browser(parent, SWT.NONE);
	}

	@PostConstruct
	private void postConstruct(IEclipseContext context, MPart part)
	{
		context.set(InfoPart.class, this);
		link = LinkHandler.isLink(part);
	}

	@PreDestroy
	private void preDestroy(IEclipseContext context)
	{
		context.remove(InfoPart.class);
	}

	public boolean isLink()
	{
		return link;
	}

	public void setLink(boolean link)
	{
		this.link = link;
		select(informationed);
	}

	@Inject
	private void select(@Optional @Named(IServiceConstants.ACTIVE_SELECTION) IInformationed informationed)
	{
		this.informationed = informationed;
		if (isLink())
		{
			context.modify(INPUT_NAME, informationed);
			context.declareModifiable(INPUT_NAME);
		}
	}

	@Inject
	private void select(@Optional @Named(IServiceConstants.ACTIVE_SELECTION) IInformationed[] informationed)
	{
		if (informationed != null && informationed.length == 1)
		{
			select(informationed[0]);
		}
	}

	@Inject
	@Optional
	private void setPartInput(@Named(INPUT_NAME) IInformationed informationed)
	{
		showInfo(informationed);
	}

	public void showInfo(IInformationed informationed)
	{
		if (informationed != null)
		{
			URL url = informationed.getInformationURL();
			if (url != null)
			{
				browser.setUrl(url.toString());
			}
			else
			{
				String html = informationed.getInformationString();
				if (html == null)
				{
					html = ""; //$NON-NLS-1$
				}
				browser.setText(html);
			}
		}
	}
}
