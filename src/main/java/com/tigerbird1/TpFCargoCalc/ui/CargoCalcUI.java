package com.tigerbird1.TpFCargoCalc.ui;

import com.tigerbird1.TpFCargoCalc.Cargo;
import com.tigerbird1.TpFCargoCalc.Utils;
import com.tigerbird1.TpFCargoCalc.io.Cargoes;
import com.tigerbird1.TpFCargoCalc.io.RecipeGraph;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Enumeration;

public class CargoCalcUI {
	private JFrame frame;
	private JPanel contentPane;
	private JTextField frequency;
	private JTextField capacity;
	private JButton addLeg;
	private JButton addCity;
	private JSlider nVehicles;
	private JTree legs;
	private JLabel nVehiclesLabel;
	private JComboBox<Cargo> cargoChooser;
	private JButton moveNodeU;
	private JButton moveNodeD;
	private JButton editSettings;
	private JButton addChain;
	private JButton validate;
	private JButton deleteNode;
	private JButton addLabel;

	private CityDialog cityDialog;
	private DefaultComboBoxModel<Cargo> cargoChooserModel;
	private DefaultMutableTreeNode root;
	private DefaultTreeModel treeModel;

	private RecipeGraph recipeGraph;
	private Cargoes cargoes;
	private int chainCnt = 1;

	public CargoCalcUI() {
		$$$setupUI$$$();
		addLeg.addActionListener(e -> addLeg());
		addCity.addActionListener(e -> addCity());

		moveNodeU.addActionListener(e -> moveNode(true));
		moveNodeD.addActionListener(e -> moveNode(false));

		nVehicles.addChangeListener(e -> nVehiclesLabel.setText("Vehicle(s): " + nVehicles.getValue()));

		// call onCancel() on ESCAPE
		contentPane.registerKeyboardAction(e -> onClose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

		this.frame = new JFrame("Cargo Calc");
		this.frame.setContentPane(this.contentPane);
		this.frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.frame.setSize(720, 360);
		this.frame.pack();

		this.frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				onClose();
			}
		});
		addChain.addActionListener(e -> treeModel.insertNodeInto(new DefaultMutableTreeNode("Chain " + ( chainCnt++ )), root, root.getChildCount()));
		deleteNode.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteNode();
			}
		});
		validate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (validateChains()) { Utils.showChainsValidInfo(frame); }
				else { Utils.showChainsUnoptimzedWarning(frame); }
			}
		});
	}

	private void onClose() {
		this.cityDialog.dispose();
	}

	private void createUIComponents() {
		cityDialog = new CityDialog(this.frame);

		cargoChooserModel = new DefaultComboBoxModel<>();
		cargoChooser = new JComboBox<>(cargoChooserModel);


		root = new DefaultMutableTreeNode("Root");
		treeModel = new DefaultTreeModel(root);
		treeModel.insertNodeInto(new DefaultMutableTreeNode("Chain " + ( chainCnt++ )), root, root.getChildCount());
		legs = new JTree(treeModel);
		legs.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		legs.setEditable(true);

	}

	private DefaultMutableTreeNode addNode(Object child) {
		DefaultMutableTreeNode parentNode;
		TreePath parentPath = legs.getSelectionPath();

		if (parentPath == null) { //There is no selection. Default to the root node.
			parentNode = (DefaultMutableTreeNode) root.getFirstChild();
		} else {
			parentNode = (DefaultMutableTreeNode) ( parentPath.getLastPathComponent() );
		}

		return addNode(parentNode, child, true);
	}

	private DefaultMutableTreeNode addNode(DefaultMutableTreeNode parent, Object child, boolean shouldBeVisible) {
		DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);
		treeModel.insertNodeInto(childNode, parent, parent.getChildCount());

		//Make sure the user can see the new node.
		if (shouldBeVisible) {
			legs.scrollPathToVisible(new TreePath(childNode.getPath()));
		}
		return childNode;
	}

	private void moveNode(boolean moveUp) {
		TreePath path = legs.getSelectionPath();
		if (path == null) { //There is no selection. Default to the root node.
			return;
		}
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) ( path.getLastPathComponent() );
		if (node.equals(root)) {
			return;
		}

		moveNode(node, moveUp);
		TreePath newPath = new TreePath(node.getPath());
		legs.setSelectionPath(newPath);
	}

	private void moveNode(DefaultMutableTreeNode node, boolean moveUp) {
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
		DefaultMutableTreeNode other = ( moveUp ) ? node.getPreviousSibling() : node.getNextSibling();
		try {
			int newIdx = -1;
			if (other != null) {
				parent = (DefaultMutableTreeNode) ( ( moveUp ) ? other.getLastLeaf().getParent() : other );
				newIdx = ( node.getParent().equals(parent) ) ? ( ( moveUp ) ? parent.getIndex(node) - 1 : parent.getIndex(node) + 1 ) : ( ( moveUp ) ? parent.getChildCount() : 0 );
			} else {
				DefaultMutableTreeNode newParent;
				DefaultMutableTreeNode grandParent = (DefaultMutableTreeNode) parent.getParent();
				if (grandParent == null) {
					return;
				}

				if (moveUp) {
					if (!grandParent.getFirstChild().equals(parent)) {
						DefaultMutableTreeNode uncle = (DefaultMutableTreeNode) grandParent.getChildBefore(parent);
						newParent = (DefaultMutableTreeNode) uncle.getLastLeaf().getParent();
						newIdx = newParent.getChildCount();
					} else if (grandParent.equals(root)) {
						return;
					} else {
						newParent = grandParent;
						newIdx = 0;
					}
				} else {
					if (!grandParent.getLastChild().equals(parent)) {
						DefaultMutableTreeNode uncle = (DefaultMutableTreeNode) grandParent.getChildAfter(parent);
						newParent = uncle;
						newIdx = 0;
					} else if (grandParent.equals(root)) {
						return;
					} else {
						newParent = grandParent;
						newIdx = newParent.getChildCount();
					}
				}

				parent = newParent;
			}

			treeModel.removeNodeFromParent(node);
			treeModel.insertNodeInto(node, parent, newIdx);
		} catch (NullPointerException | IllegalArgumentException ignored) {
		}
	}

	private void deleteNode() {
		TreePath path = legs.getSelectionPath();
		if (path == null) { //There is no selection. Default to the root node.
			return;
		}
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) ( path.getLastPathComponent() );
		treeModel.removeNodeFromParent(node);
	}

	private void addLeg() {
		if (cargoChooser.getSelectedIndex() == -1) {
			Utils.showNoCargoSelectedError(this.frame);
		} else {
			String legStr = getLegString();
			addNode(legStr);
		}
	}

	private String getLegString() {
		Cargo cargo = ( (Cargo) cargoChooser.getSelectedItem() );
		int freq = getFrequency();
		float cap = Float.valueOf(capacity.getText());
		int nVehicles = this.nVehicles.getValue();
		float waresPerSecond = cap / ( freq * nVehicles );
		return cargo + "--" + waresPerSecond + ':' + cap + "--" + freq + "--" + nVehicles;
	}

	private int getFrequency() {
		String text = frequency.getText();
		String[] tmp = text.split(":");
		int minutes = Integer.valueOf(tmp[0]);
		int seconds = Integer.valueOf(tmp[1]);
		return minutes * 60 + seconds;
	}

	private void addCity() {
		TreePath path = legs.getSelectionPath();
		DefaultMutableTreeNode parent;
		DefaultMutableTreeNode lastPathComponent = (DefaultMutableTreeNode) ( ( path != null ) ? path.getLastPathComponent() : null );
		if (lastPathComponent == null || !( (String) lastPathComponent.getUserObject() ).toLowerCase().contains("chain")) {
			Utils.showNoChainSelectedError(this.frame);
			return;
		} else {
			parent = lastPathComponent;
		}

		String label = createCityString();
		if (label == null) { return; }

		addNode(parent, label, true);
	}

	private String createCityString() {
		cityDialog.setVisible(true);

		if (!cityDialog.areValuesReady()) {
			return null;
		}
		cityDialog.setVisible(false);
		Cargo cargo = cityDialog.getCargo();
		float cap = cityDialog.getCapacity();
		float waresPerSecond = cityDialog.getWaresPerSecond();
		String cityName = cityDialog.getCityName();
		String label = ( cityName.length() > 0 ? cityName + "--" + waresPerSecond + ":" : waresPerSecond + "--" ) + cargo + "--" + cap;
		return label;
	}

	public boolean validateChains() {
		Enumeration enume = root.children();
		while (enume.hasMoreElements()) {
			DefaultMutableTreeNode chain = (DefaultMutableTreeNode) enume.nextElement();
			if (!validateChain(chain)) {
				return false;
			}
		}

		return true;
	}

	private boolean validateChain(DefaultMutableTreeNode chain) {
		int nChildren = chain.getChildCount();
		ArrayList<Float> waresPerSecondFactories = new ArrayList<>(nChildren);
		ArrayList<Float> waresPerSecondCities = new ArrayList<>(nChildren);
		Enumeration enume = chain.children();

		while (enume.hasMoreElements()) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) enume.nextElement();
			String childStr = ( (String) ( child.getUserObject() ) ).toLowerCase();
			String[] tmp = childStr.split(":");
			String[][] vals = new String[2][0];

			for (int i = 0; i < 2; i++) { vals[i] = tmp[i].split("--"); }
			Float wps = Float.valueOf(vals[0][1]);
			if (vals[1].length == 2) {
				waresPerSecondCities.add(wps);
			} else {
				waresPerSecondFactories.add(wps);
				if (!validateSubTree(child, wps)) { return false; }
			}
		}
		float aggregatedCityWPS = 0.0f, aggregatedFactoryWPS = 0.0f;
		for (Float f : waresPerSecondFactories) { aggregatedFactoryWPS += f; }
		for (Float f : waresPerSecondCities) { aggregatedCityWPS += f; }

		return Utils.epsilonEquals(aggregatedCityWPS, aggregatedFactoryWPS);
	}

	private boolean validateSubTree(DefaultMutableTreeNode root, float rootWPS) {
		ArrayList<Float> childrenWPS = new ArrayList<>(root.getChildCount());
		Enumeration enume = root.children();
		while (enume.hasMoreElements()) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) enume.nextElement();
			String childStr = ( (String) child.getUserObject() ).toLowerCase();
			String[] tmp = childStr.split(":");
			String[] vals = tmp[0].split("--");
			float childWPS = Float.valueOf(vals[1]);

			boolean childValid = validateSubTree(child, childWPS);
			if (!childValid) {
				return false;
			} else {
				childrenWPS.add(childWPS);
			}
		}
		float aggregatedChildrenWPS = 0.0f;
		for (Float childWPS : childrenWPS) {
			aggregatedChildrenWPS += childWPS;
		}
		return rootWPS >= aggregatedChildrenWPS;
	}

	public void setCargoes(Cargoes cargoes) {
		this.cargoes = cargoes;
		for (Cargo c : this.cargoes.getCargoes()) {
			this.cargoChooserModel.addElement(c);
		}
		this.cargoChooser.setModel(this.cargoChooserModel);
		this.cityDialog.setCargoChooserModel(this.cargoChooserModel);
	}

	public void setRecicpeGraph(RecipeGraph graph) {
		this.recipeGraph = graph;
	}

	public void setVisible(boolean flag) {
		this.frame.setVisible(flag);
	}

	public boolean isShowing() {
		return this.frame.isShowing();
	}

	/**
	 * Method generated by IntelliJ IDEA GUI Designer
	 * >>> IMPORTANT!! <<<
	 * DO NOT edit this method OR call it in your code!
	 *
	 * @noinspection ALL
	 */
	private void $$$setupUI$$$() {
		createUIComponents();
		contentPane = new JPanel();
		contentPane.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(10, 5, new Insets(16, 16, 16, 16), -1, -1));
		contentPane.putClientProperty("html.disable", Boolean.TRUE);
		capacity = new JTextField();
		capacity.setText("1.0");
		capacity.putClientProperty("html.disable", Boolean.TRUE);
		contentPane.add(capacity, new com.intellij.uiDesigner.core.GridConstraints(9, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		final JLabel label1 = new JLabel();
		label1.setText("Capacity");
		label1.putClientProperty("html.disable", Boolean.TRUE);
		contentPane.add(label1, new com.intellij.uiDesigner.core.GridConstraints(8, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		legs.setAutoscrolls(false);
		legs.setDragEnabled(true);
		legs.setDropMode(DropMode.INSERT);
		legs.setEditable(true);
		legs.setEnabled(true);
		legs.setRootVisible(false);
		legs.setShowsRootHandles(true);
		legs.setToolTipText("Legs");
		legs.setVisible(true);
		legs.putClientProperty("JTree.lineStyle", "");
		legs.putClientProperty("html.disable", Boolean.TRUE);
		contentPane.add(legs, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 8, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(20, 60), new Dimension(20, 60), null, 0, false));
		final JLabel label2 = new JLabel();
		label2.setText("Frequency (mins:secs)");
		contentPane.add(label2, new com.intellij.uiDesigner.core.GridConstraints(8, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		frequency = new JTextField();
		frequency.setText("0:1");
		frequency.putClientProperty("html.disable", Boolean.TRUE);
		contentPane.add(frequency, new com.intellij.uiDesigner.core.GridConstraints(9, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		nVehiclesLabel = new JLabel();
		nVehiclesLabel.setText("Vehicle(s): ");
		contentPane.add(nVehiclesLabel, new com.intellij.uiDesigner.core.GridConstraints(8, 4, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		nVehicles = new JSlider();
		nVehicles.setExtent(0);
		nVehicles.setInverted(false);
		nVehicles.setMajorTickSpacing(16);
		nVehicles.setMaximum(80);
		nVehicles.setMinimum(1);
		nVehicles.setMinorTickSpacing(4);
		nVehicles.setPaintLabels(false);
		nVehicles.setPaintTicks(true);
		nVehicles.setPaintTrack(true);
		nVehicles.setSnapToTicks(false);
		nVehicles.setValue(1);
		nVehicles.setValueIsAdjusting(false);
		nVehicles.putClientProperty("JSlider.isFilled", Boolean.FALSE);
		nVehicles.putClientProperty("Slider.paintThumbArrowShape", Boolean.TRUE);
		nVehicles.putClientProperty("html.disable", Boolean.TRUE);
		contentPane.add(nVehicles, new com.intellij.uiDesigner.core.GridConstraints(9, 4, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		addLeg = new JButton();
		addLeg.setText("+ Leg");
		addLeg.setToolTipText("Add leg");
		contentPane.add(addLeg, new com.intellij.uiDesigner.core.GridConstraints(8, 0, 2, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		editSettings = new JButton();
		editSettings.setEnabled(false);
		editSettings.setText("Settings");
		contentPane.add(editSettings, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		moveNodeU = new JButton();
		moveNodeU.setText("Move up");
		contentPane.add(moveNodeU, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		contentPane.add(cargoChooser, new com.intellij.uiDesigner.core.GridConstraints(9, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
		contentPane.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(7, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		final com.intellij.uiDesigner.core.Spacer spacer2 = new com.intellij.uiDesigner.core.Spacer();
		contentPane.add(spacer2, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 6, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		validate = new JButton();
		validate.setText("Validate");
		contentPane.add(validate, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		moveNodeD = new JButton();
		moveNodeD.setText("Move down");
		contentPane.add(moveNodeD, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label3 = new JLabel();
		label3.setText("Choose Cargo");
		contentPane.add(label3, new com.intellij.uiDesigner.core.GridConstraints(8, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final com.intellij.uiDesigner.core.Spacer spacer3 = new com.intellij.uiDesigner.core.Spacer();
		contentPane.add(spacer3, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		deleteNode = new JButton();
		deleteNode.setText("X");
		deleteNode.setToolTipText("Delete selection");
		contentPane.add(deleteNode, new com.intellij.uiDesigner.core.GridConstraints(5, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		addChain = new JButton();
		addChain.setText("+ Chain");
		addChain.setToolTipText("Add new chain");
		contentPane.add(addChain, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		addCity = new JButton();
		addCity.setText("+ City");
		addCity.setToolTipText("Add City");
		contentPane.add(addCity, new com.intellij.uiDesigner.core.GridConstraints(4, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		addLabel = new JButton();
		addLabel.setEnabled(false);
		addLabel.setText("Label As");
		addLabel.setToolTipText("Label Selection");
		contentPane.add(addLabel, new com.intellij.uiDesigner.core.GridConstraints(6, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		label1.setLabelFor(capacity);
		label2.setLabelFor(frequency);
		label3.setLabelFor(cargoChooser);
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return contentPane;
	}
}
