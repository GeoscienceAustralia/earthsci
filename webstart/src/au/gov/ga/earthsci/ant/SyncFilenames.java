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
package au.gov.ga.earthsci.ant;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Ant task that deletes any files from a destination directory that don't exist
 * in a source directory.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class SyncFilenames extends Task
{
	private String fromdir;
	private String todir;
	private boolean keeppacked = false;

	public void setFromdir(String fromdir)
	{
		this.fromdir = fromdir;
	}

	public void setTodir(String todir)
	{
		this.todir = todir;
	}

	public void setKeeppacked(boolean keeppacked)
	{
		this.keeppacked = keeppacked;
	}

	@Override
	public void execute() throws BuildException
	{
		Set<String> fromdirFiles = new HashSet<String>();
		Set<String> todirFiles = new HashSet<String>();

		File src = new File(fromdir);
		File dst = new File(todir);

		if (src.isDirectory() && dst.isDirectory())
		{
			List<String> fromdirList = Arrays.asList(src.list());
			fromdirFiles.addAll(fromdirList);
			if (keeppacked)
			{
				for (String filename : fromdirList)
				{
					fromdirFiles.add(filename + ".pack.gz");
				}
			}

			todirFiles.addAll(Arrays.asList(dst.list()));

			todirFiles.removeAll(fromdirFiles);
			for (String filename : todirFiles)
			{
				File file = new File(dst, filename);
				System.out.println("Deleting file: " + file);
				file.delete();
			}
		}
	}
}
