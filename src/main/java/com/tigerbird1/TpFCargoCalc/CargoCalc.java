package com.tigerbird1.TpFCargoCalc;

import com.tigerbird1.TpFCargoCalc.cargo.Cargoes;
import com.tigerbird1.TpFCargoCalc.cargo.RecipeGraph;
import com.tigerbird1.TpFCargoCalc.config.Configuration;
import com.tigerbird1.TpFCargoCalc.io.AppIO;
import com.tigerbird1.TpFCargoCalc.ui.CargoCalcUI;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;

public class CargoCalc {

	private AppIO appIO = new AppIO();
	private CargoCalcUI ui;

	private Cargoes cargoes;
	private RecipeGraph recipes;
	private Configuration config;


	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException | ClassNotFoundException ignored) {
		}

		Class loader = System.class;
		CargoCalc cc = new CargoCalc();

		try (InputStream config_file = loader.getResource("/config.xml").openStream()) {
			cc.config = cc.appIO.readConfig(config_file);
		} catch (NullPointerException | IOException e) {
			e.printStackTrace();
		}
		try (InputStream data_file = loader.getResource("/cargo_data.xml").openStream()) {
			cc.appIO.readCargoData(data_file);
		} catch (NullPointerException | IOException e) {
			e.printStackTrace();
		}

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
		cc.appIO.saveConfig();

		return;
	}
}
