package com.tigerbird1.TpFCargoCalc.io;

import com.tigerbird1.TpFCargoCalc.Configuration;
import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;


public class AppIO {
	private XMLReader configReader;
	private XMLReader dataReader;

	private ConfigHandler config;

	private DataHandler cargoData;
	private CargoTypes cargoes;
	private RecipeGraph recipes;

	public AppIO() {
		try {
			configReader = XMLReaderFactory.createXMLReader();
			dataReader = XMLReaderFactory.createXMLReader();
		} catch (SAXException e) {
			e.printStackTrace();
		}

		config = new ConfigHandler();

		cargoData = new DataHandler();
		cargoes = new CargoTypes();
		recipes = new RecipeGraph();

		recipes.setCargoTypes(cargoes);
		cargoData.addDelegate(cargoes);
		cargoData.addDelegate(recipes);

		configReader.setContentHandler(config);
		dataReader.setContentHandler(cargoData);
	}

	public Configuration readConfig() {
		try {
			configReader.parse(new InputSource(new FileReader(this.getClass().getClassLoader().getResource("config.xml").getFile())));
		} catch (IOException | SAXException | NullPointerException e) {
			e.printStackTrace();
		}
		return config.getConfiguration();
	}

	public void saveConfig(Configuration configuration) {
		//TODO save configuration
	}

	public void readCargoData() {
		try {
			dataReader.parse(new InputSource(new FileReader(this.getClass().getClassLoader().getResource("cargo_data.xml").getFile())));
		} catch (IOException | SAXException | NullPointerException e) {
			e.printStackTrace();
		}
	}

	public CargoTypes getCargoes() {
		return cargoes;
	}

	public RecipeGraph getRecipes() {
		return recipes;
	}
}
