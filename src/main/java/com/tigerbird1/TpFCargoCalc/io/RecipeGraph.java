package com.tigerbird1.TpFCargoCalc.io;

import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class RecipeGraph implements DataDelegate {
	private ArrayList<RecipeItem> vertices;
	private ArrayList<Recipe> edges;

	private CargoTypes cargoes;
	private RecipeContext context;
	private boolean receivingInputs;

	@Override
	public void initialize() {
		vertices = new ArrayList<>();
		edges = new ArrayList<>();
		context = new RecipeContext();
		receivingInputs = true;
	}

	@Override
	public void receiveData(String element, Attributes atts) {
		String cargo = "";
		int amount = 0;
		for (int i = 0; i < atts.getLength(); i++) {
			switch (atts.getQName(i)) {
				case "id":
					cargo = this.cargoes.getName(atts.getValue(i));
					break;
				case "amount":
					amount = Integer.valueOf(atts.getValue(i));
					break;
				default:
					break;
			}
		}
		switch(element) {
			case "recipe":
				if (!context.cleared()) { edges.add(context.createRecipe()); }
				context.clearContext();
				break;
			case "input":
				receivingInputs = true;
				break;
			case "output":
				receivingInputs = false;
				break;
			case "cargo":
				if (this.receivingInputs) { this.context.addInput(cargo, amount); }
				else { this.context.addOutput(cargo, amount); }
				break;
			default:
				break;
		}

	}

	@Override
	public void completed() {
		this.context = null;
		this.receivingInputs = false;
	}

	public void setCargoTypes(CargoTypes cargoTypes) {
		this.cargoes = cargoTypes;
	}


	private class RecipeContext {
		private HashMap<RecipeItem, Integer> inputs;
		private HashMap<RecipeItem, Integer> outputs;
		private boolean clear;

		RecipeContext() {
			this.inputs = new HashMap<>();
			this.outputs = new HashMap<>();
			clear = true;
		}

		void addInput(String cargoType, int amount) {
			this.inputs.put(new RecipeItem(cargoType), amount);
			clear = false;
		}
		void addOutput(String cargoType, int amount) {
			this.outputs.put(new RecipeItem(cargoType), amount);
			clear = false;
		}

		Recipe createRecipe() {
			Recipe ret = new Recipe();

			inputs.forEach(ret::addProduct);
			outputs.forEach(ret::addComponent);

			return ret;
		}

		void clearContext() {
			inputs.clear();
			outputs.clear();
			clear = true;
		}
		boolean cleared() { return clear; }
	}

	private class Recipe {
		private HashMap<RecipeItem, Integer> products;
		private HashMap<RecipeItem, Integer> components;

		Recipe() {
			this.products = new HashMap<>();
			this.components = new HashMap<>();
		}

		public void addProduct(RecipeItem prod, int yield) {
			components.put(prod, yield);
		}
		public void addComponent(RecipeItem comp, int amount) {
			components.put(comp, amount);
		}
	}

	private class RecipeItem {
		String cargoType;

		RecipeItem(String cargoType) {
			this.cargoType = cargoType;
		}

		public String getCargoType() { return cargoType; }
		public void setCargoType(String cargoType) { this.cargoType = cargoType; }

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			RecipeItem that = (RecipeItem) o;
			return Objects.equals(cargoType, that.cargoType);
		}

		@Override
		public int hashCode() {
			return cargoType.hashCode();
		}
	}
}
