package com.tigerbird1.TpFCargoCalc.io;

import com.tigerbird1.TpFCargoCalc.Configuration;
import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.*;


public class AppIO {
	private XMLReader configReader;
	private XMLReader dataReader;

	private ConfigHandler config;

	private DataHandler cargoData;
	private Cargoes cargoes;
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
		cargoes = new Cargoes();
		recipes = new RecipeGraph();

		recipes.setCargoes(cargoes);
		cargoData.addDelegate(cargoes);
		cargoData.addDelegate(recipes);

		configReader.setContentHandler(config);
		dataReader.setContentHandler(cargoData);
	}

	public Configuration readConfig(InputStream config_file) {
		try {
			configReader.parse(new InputSource(config_file));
		} catch (IOException | SAXException | NullPointerException e) {
			e.printStackTrace();
		}
		return config.getConfiguration();
	}

	public void saveConfig(Configuration configuration) {
		//TODO save configuration
		return;
	}

	public void readCargoData(InputStream data_file) {
		try {
			dataReader.parse(new InputSource(data_file));
		} catch (IOException | SAXException | NullPointerException e) {
			e.printStackTrace();
		}
	}

	public Cargoes getCargoes() {
		return cargoes;
	}

	public RecipeGraph getRecipes() {
		return recipes;
	}
}
