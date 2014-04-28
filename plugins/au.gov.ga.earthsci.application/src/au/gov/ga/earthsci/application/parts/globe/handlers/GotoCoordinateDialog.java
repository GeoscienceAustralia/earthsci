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
package au.gov.ga.earthsci.application.parts.globe.handlers;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.view.orbit.OrbitView;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import au.gov.ga.earthsci.worldwind.common.util.Util;
import au.gov.ga.earthsci.worldwind.common.view.orbit.FlyToOrbitViewAnimator;

/**
 * Dialog which allows a user to input coordinates they wish to fly to.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class GotoCoordinateDialog extends Dialog
{
	private final WorldWindow wwd;
	private LatLon latlon = null;
	private Button okButton = null;

	public GotoCoordinateDialog(Shell parentShell, WorldWindow wwd)
	{
		super(parentShell);
		this.wwd = wwd;
	}

	@Override
	protected void configureShell(Shell shell)
	{
		super.configureShell(shell);
		shell.setText("Go to coordinates");
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout());

		Label label = new Label(composite, SWT.NONE);
		String prefix = "     "; //$NON-NLS-1$
		String supported = "Supports Lat/Lon, DMS, UTM, and MGRS formats" + ":\n"; //$NON-NLS-2$
		supported += prefix + "-27.0 133.5\n"; //$NON-NLS-1$
		supported += prefix + "27.0S 133.5E\n"; //$NON-NLS-1$
		supported += prefix + "-27\u00B00'0\" 133\u00B030'0\"\n"; //$NON-NLS-1$
		supported += prefix + "27d0'0\"S 133d30'0\"E\n"; //$NON-NLS-1$
		supported += prefix + "53J 351167E 7012680N\n"; //$NON-NLS-1$
		supported += prefix + "53JLL 51167 12680\n "; //$NON-NLS-1$
		label.setText(supported);

		label = new Label(composite, SWT.NONE);
		label.setText("Enter coordinates" + ':');

		final Text text = new Text(composite, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		text.setToolTipText("Type coordinates and press Enter");

		final Label resultLabel = new Label(composite, SWT.NONE);
		resultLabel.setText(" "); //$NON-NLS-1$

		text.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
				boolean valid = false;
				if (text.getText().length() == 0)
				{
					latlon = null;
					resultLabel.setText(" "); //$NON-NLS-1$
				}
				else
				{
					latlon = stringToLatLon(text.getText());
					if (latlon == null)
					{
						resultLabel.setText("Invalid coordinates");
					}
					else
					{
						resultLabel.setText(String.format(
								"Lat %7.4f\u00B0 Lon %7.4f\u00B0", latlon.getLatitude().degrees, //$NON-NLS-1$
								latlon.getLongitude().degrees));
						valid = true;
					}
				}
				resultLabel.pack();
				if (okButton != null)
				{
					okButton.setEnabled(valid);
				}
			}
		});

		return composite;
	}

	@Override
	protected Button createButton(Composite parent, int id, String label, boolean defaultButton)
	{
		Button button = super.createButton(parent, id, label, defaultButton);
		if (id == IDialogConstants.OK_ID)
		{
			okButton = button;
			button.setEnabled(false);
		}
		return button;
	}

	@Override
	protected void okPressed()
	{
		if (latlon != null && wwd.getView() instanceof OrbitView)
		{
			OrbitView view = (OrbitView) wwd.getView();

			Position beginCenter = view.getCenterPosition();
			Position center = new Position(latlon, 0);
			long lengthMillis = Util.getScaledLengthMillis(1, beginCenter, center);

			view.addAnimator(FlyToOrbitViewAnimator.createFlyToOrbitViewAnimator(view, beginCenter, center,
					view.getHeading(), view.getHeading(), view.getPitch(), view.getPitch(), view.getZoom(),
					view.getZoom(), lengthMillis, WorldWind.ABSOLUTE));
			wwd.redraw();
		}

		super.okPressed();
	}

	private LatLon stringToLatLon(String s)
	{
		Globe globe = wwd.getModel().getGlobe();
		LatLon ll = Util.computeLatLonFromString(s, globe);
		if (ll == null)
		{
			ll = Util.computeLatLonFromUTMString(s, globe, false);
		}
		return ll;
	}
}
