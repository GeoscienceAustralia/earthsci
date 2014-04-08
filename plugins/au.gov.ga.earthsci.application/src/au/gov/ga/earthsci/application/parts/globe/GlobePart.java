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

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.WorldMapLayer;
import gov.nasa.worldwindx.examples.ClickAndGoSelectListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarSeparator;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.application.parts.globe.handlers.ToggleHudHandler;
import au.gov.ga.earthsci.core.worldwind.view.DoubleClickZoomListener;
import au.gov.ga.earthsci.core.worldwind.view.TargetOrbitView;
import au.gov.ga.earthsci.layer.hud.HudLayer;
import au.gov.ga.earthsci.layer.hud.HudLayers;
import au.gov.ga.earthsci.layer.worldwind.ITreeModel;
import au.gov.ga.earthsci.newt.swt.NewtInputHandlerSWT;
import au.gov.ga.earthsci.newt.swt.WorldWindowNewtAutoDrawableSWT;
import au.gov.ga.earthsci.newt.swt.WorldWindowNewtCanvasSWT;
import au.gov.ga.earthsci.worldwind.common.WorldWindowRegistry;

/**
 * Part which displays a {@link WorldWindow}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class GlobePart
{
	private static final Logger logger = LoggerFactory.getLogger(GlobePart.class);
	public final static String TOOLBAR_ID = "au.gov.ga.earthsci.application.part.globe.toolbar"; //$NON-NLS-1$
	public final static String HUD_ELEMENT_TAG = "au.gov.ga.earthsci.core.hudLayers"; //$NON-NLS-1$

	@Inject
	private ITreeModel model;

	@Inject
	private IEclipseContext context;

	private WorldWindow worldWindow;
	private GlobeSceneController sceneController;
	private final Map<String, Layer> hudLayers = new HashMap<String, Layer>();

	@Inject
	private EModelService service;

	@Inject
	private MApplication application;

	@Inject
	private MPart part;

	@Inject
	public void init(final Composite parent)
	{
		context.set(GlobePart.class, this);

		Configuration.setValue(AVKey.INPUT_HANDLER_CLASS_NAME, NewtInputHandlerSWT.class.getName());
		Configuration.setValue(AVKey.WORLD_WINDOW_CLASS_NAME, WorldWindowNewtAutoDrawableSWT.class.getName());
		Configuration.setValue(AVKey.SCENE_CONTROLLER_CLASS_NAME, GlobeSceneController.class.getName());
		worldWindow = new WorldWindowNewtCanvasSWT(parent, SWT.NONE, WorldWindowRegistry.INSTANCE.getFirstRegistered());
		sceneController = (GlobeSceneController) worldWindow.getSceneController();
		worldWindow.setModel(model);
		worldWindow.setView(new TargetOrbitView());
		worldWindow.addSelectListener(new ClickAndGoSelectListener(worldWindow, WorldMapLayer.class));
		context.set(WorldWindow.class, worldWindow);
		worldWindow.getInputHandler().addMouseListener(new DoubleClickZoomListener(worldWindow, 5000d));

		WorldWindowRegistry.INSTANCE.register(worldWindow);

		createHudLayers();
	}

	@PreDestroy
	private void preDestroy()
	{
		WorldWindowRegistry.INSTANCE.unregister(worldWindow);
	}

	public WorldWindow getWorldWindow()
	{
		return worldWindow;
	}

	public Layer getHudLayerForId(String id)
	{
		return hudLayers.get(id);
	}

	protected void createHudLayers()
	{
		MToolBar toolbar = part.getToolbar();
		if (toolbar != null)
		{
			//first clear the old elements from the model
			List<MUIElement> hudElements =
					service.findElements(toolbar, null, null, Arrays.asList(new String[] { HUD_ELEMENT_TAG }));
			for (MUIElement hudElement : hudElements)
			{
				// For some reason removing the element from it's parent doesn't hide the element, so make it invisible
				// Important - change the visibility before removing from the toolbar
				hudElement.setToBeRendered(false);
				hudElement.setVisible(false);
				toolbar.getChildren().remove(hudElement);
			}

			//find the hud toggle command
			MCommand command = null;
			for (MCommand c : application.getCommands())
			{
				if (ToggleHudHandler.HUD_COMMAND_ID.equals(c.getElementId()))
				{
					command = c;
					break;
				}
			}

			if (command != null)
			{
				//create new tool items for each hud layer
				boolean separatorAdded = false;
				int index = 0;
				for (HudLayer l : HudLayers.get())
				{
					if (!separatorAdded)
					{
						MToolBarSeparator separator = MMenuFactory.INSTANCE.createToolBarSeparator();
						toolbar.getChildren().add(0, separator);
						separator.getTags().add(HUD_ELEMENT_TAG);
						separatorAdded = true;
					}

					try
					{
						Layer layer = l.getLayerClass().newInstance();
						hudLayers.put(l.getId(), layer);
						if (l.getLabel() != null)
						{
							layer.setName(l.getLabel());
						}
						layer.setEnabled(l.isEnabled());
						layer.setPickEnabled(true);
						sceneController.getPostLayers().add(layer);

						String toolItemId = l.getId() + ".toolitem"; //$NON-NLS-1$
						MHandledToolItem toolItem = MMenuFactory.INSTANCE.createHandledToolItem();
						toolItem.getTags().add(HUD_ELEMENT_TAG);
						toolItem.setIconURI(l.getIconURI());
						toolItem.setCommand(command);
						toolItem.setElementId(toolItemId);
						toolItem.setType(ItemType.CHECK);
						toolItem.setSelected(l.isEnabled());
						toolItem.setTooltip(Messages.bind(Messages.GlobePart_ToggleHUDTooltip, layer.getName()));

						MParameter parameter = MCommandsFactory.INSTANCE.createParameter();
						parameter.setName(ToggleHudHandler.HUD_ID_PARAMETER_ID);
						parameter.setValue(l.getId());
						toolItem.getParameters().add(parameter);

						toolbar.getChildren().add(index++, toolItem);
					}
					catch (Exception e)
					{
						logger.error("Error creating hud layer", e); //$NON-NLS-1$
					}
				}
			}
		}
	}
}
