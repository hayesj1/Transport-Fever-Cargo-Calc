package com.tigerbird1.TpFCargoCalc;

import com.tigerbird1.TpFCargoCalc.io.AppIO;
import com.tigerbird1.TpFCargoCalc.io.CargoTypes;
import com.tigerbird1.TpFCargoCalc.io.RecipeGraph;
import com.tigerbird1.TpFCargoCalc.ui.CargoCalcUI;

import javax.swing.*;

public class CargoCalc {

	private AppIO appIO = new AppIO();
	private CargoCalcUI ui;

	private CargoTypes cargoes;
	private RecipeGraph recipes;


	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException e) {
			e.printStackTrace();
		}

		CargoCalc cc = new CargoCalc();
		Configuration config = cc.appIO.readConfig();
		cc.appIO.readCargoData();

		cc.cargoes = cc.appIO.getCargoes();
		cc.recipes = cc.appIO.getRecipes();

		cc.ui = new CargoCalcUI();
		cc.ui.setCargoTypes(cc.cargoes);

		cc.ui.setVisible(true);
		cc.appIO.saveConfig(config);
	}
}
