package com.covidien.util;

import java.util.Arrays;

public class DomXML {
	private String[] serverName, serverValue;
	private boolean enableEdit = false;
	public static final DomXML INSTANCE = new DomXML();

	private DomXML() {

	}

	public void set(String[] serverName, String[] serverValue) {
		this.serverName = Arrays.copyOf(serverName, serverName.length);
		this.serverValue = Arrays.copyOf(serverValue, serverValue.length);
	}

	public String[] getServerValue() {
		return this.serverValue;
	}

	public String[] getServerName() {
		return this.serverName;
	}

	public String getServerUrl(int i) {
		return serverValue[i];
	}

	public void setServerName(String[] serverName) {
		this.serverName = serverName;
	}

	public void setServerValue(String[] serverValue) {
		this.serverValue = serverValue;
	}

	public boolean getEnableEdit() {
		return this.enableEdit;
	}
}
