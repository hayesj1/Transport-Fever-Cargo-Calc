package com.tigerbird1.TpFCargoCalc.ui;

import com.tigerbird1.TpFCargoCalc.cargo.Cargo;

import javax.swing.*;
import java.util.ArrayList;

public class Node extends JLabel {
	private Cargo data;
	private ArrayList<Node> components;
	private ArrayList<Node> consumedBy;

	/**
	 * Creates a <code>Node</code> instance with the specified cargo and image.
	 * The label is centered vertically and horizontally in its display area.
	 *
	 * @param data The cargo to be used by the node.
	 * @param image The image to be displayed by the label.
	 */
	public Node(Cargo data, Icon image) {
		super(data.getName(), image, SwingUtilities.CENTER);
		init(data);
	}
	/**
	 * Creates a <code>Node</code> instance with the specified cargo.
	 * The label is centered vertically and horizontally in its display area.
	 *
	 * @param data The cargo to be used by the node.
	 */
	public Node(Cargo data) {
		super(data.getName(), SwingUtilities.CENTER);
		init(data);
	}

	private void init(Cargo data) {
		components = new ArrayList<>(3);
		consumedBy = new ArrayList<>(3);
	}

	public void addComponent(Node comp) {
		this.components.add(comp);
	}
	public Node removeComponent(Node comp) {
		return (this.components.remove(comp)) ? comp : null;
	}

	public void addConsumer(Node consumer) {
		this.consumedBy.add(consumer);
	}
	public Node removeConsumer(Node consumer) {
		return (this.consumedBy.remove(consumer)) ? consumer : null;
	}

	public Cargo getCargo() { return this.data; }
	public Node[] getComponents() { return this.components.toArray(new Node[0]); }
	public Node[] getConsumers() { return this.consumedBy.toArray(new Node[0]); }
}
