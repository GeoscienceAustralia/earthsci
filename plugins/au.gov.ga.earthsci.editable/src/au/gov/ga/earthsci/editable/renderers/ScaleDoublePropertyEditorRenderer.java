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
package au.gov.ga.earthsci.editable.renderers;

import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.*;

import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.NumericRange;
import org.eclipse.sapphire.modeling.util.MiscUtil;
import org.eclipse.sapphire.ui.assist.internal.PropertyEditorAssistDecorator;
import org.eclipse.sapphire.ui.forms.FormComponentPart;
import org.eclipse.sapphire.ui.forms.PropertyEditorPart;
import org.eclipse.sapphire.ui.forms.swt.PropertyEditorPresentation;
import org.eclipse.sapphire.ui.forms.swt.PropertyEditorPresentationFactory;
import org.eclipse.sapphire.ui.forms.swt.SwtPresentation;
import org.eclipse.sapphire.ui.forms.swt.ValuePropertyEditorPresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Text;

import au.gov.ga.earthsci.editable.annotations.Accuracy;
import au.gov.ga.earthsci.editable.annotations.Sync;

/**
 * Property editor for double values that shows a scale bar.
 * <p/>
 * Adapted from Sapphire's {@link ScalePropertyEditorRenderer}.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ScaleDoublePropertyEditorRenderer extends ValuePropertyEditorPresentation
{
	private double minimum;
	private double maximum;
	private double scale;
	private boolean sync = false;
	private int accuracy = 3;
	private Scale widget;
	private Text textField;

	public ScaleDoublePropertyEditorRenderer(FormComponentPart part, SwtPresentation parent, Composite composite)
	{
		super(part, parent, composite);
	}

	@Override
	protected void createContents(final Composite parent)
	{
		final PropertyEditorPart part = part();
		final ValueProperty property = (ValueProperty) part.property().definition();

		final NumericRange rangeAnnotation = property.getAnnotation(NumericRange.class);
		this.sync = property.getAnnotation(Sync.class) != null;
		final Accuracy accuracyAnnotation = property.getAnnotation(Accuracy.class);
		if (accuracyAnnotation != null)
		{
			accuracy = accuracyAnnotation.value();
		}

		try
		{
			this.minimum = Double.parseDouble(rangeAnnotation.min());
			this.maximum = Double.parseDouble(rangeAnnotation.max());
		}
		catch (NumberFormatException e)
		{
			// Should not happen here. We already checked this in property editor applicability test.
			throw new RuntimeException(e);
		}

		//calculate a scale such that the range of the scale widget is between 200 and 2000 if accuracy == 3
		double range = this.maximum - this.minimum;
		int log10i = (int) Math.round(Math.floor(Math.log10(range)));
		scale = Math.pow(10, accuracy - log10i - 1);
		double accuracyMinimum = Math.pow(10, accuracy) * 2;
		if (range * scale < accuracyMinimum)
		{
			scale *= 10;
		}

		int widgetMinimum = 0;
		int widgetMaximum = (int) (range * scale); //guaranteed between 200 and 2000

		final Composite composite = createMainComposite(parent);
		composite.setLayout(glspacing(glayout(2, 0, 0), 2));

		final Composite textFieldComposite = new Composite(composite, SWT.NONE);
		textFieldComposite.setLayoutData(gdwhint(gd(), 60));
		textFieldComposite.setLayout(glspacing(glayout(2, 0, 0), 2));

		final PropertyEditorAssistDecorator decorator = createDecorator(textFieldComposite);
		decorator.control().setLayoutData(gdvalign(gd(), SWT.TOP));

		this.textField = new Text(textFieldComposite, SWT.BORDER);
		this.textField.setLayoutData(gdhfill());

		this.textField.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(final ModifyEvent event)
			{
				setPropertyValue(textField.getText(), !sync);
			}
		});

		/*final TextOverlayPainter.Controller textOverlayPainterController = new TextOverlayPainter.Controller()
		{
			@Override
			public String overlay()
			{
				return property().getDefaultText();
			}
		};

		TextOverlayPainter.install(this.textField, textOverlayPainterController);*/

		this.widget = new Scale(composite, SWT.HORIZONTAL);
		this.widget.setLayoutData(gdhfill());
		this.widget.setMinimum(widgetMinimum);
		this.widget.setMaximum(widgetMaximum);
		this.widget.setIncrement(1);
		this.widget.setPageIncrement(10);

		this.widget.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(final SelectionEvent event)
			{
				final int i = widget.getSelection();
				final double d = i / scale + minimum;
				final double r = Math.round(d * scale) / (scale); //get rid of double rounding errors 
				setPropertyValue(String.valueOf(r), !sync);
			}
		});

		decorator.addEditorControl(composite);

		addControl(this.textField);
		addControl(this.widget);
	}

	@Override
	protected void handlePropertyChangedEvent()
	{
		super.handlePropertyChangedEvent();

		final Value<?> value = property();

		final String existingValueInTextField = this.textField.getText();
		final String newValueForTextField = value.text(false);

		if (!existingValueInTextField.equals(newValueForTextField))
		{
			this.textField.setText(newValueForTextField == null ? MiscUtil.EMPTY_STRING : newValueForTextField);
		}

		final Double newValueDouble = (Double) value.content(true);
		int newValueForScale =
				(newValueDouble == null ? this.widget.getMinimum()
						: (int) Math.round((newValueDouble.doubleValue() - minimum) * scale));

		if (this.widget.getSelection() != newValueForScale)
		{
			this.widget.setSelection(newValueForScale);
		}
	}

	@Override
	protected void handleFocusReceivedEvent()
	{
		this.widget.setFocus();
	}

	public static class Factory extends PropertyEditorPresentationFactory
	{
		@Override
		public boolean isApplicableTo(final PropertyEditorPart propertyEditorDefinition)
		{
			final PropertyDef property = propertyEditorDefinition.property().definition();

			if (property.isOfType(Double.class))
			{
				final NumericRange rangeAnnotation = property.getAnnotation(NumericRange.class);

				if (rangeAnnotation != null)
				{
					final String minStr = rangeAnnotation.min();
					final String maxStr = rangeAnnotation.max();

					if (minStr.length() > 0 && maxStr.length() > 0)
					{
						return true;
					}
				}
			}

			return false;
		}

		@Override
		public PropertyEditorPresentation create(PropertyEditorPart part, SwtPresentation parent, Composite composite)
		{
			return new ScaleDoublePropertyEditorRenderer(part, parent, composite);
		}
	}
}
