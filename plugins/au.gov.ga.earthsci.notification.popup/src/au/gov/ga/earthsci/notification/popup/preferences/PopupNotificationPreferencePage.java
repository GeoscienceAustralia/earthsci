package au.gov.ga.earthsci.notification.popup.preferences;

import static au.gov.ga.earthsci.notification.popup.preferences.IPopupNotificationPreferences.CATEGORY_FILTER;
import static au.gov.ga.earthsci.notification.popup.preferences.IPopupNotificationPreferences.ENABLE_POPUPS;
import static au.gov.ga.earthsci.notification.popup.preferences.IPopupNotificationPreferences.POPUP_DURATION;
import static au.gov.ga.earthsci.notification.popup.preferences.IPopupNotificationPreferences.QUALIFIER_ID;
import static au.gov.ga.earthsci.notification.popup.preferences.IPopupNotificationPreferences.SHOW_ERROR_NOTIFICATIONS;
import static au.gov.ga.earthsci.notification.popup.preferences.IPopupNotificationPreferences.SHOW_INFO_NOTIFICATIONS;
import static au.gov.ga.earthsci.notification.popup.preferences.IPopupNotificationPreferences.SHOW_WARNING_NOTIFICATIONS;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import au.gov.ga.earthsci.core.preferences.LabelFieldEditor;
import au.gov.ga.earthsci.core.preferences.MultiSelectTableListFieldEditor;
import au.gov.ga.earthsci.core.preferences.MultiSelectTableListFieldEditor.IItemSerializer;
import au.gov.ga.earthsci.core.preferences.MultiSelectTableListFieldEditor.ITableItemCreator;
import au.gov.ga.earthsci.core.preferences.ScopedPreferenceStore;
import au.gov.ga.earthsci.core.preferences.SpacerFieldEditor;
import au.gov.ga.earthsci.notification.NotificationCategory;
import au.gov.ga.earthsci.notification.popup.Messages;

/**
 * {@link PreferencePage} which allows configuration of how popup notifications behave
 * in the application
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class PopupNotificationPreferencePage extends FieldEditorPreferencePage
{
	private EnableNotificationsFieldEditor enabledEditor;
	private LabelFieldEditor notificationLabelEditor;
	private BooleanFieldEditor showInfoEditor;
	private BooleanFieldEditor showWarningEditor;
	private BooleanFieldEditor showErrorEditor;
	private IntegerFieldEditor showDurationEditor;
	private LabelFieldEditor categoryFilterLabelEditor;
	private MultiSelectTableListFieldEditor<NotificationCategory> categoryFilterEditor;
	
	@SuppressWarnings("restriction")
	public PopupNotificationPreferencePage()
	{
		super(GRID);
		
		IPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, QUALIFIER_ID);
		
		setPreferenceStore(store);
		setDescription(Messages.PopupNotificationPreferences_Description);
	}
	
	@Override
	protected void createFieldEditors()
	{
		enabledEditor = new EnableNotificationsFieldEditor(ENABLE_POPUPS, Messages.PopupNotificationPreferences_EnablePopupsLabel, getFieldEditorParent());
		addField(enabledEditor);
		
		addField(new SpacerFieldEditor(getFieldEditorParent()));
		
		notificationLabelEditor = new LabelFieldEditor(Messages.PopupNotificationPreferences_NotificationLevelsLabel, getFieldEditorParent());
		addField(notificationLabelEditor);
		
		showInfoEditor = new BooleanFieldEditor(SHOW_INFO_NOTIFICATIONS, Messages.PopupNotificationPreferences_InformationLevelLabel, getFieldEditorParent());
		showWarningEditor = new BooleanFieldEditor(SHOW_WARNING_NOTIFICATIONS, Messages.PopupNotificationPreferences_WarningLevelLabel, getFieldEditorParent());
		showErrorEditor = new BooleanFieldEditor(SHOW_ERROR_NOTIFICATIONS, Messages.PopupNotificationPreferences_ErrorLevelLabel, getFieldEditorParent());
		
		addField(showInfoEditor);
		addField(showWarningEditor);
		addField(showErrorEditor);
		
		addField(new SpacerFieldEditor(getFieldEditorParent()));
		
		showDurationEditor = new IntegerFieldEditor(POPUP_DURATION, Messages.PopupNotificationPreferences_DurationLabel, getFieldEditorParent(), 5);
		showDurationEditor.setValidRange(100, 10000);
		addField(showDurationEditor);
		
		addField(new SpacerFieldEditor(getFieldEditorParent()));
		
		categoryFilterLabelEditor = new LabelFieldEditor("Notification categories:", getFieldEditorParent());
		addField(categoryFilterLabelEditor);
		
		ITableItemCreator<NotificationCategory> categoryItemCreator = new ITableItemCreator<NotificationCategory>() {
			@Override
			public TableItem createTableItem(Table parent, NotificationCategory object)
			{
				TableItem item = new TableItem(parent, SWT.NONE);
				item.setText(object.getLabel());
				return item;
			}
		};
		
		IItemSerializer<NotificationCategory> categoryItemSerialiser = new IItemSerializer<NotificationCategory>()
		{
			@Override
			public String asString(NotificationCategory object)
			{
				return object.getId();
			}

			@Override
			public NotificationCategory fromString(String string)
			{
				return NotificationCategory.get(string);
			}
		};
		
		categoryFilterEditor = new MultiSelectTableListFieldEditor<NotificationCategory>(CATEGORY_FILTER,
																						 new ArrayList<NotificationCategory>(NotificationCategory.getRegisteredCategories()),
																						 new String[] {Messages.PopupNotificationPreferencePage_NotificationCategoryColumnLabel},
																						 categoryItemCreator,
																						 categoryItemSerialiser,
																						 getFieldEditorParent());
		
		addField(categoryFilterEditor);
	}
	
	private void updateEnableFields(boolean enable)
	{
		if (notificationLabelEditor == null)
		{
			return;
		}
		
		try
		{
			for (Field f : getClass().getDeclaredFields())
			{
				if (FieldEditor.class.isAssignableFrom(f.getType()) && !(f.getName().equals("enabledEditor"))) //$NON-NLS-1$
				{
					Method method = f.getType().getMethod("setEnabled", boolean.class, Composite.class); //$NON-NLS-1$
					method.invoke(f.get(this), enable, getFieldEditorParent());
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * A simple extension of the boolean field editor that allows detection of changes to the 
	 * boolean state from various sources
	 */
	private class EnableNotificationsFieldEditor extends BooleanFieldEditor
	{

		public EnableNotificationsFieldEditor(String name, String label, Composite parent)
		{
			super(name, label, parent);
			getChangeControl(parent).addSelectionListener(new SelectionListener()
			{
				
				@Override
				public void widgetSelected(SelectionEvent e)
				{
					updateEnableFields(enabledEditor.getBooleanValue());
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e){}
			});
		}

		@Override
		protected void doLoad()
		{
			super.doLoad();
			updateEnableFields(getBooleanValue());
		}
		
		@Override
		protected void doLoadDefault()
		{
			super.doLoadDefault();
			updateEnableFields(getBooleanValue());
		}
	}

	
	
}
