package com.tigerbird1.TpFCargoCalc;

import com.tigerbird1.TpFCargoCalc.io.AppIO;
import com.tigerbird1.TpFCargoCalc.io.Cargoes;
import com.tigerbird1.TpFCargoCalc.io.RecipeGraph;
import com.tigerbird1.TpFCargoCalc.ui.CargoCalcUI;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CargoCalc {

	private AppIO appIO = new AppIO();
	private CargoCalcUI ui;

	private Cargoes cargoes;
	private RecipeGraph recipes;


	public static void main(String[] args) {
		InputStream config_file = null;
		InputStream data_file = null;

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

			Class loader = System.class;
			config_file = loader.getResource("/config.xml").openStream();
			data_file = loader.getResource("/cargo_data.xml").openStream();
		} catch (ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException | NullPointerException | IOException e) {
			e.printStackTrace();
		}

		CargoCalc cc = new CargoCalc();
		Configuration config = cc.appIO.readConfig(config_file);
		cc.appIO.readCargoData(data_file);

		cc.cargoes = cc.appIO.getCargoes();
		cc.recipes = cc.appIO.getRecipes();

		cc.ui = new CargoCalcUI();
		cc.ui.setRecicpeGraph(cc.recipes);
		cc.ui.setCargoes(cc.cargoes);

		cc.ui.setVisible(true);

		while (cc.ui.isShowing()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		cc.appIO.saveConfig(config);

		return;
	}
}
