package au.gov.ga.earthsci.notification.popup.preferences;

import static au.gov.ga.earthsci.notification.popup.preferences.IPopupNotificationPreferences.*;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;

import au.gov.ga.earthsci.application.preferences.ScopedPreferenceStore;
import au.gov.ga.earthsci.application.preferences.fieldeditor.LabelFieldEditor;
import au.gov.ga.earthsci.application.preferences.fieldeditor.SpacerFieldEditor;
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
	}
	
	private void updateEnableFields(boolean enable)
	{
		if (notificationLabelEditor == null)
		{
			return;
		}
		
		notificationLabelEditor.setEnabled(enable, getFieldEditorParent());
		showInfoEditor.setEnabled(enable, getFieldEditorParent());
		showWarningEditor.setEnabled(enable, getFieldEditorParent());
		showErrorEditor.setEnabled(enable, getFieldEditorParent());
		showDurationEditor.setEnabled(enable, getFieldEditorParent());
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
