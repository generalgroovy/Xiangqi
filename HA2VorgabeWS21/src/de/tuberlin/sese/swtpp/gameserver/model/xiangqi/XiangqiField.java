package de.tuberlin.sese.swtpp.gameserver.model.xiangqi;

import java.util.ArrayList;

public class XiangqiField {
	
	private int row;
	private int column;
	private String pieceOnMe = "";
	private ArrayList<XiangqiField> neighborsll = new ArrayList<XiangqiField>();
	private ArrayList<XiangqiField> neighborsMove = new ArrayList<XiangqiField>();
	private ArrayList<XiangqiField> neighborsAttack = new ArrayList<XiangqiField>();
	
}