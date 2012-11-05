package au.gov.ga.earthsci.worldwind.common.layers.styled;

public class StringWithPlaceholderGetter
{
	public static String getTextString(Attribute attribute)
	{
		return attribute.textString.string;
	}
	
	public static String getTextPlaceholder(Attribute attribute)
	{
		return attribute.textString.placeholder;
	}
}
