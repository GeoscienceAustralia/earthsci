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
package au.gov.ga.earthsci.worldwind.common.layers.delegate.reader;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import au.gov.ga.earthsci.worldwind.common.util.URLUtil;

/**
 * Helper class used for retrieving a mask png file relative to an image tile.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class MaskHelper
{
	/**
	 * Wrapper around a function used to handling reading mask files from within
	 * a zip file.
	 */
	public static interface MaskInsideZipDelegate
	{
		/**
		 * Read the given {@link ZipEntry} from the given {@link ZipInputStream}
		 * . This function should check if the entry matches a mask entry, and
		 * if so, reads the mask to a local image. It should also read any other
		 * entries within the zip file that are not mask images, so that the zip
		 * entry doesn't have to be read twice.
		 * 
		 * @param entry
		 *            {@link ZipEntry} to read. <code>zis</code> is positioned
		 *            at the beginning of this entry.
		 * @param zis
		 *            {@link ZipInputStream} containing the zip entry.
		 * @throws IOException
		 *             If reading the entry fails.
		 */
		void readEntry(ZipEntry entry, ZipInputStream zis) throws IOException;
	}

	/**
	 * Generate a URL pointing to the mask image for the given image url.
	 * 
	 * @param url
	 *            URL of the image to find the mask image for.
	 * @param upDirectoryCount
	 *            How many directories to move up to find the mask directory.
	 *            For example, if the image file was stored in
	 *            <code>dataset/level/row/tile.jpg</code>, and the mask was
	 *            stored in <code>mask/level/row/tile.png</code>, the number of
	 *            directories to move up would be 3.
	 * @param delegate
	 *            Delegate to call if the image/mask is containing within a zip
	 *            file. This should handle reading the image/mask from the
	 *            associated zip entry, as if this delegate is called, this
	 *            function returns null.
	 * @return URL pointing to the mask image file. It is the caller's
	 *         responsibility to check if this file exists. Null is returned if
	 *         the image file is within a zip file; instead, the given
	 *         <code>delegate</code> is called.
	 */
	public static URL getMaskURL(URL url, int upDirectoryCount, MaskInsideZipDelegate delegate)
	{
		boolean isZIP = url.toString().toLowerCase().endsWith("zip");
		if (isZIP)
		{
			try
			{
				ZipInputStream zis = new ZipInputStream(url.openStream());
				ZipEntry entry;
				while ((entry = zis.getNextEntry()) != null)
				{
					try
					{
						delegate.readEntry(entry, zis);
					}
					catch (IOException e)
					{
						//ignore (read next ZipEntry)
					}
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			return null;
		}

		try
		{
			if (url.getProtocol().equalsIgnoreCase("jar") || url.getProtocol().equalsIgnoreCase("zip"))
			{
				//if the URL is pointing to an entry within a zip file, then create a
				//new URL for the mask png file inside another zip file (mask.zip)

				String urlString = url.toString();
				int indexOfBang = urlString.lastIndexOf('!');

				String zipFile = urlString.substring(0, indexOfBang);
				int lastIndexOfSlash = zipFile.lastIndexOf('/');
				String maskFile = zipFile.substring(0, lastIndexOfSlash + 1) + "mask.zip";

				String entry = urlString.substring(indexOfBang);
				int lastIndexOfPeriod = entry.lastIndexOf('.');
				entry = entry.substring(0, lastIndexOfPeriod + 1) + "png";

				return new URL(maskFile + entry);
			}

			File imageFile = URLUtil.urlToFile(url);
			if (imageFile == null) //probably will never happen, as url should be the file:// protocol
				return null;

			//search for a mask file relative to the image file
			File maskFile = getMaskFile(imageFile, upDirectoryCount);
			return maskFile.toURI().toURL();
		}
		catch (MalformedURLException e)
		{
			return null;
		}
	}

	/**
	 * Create a File pointing to a 'mask' directory relative to the imageFile
	 * passed. The function moves up {@code upDirectoryCount} parent
	 * directories, replaces the directory with 'mask', and then moves back down
	 * the directories and file again.
	 * 
	 * @param imageFile
	 *            File for which to find a mask
	 * @return
	 */
	protected static File getMaskFile(File imageFile, int upDirectoryCount)
	{
		String[] directories = new String[upDirectoryCount];
		File parent = imageFile.getParentFile();
		for (int i = upDirectoryCount - 1; i >= 0 && parent != null; i--)
		{
			directories[i] = parent.getName();
			parent = parent.getParentFile();
		}

		if (upDirectoryCount > 0)
		{
			parent = new File(parent, "mask");
			for (int i = 1; i < upDirectoryCount; i++)
			{
				parent = new File(parent, directories[i]);
			}
		}

		int lastIndexOfPeriod = imageFile.getName().lastIndexOf('.');
		String filename = imageFile.getName().substring(0, lastIndexOfPeriod);
		return new File(parent, filename + ".png");
	}

	/**
	 * Add the alpha channel of mask to image, and return the composed image.
	 * 
	 * @param image
	 * @param mask
	 * @return image masked by mask
	 */
	public static BufferedImage compose(BufferedImage image, BufferedImage mask)
	{
		Graphics2D g2d = mask.createGraphics();
		g2d.setComposite(AlphaComposite.SrcIn);
		g2d.drawImage(image, 0, 0, null);
		g2d.dispose();
		return mask;
	}
}
