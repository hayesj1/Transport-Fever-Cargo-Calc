package com.tigerbird1.TpFCargoCalc.config;

import com.tigerbird1.TpFCargoCalc.io.XMLEventHandler;
import org.xml.sax.Attributes;

public class ConfigHandler extends XMLEventHandler {

	private Configuration configuration = Configuration.getInstance();

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) {
		if (!"".equals(uri)) {
			readOption(uri + "." + localName, atts);
		} else {
			readOption(qName, atts);
		}
	}

	private void readOption(String element, Attributes atts) {
		String type = "";
		String name = "";
		String value = "";
		for (int i = 0; i < atts.getLength(); i++) {
			String att_val = atts.getValue(i);
			switch (atts.getQName(i)) {
				case "type":
					type = att_val;
					break;
				case "name":
					name = att_val;
					break;
				case "value":
					value = att_val;
				default:
					break;
			}
		}
		switch (element) {
			case "option":
				try {
					switch (type.toLowerCase()) {
						case "float":
							configuration.setFloat(name, Float.valueOf(value));
							break;
						case "short":
						case "int":
							configuration.setInt(name, Integer.valueOf(value));
							break;
						case "boolean":
							configuration.setBoolean(name, Boolean.valueOf(value));
							break;
						case "string":
						case "object":
							configuration.setObject(name, value);
							break;
						default:
							break;
					}
				} catch (NoSuchFieldException | IllegalAccessException ignored) {
				}
				break;
			default:
				break;
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) {
	}

	public Configuration getConfiguration() {
		return configuration;
	}
}
