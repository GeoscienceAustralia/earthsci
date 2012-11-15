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
package au.gov.ga.earthsci.catalog.part;

import gov.nasa.worldwind.util.WWXML;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import au.gov.ga.earthsci.core.model.catalog.ICatalogTreeNode;
import au.gov.ga.earthsci.core.persistence.Exportable;
import au.gov.ga.earthsci.core.persistence.PersistenceException;
import au.gov.ga.earthsci.core.persistence.Persistent;
import au.gov.ga.earthsci.core.persistence.Persister;
import au.gov.ga.earthsci.core.util.Validate;
import au.gov.ga.earthsci.core.util.XmlUtil;

/**
 * A data transfer object used by the {@link CatalogTransfer} class for 
 * transfer of catalog nodes
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
@Exportable
public class CatalogTransferData
{

	private CatalogTransferData() {};
	
	@Persistent
	private TransferredCatalogNode[] catalogNodes;

	public TransferredCatalogNode[] getCatalogNodes()
	{
		return catalogNodes;
	}

	public void setCatalogNodes(TransferredCatalogNode[] catalogNodes)
	{
		this.catalogNodes = catalogNodes;
	}
	
	/**
	 * Create a transfer object that wraps the provided catalog nodes
	 * 
	 * @param nodes The nodes to set. If <code>null</code>, will create an empty transfer object.
	 */
	public static CatalogTransferData fromNodes(ICatalogTreeNode[] nodes)
	{
		CatalogTransferData data = new CatalogTransferData();
		if (nodes == null)
		{
			data.catalogNodes = new TransferredCatalogNode[0];
			return data;
		}
		
		ArrayList<TransferredCatalogNode> catalogNodes = new ArrayList<TransferredCatalogNode>();
		for (int i = 0; i < nodes.length; i++)
		{
			if (nodes[i] == null)
			{
				continue;
			}
			
			TransferredCatalogNode transfer = new TransferredCatalogNode();
			transfer.setNode(nodes[i]);
			transfer.setTreePath(nodes[i].indicesToRoot());
			
			catalogNodes.add(transfer);
		}
		data.catalogNodes = catalogNodes.toArray(new TransferredCatalogNode[catalogNodes.size()]);
		
		return data;
	}

	public static CatalogTransferData load(InputStream is) throws PersistenceException, SAXException, IOException
	{
		Persister persister = new Persister();
		persister.registerClassLoader(CatalogTransferData.class.getClassLoader());
		Document document = WWXML.createDocumentBuilder(false).parse(is);
		Element parent = document.getDocumentElement();
		Element element = XmlUtil.getFirstChildElement(parent);
		
		return (CatalogTransferData) persister.load(element, null);
	}

	public static void save(CatalogTransferData data, OutputStream os) throws PersistenceException, TransformerException, IOException
	{
		Validate.notNull(data, "Transfer data is required"); //$NON-NLS-1$
		Validate.notNull(os, "An output stream is required"); //$NON-NLS-1$
		
		Persister persister = new Persister();
		persister.registerClassLoader(CatalogTransferData.class.getClassLoader());
		
		DocumentBuilder documentBuilder = WWXML.createDocumentBuilder(false);
		Document document = documentBuilder.newDocument();
		
		Element element = document.createElement(CatalogTransferData.class.getName());
		document.appendChild(element);
		
		persister.save(data, element, null);
		XmlUtil.saveDocumentToFormattedStream(document, os);
	}
	
	@Exportable
	public static class TransferredCatalogNode
	{
		@Persistent
		private ICatalogTreeNode node;
		
		@Persistent
		private int[] treePath;

		public ICatalogTreeNode getNode()
		{
			return node;
		}

		public void setNode(ICatalogTreeNode node)
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
