package com.tigerbird1.TpFCargoCalc.config;

public class Configuration {
	private static Configuration instance = new Configuration();

	float wps_tolerance = 0.125f;

	String gs_modifier = "1.0x";


	private Configuration() {
	}

	public static Configuration getInstance() {
		return instance;
	}

	public float getFloat(String optName) throws IllegalAccessException, NoSuchFieldException {
		return getClass().getDeclaredField(optName).getFloat(this);
	}

	public int getInt(String optName) throws IllegalAccessException, NoSuchFieldException {
		return getClass().getDeclaredField(optName).getInt(this);
	}

	public boolean getBoolean(String optName) throws IllegalAccessException, NoSuchFieldException {
		return getClass().getDeclaredField(optName).getBoolean(this);
	}

	public String getString(String optName) throws IllegalAccessException, NoSuchFieldException {
		return (String) getClass().getDeclaredField(optName).get(this);
	}

	public Object getObject(String optName) throws IllegalAccessException, NoSuchFieldException {
		return getClass().getDeclaredField(optName).get(this);
	}

	public void setFloat(String optName, float value) throws IllegalAccessException, NoSuchFieldException {
		getClass().getDeclaredField(optName).setFloat(this, value);
	}

	public void setInt(String optName, int value) throws IllegalAccessException, NoSuchFieldException {
		getClass().getDeclaredField(optName).setInt(this, value);
	}

	public void setBoolean(String optName, boolean value) throws IllegalAccessException, NoSuchFieldException {
		getClass().getDeclaredField(optName).setBoolean(this, value);
	}

	public void setString(String optName, String value) throws IllegalAccessException, NoSuchFieldException {
		setObject(optName, value);
	}

	public void setObject(String optName, Object value) throws IllegalAccessException, NoSuchFieldException {
		getClass().getDeclaredField(optName).set(this, value);
	}
}
