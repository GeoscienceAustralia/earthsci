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
package au.gov.ga.earthsci.worldwind.common.view.hmd.oculus;

import gov.nasa.worldwind.geom.Angle;
import au.gov.ga.earthsci.worldwind.common.view.hmd.IHMDParameters;

import com.oculusvr.jrift.JRiftActivator;

import de.fruitfly.ovr.IOculusRift;
import de.fruitfly.ovr.OculusRift;

/**
 * Multiple instances of the {@link OculusRift} object are not supported, so an
 * single instance can be accessed using this class.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class OculusSingleton
{
	private final static OculusSingleton INSTANCE = new OculusSingleton();

	public static OculusSingleton getInstance()
	{
		return INSTANCE;
	}

	private final static int POLL_DELAY = 10; //ms
	private final IOculusRift oculus;
	private final IHMDParameters parameters;
	private final OculusListenerList listeners = new OculusListenerList();

	public OculusSingleton()
	{
		oculus = new OculusRift();
		if (!JRiftActivator.isLibraryLoaded())
		{
			parameters = null;
			return;
		}

		boolean initialized = oculus.init();
		if (!initialized)
		{
			parameters = null;
			return;
		}

		parameters = new OculusHMDParameters(oculus.getHMDInfo());
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				while (true)
				{
					try
					{
						Thread.sleep(POLL_DELAY);
					}
					catch (InterruptedException e)
					{
					}
					oculus.poll();
					listeners.trackingUpdated(Angle.fromDegrees(oculus.getYawDegrees_LH()),
							Angle.fromDegrees(oculus.getPitchDegrees_LH()),
							Angle.fromDegrees(oculus.getRollDegrees_LH()));
				}
			}
		});
		thread.setDaemon(true);
		thread.setName("Oculus poller");
		thread.start();
	}

	public boolean isInitialized()
	{
		return oculus.isInitialized();
	}

	public IHMDParameters getParameters()
	{
		return parameters;
	}

	public void addListener(IOculusListener listener)
	{
		listeners.add(listener);
	}

	public void removeListener(IOculusListener listener)
	{
		listeners.remove(listener);
	}
}
