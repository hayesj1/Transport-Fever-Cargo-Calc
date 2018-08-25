package com.tigerbird1.TpFCargoCalc.cargo;

import com.tigerbird1.TpFCargoCalc.io.DataDelegate;
import org.xml.sax.Attributes;

import java.util.HashMap;
import java.util.HashSet;

public final class RecipeGraph implements DataDelegate {
	private static int recipeCounter = 0;
	private HashMap<Cargo, RecipeItem> vertices;
	private HashSet<Recipe> edges;

	private Cargoes cargoes;
	private RecipeContext context;
	private boolean receivingInputs;

	@Override
	public void initialize() {
		vertices = new HashMap<>();
		edges = new HashSet<>();
		context = new RecipeContext();
		receivingInputs = true;
	}

	@Override
	public void receiveData(String element, Attributes atts) {
		RecipeItem item;
		String name = "";
		int c_id = -1;
		int amount = 0;

		for (int i = 0; i < atts.getLength(); i++) {
			String value = atts.getValue(i);
			switch (atts.getQName(i)) {
				case "c_id":
					c_id = Integer.valueOf(value);
					name = cargoes.getName(value);
					break;
				case "amount":
					amount = Integer.valueOf(value);
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
				Cargo cargo = cargoes.getCargoByCID(c_id);
				item = vertices.getOrDefault(cargo,  new RecipeItem(cargo));
				if (this.receivingInputs) { this.context.addInput(item, amount); }
				else { this.context.addOutput(item, amount); }

			default:
				break;
		}
	}

	@Override
	public void completed() {
		this.context = null;
		this.receivingInputs = false;
	}

	public void setCargoes(Cargoes cargoes) {
		this.cargoes = cargoes;
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

		void addInput(RecipeItem vertex, int amount) {
			this.inputs.put(vertex, amount);
			clear = false;
		}
		void addOutput(RecipeItem vertex, int amount) {
			this.outputs.put(vertex,  amount);
			clear = false;
		}

		Recipe createRecipe() {
			Recipe ret = new Recipe();
			inputs.forEach(ret::addComponent);
			outputs.forEach(ret::addProduct);

			return ret;
		}

		void clearContext() {
			inputs.clear();
			outputs.clear();
			clear = true;
		}
		boolean cleared() { return clear; }
	}

	private final class Recipe {
		private HashMap<RecipeItem, Integer> products;
		private HashMap<RecipeItem, Integer> components;
		private final int id = ++recipeCounter;

		Recipe() {
			this.products = new HashMap<>();
			this.components = new HashMap<>();
		}

		public void addProduct(RecipeItem prod, int yield) {
			products.put(prod, yield);
		}
		public void addComponent(RecipeItem comp, int amount) {
			components.put(comp, amount);
		}
	}

	private final class RecipeItem {
		private Cargo cargo;

		RecipeItem(Cargo cargo) { this.cargo = cargo; }
		RecipeItem(String cargoName) { this.cargo = cargoes.getCargoByName(cargoName); }
		RecipeItem(int c_id) { this.cargo = cargoes.getCargoByCID(c_id); }

		Cargo getCargo() { return cargo; }

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			RecipeItem that = (RecipeItem) o;
			return cargo.equals(that.cargo);
		}

		@Override
		public int hashCode() { return cargo.hashCode(); }
	}
}