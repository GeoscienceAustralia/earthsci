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
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Properties;

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
	private final static String CONTENT_TYPE_SUFFIX = ".contenttype"; //$NON-NLS-1$
	private final static String URLS_PROPERTIES_FILENAME = "urls.properties"; //$NON-NLS-1$

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
				private boolean unlocked = false;

				@Override
				public void close() throws IOException
				{
					try
					{
						super.close();
					}
					finally
					{
						if (!unlocked)
						{
							unlocked = true;
							locker.unlockWrite(partialFile);
						}
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
	public boolean writeComplete(URL url, long lastModified, String contentType)
	{
		File partialFile = getPartialFile(url);
		File completeFile = getCompleteFile(url);

		locker.lockWrite(partialFile);
		try
		{
			locker.lockRead(completeFile);
			try
			{
				if (fileEquals(partialFile, completeFile))
				{
					partialFile.delete();
					return false;
				}
			}
			finally
			{
				locker.unlockRead(completeFile);
			}

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
				locker.unlockWrite(completeFile);
			}
		}
		finally
		{
			locker.unlockWrite(partialFile);
		}
		return true;
	}

	public static boolean fileEquals(File file1, File file2)
	{
		byte[] md51 = fileMD5(file1);
		byte[] md52 = fileMD5(file2);
		if (md51 == null || md52 == null)
		{
			return false;
		}
		return byteArrayEquals(md51, md52);
	}

	public static boolean byteArrayEquals(byte[] b1, byte[] b2)
	{
		if (b1 == b2)
		{
			return true;
		}
		if (b1.length != b2.length)
		{
			return false;
		}
		for (int i = 0; i < b1.length; i++)
		{
			if (b1[i] != b2[i])
			{
				return false;
			}
		}
		return true;
	}

	public static byte[] fileMD5(File file)
	{
		try
		{
			MessageDigest md = MessageDigest.getInstance("MD5"); //$NON-NLS-1$
			InputStream is = null;
			try
			{
				is = new DigestInputStream(new BufferedInputStream(new FileInputStream(file)), md);
				byte[] buffer = new byte[8192];
				while (is.read(buffer) >= 0)
				{
				}
			}
			finally
			{
				if (is != null)
				{
					is.close();
				}
			}
			return md.digest();
		}
		catch (Exception e)
		{
			return null;
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
		if (contentType == null)
		{
			return;
		}

		String guessedContentType = URLConnection.guessContentTypeFromName(completeFile.getName());
		if ((guessedContentType == null && contentType == null) || contentType.equals(guessedContentType))
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
				private boolean unlocked = false;

				@Override
				public void close() throws IOException
				{
					try
					{
						super.close();
					}
					finally
					{
						if (!unlocked)
						{
							unlocked = true;
							locker.unlockRead(completeFile);
						}
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

	@Override
	public File getFile(URL url)
	{
		return getCompleteFile(url);
	}

	private File getCompleteFile(URL url)
	{
		return fileForURL(url, ""); //$NON-NLS-1$
	}

	private File getPartialFile(URL url)
	{
		return fileForURL(url, PARTIAL_SUFFIX);
	}

	private File getContentTypeFile(URL url)
	{
		return fileForURL(url, CONTENT_TYPE_SUFFIX);
	}

	private File fileForURL(URL url, String suffix)
	{
		String hashDirectory = !Util.isBlank(url.getHost()) ? url.getHost() + File.separator : ""; //$NON-NLS-1$
		hashDirectory += getHashDirectory(url);

		String extension = au.gov.ga.earthsci.common.util.Util.getExtension(url.getPath());
		if (extension == null || extension.length() > 30)
		{
			//probably not an extension
			extension = ""; //$NON-NLS-1$
		}

		File propertiesFile = new File(directory, hashDirectory + File.separator + URLS_PROPERTIES_FILENAME);
		locker.lockWrite(propertiesFile);
		try
		{
			Properties properties = new Properties();
			if (propertiesFile.exists())
			{
				try
				{
					loadProperties(properties, propertiesFile);
				}
				catch (IOException e)
				{
					logger.error("Error reading url properties file", e); //$NON-NLS-1$
				}
			}
			String filename = properties.getProperty(url.toString());
			if (filename == null)
			{
				filename = properties.size() + extension;
			}
			properties.setProperty(url.toString(), filename);
			propertiesFile.getParentFile().mkdirs();
			try
			{
				saveProperties(properties, propertiesFile);
			}
			catch (IOException e)
			{
				logger.error("Error writing url properties file", e); //$NON-NLS-1$
			}
			return new File(directory, hashDirectory + File.separator + filename + suffix);
		}
		finally
		{
			locker.unlockWrite(propertiesFile);
		}
	}

	private static String getHashDirectory(URL url)
	{
		String hashCode = String.valueOf(url.toString().hashCode());
		StringBuilder directory = new StringBuilder();
		if (hashCode.charAt(0) == '-')
		{
			directory.append('-');
			hashCode = hashCode.substring(1);
		}
		while (hashCode.length() < 10)
		{
			hashCode = "0" + hashCode; //$NON-NLS-1$
		}
		directory.append(hashCode.substring(0, 3));
		directory.append(File.separator);
		directory.append(hashCode.substring(3, 6));
		directory.append(File.separator);
		directory.append(hashCode.substring(6));
		return directory.toString();
	}

	private static void loadProperties(Properties properties, File file) throws IOException
	{
		FileInputStream fis = null;
		try
		{
			fis = new FileInputStream(file);
			properties.load(fis);
		}
		finally
		{
			if (fis != null)
			{
				fis.close();
			}
		}
	}

	private static void saveProperties(Properties properties, File file) throws IOException
	{
		FileOutputStream fos = null;
		try
		{
			fos = new FileOutputStream(file);
			properties.store(fos, null);
		}
		finally
		{
			if (fos != null)
			{
				fos.close();
			}
		}
	}
}
