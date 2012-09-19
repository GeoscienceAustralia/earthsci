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
package au.gov.ga.earthsci.core.worldwind;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.globes.Globe;
import au.gov.ga.earthsci.core.model.layer.DefaultLayers;
import au.gov.ga.earthsci.core.model.layer.FolderNode;

/**
 * {@link BasicModel} subclass used to override specific functionality, such as
 * the LayerList creation.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class WorldWindModel extends BasicModel implements TreeModel
{
	private final FolderNode rootNode;

	public WorldWindModel()
	{
		this(createRootNode());
	}

	private WorldWindModel(FolderNode rootNode)
	{
		super(createGlobe(), rootNode.getLayerList());
		this.rootNode = rootNode;
	}

	protected static FolderNode createRootNode()
	{
		FolderNode rootNode = new FolderNode();
		rootNode.setName("root"); //$NON-NLS-1$
		rootNode.add(DefaultLayers.getDefaultLayers());
		return rootNode;
	}

	protected static Globe createGlobe()
	{
		return (Globe) WorldWind.createConfigurationComponent(AVKey.GLOBE_CLASS_NAME);
	}

	@Override
	public FolderNode getRootNode()
	{
		return rootNode;
	}
}
