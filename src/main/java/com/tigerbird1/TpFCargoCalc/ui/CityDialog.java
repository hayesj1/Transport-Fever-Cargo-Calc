package com.tigerbird1.TpFCargoCalc.ui;

import com.tigerbird1.TpFCargoCalc.CargoCalc;
import com.tigerbird1.TpFCargoCalc.Utils;
import com.tigerbird1.TpFCargoCalc.cargo.Cargo;
import com.tigerbird1.TpFCargoCalc.cargo.Cargoes;
import com.tigerbird1.TpFCargoCalc.config.Configuration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class CityDialog extends JDialog {
	private JPanel contentPane;
	private JButton buttonOK;
	private JButton buttonCancel;
	private JRadioButton gs2xFaster;
	private JRadioButton gsNormal;
	private JRadioButton gs2xSlower;
	private JRadioButton gs4xSlower;
	private JComboBox<Cargo> cargoChooser;
	private JTextField cityCapacity;
	private JTextField cityName;

	private ButtonGroup gsGrp = new ButtonGroup();
	private JRadioButton[] gsButtons;
	private float gsModifier;

	private DefaultComboBoxModel<Cargo> cargoChooserModel;
	private Cargoes cargoes;

	private final String[] gsActionCommands = new String[] { "2.0x", "1.0x", "0.5x", "0.25x" };
	private final float[] gsSpeeds = new float[] { 0.5f, 1.0f, 2.0f, 4.0f };

	private boolean valuesReady = false;
	private Cargo cargo;
	private int capacity = -1;
	private float waresPerSecond = -1.0f;

	public CityDialog() {
		this(null);
	}

	public CityDialog(Frame parent) {
		super(parent);

		$$$setupUI$$$();
		Dimension size = new Dimension(700, 150);
		contentPane.setMinimumSize(size);
		contentPane.setPreferredSize(size);
		contentPane.setMaximumSize(size);
		setContentPane(contentPane);
		setModal(true);

		getRootPane().setDefaultButton(buttonOK);
		setTitle("Add City Route");

		gs2xFaster.setMnemonic(0);
		gsNormal.setMnemonic(1);
		gs2xSlower.setMnemonic(2);
		gs4xSlower.setMnemonic(3);

		// allow for adjustment of calculations if user has game speed mods
		gsGrp.add(gs2xFaster);
		gsGrp.add(gsNormal);
		gsGrp.add(gs2xSlower);
		gsGrp.add(gs4xSlower);

		gsButtons = new JRadioButton[] { gs2xFaster, gsNormal, gs2xSlower, gs4xSlower };

		try {
			String savedGS = Configuration.getInstance().getString("gs_modifier").toLowerCase();
			int idx = getGSIdxForActionCommand(savedGS);
			gsGrp.setSelected(gsButtons[idx].getModel(), true);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			gsGrp.setSelected(gsNormal.getModel(), true);
		}

		gsModifier = gsSpeeds[gsGrp.getSelection().getMnemonic()]; // Normal speed
		ActionListener gsListener = e -> {
			float speed = getGSForActionCommand(e.getActionCommand());
			gsModifier = speed;
		};
		gs2xFaster.addActionListener(gsListener);
		gsNormal.addActionListener(gsListener);
		gs2xSlower.addActionListener(gsListener);
		gs4xSlower.addActionListener(gsListener);

		if (cargoChooser.getSelectedIndex() == -1) {
			cargoChooser.setSelectedItem(cargoChooserModel.getElementAt(0));
			cargo = (Cargo) cargoChooserModel.getSelectedItem();
		}

		buttonOK.addActionListener(e -> onOK());
		buttonCancel.addActionListener(e -> onCancel());
/*
		cityCapacity.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				computeWaresPerSecond();
			}
		});
*/
		//cargoChooser.addItemListener(e -> cargo = (Cargo) e.getItem());

		// call onCancel() when cross is clicked
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				onCancel();
			}
		});

		// call onCancel() on ESCAPE
		contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

		this.pack();
	}

	private void computeWaresPerSecond() {
		if (cargoChooser.getSelectedIndex() == -1 || !cityCapacity.getText().matches("[0-9]*.?[0-9]*")) {
			valuesReady = false;
			return;
		} else {
			//   city capacity       wares/year
			// ------------------ = -------------- = city capacity = wares/second
			//  seconds per year     seconds/year
			capacity = Integer.valueOf(cityCapacity.getText());
			waresPerSecond = capacity / ( ( 12 * gsModifier ) * 60 );
			cargo = (Cargo) cargoChooser.getSelectedItem();
			valuesReady = true;
		}
	}

	private void onClose() {
		String selectedSpeed = gsGrp.getSelection().getActionCommand();

		try {
			Configuration.getInstance().setString("gs_modifier", selectedSpeed);
		} catch (IllegalAccessException | NoSuchFieldException ignored) {
		}
	}

	private void onOK() {
		computeWaresPerSecond();
		if (!valuesReady) {
			if (capacity <= 0.0f) {
				Utils.showInvalidCapacityError(this);
			}
			if (cargo == null || cargo.equals(Cargo.NIL_CARGO)) {
				Utils.showNoCargoSelectedError(this);
			}
		} else {
			this.onClose();
			dispose();
		}
	}

	private void onCancel() {
		valuesReady = false;

		this.onClose();
		dispose();
	}

	private void createUIComponents() {
		cargoChooserModel = CargoCalc.getUtils().getCargoChooserModel();
		cargoChooser = new JComboBox<>(cargoChooserModel);

	}

	public void setCargoChooserModel(DefaultComboBoxModel<Cargo> model) {
		this.cargoChooserModel = model;
		this.cargoChooser.setModel(this.cargoChooserModel);
	}

	public boolean areValuesReady() {
		return valuesReady;
	}

	public int getCapacity() {
		return this.capacity;
	}

	public int getFrequency() {
		return (int) ( ( 12 * gsModifier ) * 60 );
	}

	public float getWaresPerSecond() {
		return this.waresPerSecond;
	}

	public Cargo getCargo() {
		return this.cargo;
	}

	public String getCityName() {
		return this.cityName.getText();
	}

	private float getGSForActionCommand(String cmd) {
		if (gsActionCommands[0].equals(cmd)) {
			return gsSpeeds[0];
		} else if (gsActionCommands[1].equals(cmd)) {
			return gsSpeeds[1];
		} else if (gsActionCommands[2].equals(cmd)) {
			return gsSpeeds[2];
		} else if (gsActionCommands[3].equals(cmd)) {
			return gsSpeeds[3];
		} else {
			return gsSpeeds[1];
		}
	}

	private int getGSIdxForActionCommand(String cmd) {
		if (gsActionCommands[0].equals(cmd)) {
			return 0;
		} else if (gsActionCommands[1].equals(cmd)) {
			return 1;
		} else if (gsActionCommands[2].equals(cmd)) {
			return 2;
		} else if (gsActionCommands[3].equals(cmd)) {
			return 3;
		} else {
			return 1;
		}
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
		contentPane.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
		final JPanel panel1 = new JPanel();
		panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
		contentPane.add(panel1, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
		final JPanel panel2 = new JPanel();
		panel2.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
		panel1.add(panel2, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		buttonOK = new JButton();
		buttonOK.setText("OK");
		panel2.add(buttonOK, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		buttonCancel = new JButton();
		buttonCancel.setText("Cancel");
		panel2.add(buttonCancel, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JPanel panel3 = new JPanel();
		panel3.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
		panel3.setToolTipText("Game Speed Modifier");
		panel1.add(panel3, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		gs2xFaster = new JRadioButton();
		gs2xFaster.setActionCommand("2.0x");
		gs2xFaster.setHideActionText(false);
		gs2xFaster.setText("Double Speed");
		gs2xFaster.putClientProperty("html.disable", Boolean.FALSE);
		panel3.add(gs2xFaster, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		gsNormal = new JRadioButton();
		gsNormal.setActionCommand("1.0x");
		gsNormal.setSelected(false);
		gsNormal.setText("Normal speed");
		panel3.add(gsNormal, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		gs2xSlower = new JRadioButton();
		gs2xSlower.setActionCommand("0.5x");
		gs2xSlower.setText("Half Speed");
		panel3.add(gs2xSlower, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		gs4xSlower = new JRadioButton();
		gs4xSlower.setActionCommand("0.25x");
		gs4xSlower.setText("Quarter Speed");
		panel3.add(gs4xSlower, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JPanel panel4 = new JPanel();
		panel4.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
		contentPane.add(panel4, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
		cityCapacity = new JTextField();
		cityCapacity.setText("100");
		cityCapacity.setToolTipText("Quantity of cargo this city accepts in one year");
		panel4.add(cityCapacity, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(60, -1), new Dimension(80, 26), new Dimension(120, -1), 0, false));
		final JLabel label1 = new JLabel();
		label1.setHorizontalAlignment(0);
		label1.setText("City Capacity per Year");
		panel4.add(label1, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(80, 26), null, 0, false));
		final JLabel label2 = new JLabel();
		label2.setHorizontalAlignment(0);
		label2.setHorizontalTextPosition(11);
		label2.setText("Cargo");
		panel4.add(label2, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		panel4.add(cargoChooser, new com.intellij.uiDesigner.core.GridConstraints(1, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		cityName = new JTextField();
		cityName.setText("Springfield");
		panel4.add(cityName, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, new Dimension(120, -1), new Dimension(240, -1), new Dimension(300, -1), 0, false));
		final JLabel label3 = new JLabel();
		label3.setHorizontalAlignment(0);
		label3.setText("City Name");
		panel4.add(label3, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(80, 26), null, 0, false));
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return contentPane;
	}
}
