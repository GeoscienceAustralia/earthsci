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
package au.gov.ga.earthsci.core.retrieve.cache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.ga.earthsci.core.util.HashReadWriteLocker;
import au.gov.ga.earthsci.worldwind.common.util.Util;

/**
 * {@link IURLCache} implementation that uses a directory in a file system for
 * caching data.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class FileURLCache implements IURLCache
{
	private Logger logger = LoggerFactory.getLogger(FileURLCache.class);

	private final File directory;
	private final HashReadWriteLocker locker = new HashReadWriteLocker();
	private final static String PARTIAL_SUFFIX = ".partial"; //$NON-NLS-1$
	private final static String PARTIAL_SUFFIX_REPLACEMENT = ".partialr"; //$NON-NLS-1$
	private final static String CONTENT_TYPE_SUFFIX = ".contenttype"; //$NON-NLS-1$
	private final static String CONTENT_TYPE_SUFFIX_REPLACEMENT = ".contenttyper"; //$NON-NLS-1$

	public FileURLCache(File directory)
	{
		if (directory == null)
		{
			throw new NullPointerException("Directory cannot be null"); //$NON-NLS-1$
		}
		this.directory = directory;
	}

	@Override
	public boolean isPartial(URL url)
	{
		return isFileLocked(getPartialFile(url));
	}

	@Override
	public long getPartialLength(URL url)
	{
		return lengthLocked(getPartialFile(url));
	}

	@Override
	public long getPartialLastModified(URL url)
	{
		return lastModifiedLocked(getPartialFile(url));
	}

	@Override
	public OutputStream writePartial(URL url, long offset) throws IOException
	{
		final File partialFile = getPartialFile(url);
		locker.lockWrite(partialFile);
		RandomAccessFile raf = null;
		try
		{
			partialFile.getParentFile().mkdirs();
			raf = new RandomAccessFile(partialFile, "rw"); //$NON-NLS-1$
			partialFile.setReadable(true, false);
			partialFile.setWritable(true, false);
			FileChannel channel = raf.getChannel();
			offset = Math.max(0l, offset);
			channel.truncate(offset);
			channel.position(offset);
			return new FilterOutputStream(Channels.newOutputStream(channel))
			{
				@Override
				public void close() throws IOException
				{
					try
					{
						super.close();
					}
					finally
					{
						locker.unlockWrite(partialFile);
					}
				}
			};
		}
		catch (IOException e)
		{
			if (raf != null)
			{
				raf.close();
			}
			locker.unlockWrite(partialFile);
			throw e;
		}
	}

	@Override
	public void writeComplete(URL url, long lastModified, String contentType)
	{
		File partialFile = getPartialFile(url);
		File completeFile = getCompleteFile(url);
		locker.lockWrite(partialFile);
		locker.lockWrite(completeFile);
		try
		{
			partialFile.renameTo(completeFile);
			if (lastModified > 0)
			{
				completeFile.setLastModified(lastModified);
			}
			setContentType(url, contentType, completeFile);
		}
		finally
		{
			locker.unlockWrite(partialFile);
			locker.unlockWrite(completeFile);
		}
	}

	@Override
	public boolean isComplete(URL url)
	{
		return isFileLocked(getCompleteFile(url));
	}

	@Override
	public long getLength(URL url)
	{
		return lengthLocked(getCompleteFile(url));
	}

	@Override
	public long getLastModified(URL url)
	{
		return lastModifiedLocked(getCompleteFile(url));
	}

	@Override
	public String getContentType(URL url)
	{
		File contentTypeFile = getContentTypeFile(url);
		try
		{
			locker.lockRead(contentTypeFile);
			if (contentTypeFile.isFile())
			{
				if (contentTypeFile.length() > 0)
				{
					try
					{
						return readTextFile(contentTypeFile);
					}
					catch (IOException e)
					{
						logger.warn("Error reading content type for url: " + url, e); //$NON-NLS-1$
					}
				}
				return null;
			}
			else
			{
				File completeFile = getCompleteFile(url);
				return URLConnection.guessContentTypeFromName(completeFile.getName());
			}
		}
		finally
		{
			locker.unlockRead(contentTypeFile);
		}
	}

	private void setContentType(URL url, String contentType, File completeFile)
	{
		String guessedContentType = URLConnection.guessContentTypeFromName(completeFile.getName());
		if ((guessedContentType == null && contentType == null) || guessedContentType.equals(contentType))
		{
			//don't need to write a content type file if the URLConnection can guess the content type from the complete filename
			return;
		}

		File contentTypeFile = getContentTypeFile(url);
		try
		{
			writeTextFile(contentTypeFile, contentType);
		}
		catch (IOException e)
		{
			logger.warn("Error writing content type for url: " + url, e); //$NON-NLS-1$
		}
	}

	protected boolean isFileLocked(File file)
	{
		try
		{
			locker.lockRead(file);
			return file.isFile();
		}
		finally
		{
			locker.unlockRead(file);
		}
	}

	protected long lengthLocked(File file)
	{
		try
		{
			locker.lockRead(file);
			return file.length();
		}
		finally
		{
			locker.unlockRead(file);
		}
	}

	protected long lastModifiedLocked(File file)
	{
		try
		{
			locker.lockRead(file);
			return file.lastModified();
		}
		finally
		{
			locker.unlockRead(file);
		}
	}

	protected String readTextFile(File file) throws IOException
	{
		try
		{
			locker.lockRead(file);
			StringBuilder sb = new StringBuilder();
			InputStream is = null;
			try
			{
				is = new BufferedInputStream(new FileInputStream(file));
				byte[] buffer = new byte[1024];
				int len;
				while ((len = is.read(buffer)) >= 0)
				{
					String s = new String(buffer, 0, len, "UTF-8"); //$NON-NLS-1$
					sb.append(s);
				}
			}
			finally
			{
				if (is != null)
				{
					is.close();
				}
			}
			return sb.toString();
		}
		finally
		{
			locker.unlockRead(file);
		}
	}

	protected void writeTextFile(File file, String text) throws IOException
	{
		try
		{
			locker.lockWrite(file);
			OutputStream os = null;
			try
			{
				os = new BufferedOutputStream(new FileOutputStream(file));
				if (text != null)
				{
					byte[] buffer = text.getBytes("UTF-8"); //$NON-NLS-1$
					os.write(buffer);
				}
			}
			finally
			{
				if (os != null)
				{
					os.close();
				}
			}
		}
		finally
		{
			locker.unlockWrite(file);
		}
	}

	@Override
	public InputStream read(URL url) throws IOException
	{
		final File completeFile = getCompleteFile(url);
		locker.lockRead(completeFile);
		try
		{
			return new FilterInputStream(new FileInputStream(completeFile))
			{
				@Override
				public void close() throws IOException
				{
					try
					{
						super.close();
					}
					finally
					{
						locker.unlockRead(completeFile);
					}
				}
			};
		}
		catch (IOException e)
		{
			locker.unlockRead(completeFile);
			throw e;
		}
	}

	private File getCompleteFile(URL url)
	{
		return new File(directory, filenameForURL(url));
	}

	private File getPartialFile(URL url)
	{
		return new File(directory, filenameForURL(url) + PARTIAL_SUFFIX);
	}

	private File getContentTypeFile(URL url)
	{
		return new File(directory, filenameForURL(url) + CONTENT_TYPE_SUFFIX);
	}

	private static String filenameForURL(URL url)
	{
		String filename;
		if (!Util.isBlank(url.getHost()) && !Util.isBlank(url.getPath()))
		{
			filename = fixForFilename(url.getHost()) + File.separator + fixForFilename(url.getPath());
			if (url.getQuery() != null)
			{
				filename += "#" + url.getQuery(); //$NON-NLS-1$
			}
		}
		else
		{
			filename = fixForFilename(url.toExternalForm());
		}
		//ensure we don't get collisions with the partial and content type suffixes
		filename = filename.replace(PARTIAL_SUFFIX, PARTIAL_SUFFIX_REPLACEMENT);
		filename = filename.replace(CONTENT_TYPE_SUFFIX, CONTENT_TYPE_SUFFIX_REPLACEMENT);
		return filename;
	}

	private static String fixForFilename(String s)
	{
		// need to replace the following invalid filename characters: \/:*?"<>|
		// replace them with exclamation points, because that is cool
		return s.replaceAll("!", "!!").replaceAll("[\\/:*?\"<>|]", "!"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
}
