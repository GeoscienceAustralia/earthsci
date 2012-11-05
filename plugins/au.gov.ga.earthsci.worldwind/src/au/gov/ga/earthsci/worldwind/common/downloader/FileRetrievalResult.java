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
package au.gov.ga.earthsci.worldwind.common.downloader;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.nio.ByteBuffer;

/**
 * Extension of the {@link ByteBufferRetrievalResult} which fills the ByteBuffer
 * from a File. Used for returning results from the local cache.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class FileRetrievalResult extends ByteBufferRetrievalResult
{
	private final File file;

	public FileRetrievalResult(URL sourceURL, File file, boolean fromCache)
	{
		super(sourceURL, readFile(file), fromCache, false, null, null);
		this.file = file;
	}

	private static ByteBuffer readFile(File file)
	{
		if (!file.exists() || file.isDirectory() || !file.canRead())
			return null;
		ByteBuffer buffer = ByteBuffer.allocate((int) file.length());
		try
		{
			FileInputStream fis = new FileInputStream(file);
			fis.read(buffer.array());
			fis.close();
			return buffer;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/**
	 * @return Last Modification Date of the file
	 */
	public Long lastModified()
	{
		if (hasData())
			return file.lastModified();
		return null;
	}
}
