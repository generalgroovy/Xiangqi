package de.tuberlin.sese.swtpp.gameserver.model.xiangqi;

import java.util.ArrayList;

public class XiangqiField {
	
	private int row;
	private int column;
	private String pieceOnMe = "";
	private ArrayList<XiangqiField> neighborsll = new ArrayList<XiangqiField>();
	private ArrayList<XiangqiField> neighborsMove = new ArrayList<XiangqiField>();
	private ArrayList<XiangqiField> neighborsAttack = new ArrayList<XiangqiField>();
	
	public XiangqiField (int row, int column, String pieceOnMe) {
		this.row = row;
		this.column = column;
		this.pieceOnMe = pieceOnMe;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getColumn() {
		return column;
	}

	public void setColumn(int column) {
		this.column = column;
	}

	public String getPieceOnMe() {
		return pieceOnMe;
	}

	public void setPieceOnMe(String pieceOnMe) {
		this.pieceOnMe = pieceOnMe;
	}

	public ArrayList<XiangqiField> getNeighborsll() {
		return neighborsll;
	}

	public ArrayList<XiangqiField> getNeighborsMove() {
		return neighborsMove;
	}

	public ArrayList<XiangqiField> getNeighborsAttack() {
		return neighborsAttack;
	}

	
	
}