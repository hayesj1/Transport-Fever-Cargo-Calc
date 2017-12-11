package com.tigerbird1.TpFCargoCalc.io;

import org.xml.sax.Attributes;

import java.util.HashMap;

public class CargoTypes implements DataDelegate {
	private HashMap<Integer, String> cargoes;
	private String group;

	@Override
	public void initialize() {
		cargoes = new HashMap<>();
		group = "";
	}

	@Override
	public void receiveData(String element, Attributes atts) {
		String label = "";
		String name = "None";
		int id = -1;

		for (int i = 0; i < atts.getLength(); i++) {
			switch (atts.getQName(i)) {
				case "label":
					label = atts.getValue(i);
					break;
				case "id":
					id = Integer.valueOf(atts.getValue(i));
					break;
				case "name":
					name = atts.getValue(i);
				default:
					break;
			}
		}
		switch(element) {
			case "cargogroup":
				this.group = label;
				break;
			case "cargodef":
				this.cargoes.put(id, name);
				break;
			default:
				break;
		}

		return;
	}

	@Override
	public void completed() {
		this.group = "";
	}

	public String getName(String id) {
		return cargoes.getOrDefault(Integer.valueOf(id), "None");
	}

	public String[] getCargoTypes() { if (cargoes == null) { } return cargoes.values().toArray(new String[0]); }
}
