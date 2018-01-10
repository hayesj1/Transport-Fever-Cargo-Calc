package com.tigerbird1.TpFCargoCalc.cargo;

import com.tigerbird1.TpFCargoCalc.ui.TierPaneItem;

public class Route extends TierPaneItem {
	private Cargo cargo;
	private boolean isCityRoute;
	private int capacity;
	private int frequency;
	private int nVehicles;

	public Route(String title, String label, boolean isCityRoute, Cargo cargo, int capacity, int frequency, int nVehicles) {
		super(title, label);
		this.cargo = cargo;
		this.isCityRoute = isCityRoute;

		this.nVehicles = ( isCityRoute ) ? -1 : nVehicles;
		this.capacity = capacity;
		this.frequency = frequency;
	}

	@Override
	public String toString() {
		return this.cargo + ( isCityRoute ? " --> " + this.title : " -- " + this.getWps() );
	}


	public Cargo getCargo() {
		return cargo;
	}

	public void setCargo(Cargo cargo) {
		this.cargo = cargo;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public int getNVehicles() {
		return ( isCityRoute() ) ? -1 : nVehicles;
	}

	public void setnVehicles(int nVehicles) {
		this.nVehicles = nVehicles;
	}

	public float getWps() {
		return capacity / ( frequency * ( isCityRoute() ? 1.0f : nVehicles * 1.0f ) );
	}

	public boolean isCityRoute() {
		return isCityRoute;
	}
}
