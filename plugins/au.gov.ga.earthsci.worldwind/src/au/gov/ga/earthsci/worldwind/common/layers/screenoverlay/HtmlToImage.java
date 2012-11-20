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
package au.gov.ga.earthsci.worldwind.common.layers.screenoverlay;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import au.gov.ga.earthsci.worldwind.common.util.Validate;

/**
 * Utility class to convert a HTML document to an image of a specified size.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class HtmlToImage
{

	/**
	 * Create and return an image of the provided HTML document. The image will have
	 * the provided dimensions.
	 * <p/>
	 * Note: This method will not change any styles to ensure that the html document will fit inside the
	 * specified dimensions.
	 */
	public static BufferedImage createImageFromHtml(URL htmlSource, int width, int height) throws IOException
	{
		Validate.notNull(htmlSource, "A html source is required");
		Validate.isTrue(width > 0 && height > 0, "Invalid dimensions. Dimensions must be greater than 0.");
		
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		JEditorPane editorPane = createEditorPane(width, height);
		editorPane.setPage(htmlSource);
		editorPane.paint(image.getGraphics());
		
		return image;
	}

	/**
	 * Create and return an image of the provided HTML content. The image will have
	 * the provided dimensions.
	 * <p/>
	 * Note: This method will not change any styles to ensure that the html document will fit inside the
	 * specified dimensions.
	 */
	public static BufferedImage createImageFromHtml(String sourceHtml, int width, int height)
	{
		Validate.notNull(sourceHtml, "A html source is required");
		Validate.isTrue(width > 0 && height > 0, "Invalid dimensions. Dimensions must be greater than 0.");
		
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		JEditorPane editorPane = createEditorPane(width, height);
		editorPane.setText(sourceHtml);
		editorPane.paint(image.getGraphics());
		
		return image;
	}
	
	
	private static JEditorPane createEditorPane(int width, int height)
	{
		JEditorPane editorPane = new JEditorPane();
		editorPane.setEditorKit(new HTMLEditorKit(){
			@Override
			public Document createDefaultDocument()
			{
				Document doc = super.createDefaultDocument();
		        ((HTMLDocument)doc).setAsynchronousLoadPriority(-1);
		        return doc;
			}
		});
		editorPane.setSize(width, height);
		editorPane.setPreferredSize(new Dimension(width, height));
		return editorPane;
	}
}
