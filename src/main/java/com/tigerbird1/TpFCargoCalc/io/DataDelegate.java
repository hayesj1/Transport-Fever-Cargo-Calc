package com.tigerbird1.TpFCargoCalc.io;

import org.xml.sax.Attributes;

public interface DataDelegate {

	void initialize();
	void receiveData(String element, Attributes attributes);
	void completed();
}
