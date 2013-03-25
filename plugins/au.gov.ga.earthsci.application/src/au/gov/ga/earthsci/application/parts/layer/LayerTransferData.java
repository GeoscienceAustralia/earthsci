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
package au.gov.ga.earthsci.application.parts.layer;

import gov.nasa.worldwind.util.WWXML;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import au.gov.ga.earthsci.common.util.XmlUtil;
import au.gov.ga.earthsci.core.model.layer.ILayerTreeNode;
import au.gov.ga.earthsci.core.persistence.Exportable;
import au.gov.ga.earthsci.core.persistence.PersistenceException;
import au.gov.ga.earthsci.core.persistence.Persistent;
import au.gov.ga.earthsci.core.persistence.Persister;

/**
 * Drag and drop data used by {@link LayerTransfer}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
@Exportable
public class LayerTransferData
{
	@Persistent
	private TransferredLayer[] layers;

	public TransferredLayer[] getLayers()
	{
		return layers;
	}

	public void setLayers(TransferredLayer[] layers)
	{
		this.layers = layers;
	}

	public static LayerTransferData fromNodes(ILayerTreeNode[] nodes)
	{
		LayerTransferData data = new LayerTransferData();
		data.layers = new TransferredLayer[nodes.length];
		for (int i = 0; i < nodes.length; i++)
		{
			data.layers[i] = new TransferredLayer();
			data.layers[i].setNode(nodes[i]);
			data.layers[i].setTreePath(nodes[i].indicesToRoot());
		}
		return data;
	}

	public static LayerTransferData load(InputStream is) throws PersistenceException, SAXException, IOException
	{
		Persister persister = new Persister();
		persister.registerClassLoader(LayerTransferData.class.getClassLoader());
		Document document = WWXML.createDocumentBuilder(false).parse(is);
		Element parent = document.getDocumentElement();
		Element element = XmlUtil.getFirstChildElement(parent);
		return (LayerTransferData) persister.load(element, null);
	}

	public static void save(LayerTransferData data, OutputStream os) throws PersistenceException, TransformerException,
			IOException
	{
		Persister persister = new Persister();
		persister.registerClassLoader(LayerTransferData.class.getClassLoader());
		DocumentBuilder documentBuilder = WWXML.createDocumentBuilder(false);
		Document document = documentBuilder.newDocument();
		Element element = document.createElement(LayerTransferData.class.getName());
		document.appendChild(element);
		persister.save(data, element, null);
		XmlUtil.saveDocumentToFormattedStream(document, os);
	}

	@Exportable
	public static class TransferredLayer
	{
		@Persistent
		private ILayerTreeNode node;
		@Persistent
		private int[] treePath;

		public ILayerTreeNode getNode()
		{
			return node;
		}

		public void setNode(ILayerTreeNode node)
		{
			this.node = node;
		}

		public int[] getTreePath()
		{
			return treePath;
		}

		public void setTreePath(int[] treePath)
		{
			this.treePath = treePath;
		}
	}
}
