package au.gov.ga.earthsci.model.ui.raster.wizard;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "au.gov.ga.earthsci.model.ui.raster.wizard.messages"; //$NON-NLS-1$
	public static String RasterModelBandSelectPage_BandDropdownPrefix;
	public static String RasterModelBandSelectPage_BandSelectDropdownLabel;
	public static String RasterModelBandSelectPage_BandSelectGroupDescription;
	public static String RasterModelBandSelectPage_BandSelectGroupTitle;
	public static String RasterModelBandSelectPage_InvalidOffsetMessage;
	public static String RasterModelBandSelectPage_InvalidScaleMessage;
	public static String RasterModelBandSelectPage_PageDescription;
	public static String RasterModelBandSelectPage_PageTitle;
	public static String RasterModelBandSelectPage_ScaleOffsetGroupTitle;
	public static String RasterModelBandSelectPage_ScaleOffsetGroupDescription;
	public static String RasterModelBandSelectPage_ScaleFieldLabel;
	public static String RasterModelBandSelectPage_OffsetFieldLabel;
	public static String RasterModelParametersWizard_WizardTitle;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
