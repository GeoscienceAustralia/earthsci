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
package au.gov.ga.earthsci.application.parts.layer.handlers;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import au.gov.ga.earthsci.core.model.layer.ILayerTreeNode;
import au.gov.ga.earthsci.core.worldwind.ITreeModel;

/**
 * Handles rename commands for the layer tree.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class RenameHandler
{
	@Inject
	private ITreeModel model;

	private Tree tree;
	private TreeEditor treeEditor;
	private ILayerTreeNode layerNode;
	private Text textEditor;
	private Composite textEditorParent;
	private boolean saving = false;

	@Execute
	public void execute(TreeViewer viewer)
	{
		tree = viewer.getTree();
		treeEditor = new TreeEditor(tree);

		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		if (selection.size() != 1)
			return;

		layerNode = (ILayerTreeNode) selection.getFirstElement();
		if (layerNode == null)
			return;

		createTextEditor();
		textEditor.setText(layerNode.getLabelOrName());

		showTextEditor();
	}

	private int getInset()
	{
		return 1;
	}

	private void createTextEditor()
	{
		if (textEditorParent != null)
			return;

		textEditorParent = new Composite(tree, SWT.NONE);
		TreeItem[] selectedItems = tree.getSelection();
		treeEditor.horizontalAlignment = SWT.LEFT;
		treeEditor.grabHorizontal = true;
		treeEditor.setEditor(textEditorParent, selectedItems[0]);

		textEditorParent.setVisible(false);
		final int inset = 1;

		// Create inner text editor.
		textEditor = new Text(textEditorParent, SWT.NONE);
		textEditor.setFont(tree.getFont());
		textEditorParent.setBackground(textEditor.getBackground());
		textEditor.addListener(SWT.Modify, new Listener()
		{
			@Override
			public void handleEvent(Event e)
			{
				Point textSize = textEditor.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				textSize.x += textSize.y; // Add extra space for new
				// characters.
				Point parentSize = textEditorParent.getSize();
				textEditor.setBounds(2, inset, Math.min(textSize.x, parentSize.x - 4), parentSize.y - 2 * inset);
				textEditorParent.redraw();
			}
		});

		if (inset > 0)
		{
			textEditorParent.addListener(SWT.Paint, new Listener()
			{
				@Override
				public void handleEvent(Event e)
				{
					Point textSize = textEditor.getSize();
					Point parentSize = textEditorParent.getSize();
					e.gc.drawRectangle(0, 0, Math.min(textSize.x + 4, parentSize.x - 1), parentSize.y - 1);
				}
			});
		}

		textEditor.addListener(SWT.Traverse, new Listener()
		{
			@Override
			public void handleEvent(Event event)
			{

				// Workaround for Bug 20214 due to extra
				// traverse events
				switch (event.detail)
				{
				case SWT.TRAVERSE_ESCAPE:
					// Do nothing in this case
					disposeTextWidget();
					event.doit = true;
					event.detail = SWT.TRAVERSE_NONE;
					break;
				case SWT.TRAVERSE_RETURN:
					saveChangesAndDispose();
					event.doit = true;
					event.detail = SWT.TRAVERSE_NONE;
					break;
				}
			}
		});
		textEditor.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusLost(FocusEvent fe)
			{
				saveChangesAndDispose();
			}
		});
	}

	private void showTextEditor()
	{
		//Open text editor with initial size.
		int inset = getInset();
		textEditorParent.setVisible(true);
		Point textSize = textEditor.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		textSize.x += textSize.y; // Add extra space for new characters.
		Point parentSize = textEditorParent.getSize();
		textEditor.setBounds(2, inset, Math.min(textSize.x, parentSize.x - 4), parentSize.y - 2 * inset);
		textEditorParent.redraw();
		textEditor.selectAll();
		textEditor.setFocus();
	}

	private void disposeTextWidget()
	{
		if (textEditorParent != null)
		{
			textEditorParent.dispose();
			textEditorParent = null;
			textEditor = null;
			treeEditor.setEditor(null, null);
		}
	}

	private void saveChangesAndDispose()
	{
		if (saving == true)
			return;

		saving = true;
		final String newLabel = textEditor.getText();
		layerNode.setLabel(newLabel);
		disposeTextWidget();
		saving = false;
	}
}
