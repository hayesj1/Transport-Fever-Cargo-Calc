package com.tigerbird1.TpFCargoCalc.io;

import com.tigerbird1.TpFCargoCalc.Configuration;
import org.xml.sax.*;

public class ConfigHandler extends XMLEventHandler{

	private Configuration configuration;

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {

	}

	public Configuration getConfiguration() {
		return configuration;
	}
}
