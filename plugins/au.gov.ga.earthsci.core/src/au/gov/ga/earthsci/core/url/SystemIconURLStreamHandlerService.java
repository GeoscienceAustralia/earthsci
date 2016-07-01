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
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.SwingUtilities;

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
					final File file = new File(fileURL.toURI());
					/*String filename = file.getName();
					int lastIndexOfDot = filename.lastIndexOf('.');
					String extension = lastIndexOfDot >= 0 ? filename.substring(lastIndexOfDot + 1) : null;
					if (extension != null)
					{
						try
						{
							return ExtensionIconURLStreamHandlerService.getImageInputStreamForExtension(extension);
						}
						catch (IOException e)
						{
							//ignore, try the swing way below instead
						}
					}*/
					if (!file.exists())
					{
						throw new FileNotFoundException(file.getAbsolutePath());
					}

					final Icon[] iconResult = new Icon[1];
					Runnable task = new Runnable()
					{
						@Override
						public void run()
						{
							iconResult[0] =
									javax.swing.filechooser.FileSystemView.getFileSystemView()
											.getSystemIcon(file);
						}
					};
					if (SwingUtilities.isEventDispatchThread())
					{
						task.run();
					}
					else
					{
						SwingUtilities.invokeAndWait(task);
					}

					Icon icon = iconResult[0];
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					BufferedImage bi =
							new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
					Graphics2D g = bi.createGraphics();
					icon.paintIcon(null, g, 0, 0);
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
			return createURL(file.toURI());
		}
		catch (Exception e)
		{
			//not possible
			throw new IllegalStateException();
		}
	}

	public static URL createURL(URI uri) throws MalformedURLException
	{
		return new URL(PROTOCOL, null, uri.toURL().getPath());
	}
}
