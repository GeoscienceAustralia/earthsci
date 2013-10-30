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

import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.*;

import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.util.MiscUtil;
import org.eclipse.sapphire.ui.PropertyEditorPart;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.assist.internal.PropertyEditorAssistDecorator;
import org.eclipse.sapphire.ui.renderers.swt.PropertyEditorRenderer;
import org.eclipse.sapphire.ui.renderers.swt.PropertyEditorRendererFactory;
import org.eclipse.sapphire.ui.renderers.swt.ValuePropertyEditorRenderer;
import org.eclipse.sapphire.ui.swt.renderer.TextOverlayPainter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import au.gov.ga.earthsci.editable.annotations.Sync;
import au.gov.ga.earthsci.editable.serialization.ColorAwtSerializationService;

/**
 * {@link ValuePropertyEditorRenderer} for editing color properties.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class ColorPropertyEditorRenderer extends ValuePropertyEditorRenderer
{
	private boolean sync = false;
	private Text textField;
	private Canvas canvas;
	private Color lastColor = null;
	private final ColorAwtSerializationService awtColorSerializationService = new ColorAwtSerializationService();

	public ColorPropertyEditorRenderer(SapphireRenderingContext context, PropertyEditorPart part)
	{
		super(context, part);
	}

	@Override
	protected void createContents(Composite parent)
	{
		final PropertyEditorPart part = getPart();
		final ValueProperty property = (ValueProperty) part.getProperty();

		this.sync = property.getAnnotation(Sync.class) != null;

		final Composite composite = createMainComposite(parent);
		composite.setLayout(glspacing(glayout(2, 0, 0), 2));

		final Composite textFieldComposite = new Composite(composite, SWT.NONE);
		textFieldComposite.setLayoutData(gdwhint(gd(), 70));
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

		final TextOverlayPainter.Controller textOverlayPainterController = new TextOverlayPainter.Controller()
		{
			@Override
			public String getDefaultText()
			{
				return getPropertyValue().getDefaultText();
			}
		};

		TextOverlayPainter.install(this.textField, textOverlayPainterController);

		this.canvas = new Canvas(composite, SWT.BORDER);
		this.canvas.setLayoutData(gdhhint(gd(), 20));

		this.canvas.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseUp(MouseEvent e)
			{
				ColorDialog dialog = new ColorDialog(canvas.getShell());
				RGB rgb = dialog.open();
				if (rgb != null)
				{
					setCanvasBackground(rgb.red, rgb.green, rgb.blue);
					java.awt.Color awtColor = new java.awt.Color(rgb.red, rgb.green, rgb.blue);
					String value = awtColorSerializationService.encode(awtColor);
					setPropertyValue(value, !sync);
				}
			}
		});

		this.canvas.addDisposeListener(new DisposeListener()
		{
			@Override
			public void widgetDisposed(DisposeEvent e)
			{
				if (lastColor != null)
				{
					lastColor.dispose();
				}
			}
		});

		decorator.addEditorControl(composite);

		addControl(this.textField);
		addControl(this.canvas);
	}

	@Override
	protected void handlePropertyChangedEvent()
	{
		super.handlePropertyChangedEvent();

		final Value<?> value = getPropertyValue();

		final String existingValueInTextField = this.textField.getText();
		final String newValueForTextField = value.getText(false);

		if (!existingValueInTextField.equals(newValueForTextField))
		{
			this.textField.setText(newValueForTextField == null ? MiscUtil.EMPTY_STRING : newValueForTextField);
		}

		Object object = awtColorSerializationService.decode(newValueForTextField);
		if (object instanceof java.awt.Color)
		{
			java.awt.Color awtColor = (java.awt.Color) object;
			setCanvasBackground(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue());
		}
	}

	private void setCanvasBackground(int r, int g, int b)
	{
		if (lastColor != null)
		{
			lastColor.dispose();
		}
		lastColor = new Color(canvas.getDisplay(), r, g, b);
		canvas.setBackground(lastColor);
	}

	@Override
	protected void handleFocusReceivedEvent()
	{
		this.textField.setFocus();
	}

	public static class Factory extends PropertyEditorRendererFactory
	{
		@Override
		public boolean isApplicableTo(final PropertyEditorPart propertyEditorDefinition)
		{
			final ModelProperty property = propertyEditorDefinition.getProperty();
			return property.isOfType(java.awt.Color.class) || property.isOfType(org.eclipse.sapphire.ui.Color.class)
					|| property.isOfType(org.eclipse.swt.graphics.Color.class);
		}

		@Override
		public PropertyEditorRenderer create(final SapphireRenderingContext context, final PropertyEditorPart part)
		{
			return new ColorPropertyEditorRenderer(context, part);
		}
	}
}
