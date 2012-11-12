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
package au.gov.ga.earthsci.viewers;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TreeItem;

/**
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 * 
 */
public class ControlTreeViewerHelper extends ControlViewerHelper<TreeEditor>
{
	private final TreeViewer viewer;

	public ControlTreeViewerHelper(TreeViewer viewer)
	{
		this.viewer = viewer;
	}

	@Override
	public TreeEditor createEditor()
	{
		return new TreeEditor(viewer.getTree());
	}

	@Override
	public void setEditor(TreeEditor editor, Control control, Item item)
	{
		editor.setEditor(control, (TreeItem) item);
	}

	@Override
	public Composite getViewerControl()
	{
		return viewer.getTree();
	}
}
