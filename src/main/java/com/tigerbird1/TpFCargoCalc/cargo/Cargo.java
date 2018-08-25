package com.tigerbird1.TpFCargoCalc.cargo;

import java.util.Objects;

public class Cargo {
	public static final Cargo NIL_CARGO = new Cargo(-1, "Nil");
	private int c_id;
	private String name;

	public Cargo(int c_id, String name) {
		this.c_id = c_id;
		this.name = name;
	}

	public int getCID() { return c_id; }
	public String getName() { return name; }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Cargo cargo = (Cargo) o;
		return c_id == cargo.c_id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(c_id, name);
	}

	@Override
	public String toString() {
		StringBuilder tmp = new StringBuilder(name);
		Character first = Character.toTitleCase(tmp.charAt(0));
		tmp.replace(0,1, first.toString());
		return tmp.toString();
	}
}
