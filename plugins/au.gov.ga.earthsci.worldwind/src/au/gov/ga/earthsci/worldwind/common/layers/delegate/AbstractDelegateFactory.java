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
package au.gov.ga.earthsci.worldwind.common.layers.delegate;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.layers.Layer;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.layers.delegate.reader.MaskImageReaderDelegate;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.render.ElevationOffsetRenderDelegate;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.render.IgnoreElevationRenderDelegate;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.retriever.HttpRetrieverFactoryDelegate;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.retriever.PassThroughZipRetrieverFactoryDelegate;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.transformer.ColorLimitTransformerDelegate;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.transformer.ColorToAlphaTransformerDelegate;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.transformer.FilterTransformerDelegate;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.transformer.ResizeTransformerDelegate;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.transformer.StripingFilterTransformerDelegate;
import au.gov.ga.earthsci.worldwind.common.layers.delegate.transformer.TransparentColorTransformerDelegate;

/**
 * Abstract implmentation of the {@link IDelegateFactory}. It contains all the
 * general delegates that are supported on all tiled {@link Layer}s.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public abstract class AbstractDelegateFactory implements IDelegateFactory
{
	private Map<Class<? extends IDelegate>, IDelegate> classToInstanceMap =
			new HashMap<Class<? extends IDelegate>, IDelegate>();
	private Map<Class<? extends IDelegate>, Class<? extends IDelegate>> replacementClasses =
			new HashMap<Class<? extends IDelegate>, Class<? extends IDelegate>>();

	protected AbstractDelegateFactory()
	{
		registerDelegate(HttpRetrieverFactoryDelegate.class);
		registerDelegate(PassThroughZipRetrieverFactoryDelegate.class);

		registerDelegate(MaskImageReaderDelegate.class);

		registerDelegate(ColorToAlphaTransformerDelegate.class);
		registerDelegate(TransparentColorTransformerDelegate.class);
		registerDelegate(StripingFilterTransformerDelegate.class);
		registerDelegate(ResizeTransformerDelegate.class);
		registerDelegate(ColorLimitTransformerDelegate.class);
		registerDelegate(FilterTransformerDelegate.class);
		
		registerDelegate(ElevationOffsetRenderDelegate.class);
		registerDelegate(IgnoreElevationRenderDelegate.class);
	}

	@Override
	public void registerDelegate(Class<? extends IDelegate> delegateClass)
	{
		if (!classToInstanceMap.containsKey(delegateClass))
		{
			try
			{
				Constructor<? extends IDelegate> c = delegateClass.getDeclaredConstructor();
				c.setAccessible(true);
				classToInstanceMap.put(delegateClass, c.newInstance());
			}
			catch (Exception e)
			{
				throw new IllegalArgumentException("Error instanciating delegate", e);
			}
		}
	}

	@Override
	public void registerReplacementClass(Class<? extends IDelegate> fromClass, Class<? extends IDelegate> toClass)
	{
		replacementClasses.put(fromClass, toClass);
	}

	@Override
	public IDelegate createDelegate(String definition, Element layerElement, AVList params)
	{
		for (IDelegate delegate : classToInstanceMap.values())
		{
			IDelegate d = delegate.fromDefinition(definition, layerElement, params);
			if (d != null)
			{
				if (replacementClasses.containsKey(d.getClass()))
				{
					Class<? extends IDelegate> replacementClass = replacementClasses.get(d.getClass());
					IDelegate instance = classToInstanceMap.get(replacementClass);
					IDelegate newInstance = instance.fromDefinition(definition, layerElement, params);
					if (newInstance != null)
					{
						return newInstance;
					}
				}
				return d;
			}
		}
		throw new IllegalArgumentException("Don't know how to create delegate from definition: " + definition);
	}
}
