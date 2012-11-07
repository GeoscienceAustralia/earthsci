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
package au.gov.ga.earthsci.application.util;

import java.awt.Component;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.GraphicsEnvironment;
import java.awt.KeyboardFocusManager;
import java.awt.peer.KeyboardFocusManagerPeer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.LinkedList;

/**
 * Reflection bug-fix for <a
 * href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6454631"
 * >http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6454631</a>.
 * <p/>
 * How this fix works: The {@link #clearGlobalFocusOwner()} method ends up
 * calling the static method markClearGlobalFocusOwner().
 * markClearGlobalFocusOwner() checks the heavyweightRequests field for any
 * objects. If it is not empty, the current focused window is retrieved from
 * that. Otherwise it is retrieved from the peer. Retrieving it from the peer
 * when the thread holds a lock on heavyweightRequests can sometimes cause a
 * deadlock. So the fix is to ensure that the current focused window is never
 * retrieved while holding a lock on heavyweightRequests, by passing the current
 * focused window wrapped in a HeavyweightFocusRequest. This can be done by
 * adding a new HeavyweightFocusRequest to the heavyweightRequests list, via
 * reflection.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class KeyboardFocusManagerFix extends DefaultKeyboardFocusManager
{
	public static void initialize()
	{
		try
		{
			//if JRE is made by Sun/Oracle and is Java 1.6 or less, install the fix
			//(the bug was fixed in Java 1.7)

			String version = System.getProperty("java.version"); //$NON-NLS-1$
			String vendor = System.getProperty("java.vendor"); //$NON-NLS-1$
			vendor = vendor.toLowerCase();
			version = version.replaceAll("\\D+", ""); //$NON-NLS-1$ //$NON-NLS-2$
			String twoDigitVersion = version.substring(0, 2);
			int intVersion = Integer.parseInt(twoDigitVersion);
			if (intVersion <= 16 && (vendor.contains("sun") || vendor.contains("oracle"))) //$NON-NLS-1$ //$NON-NLS-2$
			{
				setCurrentKeyboardFocusManager(new KeyboardFocusManagerFix());
			}
		}
		catch (Exception e)
		{
			//ignore
		}
	}

	static
	{
		LinkedList<Object> heavyweightRequestsLocal = null;
		try
		{
			Field heavyweightRequestsField = KeyboardFocusManager.class.getDeclaredField("heavyweightRequests"); //$NON-NLS-1$
			heavyweightRequestsField.setAccessible(true);
			@SuppressWarnings("unchecked")
			LinkedList<Object> heavyweightRequestsUnchecked = (LinkedList<Object>) heavyweightRequestsField.get(null);
			heavyweightRequestsLocal = heavyweightRequestsUnchecked;
		}
		catch (Exception e)
		{
		}
		superHeavyweightRequests = heavyweightRequestsLocal;
	}

	public KeyboardFocusManagerFix()
	{
		try
		{
			Field peerField = KeyboardFocusManager.class.getDeclaredField("peer"); //$NON-NLS-1$
			peerField.setAccessible(true);
			superPeer = (KeyboardFocusManagerPeer) peerField.get(this);
		}
		catch (Exception e)
		{
		}
	}

	private transient KeyboardFocusManagerPeer superPeer;
	private final static LinkedList<Object> superHeavyweightRequests;

	@Override
	public void clearGlobalFocusOwner()
	{
		if (!GraphicsEnvironment.isHeadless())
		{
			final Component nativeFocusedWindow = superPeer.getCurrentFocusedWindow();
			synchronized (superHeavyweightRequests)
			{
				if (superHeavyweightRequests.isEmpty())
				{
					addHeavyweightFocusRequest(nativeFocusedWindow);
				}
			}
		}
		super.clearGlobalFocusOwner();
	}

	private void addHeavyweightFocusRequest(Component heavyweight)
	{
		Class<?>[] classes = KeyboardFocusManager.class.getDeclaredClasses();
		Class<?> heavyweightFocusRequestClass = null;
		for (Class<?> c : classes)
		{
			if (c.getSimpleName().equals("HeavyweightFocusRequest")) //$NON-NLS-1$
			{
				heavyweightFocusRequestClass = c;
				break;
			}
		}
		if (heavyweightFocusRequestClass != null)
		{
			try
			{
				Field heavyweightField = heavyweightFocusRequestClass.getDeclaredField("heavyweight"); //$NON-NLS-1$
				heavyweightField.setAccessible(true);
				Constructor<?> heavyweightFocusRequestDefaultConstructor =
						heavyweightFocusRequestClass.getDeclaredConstructor();
				heavyweightFocusRequestDefaultConstructor.setAccessible(true);
				Object newHeavyweightFocusRequest = heavyweightFocusRequestDefaultConstructor.newInstance();
				heavyweightField.set(newHeavyweightFocusRequest, heavyweight);
				superHeavyweightRequests.add(newHeavyweightFocusRequest);
			}
			catch (Exception e)
			{
			}
		}
	}
}
