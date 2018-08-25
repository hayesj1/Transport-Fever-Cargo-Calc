package com.tigerbird1.TpFCargoCalc.cargo;

import com.tigerbird1.TpFCargoCalc.io.DataDelegate;
import org.xml.sax.Attributes;

import java.util.HashSet;

public class Cargoes implements DataDelegate {
	private HashSet<Cargo> cargoes;
	private String group;

	@Override
	public void initialize() {
		cargoes = new HashSet<>();
		group = "";
	}

	@Override
	public void receiveData(String element, Attributes atts) {
		String label = "";
		String name = Cargo.NIL_CARGO.getName();
		int c_id = -1;
		for (int i = 0; i < atts.getLength(); i++) {
			String value = atts.getValue(i);
			switch (atts.getQName(i)) {
				case "label":
					label = value;
					break;
				case "c_id":
					c_id = Integer.valueOf(value);
					break;
				case "name":
					name = value;
				default:
					break;
			}
		}

		switch(element) {
			case "cargogroup":
				this.group = label;
				break;
			case "cargodef":
				this.cargoes.add(new Cargo(c_id, name.toLowerCase()));
				break;
			default:
				break;
		}
	}

	@Override
	public void completed() {
		this.group = "";
	}

	public String getName(String c_id) { return this.getName(Integer.valueOf(c_id)); }
	public String getName(int c_id) { return this.getCargoByCID(c_id).getName().toLowerCase(); }

	public Cargo getCargoByCID(String c_id) { return this.getCargoByCID(Integer.valueOf(c_id)); }
	public Cargo getCargoByCID(int c_id) {
		for (Cargo tmp : cargoes) {
			if (c_id == tmp.getCID()) { return tmp; }
		}
		return Cargo.NIL_CARGO;
	}
	public Cargo getCargoByName(String name) {
		name = name.toLowerCase();
		for (Cargo tmp : cargoes) {
			if (name.equals(tmp.getName())) { return tmp; }
		}
		return Cargo.NIL_CARGO;
	}

	public Cargo[] getCargoes() { return  (cargoes == null) ? (new Cargo[0]) : (cargoes.toArray(new Cargo[0])); }
}
