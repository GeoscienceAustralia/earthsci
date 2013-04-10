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
package au.gov.ga.earthsci.core.url;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.osgi.service.url.AbstractURLStreamHandlerService;
import org.osgi.service.url.URLStreamHandlerService;

/**
 * {@link URLStreamHandlerService} that handles systemicon://filename URLs,
 * which provide access to the default system icon for filenames.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class SystemIconURLStreamHandlerService extends AbstractURLStreamHandlerService
{
	public final static String PROTOCOL = "systemicon"; //$NON-NLS-1$

	@Override
	public URLConnection openConnection(URL u) throws IOException
	{
		return new URLConnection(u)
		{
			@Override
			public void connect() throws IOException
			{
			}

			@Override
			public String getContentType()
			{
				return "image/png"; //$NON-NLS-1$
			}

			@Override
			public InputStream getInputStream() throws IOException
			{
				try
				{
					URL fileURL = new URL("file:" + getURL().getPath()); //$NON-NLS-1$
					File file = new File(fileURL.toURI());
					if (!file.exists())
					{
						throw new FileNotFoundException(file.getAbsolutePath());
					}
					ImageIcon icon =
							(ImageIcon) javax.swing.filechooser.FileSystemView.getFileSystemView().getSystemIcon(file);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					Image image = icon.getImage();
					BufferedImage bi =
							new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
					Graphics2D g = bi.createGraphics();
					g.drawImage(image, 0, 0, null);
					g.dispose();
					ImageIO.write(bi, "PNG", baos); //$NON-NLS-1$
					baos.close();
					return new ByteArrayInputStream(baos.toByteArray());
				}
				catch (IOException e)
				{
					throw e;
				}
				catch (Exception e)
				{
					throw new IOException(e);
				}
			}
		};
	}

	public static URL createURL(File file)
	{
		try
		{
			return new URL(PROTOCOL, null, file.toURI().toURL().getPath());
		}
		catch (Exception e)
		{
			//not possible
			throw new IllegalStateException();
		}
	}
}
