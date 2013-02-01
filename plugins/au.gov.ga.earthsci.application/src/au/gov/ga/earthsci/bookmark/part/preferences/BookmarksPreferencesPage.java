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
package au.gov.ga.earthsci.bookmark.part.preferences;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import au.gov.ga.earthsci.bookmark.BookmarkPropertyFactory;
import au.gov.ga.earthsci.bookmark.model.IBookmarkProperty;
import au.gov.ga.earthsci.bookmark.part.Messages;
import au.gov.ga.earthsci.core.preferences.LabelFieldEditor;
import au.gov.ga.earthsci.core.preferences.MultiSelectTableListFieldEditor;
import au.gov.ga.earthsci.core.preferences.MultiSelectTableListFieldEditor.IItemSerializer;
import au.gov.ga.earthsci.core.preferences.MultiSelectTableListFieldEditor.ITableItemCreator;
import au.gov.ga.earthsci.core.preferences.ScopedPreferenceStore;
import au.gov.ga.earthsci.core.preferences.SpacerFieldEditor;

/**
 * Preference page for Bookmarks preferences
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class BookmarksPreferencesPage extends FieldEditorPreferencePage
{

	private IntegerFieldEditor transitionDurationEditor;
	private IntegerFieldEditor playBookmarksWaitDurationEditor;
	private BooleanFieldEditor askForDeleteConfirmationEditor;
	private MultiSelectTableListFieldEditor<IBookmarkProperty> defaultProperties;
	private Map<String, IBookmarkProperty> cachedProperties;
	
	public BookmarksPreferencesPage()
	{
		super(GRID);
		setTitle(Messages.BookmarksPreferencesPage_Title);
		setDescription(Messages.BookmarksPreferencesPage_Description);
		
		IPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, IBookmarksPreferences.QUALIFIER_ID);
		
		setPreferenceStore(store);
	}
	
	@Override
	protected void createFieldEditors()
	{
		addField(new SpacerFieldEditor(getFieldEditorParent()));
		
		transitionDurationEditor = new IntegerFieldEditor(IBookmarksPreferences.DEFAULT_TRANSITION_DURATION, 
														  Messages.BookmarksPreferencesPage_TransitionDurationFieldTitle, 
														  getFieldEditorParent());
		addField(transitionDurationEditor);
		
		
		playBookmarksWaitDurationEditor = new IntegerFieldEditor(IBookmarksPreferences.PLAY_BOOKMARKS_WAIT_DURATION, 
																 Messages.BookmarksPreferencesPage_PlayBookmarksPauseDurationLabel, 
																 getFieldEditorParent());
		
		addField(playBookmarksWaitDurationEditor);
		
		addField(new SpacerFieldEditor(getFieldEditorParent()));
		
		askForDeleteConfirmationEditor = new BooleanFieldEditor(IBookmarksPreferences.ASK_LIST_DELETE_CONFIRM,
																Messages.BookmarksPreferencesPage_AskConfirmDeleteLabel, 
																getFieldEditorParent());
		addField(askForDeleteConfirmationEditor);
		
		addField(new SpacerFieldEditor(getFieldEditorParent()));
		
		LabelFieldEditor defaultPropertiesLabel = new LabelFieldEditor(Messages.BookmarksPreferencesPage_DefaultPropertiesLabel, getFieldEditorParent());
		addField(defaultPropertiesLabel);
		
		loadAvailableBookmarkProperties();
		
		ITableItemCreator<IBookmarkProperty> tableItemCreator = new ITableItemCreator<IBookmarkProperty>()
		{
			@Override
			public TableItem createTableItem(Table parent, IBookmarkProperty object)
			{
				TableItem item = new TableItem(parent, SWT.NONE);
				item.setText(object.getName());
				return item;
			}
		};
		
		IItemSerializer<IBookmarkProperty> itemSerializer = new IItemSerializer<IBookmarkProperty>() {

			@Override
			public String asString(IBookmarkProperty object)
			{
				return object.getType();
			}

			@Override
			public IBookmarkProperty fromString(String string)
			{
				return cachedProperties.get(string);
			}

		};
		
		defaultProperties = new MultiSelectTableListFieldEditor<IBookmarkProperty>(IBookmarksPreferences.DEFAULT_PROPERTIES, 
																		new ArrayList<IBookmarkProperty>(cachedProperties.values()),
																		new String[] {Messages.BookmarksPreferencesPage_PropertyColumn},
																		tableItemCreator,
																		itemSerializer, 
																		getFieldEditorParent());
		addField(defaultProperties);
	}

	private void loadAvailableBookmarkProperties()
	{
		cachedProperties = new TreeMap<String, IBookmarkProperty>();
		for (IBookmarkProperty p : BookmarkPropertyFactory.createKnownProperties())
		{
			cachedProperties.put(p.getType(), p);
		}
	}

}
