/*******************************************************************************
 * Copyright (c) 2005-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *  $Id: SettingsPreferenceStore.java 5320 2009-05-27 16:51:14Z rgw_ch $
 *******************************************************************************/
package ch.elexis.preferences;

import java.util.LinkedList;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import ch.rgw.io.Settings;

/**
 * Dies ist eine Adapterklasse, die ch.rgw.tools.IO.Settings auf
 * Eclipse-Preferences abbildet. Es sollte keine direkte Verwendung dieser
 * Klasse notwendig sein. Intern wird das localCfg-Feld auf SysSettings und das
 * globalCfg-Feld auf SqlSettings abgebildet
 * 
 * @author Gerry
 */
public class SettingsPreferenceStore implements IPreferenceStore {

	Settings base;
	private LinkedList<IPropertyChangeListener> listeners = new LinkedList<IPropertyChangeListener>();

	public SettingsPreferenceStore(Settings base) {
		this.base = base;
	}

	public Settings getBase() {
		return base;
	}

	public void flush() {
		base.flush();
	}

	public void undo() {
		base.undo();
	}

	private void set(String field, String value) {
		base.set(field, value);
	}

	private String get(String field) {
		String z = base.get(field, null);
		if (z == null) {
			z = base.get(field + "_default", null);
			if (z == null) {
				z = "";
			}
		}
		return z;
	}

	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		listeners.add(listener);
	}

	public boolean contains(String name) {
		if (base.get(name, null) == null) {
			return false;
		}
		return true;
	}

	public void firePropertyChangeEvent(String name, Object oldValue,
			Object newValue) {
		for (IPropertyChangeListener l : listeners) {
			l.propertyChange(new PropertyChangeEvent(this, name, oldValue,
					newValue));
		}
	}

	public boolean getBoolean(String name) {
		String z = get(name);
		if (z.equals("0")) {
			return false;
		}
		if (z.equalsIgnoreCase("FALSE")) {
			return false;
		}
		return true;
	}

	public boolean getDefaultBoolean(String name) {
		return getBoolean(name + "_default");
	}

	public double getDefaultDouble(String name) {
		return getDouble(name + "_default");
	}

	public float getDefaultFloat(String name) {
		return getFloat(name + "_default");
	}

	public int getDefaultInt(String name) {
		return getInt(name + "_default");
	}

	public long getDefaultLong(String name) {
		return getLong(name + "_default");
	}

	public String getDefaultString(String name) {
		return getString(name + "_default");
	}

	public double getDouble(String name) {
		return Double.parseDouble(get(name));
	}

	public float getFloat(String name) {
		return Float.parseFloat(get(name));
	}

	/**
	 * return an Integer. If the Value is not an Integer ot nonexistent, we return 0
	 * (@see IPreferenceStore)
	 */
	public int getInt(String name) {
		try{
			return Integer.parseInt(get(name));
		}catch(NumberFormatException ne){
			return 0;
		}
	}

	public long getLong(String name) {
		return Long.parseLong(get(name));
	}

	public String getString(String name) {
		return get(name);
	}

	public boolean isDefault(String name) {
		String def = get(name + "_default");
		String act = get(name);
		return def.equals(act);
	}

	public boolean needsSaving() {
		return base.isDirty();
	}

	public void putValue(String name, String value) {
		set(name, value);
	}

	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		listeners.remove(listener);
	}

	public void setDefault(String name, double value) {
		set(name + "_default", Double.toString(value));

	}

	public void setDefault(String name, float value) {
		set(name + "_default", Float.toString(value));

	}

	public void setDefault(String name, int value) {
		set(name + "_default", Integer.toString(value));
	}

	public void setDefault(String name, long value) {
		set(name + "_default", Long.toString(value));

	}

	public void setDefault(String name, String defaultObject) {
		set(name + "_default", defaultObject);

	}

	public void setDefault(String name, boolean value) {
		set(name + "_default", Boolean.toString(value));

	}

	public void setToDefault(String name) {
		set(name, get(name + "_default"));

	}

	public void setValue(String name, double value) {
		firePropertyChangeEvent(name, getDouble(name), value);
		set(name, Double.toString(value));
	}

	public void setValue(String name, float value) {
		firePropertyChangeEvent(name, getFloat(name), value);
		set(name, Float.toString(value));

	}

	public void setValue(String name, int value) {
		firePropertyChangeEvent(name, getInt(name), value);
		set(name, Integer.toString(value));
	}

	public void setValue(String name, long value) {
		firePropertyChangeEvent(name, getLong(name), value);
		set(name, Long.toString(value));

	}

	public void setValue(String name, String value) {
		firePropertyChangeEvent(name, getString(name), value);
		set(name, value);
	}

	public void setValue(String name, boolean value) {
		firePropertyChangeEvent(name, getBoolean(name), value);
		set(name, Boolean.toString(value));
	}

}
