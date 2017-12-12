package com.tigerbird1.TpFCargoCalc;

import com.tigerbird1.TpFCargoCalc.config.Configuration;

import javax.swing.*;
import java.awt.*;

public class Utils {
	private static Configuration config = Configuration.getInstance();


	public static boolean epsilonEquals(float f1, float f2) {
		try {
			return Math.abs(f1 - f2) <= config.getFloat("wps_tolerance");
		} catch (NoSuchFieldException | IllegalAccessException ignored) {
			return false;
		}
	}

	public static void showNoCargoSelectedError(Component parent) {
		JOptionPane.showMessageDialog(parent, "Please choose a Cargo!", "No Cargo Selected!", JOptionPane.ERROR_MESSAGE);
	}
	public static void showNoChainSelectedError(Component parent) {
		JOptionPane.showMessageDialog(parent, "Please select a Chain!", "No Chain Selected!", JOptionPane.ERROR_MESSAGE);
	}
	public static void showInvalidCapacityError(Component parent) {
		JOptionPane.showMessageDialog(parent, "Please enter a valid capacity!", "Invalid Capacity!", JOptionPane.ERROR_MESSAGE);
	}

	public static void showChainsUnoptimzedWarning(Component parent) {
		JOptionPane.showMessageDialog(parent, "Chains are invalid and/or unoptimized!", "Chains Invalid and/or Unoptimized".toLowerCase(), JOptionPane.WARNING_MESSAGE);
	}

	public static void showChainsValidInfo(Component parent) {
		JOptionPane.showMessageDialog(parent, "Chains are valid and optimized!", "Chains Valid and Optimized".toLowerCase(), JOptionPane.INFORMATION_MESSAGE);
	}
}
