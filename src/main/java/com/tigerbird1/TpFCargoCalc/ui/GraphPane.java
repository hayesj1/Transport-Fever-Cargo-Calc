package com.tigerbird1.TpFCargoCalc.ui;

import com.tigerbird1.TpFCargoCalc.cargo.Cargo;
import com.tigerbird1.TpFCargoCalc.cargo.RecipeGraph;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class GraphPane extends JPanel {
	private ArrayList<Node> nodes;
	private ArrayList<Edge> edges;
	private HashMap<Integer, Chain> chains;
	private RecipeGraph recipes;

	private int chainCnt = 0;
	private int cityCnt = 0;

	public GraphPane(RecipeGraph recipes) {
		this(recipes, true);
	}
	public GraphPane(RecipeGraph recipes, LayoutManager layout) {
		this(recipes, layout, true);
	}
	public GraphPane(RecipeGraph recipes, boolean isDoubleBuffered) {
		this(recipes, new FlowLayout(), isDoubleBuffered);
	}
	public GraphPane(RecipeGraph recipes, LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);

		this.nodes = new ArrayList<>(5);
		this.edges = new ArrayList<>(4);
		this.chains = new HashMap<>(6);
		this.recipes = recipes;
	}

	public void addChain(Cargo end) {
		Node terminal = new Node(end);
		chains.put(++chainCnt, new Chain(terminal));

	}
	public void addIntermediateNode(int chainID, Node n) {
		this.nodes.add(n);
		this.chains.get(chainID).addIntermediate(n);
	}
	public void addInitialNode(int chainID, int intermediateIndex, Node n) {
		this.nodes.add(n);
		this.chains.get(chainID).addInitial(n, 0);
	}

	private class Edge {
		Node src;
		Node dest;

		Edge(Node src, Node dest) {
			this.src = src;
			this.dest = dest;
		}
	}

	private class Chain {
		Node term;
		ArrayList<Node> intermediates;
		ArrayList<ArrayList<Node>> initials;

		Chain(Node end) {
			this.term = end;
			this.intermediates = new ArrayList<>(3);
			this.initials = new ArrayList<>(5);
		}

		void addIntermediate(Node n) {
			if (!intermediates.contains(n)) {
				intermediates.add(n);
				initials.add(new ArrayList<>(3));
			}
		}

		void addInitial(Node n, int intermediateIndex) {
			ArrayList<Node> arr = initials.get(intermediateIndex);
			if (!arr.contains(n)) { arr.add(n); }
		}
	}
}
