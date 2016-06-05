/*******************************************************************************
 * Copyright 2016 Geoscience Australia
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
package au.gov.ga.earthsci.layer.ui.wizard.gocad;

import gov.nasa.worldwind.BasicFactory;
import gov.nasa.worldwind.avlist.AVKey;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.gov.ga.earthsci.core.intent.AbstractRetrieveIntentHandler;
import au.gov.ga.earthsci.core.retrieve.IRetrievalData;
import au.gov.ga.earthsci.intent.IIntentCallback;
import au.gov.ga.earthsci.intent.Intent;
import au.gov.ga.earthsci.worldwind.common.layers.borehole.providers.GocadWellBoreholeProvider;
import au.gov.ga.earthsci.worldwind.common.layers.model.gocad.GocadReader;
import au.gov.ga.earthsci.worldwind.common.layers.model.gocad.GocadSGridReader;
import au.gov.ga.earthsci.worldwind.common.util.ColorMap;
import au.gov.ga.earthsci.worldwind.common.util.FileUtil;
import au.gov.ga.earthsci.worldwind.common.util.XMLUtil;

/**
 * Intent handler implementation for GOCAD objects.
 *
 * @author Michael de Hoog
 */
public class GOCADLayerIntentHandler extends AbstractRetrieveIntentHandler
{
	@Inject
	@Named(IServiceConstants.ACTIVE_SHELL)
	private Shell shell;

	@Override
	protected void handle(final IRetrievalData data, final URL url, final Intent intent, final IIntentCallback callback)
	{
		shell.getDisplay().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					String extension = FileUtil.getExtension(url.toString());
					String filename = FileUtil.getFilename(url.toString());
					boolean sgrid = extension.toLowerCase().equals("sg"); //$NON-NLS-1$
					boolean well = extension.toLowerCase().equals("wl"); //$NON-NLS-1$

					InitialParameters initialParameters = InitialParameters.readFromGOCADObject(data);
					well |= initialParameters.well;
					sgrid |= initialParameters.sgrid;

					//wells don't allow property painting
					String[] properties = well ? null : initialParameters.properties;

					final GOCADLayerParameters params = new GOCADLayerParameters();
					WizardDialog dialog =
							new WizardDialog(shell, new GOCADLayerParametersWizard(params, properties, false));
					dialog.setPageSize(400, 500);
					dialog.open();

					if (dialog.getReturnCode() != WizardDialog.OK)
					{
						callback.aborted(intent);
						return;
					}

					String layerType = well ? "BoreholeLayer" : sgrid ? "VolumeLayer" : "ModelLayer"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					String dataFormat = well ? "GOCAD Well" : sgrid ? "GOCAD SGrid" : "GOCAD"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					ColorMap colorMap = params.getColorMap() == null ? null : params.getColorMap().toLegacy();
					Color nodataColor =
							params.getColorMap() == null ? null : params.getColorMap().getNodataColour();
					String paintedVariable = params.getPaintedVariable();

					DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
					Document doc = docBuilder.newDocument();
					Element rootElement = doc.createElement("Layer"); //$NON-NLS-1$
					doc.appendChild(rootElement);
					rootElement.setAttribute("version", "1"); //$NON-NLS-1$ //$NON-NLS-2$
					rootElement.setAttribute("layerType", layerType); //$NON-NLS-1$
					XMLUtil.appendText(rootElement, "URL", url.toString()); //$NON-NLS-1$
					XMLUtil.appendText(rootElement, "DataFormat", dataFormat); //$NON-NLS-1$
					XMLUtil.appendText(rootElement, "DisplayName", filename); //$NON-NLS-1$
					XMLUtil.appendText(rootElement, "DataCacheName", filename); //$NON-NLS-1$
					XMLUtil.appendText(rootElement, "CoordinateSystem", params.getSourceProjection()); //$NON-NLS-1$
					if (colorMap != null)
					{
						XMLUtil.appendColorMap(rootElement, "ColorMap", colorMap); //$NON-NLS-1$
					}
					if (nodataColor != null)
					{
						XMLUtil.appendColor(rootElement, "NoDataColor", nodataColor); //$NON-NLS-1$
					}
					if (paintedVariable != null)
					{
						XMLUtil.appendText(rootElement, "PaintedVariable", paintedVariable); //$NON-NLS-1$
					}
					Object layer = BasicFactory.create(AVKey.LAYER_FACTORY, rootElement);
					callback.completed(layer, intent);
				}
				catch (Exception e)
				{
					callback.error(e, intent);
				}
			}
		});
	}

	public static class InitialParameters
	{
		public final String[] properties;
		public final boolean sgrid;
		public final boolean well;

		public InitialParameters(String[] properties, boolean sgrid, boolean well)
		{
			this.properties = properties;
			this.sgrid = sgrid;
			this.well = well;
		}

		public static InitialParameters readFromGOCADObject(IRetrievalData data) throws IOException
		{
			List<String> list = new ArrayList<String>();
			boolean sgrid = false, well = false;
			InputStream is = null;
			try
			{
				is = data.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));
				String line;
				while ((line = reader.readLine()) != null)
				{
					Matcher matcher;

					matcher = GocadReader.propertiesPattern.matcher(line);
					if (matcher.matches())
					{
						String properties = matcher.group(1).trim();
						String[] split = properties.split("\\s+"); //$NON-NLS-1$
						list.addAll(Arrays.asList(split));
						continue;
					}

					matcher = GocadSGridReader.propertyNamePattern.matcher(line);
					if (matcher.matches())
					{
						String propertyName = matcher.group(2);
						list.add(propertyName);
						continue;
					}

					matcher = GocadSGridReader.headerPattern.matcher(line);
					if (matcher.matches())
					{
						sgrid = true;
						continue;
					}

					matcher = GocadWellBoreholeProvider.headerPattern.matcher(line);
					if (matcher.matches())
					{
						well = true;
						continue;
					}
				}
				String[] properties = list.toArray(new String[list.size()]);
				return new InitialParameters(properties, sgrid, well);
			}
			finally
			{
				if (is != null)
				{
					try
					{
						is.close();
					}
					catch (IOException e)
					{
					}
				}
			}
		}
	}
}
