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

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.util.BufferWrapper;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;

import au.gov.ga.earthsci.worldwind.common.downloader.ZipRetriever;

/**
 * Utility methods for reading/writing data.
 */
public class IOUtil extends WWIO
{
	/**
	 * Read the given stream into a string, keeping newlines.
	 * 
	 * @param stream
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
	public static String readStreamToStringKeepingNewlines(InputStream stream, String encoding) throws IOException
	{
		if (stream == null)
		{
			String message = Logging.getMessage("nullValue.InputStreamIsNull");
			Logging.logger().severe(message);
			throw new IllegalArgumentException(message);
		}

		if (encoding == null)
		{
			encoding = DEFAULT_CHARACTER_ENCODING;
		}

		StringBuilder sb = new StringBuilder();
		byte[] buffer = new byte[1024];
		int length = 0;
		while ((length = stream.read(buffer)) >= 0)
		{
			sb.append(new String(buffer, 0, length, encoding));
		}

		return sb.toString();
	}

	/**
	 * Read the bytes from the resource referenced by the provided url.
	 * <p/>
	 * If the URL references a zip archive, the returned buffer will contain the
	 * contents of the un-zipped resource.
	 */
	public static ByteBuffer readByteBuffer(URL url) throws IOException
	{
		ByteBuffer byteBuffer = null;
		if (URLUtil.isForResourceWithExtension(url, "zip"))
		{
			//try opening the file as a zip file; if this fails, log a warning and read file directly into the buffer
			InputStream is = null;
			try
			{
				is = url.openStream();
				ZipRetriever zr = new ZipRetriever(url);
				byteBuffer = zr.readZipStream(is, url);
			}
			catch (Exception e)
			{
				String message = "Error loading zip file at '" + url + "': " + e.getLocalizedMessage();
				Logging.logger().warning(message);
			}
			finally
			{
				if (is != null)
					is.close();
			}
		}
		if (byteBuffer == null)
		{
			byteBuffer = readURLContentToBuffer(url);
		}
		return byteBuffer;
	}

	/**
	 * Read the bytes from the resource referenced by the provided url.
	 * <p/>
	 * The provided pixel types and byte ordering is used to wrap the
	 * {@link ByteBuffer} with a {@link BufferWrapper} that can manipulate the
	 * underlying data if needed.
	 */
	public static BufferWrapper readByteBuffer(URL url, String pixelType, String byteOrder) throws IOException
	{
		ByteBuffer byteBuffer = readByteBuffer(url);

		AVList bufferParams = new AVListImpl();
		bufferParams.setValue(AVKey.DATA_TYPE, pixelType);
		bufferParams.setValue(AVKey.BYTE_ORDER, byteOrder);
		BufferWrapper wrapper = BufferWrapper.wrap(byteBuffer, bufferParams);

		return wrapper;
	}

	/**
	 * Read the bytes from the resource referenced by the provided InputStream.
	 * <p/>
	 * The provided pixel types and byte ordering is used to wrap the
	 * {@link ByteBuffer} with a {@link BufferWrapper} that can manipulate the
	 * underlying data if needed.
	 */
	public static BufferWrapper readByteBuffer(InputStream is, String pixelType, String byteOrder) throws IOException
	{
		ByteBuffer byteBuffer = readStreamToBuffer(is);

		AVList bufferParams = new AVListImpl();
		bufferParams.setValue(AVKey.DATA_TYPE, pixelType);
		bufferParams.setValue(AVKey.BYTE_ORDER, byteOrder);
		BufferWrapper wrapper = BufferWrapper.wrap(byteBuffer, bufferParams);

		return wrapper;
	}
}
