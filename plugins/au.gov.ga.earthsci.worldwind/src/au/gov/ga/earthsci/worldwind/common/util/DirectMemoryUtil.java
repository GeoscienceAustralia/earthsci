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
package au.gov.ga.earthsci.worldwind.common.util;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;

/**
 * Helper class for retrieving (via reflection) the amount of memory used by
 * Direct {@link ByteBuffer}s.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class DirectMemoryUtil
{
	static
	{
		try
		{
			Class<?> bitsClass = Class.forName("java.nio.Bits");
			Field reservedMemoryField = bitsClass.getDeclaredField("reservedMemory");
			Field maxMemoryField = bitsClass.getDeclaredField("maxMemory");
			reservedMemoryField.setAccessible(true);
			maxMemoryField.setAccessible(true);
			DirectMemoryUtil.reservedMemoryField = reservedMemoryField;
			DirectMemoryUtil.maxMemoryField = maxMemoryField;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static Field reservedMemoryField;
	private static Field maxMemoryField;

	public static long getReservedMemory()
	{
		try
		{
			return reservedMemoryField.getLong(null);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return -1;
		}
	}

	public static long getMaxMemory()
	{
		try
		{
			return maxMemoryField.getLong(null);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return -1;
		}
	}

	public static void startMemoryReportingThread(final PrintStream output, final long millis)
	{
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				while (true)
				{
					double r = getReservedMemory() / 1024d / 1024d;
					double d = getMaxMemory() / 1024d / 1024d;
					double t = Runtime.getRuntime().totalMemory() / 1024d / 1024d;
					double f = Runtime.getRuntime().freeMemory() / 1024d / 1024d;
					double m = Runtime.getRuntime().maxMemory() / 1024d / 1024d;
					output.println("Reserved memory = " + r + "/" + d + ", total memory = " + t + "/" + m
							+ ", free memory = " + f);
					try
					{
						Thread.sleep(millis);
					}
					catch (InterruptedException e)
					{
					}
				}
			}
		});
		thread.setDaemon(true);
		thread.start();
	}
}
