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
package au.gov.ga.earthsci.core.model.worldwind;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import au.gov.ga.earthsci.common.util.IPropertyChangeBean;
import au.gov.ga.earthsci.model.IModel;
import au.gov.ga.earthsci.model.render.ModelRenderer;

/**
 * A basic mutable implementation of the {@link IModelLayer} interface.
 * <p/>
 * Provides access to the underlying model list and allows additional models to
 * be added to the layer, and the entire model list to be changed as
 * appropriate.
 * <p/>
 * This implementation is thread safe and allows safe addition and removal of
 * models. Modifications to the model list should be done through the provided
 * {@code add} and {@code remove} methods.
 * <p/>
 * <b>Events:</b>
 * <dl>
 * <dt>{@value #MODELS_PROPERTY_NAME}</dt>
 * <dd>Fired when a change is made to the models list contained in this layer</dd>
 * </dl>
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class BasicModelLayer extends AbstractLayer implements IModelLayer, IPropertyChangeBean
{

	public static final String MODELS_PROPERTY_NAME = "models"; //$NON-NLS-1$

	private List<IModel> models = new ArrayList<IModel>();
	private ReadWriteLock modelsLock = new ReentrantReadWriteLock();

	private ModelRenderer renderer = new ModelRenderer();

	/**
	 * Create an empty, unnamed model layer
	 */
	public BasicModelLayer()
	{
	}

	/**
	 * Create an empty model layer with the given name
	 */
	public BasicModelLayer(String name)
	{
		this(name, (IModel[]) null);
	}

	/**
	 * Create a model layer with the given name and models
	 * 
	 * @param name
	 *            The name for the layer
	 * @param models
	 *            The models to include in the layer
	 */
	public BasicModelLayer(String name, IModel... models)
	{
		setName(name);

		if (models != null)
		{
			this.models.addAll(Arrays.asList(models));
		}
	}

	/**
	 * Create a model layer with the given initialisation parameters and models
	 * 
	 * @param params
	 *            The parameters to create the model from
	 * @param models
	 *            The models to include in the layer
	 */
	public BasicModelLayer(AVList params, IModel... models)
	{
		if (params != null)
		{
			setValues(params);
		}
		if (models != null)
		{
			this.models.addAll(Arrays.asList(models));
		}
	}

	@Override
	public List<IModel> getModels()
	{
		return Collections.unmodifiableList(models);
	}

	/**
	 * Add the provided models to this layer
	 * <p/>
	 * Added models will be rendered on the next render call.
	 * 
	 * @param models
	 *            The models to add
	 */
	public void add(IModel... models)
	{
		if (models == null || models.length == 0)
		{
			return;
		}

		add(Arrays.asList(models));
	}

	/**
	 * Add the provided models to this layer
	 * <p/>
	 * Added models will be rendered on the next render call.
	 * 
	 * @param models
	 *            The models to add
	 */
	public void add(Collection<IModel> models)
	{
		if (models == null || models.isEmpty())
		{
			return;
		}

		modelsLock.writeLock().lock();
		try
		{
			this.models.addAll(models);
			firePropertyChange(MODELS_PROPERTY_NAME, null, this.models);
		}
		finally
		{
			modelsLock.writeLock().unlock();
		}
	}

	/**
	 * Remove the provided models from this layer, if they exist.
	 * 
	 * @param models
	 *            The models to remove
	 */
	public void remove(IModel... models)
	{
		if (models == null || models.length == 0)
		{
			return;
		}
		remove(Arrays.asList(models));
	}

	/**
	 * Remove the provided models from this layer, if they exist.
	 * 
	 * @param models
	 *            The models to remove
	 */
	public void remove(Collection<IModel> models)
	{
		if (models == null || models.isEmpty())
		{
			return;
		}

		modelsLock.writeLock().lock();
		try
		{
			models.removeAll(models);
			firePropertyChange(MODELS_PROPERTY_NAME, null, this.models);
		}
		finally
		{
			modelsLock.writeLock().unlock();
		}
	}

	/**
	 * Remove all models from this layer
	 */
	public void clear()
	{
		modelsLock.writeLock().lock();
		try
		{
			models.clear();
			firePropertyChange(MODELS_PROPERTY_NAME, null, this.models);
		}
		finally
		{
			modelsLock.writeLock().unlock();
		}
	}

	@Override
	protected void doRender(DrawContext dc)
	{
		if (!isEnabled())
		{
			return;
		}

		modelsLock.readLock().lock();
		try
		{
			for (IModel model : models)
			{
				renderer.render(model);
			}
		}
		finally
		{
			modelsLock.readLock().unlock();
		}

	}


	/**
	 * @param renderer
	 *            the renderer to set
	 */
	public void setRenderer(ModelRenderer renderer)
	{
		this.renderer = renderer;
	}

}
