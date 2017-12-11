package com.tigerbird1.TpFCargoCalc.io;

import org.xml.sax.*;

import java.util.ArrayList;
import java.util.Collections;

public class DataHandler extends XMLEventHandler {
	private ArrayList<DataDelegate> dataDels;

	DataHandler(DataDelegate... delegates) {
		this.dataDels = new ArrayList<>(delegates.length == 0 ? 4 : delegates.length*2);

		Collections.addAll(dataDels, delegates);
	}

	public void addDelegate(DataDelegate del) { dataDels.add(del); }

	@Override
	public void startDocument() throws SAXException {
		dataDels.forEach(DataDelegate::initialize);
	}

	@Override
	public void endDocument() throws SAXException {
		dataDels.forEach(DataDelegate::completed);
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		if ("".equals(uri)) {
			dataDels.forEach( del -> del.receiveData(uri+"."+localName, atts));
		} else {
			dataDels.forEach( del -> del.receiveData(qName, atts));
		}
	}
}
