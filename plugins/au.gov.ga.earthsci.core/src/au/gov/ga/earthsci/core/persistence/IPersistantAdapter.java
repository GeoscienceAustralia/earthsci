package au.gov.ga.earthsci.core.persistence;

import java.net.URI;

import org.w3c.dom.Element;

public interface IPersistantAdapter<E>
{
	void toXML(E object, Element parent, URI context);

	E fromXML(Element element, URI context);
}
