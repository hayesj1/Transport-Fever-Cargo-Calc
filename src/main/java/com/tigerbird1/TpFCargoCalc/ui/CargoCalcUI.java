package com.tigerbird1.TpFCargoCalc.ui;

import com.tigerbird1.TpFCargoCalc.CargoCalc;
import com.tigerbird1.TpFCargoCalc.Utils;
import com.tigerbird1.TpFCargoCalc.cargo.Cargo;
import com.tigerbird1.TpFCargoCalc.cargo.Cargoes;
import com.tigerbird1.TpFCargoCalc.cargo.RecipeGraph;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Enumeration;

public class CargoCalcUI {
	private JFrame frame;
	private JPanel contentPane;
	private JTabbedPane legPane;

	private JTextField frequency;
	private JTextField capacity;
	private JSlider nVehicles;
	private JLabel nVehiclesLabel;
	private JComboBox<Cargo> cargoChooser;

	private JButton editSettings;
	private JButton validate;

	private JButton moveItemU;
	private JButton moveItemD;

	private JButton addTier;
	private JButton addChain;
	private JButton addCity;
	private JButton addLeg;
	private JButton deleteItem;
	private JButton addItemLabel;

	private CityDialog cityDialog;
	private DefaultComboBoxModel<Cargo> cargoChooserModel;
	private ArrayList<TierPane> tierPanes = new ArrayList<>(4);

	private RecipeGraph recipeGraph;
	private Cargoes cargoes;
	private TierPane selectedTab;

	public CargoCalcUI() {
		$$$setupUI$$$();

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
		// call onClose() on ESCAPE
		contentPane.registerKeyboardAction(e -> onClose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

		addTier.addActionListener(e -> addTier());
		addChain.addActionListener(e -> ( (TierPane) legPane.getComponentAt(legPane.getSelectedIndex()) ).addChain());
		addCity.addActionListener(e -> ( (TierPane) legPane.getComponentAt(legPane.getSelectedIndex()) ).addCityRoute(getCityName(), getCityCargo(), getCityStats()));
		addLeg.addActionListener(e -> addLeg((TierPane) legPane.getComponentAt(legPane.getSelectedIndex())));
		moveItemU.addActionListener(e -> moveItem(true));
		moveItemD.addActionListener(e -> moveItem(false));
		deleteItem.addActionListener(e -> deleteItem());

		legPane.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				selectedTab = (TierPane) e.getComponent();
			}
		});

		validate.addActionListener(e -> {
			if (validateChains()) {
				Utils.showChainsValidInfo(frame);
			} else {
				Utils.showChainsUnoptimzedWarning(frame);
			}
		});

		nVehicles.addChangeListener(e -> nVehiclesLabel.setText("Vehicle(s): " + nVehicles.getValue()));
	}

	private void onClose() {
		this.cityDialog.dispose();
	}

	private void createUIComponents() {
		cargoChooserModel = CargoCalc.getUtils().getCargoChooserModel();
		cargoChooser = new JComboBox<>(cargoChooserModel);

		legPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
		addTier();
		selectedTab = (TierPane) legPane.getSelectedComponent();
		cityDialog = new CityDialog(this.frame);
	}

	private void deleteItem() {
		TierPane pane = (TierPane) legPane.getSelectedComponent();
		pane.removeItem(pane.getSelectedItem());
	}

	private void moveItem(boolean moveUp) {
		TierPane pane = selectedTab;
		TierPaneItem item = pane.getSelectedItem();
		pane.moveItem(item, moveUp);
	}

	private void addTier() {
		TierPane newTier = new TierPane();
		newTier.addListSelectionListener(e -> setListItemControlsEnabled(( (JList) e.getSource() ).getSelectedIndex() != -1));
		newTier.setParentPane(legPane);

	}

	private void addLeg(TierPane selectedPane) {
		if (cargoChooser.getSelectedIndex() == -1) {
			Utils.showNoCargoSelectedError(this.frame);
		} else {
			Cargo cargo = ( (Cargo) cargoChooser.getSelectedItem() );
			int freq = computeFrequency();
			int cap = Integer.valueOf(capacity.getText());
			int nVehicles = this.nVehicles.getValue();
			selectedPane.addLegRoute(cargo.toString(), cargo, new int[] { cap, freq, nVehicles });

			//float waresPerSecond = cap / ( freq * nVehicles );
			//return cargo + "\t" + waresPerSecond + " -- " + cap + "\t" + freq + "\t" + nVehicles;
		}
	}

	private int computeFrequency() {
		String text = frequency.getText();
		String[] tmp = text.split(":");
		int minutes = Integer.valueOf(tmp[0]);
		int seconds = Integer.valueOf(tmp[1]);
		return minutes * 60 + seconds;
	}

	private String buildCityString() {
		cityDialog.setVisible(true);

		if (!cityDialog.areValuesReady()) {
			return null;
		}
		cityDialog.setVisible(false);
		Cargo cargo = cityDialog.getCargo();
		int cap = cityDialog.getCapacity();
		float waresPerSecond = cityDialog.getWaresPerSecond();
		String cityName = cityDialog.getCityName();
		String label = ( cityName.length() > 0 ? cityName + "\t" + waresPerSecond + " -- " : waresPerSecond + " -- " ) + cargo + "\t" + cap;
		return label;
	}

	private String getCityName() {
		checkValuesReady();
		return cityDialog.getCityName();
	}

	private Cargo getCityCargo() {
		checkValuesReady();
		return cityDialog.getCargo();
	}

	private int[] getCityStats() {
		checkValuesReady();
		return new int[] { cityDialog.getCapacity(), cityDialog.getFrequency() };
	}

	private void checkValuesReady() {
		if (cityDialog.areValuesReady()) {
			return;
		} else if (cityDialog.isShowing()) {
			throw new DialogResultsNotReadyException(cityDialog);
		} else {
			cityDialog.setVisible(true);
		}
	}

	public boolean validateChains() {
		/*Enumeration enume = root.children();
		while (enume.hasMoreElements()) {
			DefaultMutableTreeNode chain = (DefaultMutableTreeNode) enume.nextElement();
			if (!validateChain(chain)) {
				return false;
			}
		}
		*/
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

			for (int i = 0; i < 2; i++) {
				vals[i] = tmp[i].split("--");
			}
			Float wps = Float.valueOf(vals[0][1]);
			if (vals[1].length == 2) {
				waresPerSecondCities.add(wps);
			} else {
				waresPerSecondFactories.add(wps);
				if (!validateSubTree(child, wps)) {
					return false;
				}
			}
		}
		float aggregatedCityWPS = 0.0f, aggregatedFactoryWPS = 0.0f;
		for (Float f : waresPerSecondFactories) {
			aggregatedFactoryWPS += f;
		}
		for (Float f : waresPerSecondCities) {
			aggregatedCityWPS += f;
		}

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

	public void setListItemControlsEnabled(boolean flag) {
		moveItemU.setEnabled(flag);
		moveItemD.setEnabled(flag);
		deleteItem.setEnabled(flag);
		addItemLabel.setEnabled(flag);
	}

	public void setVisible(boolean flag) {
		this.frame.setVisible(flag);
	}

	public boolean isShowing() {
		return this.frame.isShowing();
	}

	public void setRecicpeGraph(RecipeGraph graph) {
		this.recipeGraph = graph;
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
		contentPane.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 2, new Insets(16, 16, 16, 16), -1, -1));
		contentPane.putClientProperty("html.disable", Boolean.TRUE);
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 4, new Insets(0, 0, 0, 0), -1, -1));
		contentPane.add(panel1, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, true));
		capacity = new JTextField();
		capacity.setText("10");
		capacity.setToolTipText("Combined capacity of all vehicles on the route");
		capacity.putClientProperty("html.disable", Boolean.TRUE);
		panel1.add(capacity, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		final JLabel label1 = new JLabel();
		label1.setText("Capacity");
		label1.putClientProperty("html.disable", Boolean.TRUE);
		panel1.add(label1, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label2 = new JLabel();
		label2.setText("Frequency (mins:secs)");
		panel1.add(label2, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		frequency = new JTextField();
		frequency.setText("0:1");
		frequency.putClientProperty("html.disable", Boolean.TRUE);
		panel1.add(frequency, new com.intellij.uiDesigner.core.GridConstraints(1, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		nVehiclesLabel = new JLabel();
		nVehiclesLabel.setText("Vehicle(s): ");
		panel1.add(nVehiclesLabel, new com.intellij.uiDesigner.core.GridConstraints(0, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
		panel1.add(nVehicles, new com.intellij.uiDesigner.core.GridConstraints(1, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		panel1.add(cargoChooser, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label3 = new JLabel();
		label3.setText("Choose Cargo");
		panel1.add(label3, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JPanel panel2 = new JPanel();
		panel2.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(13, 1, new Insets(0, 0, 0, 0), -1, -1));
		contentPane.add(panel2, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		moveItemU = new JButton();
		moveItemU.setEnabled(false);
		moveItemU.setText("Move up");
		panel2.add(moveItemU, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(120, -1), null, 0, false));
		final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
		panel2.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(12, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		moveItemD = new JButton();
		moveItemD.setEnabled(false);
		moveItemD.setText("Move down");
		panel2.add(moveItemD, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(120, -1), null, 0, false));
		final com.intellij.uiDesigner.core.Spacer spacer2 = new com.intellij.uiDesigner.core.Spacer();
		panel2.add(spacer2, new com.intellij.uiDesigner.core.GridConstraints(5, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		deleteItem = new JButton();
		deleteItem.setEnabled(false);
		deleteItem.setText("X");
		deleteItem.setToolTipText("Delete selection");
		panel2.add(deleteItem, new com.intellij.uiDesigner.core.GridConstraints(10, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(120, -1), null, 0, false));
		addCity = new JButton();
		addCity.setText("+ City");
		addCity.setToolTipText("Add City");
		panel2.add(addCity, new com.intellij.uiDesigner.core.GridConstraints(8, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(120, -1), null, 0, false));
		addItemLabel = new JButton();
		addItemLabel.setEnabled(false);
		addItemLabel.setText("Label As");
		addItemLabel.setToolTipText("Label Selection");
		panel2.add(addItemLabel, new com.intellij.uiDesigner.core.GridConstraints(11, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(120, -1), null, 0, false));
		addTier = new JButton();
		addTier.setText("+ Tier");
		addTier.setToolTipText("add an intermediate tier");
		panel2.add(addTier, new com.intellij.uiDesigner.core.GridConstraints(6, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(120, -1), null, 0, false));
		addChain = new JButton();
		addChain.setText("+ Chain");
		addChain.setToolTipText("Add new chain");
		panel2.add(addChain, new com.intellij.uiDesigner.core.GridConstraints(7, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(120, -1), null, 0, false));
		addLeg = new JButton();
		addLeg.setText("+ Leg");
		addLeg.setToolTipText("Add leg");
		panel2.add(addLeg, new com.intellij.uiDesigner.core.GridConstraints(9, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(120, -1), null, 0, false));
		editSettings = new JButton();
		editSettings.setEnabled(false);
		editSettings.setText("Settings");
		panel2.add(editSettings, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(120, -1), null, 0, false));
		validate = new JButton();
		validate.setText("Validate");
		panel2.add(validate, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(120, -1), null, 0, false));
		final com.intellij.uiDesigner.core.Spacer spacer3 = new com.intellij.uiDesigner.core.Spacer();
		panel2.add(spacer3, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		contentPane.add(legPane, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(240, 480), null, 0, false));
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

	public class DialogResultsNotReadyException extends RuntimeException {
		private JDialog dialog = null;

		public DialogResultsNotReadyException() {
			this("Dialog results are not ready to be used or are invalid!", null);
		}

		public DialogResultsNotReadyException(JDialog dialog) {
			this();
			this.dialog = dialog;
		}

		public DialogResultsNotReadyException(String message, JDialog dialog) {
			super(message);
			this.dialog = dialog;
		}
	}
}
