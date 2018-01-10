package com.tigerbird1.TpFCargoCalc.ui;

import com.tigerbird1.TpFCargoCalc.cargo.Cargo;
import com.tigerbird1.TpFCargoCalc.cargo.Route;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.awt.*;

public class TierPane extends JPanel {

	private static int tierCount = 1;

	private JTabbedPane parentPane;
	private JList<JListItem> legList;

	private DefaultListModel<JListItem> legListModel;

	private int tierN;
	private int chainCnt = 0;
	private int cityCnt = 0;

	public TierPane(LayoutManager layout) {
		this(layout, true);
	}

	public TierPane(boolean isDoubleBuffered) {
		this(new FlowLayout(), isDoubleBuffered);
	}

	public TierPane() {
		this(true);
	}

	public TierPane(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);

		this.tierN = tierCount++;

		legListModel = new DefaultListModel<>();
		addChain();

		legList = new JList<>(legListModel);
		legList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		legList.setLayoutOrientation(JList.VERTICAL);
		Dimension prefSize = new Dimension(400, 480);
		this.setPreferredSize(prefSize);
		legList.setPreferredSize(prefSize);
		this.add(legList);

	}

	private void updateTierNum(int newNum) {
		int oldNum = this.tierN;
		this.tierN = newNum;
		this.parentPane.setTitleAt(oldNum, getTitle());
	}

	public void moveTo(int newIdx) {
		JTabbedPane parentPane = this.getParentPane();
		this.removeFromParentPane();
		this.setParentPane(parentPane, newIdx);
	}

	public void moveTo(JTabbedPane newParentPane, int newIdx) {
		this.removeFromParentPane();
		this.setParentPane(newParentPane, newIdx);
	}

	public void swapWith(TierPane other) {
		JTabbedPane thisParPane = this.getParentPane();
		JTabbedPane otherParPane = other.getParentPane();
		int thisIdx = thisParPane.indexOfComponent(this);
		int otherIdx = otherParPane.indexOfComponent(other);

		this.moveTo(otherParPane, otherIdx);
		other.moveTo(thisParPane, thisIdx);
	}

	public void addChain() {
		addChain("Chain " + ( chainCnt + 1 ));
	}

	public void addChain(String name) {
		addChain(name, "");
	}

	public void addChain(String name, String label) {
		legListModel.addElement(new Chain(name, label));
		chainCnt++;
		//if (legList != null) { legList.setModel(legListModel); }
	}

	public void addCityRoute(Cargo cargo, int[] stats) {
		addCityRoute("City " + ( cityCnt + 1 ), cargo, stats);
	}

	public void addCityRoute(String name, Cargo cargo, int[] stats) {
		//DefaultMutableTreeNode selected = this.legList.getSelectedValue();
		legListModel.add(legListModel.getSize(), new Route(name, null, true, cargo, stats[0], stats[1], 0));
		cityCnt++;
	}

	public void addLegRoute(String name, Cargo cargo, int[] stats) {
		//DefaultMutableTreeNode selected = this.legList.getSelectedValue();
		legListModel.add(legListModel.getSize(), new Route(name, null, false, cargo, stats[0], stats[1], stats[2]));
	}

	public void addListSelectionListener(ListSelectionListener l) {
		this.legList.addListSelectionListener(l);
	}

	public JTabbedPane getParentPane() {
		return parentPane;
	}

	public void setParentPane(JTabbedPane parentPane) {
		this.parentPane = parentPane;
		this.parentPane.addTab(getTitle(), null, this, null);
	}

	public void setParentPane(JTabbedPane parentPane, int index) {
		this.parentPane = parentPane;
		this.parentPane.insertTab(getTitle(), null, this, null, index);
	}

	public void removeFromParentPane() {
		this.parentPane.remove(this);
		this.parentPane = null;
	}

	public String getTitle() {
		return "Tier " + tierN;
	}

	public class Chain extends TierPaneItem {
		public Chain(String title) {
			this(title, null);
		}

		public Chain(String title, String label) {
			super(title, label);
		}

	}
}
