package com.tigerbird1.TpFCargoCalc.ui;

public class TierPaneItem implements JListItem {
	protected String title;
	protected String label;

	public TierPaneItem(String title, String label) {
		this.title = title;
		this.label = label;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String newTitle) {
		this.title = newTitle;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public void setLabel(String newLabel) {
		this.label = newLabel;
	}

	@Override
	public String toString() {
		return title;
	}
}
