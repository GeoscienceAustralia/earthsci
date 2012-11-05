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
package au.gov.ga.earthsci.worldwind.common.layers;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.util.WWXML;

import java.util.HashSet;
import java.util.Set;

import javax.xml.xpath.XPath;

import org.w3c.dom.Element;

import au.gov.ga.earthsci.worldwind.common.util.AVKeyMore;

/**
 * Provides timed expiration functionality to World Wind layers. Allows the
 * ability to define a layer that will expire every x timespans, which will mark
 * all textures as expired and re-download them. Useful for layers that update
 * every few hours, such as weather layers.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class TimedExpirationHandler
{
	protected final static String DATE_TIME_PATTERN = "dd MM yyyy HH:mm:ss z";

	private final static ExpiryUpdater updater = new ExpiryUpdater();

	/**
	 * Read expiration params from XML into the AVList
	 * 
	 * @param domElement
	 *            XML element
	 * @param params
	 * @return params
	 */
	public static AVList getExpirationParams(Element domElement, AVList params)
	{
		if (params == null)
			params = new AVListImpl();

		XPath xpath = WWXML.makeXPath();
		WWXML.checkAndSetLongParam(domElement, params, AVKeyMore.EXPIRY_START_TIME,
				"TimedExpiry/Start", xpath);
		WWXML.checkAndSetDateTimeParam(domElement, params, AVKeyMore.EXPIRY_START_TIME,
				"TimedExpiry/StartDate", DATE_TIME_PATTERN, xpath);
		WWXML.checkAndSetTimeParam(domElement, params, AVKeyMore.EXPIRY_TIMESPAN,
				"TimedExpiry/Time", xpath);

		return params;
	}

	/**
	 * Register a layer with TimedExpiration parameters.
	 * 
	 * @param layer
	 * @param params
	 */
	public static void registerLayer(Layer layer, AVList params)
	{
		long start = 0, time = 0;

		Long l = (Long) params.getValue(AVKeyMore.EXPIRY_START_TIME);
		if (l != null)
			start = l;

		l = (Long) params.getValue(AVKeyMore.EXPIRY_TIMESPAN);
		if (l != null)
			time = l;

		if (time <= 0)
			return;

		layer.setValue(AVKeyMore.EXPIRY_START_TIME, start);
		layer.setValue(AVKeyMore.EXPIRY_TIMESPAN, time);
		updater.registerLayer(layer);
	}

	private static void updateExpiry(Layer layer, long current)
	{
		Long start = (Long) layer.getValue(AVKeyMore.EXPIRY_START_TIME);
		Long time = (Long) layer.getValue(AVKeyMore.EXPIRY_TIMESPAN);

		if (start == null || time == null || time <= 0)
			return;

		long diff = current - start;

		//if start time hasn't come yet, don't set the expiry
		if (diff <= 0)
			return;

		long spans = diff / time; //long division
		long last = spans * time + start;

		layer.setExpiryTime(last);
	}

	private static class ExpiryUpdater
	{
		private Set<Layer> layers = new HashSet<Layer>();
		private boolean threadStarted = false;

		public void registerLayer(Layer layer)
		{
			synchronized (layers)
			{
				startThreadIfRequired();
				layers.add(layer);
			}
		}

		private void startThreadIfRequired()
		{
			if (!threadStarted)
			{
				startUpdaterThread();
				threadStarted = true;
			}
		}

		private void startUpdaterThread()
		{
			Runnable runnable = new Runnable()
			{
				@Override
				public void run()
				{
					while (true)
					{
						try
						{
							Thread.sleep(1000);
						}
						catch (InterruptedException e)
						{
							e.printStackTrace();
						}

						synchronized (layers)
						{
							long current = System.currentTimeMillis();
							for (Layer layer : layers)
							{
								if (layer.isEnabled())
									updateExpiry(layer, current);
							}
						}
					}
				}
			};

			Thread thread = new Thread(runnable);
			thread.setDaemon(true);
			thread.setName("Layer timed expiry updater");
			thread.start();
		}
	}
}
