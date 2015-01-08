/*******************************************************************************
 * Copyright 2014 Geoscience Australia
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
package au.gov.ga.earthsci.worldwind.common.view.oculus;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.oculusvr.capi.Posef;

/**
 * Utility class that logs all Oculus head movement to a custom log file.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class RiftLogger
{
	private static File output = new File("oculus_" + System.currentTimeMillis() + ".log");
	private static DataOutputStream dos;

	static
	{
		try
		{
			dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(output)));
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	public static void close()
	{
		try
		{
			dos.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static synchronized void logPose(Posef[] poses)
	{
		long millis = System.currentTimeMillis();
		long nanos = System.nanoTime();
		try
		{
			dos.writeLong(millis);
			dos.writeLong(nanos);
			for (int i = 0; i < 2; i++)
			{
				dos.writeFloat(poses[i].Orientation.x);
				dos.writeFloat(poses[i].Orientation.y);
				dos.writeFloat(poses[i].Orientation.z);
				dos.writeFloat(poses[i].Orientation.w);
				dos.writeFloat(poses[i].Position.x);
				dos.writeFloat(poses[i].Position.y);
				dos.writeFloat(poses[i].Position.z);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
