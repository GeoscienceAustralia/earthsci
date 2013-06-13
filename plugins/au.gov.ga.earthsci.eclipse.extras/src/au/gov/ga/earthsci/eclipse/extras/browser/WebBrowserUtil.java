/*******************************************************************************
 * Copyright (c) 2003, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 * Martin Oberhuber (Wind River) - [292882] Default Browser on Solaris
 *******************************************************************************/
package au.gov.ga.earthsci.eclipse.extras.browser;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
/**
 * Utility class for the Web browser tools.
 */
public class WebBrowserUtil {
	public static Boolean isInternalBrowserOperational;

	private static final char STYLE_SEP = '-';

	private static final int DEFAULT_STYLE = BrowserViewer.BUTTON_BAR
			| BrowserViewer.LOCATION_BAR;

	/**
	 * WebBrowserUtil constructor comment.
	 */
	public WebBrowserUtil() {
		super();
	}

	/**
	 * Returns true if we're running on Windows.
	 * 
	 * @return boolean
	 */
	public static boolean isWindows() {
		String os = System.getProperty("os.name"); //$NON-NLS-1$
		if (os != null && os.toLowerCase().indexOf("win") >= 0) //$NON-NLS-1$
			return true;
		return false;
	}

	/**
	 * Returns true if we're running on linux.
	 * 
	 * @return boolean
	 */
	public static boolean isLinux() {
		String os = System.getProperty("os.name"); //$NON-NLS-1$
		if (os != null && os.toLowerCase().indexOf("lin") >= 0) //$NON-NLS-1$
			return true;
		return false;
	}

	/**
	 * Open a dialog window.
	 * 
	 * @param message
	 *            java.lang.String
	 */
	public static void openError(String message) {
		Display d = Display.getCurrent();
		if (d == null)
			d = Display.getDefault();

		Shell shell = d.getActiveShell();
		MessageDialog.openError(shell, Messages.errorDialogTitle, message);
	}

	/**
	 * Open a dialog window.
	 * 
	 * @param message
	 *            java.lang.String
	 */
	public static void openMessage(String message) {
		Display d = Display.getCurrent();
		if (d == null)
			d = Display.getDefault();

		Shell shell = d.getActiveShell();
		MessageDialog.openInformation(shell, Messages.searchingTaskName,
				message);
	}

	/**
	 * Encodes browser style in the secondary id as id-style
	 * 
	 * @param browserId
	 * @param style
	 * @return secondaryId
	 */
	public static String encodeStyle(String browserId, int style) {
		return browserId + STYLE_SEP + style;
	}

	/**
	 * Decodes secondary id into a browser style.
	 * 
	 * @param secondaryId
	 * @return style
	 */
	public static int decodeStyle(String secondaryId) {
		if (secondaryId != null) {
			int sep = secondaryId.lastIndexOf(STYLE_SEP);
			if (sep != -1) {
				String stoken = secondaryId.substring(sep + 1);
				try {
					return Integer.parseInt(stoken);
				} catch (NumberFormatException e) {
					// ignore
				}
			}
		}
		return DEFAULT_STYLE;
	}

	public static String decodeId(String encodedId) {
		int sep = encodedId.lastIndexOf(STYLE_SEP);
		if (sep != -1) {
			return encodedId.substring(0, sep);
		}
		return encodedId;
	}
}