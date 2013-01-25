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
import gov.nasa.worldwind.layers.LayerList;

import java.io.File;
import java.io.FileNotFoundException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.core.model.layer.DefaultLayers;
import au.gov.ga.earthsci.core.model.layer.FolderNode;
import au.gov.ga.earthsci.core.model.layer.ILayerTreeNode;
import au.gov.ga.earthsci.core.model.layer.LayerPersister;
import au.gov.ga.earthsci.core.tree.ITreeNode;
import au.gov.ga.earthsci.core.util.ConfigurationUtil;

/**
 * {@link BasicModel} subclass used to override specific functionality, such as
 * the LayerList creation.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@Singleton
public class WorldWindModel extends BasicModel implements ITreeModel
{
	private final ConstructionParameters constructionParameters;
	private final static Logger logger = LoggerFactory.getLogger(WorldWindModel.class);
	private static final String layerFilename = "layers.xml"; //$NON-NLS-1$
	private static final File layerFile = ConfigurationUtil.getWorkspaceFile(layerFilename);

	@Inject
	public WorldWindModel()
	{
		this(new ConstructionParameters());
		logger.info("Using layer file: " + layerFile); //$NON-NLS-1$
	}

	private WorldWindModel(ConstructionParameters constructionParameters)
	{
		super(constructionParameters.createGlobe(), constructionParameters.createLayerList());
		this.constructionParameters = constructionParameters;
	}

	@Override
	public ILayerTreeNode getRootNode()
	{
		return constructionParameters.rootNode;
	}

	@PostConstruct
	public void loadLayers()
	{
		loadRootNode(constructionParameters.rootNode);
	}

	@PreDestroy
	public void saveLayers()
	{
		saveRootNode(constructionParameters.rootNode);
	}

	protected static void loadRootNode(ILayerTreeNode rootNode)
	{
		ILayerTreeNode loadedNode = null;
		try
		{
			loadedNode = LayerPersister.loadLayers(layerFile);
		}
		catch (FileNotFoundException e)
		{
			//ignore
		}
		catch (Exception e)
		{
			logger.error("Error loading layer file", e); //$NON-NLS-1$
		}
		if (loadedNode == null)
		{
			FolderNode folder = DefaultLayers.getLayers();
			folder.setExpanded(true);
			rootNode.add(folder);
		}
		else
		{
			while (loadedNode.getChildCount() > 0)
			{
				ITreeNode<ILayerTreeNode> child = loadedNode.getChild(0);
				child.removeFromParent();
				rootNode.add(child);
			}
		}
	}

	protected static void saveRootNode(ILayerTreeNode rootNode)
	{
		try
		{
			LayerPersister.saveLayers(rootNode, layerFile);
		}
		catch (Exception e)
		{
			logger.error("Error saving layer file", e); //$NON-NLS-1$
		}
	}

	private static class ConstructionParameters
	{
		public FolderNode rootNode;

		public ConstructionParameters()
		{
			rootNode = new FolderNode();
			rootNode.setName("root"); //$NON-NLS-1$
			rootNode.setExpanded(true);
		}

		public Globe createGlobe()
		{
			Globe globe = (Globe) WorldWind.createConfigurationComponent(AVKey.GLOBE_CLASS_NAME);
			globe.setElevationModel(rootNode.getElevationModels());
			return globe;
		}

		public LayerList createLayerList()
		{
			return rootNode.getLayerList();
		}
	}
}
