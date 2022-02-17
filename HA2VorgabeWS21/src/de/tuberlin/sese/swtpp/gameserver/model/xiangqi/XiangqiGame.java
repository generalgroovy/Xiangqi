package de.tuberlin.sese.swtpp.gameserver.model.xiangqi;

import de.tuberlin.sese.swtpp.gameserver.model.*;
//TODO: more imports from JVM allowed here


import java.io.Serializable;

public class XiangqiGame extends Game implements Serializable, XiangqiConstants{

	/**
	 *
	 */
	private static final long serialVersionUID = 5424778147226994452L;

	/************************
	 * member
	 ***********************/

	// just for better comprehensibility of the code: assign red and black player
	private Player blackPlayer;
	private Player redPlayer;
	
	// internal representation of the game state
	// TODO: insert additional game data here
	/************************
	 * constructors
	 ***********************/

	public XiangqiGame() {
		super();

		// TODO: initialization of game state can go here
	}

	public String getType() {
		return "xiangqi";
	}

	/*******************************************
	 * Game class functions already implemented
	 ******************************************/

	@Override
	public boolean addPlayer(Player player) {
		if (!started) {
			players.add(player);

			// game starts with two players
			if (players.size() == 2) {
				started = true;
				this.redPlayer = players.get(0);
				this.blackPlayer= players.get(1);
				nextPlayer = redPlayer;
			}
			return true;
		}

		return false;
	}

	@Override
	public String getStatus() {
		if (error)
			return "Error";
		if (!started)
			return "Wait";
		if (!finished)
			return "Started";
		if (surrendered)
			return "Surrendered";
		if (draw)
			return "Draw";

		return "Finished";
	}

	@Override
	public String gameInfo() {
		String gameInfo = "";

		if (started) {
			if (blackGaveUp())
				gameInfo = "black gave up";
			else if (redGaveUp())
				gameInfo = "red gave up";
			else if (didRedDraw() && !didBlackDraw())
				gameInfo = "red called draw";
			else if (!didRedDraw() && didBlackDraw())
				gameInfo = "black called draw";
			else if (draw)
				gameInfo = "draw game";
			else if (finished)
				gameInfo = blackPlayer.isWinner() ? "black won" : "red won";
		}

		return gameInfo;
	}

	@Override
	public String nextPlayerString() {
		return isRedNext() ? "r" : "b";
	}

	@Override
	public int getMinPlayers() {
		return 2;
	}

	@Override
	public int getMaxPlayers() {
		return 2;
	}

	@Override
	public boolean callDraw(Player player) {

		// save to status: player wants to call draw
		if (this.started && !this.finished) {
			player.requestDraw();
		} else {
			return false;
		}

		// if both agreed on draw:
		// game is over
		if (players.stream().allMatch(Player::requestedDraw)) {
			this.draw = true;
			finish();
		}
		return true;
	}

	@Override
	public boolean giveUp(Player player) {
		if (started && !finished) {
			if (this.redPlayer == player) {
				redPlayer.surrender();
				blackPlayer.setWinner();
			}
			if (this.blackPlayer == player) {
				blackPlayer.surrender();
				redPlayer.setWinner();
			}
			surrendered = true;
			finish();

			return true;
		}

		return false;
	}

	/* ******************************************
	 * Helpful stuff
	 ***************************************** */

	/**
	 *
	 * @return True if it's red player's turn
	 */
	public boolean isRedNext() {
		return nextPlayer == redPlayer;
	}

	/**
	 * Ends game after regular move (save winner, finish up game state,
	 * histories...)
	 *
	 * @param winner player who won the game
	 * @return true if game was indeed finished
	 */
	public boolean regularGameEnd(Player winner) {
		// public for tests
		if (finish()) {
			winner.setWinner();
			winner.getUser().updateStatistics();
			return true;
		}
		return false;
	}

	public boolean didRedDraw() {
		return redPlayer.requestedDraw();
	}

	public boolean didBlackDraw() {
		return blackPlayer.requestedDraw();
	}

	public boolean redGaveUp() {
		return redPlayer.surrendered();
	}

	public boolean blackGaveUp() {
		return blackPlayer.surrendered();
	}

	/*******************************************
	 * !!!!!!!!! To be implemented !!!!!!!!!!!!
	 ******************************************/

	@Override
	public void setBoard(String state) {
		// Note: This method is for automatic testing. A regular game would not start at some artificial state.
		//       It can be assumed that the state supplied is a regular board that can be reached during a game.
		// TODO: implement
	}

	@Override
	public String getBoard() {
		// TODO: implement
		return "rheagaehr/9/1c5c1/s1s1s1s1s/9/9/S1S1S1S1S/1C5C1/9/RHEAGAEHR";
	}

	@Override
	public boolean tryMove(String moveString, Player player) {
		// TODO: implement

		return false;
	}	
	
	/**
	 * turns FEN-Notation into 9x10 matrix
	 * @param state
	 * @return
	 */
	public char[][] matrizise(String state) {
		char [][] result = new char[9][10];
		Integer c = 0;
		Integer r = 0;
		for(Integer i = 0; i < state.length(); i++) {
			if(state.charAt(i) == '/') {
				r++;
				c = 0;
			} else if(PIECES_ALL.contains(state.substring(i,1))) {
				result[c][r] = state.charAt(i);
				c++;
			} else if("0123456789".contains(state.substring(i,1))) {
				c += (int) state.charAt(i);
			}
		}
		return result;
	}

	/**
	 * turns the board matrix into compact FEN-String
	 * @param matrix
	 * @return
	 */
	public String stringify(char[][] matrix) {
		String result = "";
		Integer i = 0;
		for(Integer r=0;r<10;r++) {
			for(Integer c=0;c<9;c++) {
				if(PIECES_ALL.contains("" + matrix[c][r])) {
					result = result + matrix[c][r];
				} else if(PIECES_ALL.contains("" + matrix[c+1][r]) || c == 9) {
					result = result + i;
					i = 0;
				} else {
					i++;
				}
			}
		}
		return result;
	}
	
	public Integer[] fieldInt(String posString) {
		Integer c = (int) posString.charAt(0) - 97;
		Integer r = (int) posString.charAt(1);
		Integer [] result = {c, r};
		return result;
	}
	
	/**
	 * translates column index to char as string
	 * @param column
	 * @param row
	 * @return
	 */
	public String fieldString(Integer column, Integer row) {
		String result = "";
		if(column == 0) {
			result = result + "a";
		}
		if(column == 1) {
			result = result + "b";
		}
		if(column == 2) {
			result = result + "c";
		}
		if(column == 3) {
			result = result + "d";
		}
		if(column == 4) {
			result = result + "e";
		}
		if(column == 5) {
			result = result + "f";
		}
		if(column == 6) {
			result = result + "g";
		}
		if(column == 7) {
			result = result + "h";
		}
		if(column == 8) {
			result = result + "i";
		}
		return result + row;
	}
	
	/**
	 * checks whether coordinates are within board b, red palace rp, black palace bp, red half rh oder black half bh
	 * @param pos
	 * @param area
	 * @return
	 */
	public boolean areCoordsWithinArea(Integer[] pos, String area) {
		boolean result = true;
		if(area == "b" && (pos[0] < 0 || pos[0] > 8 || pos[1] < 0 || pos[1] > 9)) {
			result = false;
		} else if(area == "rp" && (pos[0] < 3 || pos[0] > 5 || pos[1] < 0 || pos[1] > 2)) {
			result = false;
		} else if(area == "bp" && (pos[0] < 3 || pos[0] > 5 || pos[1] < 7 || pos[1] > 9)) {
			result = false;
		} else if(area == "rh" && (pos[0] < 0 || pos[0] > 8 || pos[1] < 0 || pos[1] > 4)) {
			result = false;
		} else if(area == "bh" && (pos[0] < 0 || pos[0] > 8 || pos[1] < 5 || pos[1] > 9)) {
			result = false;
		} else if(area != "b" && area != "rp" && area != "bp" && area != "rh" && area != "bh") {
			result = false;
		}
		return  result;
	}
	
	/**
	 * checks whether red is put in check
	 * @param board
	 * @return
	 */
	public boolean isRedInCheck(char[][] board) {
		boolean result = false;
		Integer[] pos = {0,0};
		String threatened = "";
		String generalPos = "";
		for(Integer c=0;c<9;c++) {
			for(Integer r=0;r<10;r++) {
				pos[0] = c;
				pos[1] = r;
				if(board[c][r] == 'G') {
					threatened = threatened + listPossibleFieldsEvilEye_G(pos, board);
				}
				if(board[c][r] == 'A') {
					threatened = threatened + listPossibleFields_A(pos, board);
				}
				if(board[c][r] == 'E') {
					threatened = threatened + listPossibleFields_E(pos, board);
				}
				if(board[c][r] == 'H') {
					threatened = threatened + listPossibleFields_H(pos, board);
				}
				if(board[c][r] == 'R') {
					threatened = threatened + listPossibleFields_R(pos, board);
				}
				if(board[c][r] == 'C') {
					threatened = threatened + listPossibleFields_C(pos, board);
				}
				if(board[c][r] == 'S') {
					threatened = threatened + listPossibleFields_S(pos, board);
				}
			}
		}
		return result;
	}
	
	/**
	 * checks whether black is put in check
	 * @param board
	 * @return
	 */
	public boolean isBlackInCheck(char[][] board) {
		boolean result = false;
		Integer[] pos = {0,0};
		String threatened = "";
		String generalPos = "";
		for(Integer c=0;c<9;c++) {
			for(Integer r=0;r<10;r++) {
				pos[0] = c;
				pos[1] = r;
				if(board[c][r] == 'g') {
					threatened = threatened + listPossibleFieldsEvilEye_g(pos, board);
				}
				if(board[c][r] == 'a') {
					threatened = threatened + listPossibleFields_a(pos, board);
				}
				if(board[c][r] == 'e') {
					threatened = threatened + listPossibleFields_e(pos, board);
				}
				if(board[c][r] == 'h') {
					threatened = threatened + listPossibleFields_h(pos, board);
				}
				if(board[c][r] == 'r') {
					threatened = threatened + listPossibleFields_r(pos, board);
				}
				if(board[c][r] == 'c') {
					threatened = threatened + listPossibleFields_c(pos, board);
				}
				if(board[c][r] == 's') {
					threatened = threatened + listPossibleFields_s(pos, board);
				}
			}
		}
		return result;
	}
	
	/**
	 * lists fields red general can move to only considering movement pattern, palace and pieces in the way
	 * @param pos
	 * @param board
	 * @return
	 */
	public String listPossibleFields_g(Integer[] pos, char[][] board) {
		String result = "";
		if(pos[1] > 0 && !PIECES_RED_ALL.contains(""+board[pos[0]][pos[1]-1])) {
			result = result + fieldString(pos[0], pos[1]-1);
		}
		if(pos[1] < 2 && !PIECES_RED_ALL.contains(""+board[pos[0]][pos[1]+1])) {
			result = result + fieldString(pos[0], pos[1]+1);
		}
		if(pos[0] > 3 && !PIECES_RED_ALL.contains(""+board[pos[0]-1][pos[1]])) {
			result = result + fieldString(pos[0]-1, pos[1]);
		}
		if(pos[0] < 5 && !PIECES_RED_ALL.contains(""+board[pos[0]+1][pos[1]])) {
			result = result + fieldString(pos[0]+1, pos[1]);
		}
		return result;
	}
	
	/**
	 * lists fields black general can move to only considering movement pattern, palace and pieces in the way
	 * @param pos
	 * @param board
	 * @return
	 */
	public String listPossibleFields_G(Integer[] pos, char[][] board) {
		String result = "";
		if(pos[1] > 7 && !PIECES_RED_ALL.contains(""+board[pos[0]][pos[1]-1])) {
			result = result + fieldString(pos[0], pos[1]-1);
		}
		if(pos[1] < 9 && !PIECES_RED_ALL.contains(""+board[pos[0]][pos[1]+1])) {
			result = result + fieldString(pos[0], pos[1]+1);
		}
		if(pos[0] > 3 && !PIECES_RED_ALL.contains(""+board[pos[0]-1][pos[1]])) {
			result = result + fieldString(pos[0]-1, pos[1]);
		}
		if(pos[0] < 5 && !PIECES_RED_ALL.contains(""+board[pos[0]+1][pos[1]])) {
			result = result + fieldString(pos[0]+1, pos[1]);
		}
		return result;
	}

	/**
	 * lists fields "threatened" by red generals evil eye
	 */
	public String listPossibleFieldsEvilEye_g(Integer[] pos, char[][] board) {
		String result = "";
		Integer[] p = pos;
		p[1]++;
		while(areCoordsWithinArea(p, "b") && !PIECES_ALL.contains(""+board[p[0]][p[1]])) {
			result = result + fieldString(p[0],p[1]);
			p[1]++;
		}
		return result;
	}

	/**
	 * lists fields "threatened" by black generals evil eye
	 * @param pos
	 * @param board
	 * @return
	 */
	public String listPossibleFieldsEvilEye_G(Integer[] pos, char[][] board) {
		String result = "";
		Integer[] p = pos;
		p[1]--;
		while(areCoordsWithinArea(p, "b") && !PIECES_ALL.contains(""+board[p[0]][p[1]])) {
			result = result + fieldString(p[0],p[1]);
			p[1]--;
		}
		return result;
	}
	
	/**
	 * lists fields red advisor can move to only considering movement pattern, palace and pieces in the way
	 * @param pos
	 * @param board
	 * @return
	 */
	public String listPossibleFields_a(Integer[] pos, char[][] board) {
		String result = "";
		if(pos[0] > 3 && pos[1] > 0 && !PIECES_RED_ALL.contains(""+board[pos[0]-1][pos[1]-1])) {
			result = result + fieldString(pos[0]-1, pos[1]-1);
		}
		if(pos[0] > 3 && pos[1] < 2 && !PIECES_RED_ALL.contains(""+board[pos[0]-1][pos[1]+1])) {
			result = result + fieldString(pos[0]-1, pos[1]+1);
		}
		if(pos[0] < 5 && pos[1] > 0 && !PIECES_RED_ALL.contains(""+board[pos[0]+1][pos[1]-1])) {
			result = result + fieldString(pos[0]+1, pos[1]-1);
		}
		if(pos[0] < 5 && pos[1] < 2 && !PIECES_RED_ALL.contains(""+board[pos[0]+1][pos[1]+1])) {
			result = result + fieldString(pos[0]+1, pos[1]+1);
		}
		return result;
	}
	
	/**
	 * lists fields black advisor can move to only considering movement pattern, palace and pieces in the way
	 * @param pos
	 * @param board
	 * @return
	 */
	public String listPossibleFields_A(Integer[] pos, char[][] board) {
		String result = "";
		if(pos[0] > 3 && pos[1] > 7 && !PIECES_RED_ALL.contains(""+board[pos[0]-1][pos[1]-1])) {
			result = result + fieldString(pos[0]-1, pos[1]-1);
		}
		if(pos[0] > 3 && pos[1] < 9 && !PIECES_RED_ALL.contains(""+board[pos[0]-1][pos[1]+1])) {
			result = result + fieldString(pos[0]-1, pos[1]+1);
		}
		if(pos[0] < 5 && pos[1] > 7 && !PIECES_RED_ALL.contains(""+board[pos[0]+1][pos[1]-1])) {
			result = result + fieldString(pos[0]+1, pos[1]-1);
		}
		if(pos[0] < 5 && pos[1] < 9 && !PIECES_RED_ALL.contains(""+board[pos[0]+1][pos[1]+1])) {
			result = result + fieldString(pos[0]+1, pos[1]+1);
		}
		return result;
	}
	
	/**
	 * lists fields red advisor can move to only considering movement pattern, river and pieces in the way
	 * @param pos
	 * @param board
	 * @return
	 */
	public String listPossibleFields_e(Integer[] pos, char[][] board) {
		String result = "";
		if(pos[0] > 1 && pos[1] > 1 && !PIECES_ALL.contains(""+board[pos[0]-1][pos[1]-1]) && !PIECES_RED_ALL.contains(""+board[pos[0]-2][pos[1]-2])) {
			result = result + fieldString(pos[0]-2, pos[1]-2);
		}
		if(pos[0] > 1 && pos[1] < 4 && !PIECES_ALL.contains(""+board[pos[0]-1][pos[1]+1]) && !PIECES_RED_ALL.contains(""+board[pos[0]-2][pos[1]+2])) {
			result = result + fieldString(pos[0]-2, pos[1]+2);
		}
		if(pos[0] < 7 && pos[1] > 1 && !PIECES_ALL.contains(""+board[pos[0]+1][pos[1]-1]) && !PIECES_RED_ALL.contains(""+board[pos[0]+2][pos[1]-2])) {
			result = result + fieldString(pos[0]+2, pos[1]-2);
		}
		if(pos[0] < 7 && pos[1] < 4 && !PIECES_ALL.contains(""+board[pos[0]+1][pos[1]+1]) && !PIECES_RED_ALL.contains(""+board[pos[0]+2][pos[1]+2])) {
			result = result + fieldString(pos[0]+2, pos[1]+2);
		}
		return result;
	}
	
	/**
	 * lists fields black advisor can move to only considering movement pattern, river and pieces in the way
	 * @param pos
	 * @param board
	 * @return
	 */
	public String listPossibleFields_E(Integer[] pos, char[][] board) {
		String result = "";
		if(pos[0] > 1 && pos[1] > 5 && !PIECES_ALL.contains(""+board[pos[0]-1][pos[1]-1]) && !PIECES_RED_ALL.contains(""+board[pos[0]-2][pos[1]-2])) {
			result = result + fieldString(pos[0]-2, pos[1]-2);
		}
		if(pos[0] > 1 && pos[1] < 8 && !PIECES_ALL.contains(""+board[pos[0]-1][pos[1]+1]) && !PIECES_RED_ALL.contains(""+board[pos[0]-2][pos[1]+2])) {
			result = result + fieldString(pos[0]-2, pos[1]+2);
		}
		if(pos[0] < 7 && pos[1] > 5 && !PIECES_ALL.contains(""+board[pos[0]+1][pos[1]-1]) && !PIECES_RED_ALL.contains(""+board[pos[0]+2][pos[1]-2])) {
			result = result + fieldString(pos[0]+2, pos[1]-2);
		}
		if(pos[0] < 7 && pos[1] < 8 && !PIECES_ALL.contains(""+board[pos[0]+1][pos[1]+1]) && !PIECES_RED_ALL.contains(""+board[pos[0]+2][pos[1]+2])) {
			result = result + fieldString(pos[0]+2, pos[1]+2);
		}
		return result;
	}
	
	/**
	 * lists fields red horse can move to only considering movement pattern and pieces in the way
	 * @param pos
	 * @param board
	 * @return
	 */
	public String listPossibleFields_h(Integer[] pos, char[][] board) {
		String result = "";
		result = result + listDir_h(pos, 0, -1, board);
		result = result + listDir_h(pos, 0, 1, board);
		result = result + listDir_h(pos, 1, -1, board);
		result = result + listDir_h(pos, 1, 1, board);
		return result;
	}
	
	/**
	 * lists fields black horse can move to only considering movement pattern and pieces in the way
	 * @param pos
	 * @param board
	 * @return
	 */
	public String listPossibleFields_H(Integer[] pos, char[][] board) {
		String result = "";
		result = result + listDir_H(pos, 0, -1, board);
		result = result + listDir_H(pos, 0, 1, board);
		result = result + listDir_H(pos, 1, -1, board);
		result = result + listDir_H(pos, 1, 1, board);
		return result;
	}
	
	/**
	 * lists possible fields in one direction for movement of red horse
	 * @param pos
	 * @param dim
	 * @param dir
	 * @param board
	 * @return
	 */
	public String listDir_h(Integer[] pos, Integer dim, Integer dir, char[][] board) {
		String result = "";
		Integer [] p1 = pos;
		Integer [] p2 = pos;
		p1[dim] = pos[dim] + dir;
		p2[dim] = p1[dim] + dir;
		if(areCoordsWithinArea(p2, "b") && !PIECES_ALL.contains(""+board[p1[0]][p1[1]])) {
			p1 = p2;
			p1[dim-1]++;
			p2[dim-1]--;
			if(areCoordsWithinArea(p1, "b") && !PIECES_RED_ALL.contains(""+board[p1[0]][p1[1]])) {
				result = result + fieldString(p1[0],p1[1]);
			}
			if(areCoordsWithinArea(p2, "b") && !PIECES_RED_ALL.contains(""+board[p2[0]][p2[1]])) {
				result = result + fieldString(p2[0],p2[1]);
			}
		}
		return result;
	}
	
	/**
	 * lists possible fields in one direction for movement of black horse
	 * @param pos
	 * @param dim
	 * @param dir
	 * @param board
	 * @return
	 */
	public String listDir_H(Integer[] pos, Integer dim, Integer dir, char[][] board) {
		String result = "";
		Integer [] p1 = pos;
		Integer [] p2 = pos;
		p1[dim] = pos[dim] + dir;
		p2[dim] = p1[dim] + dir;
		if(areCoordsWithinArea(p2, "b") && !PIECES_ALL.contains(""+board[p1[0]][p1[1]])) {
			p1 = p2;
			p1[dim-1]++;
			p2[dim-1]--;
			if(areCoordsWithinArea(p1, "b") && !PIECES_RED_ALL.contains(""+board[p1[0]][p1[1]])) {
				result = result + fieldString(p1[0],p1[1]);
			}
			if(areCoordsWithinArea(p2, "b") && !PIECES_RED_ALL.contains(""+board[p2[0]][p2[1]])) {
				result = result + fieldString(p2[0],p2[1]);
			}
		}
		return result;
	}
	
	/**
	 * lists fields red rook can move to only considering movement pattern and pieces in the way
	 * @param pos
	 * @param board
	 * @return
	 */
	public String listPossibleFields_r(Integer[] pos, char[][] board) {
		String result = "";
		result = result + listPossibleFields4Direction_r(pos, 0, -1, board);
		result = result + listPossibleFields4Direction_r(pos, 0, 1, board);
		result = result + listPossibleFields4Direction_r(pos, 1, -1, board);
		result = result + listPossibleFields4Direction_r(pos, 1, 1, board);
		return result;
	}
	
	/**
	 * lists fields red rook can move to only considering movement pattern and pieces in the way
	 * @param pos
	 * @param board
	 * @return
	 */
	public String listPossibleFields_R(Integer[] pos, char[][] board) {
		String result = "";
		result = result + listPossibleFields4Direction_R(pos, 0, -1, board);
		result = result + listPossibleFields4Direction_R(pos, 0, 1, board);
		result = result + listPossibleFields4Direction_R(pos, 1, -1, board);
		result = result + listPossibleFields4Direction_R(pos, 1, 1, board);
		return result;
	}
	
	/**
	 * lists possible fields in one direction for movement of red rook
	 * @param pos
	 * @param dim
	 * @param dir
	 * @param board
	 * @return
	 */
	public String listPossibleFields4Direction_r(Integer[] pos, Integer dim, Integer dir, char[][] board) {
		String result = "";
		Integer [] p = pos;
		p[dim] = pos[dim] + dir;
		while(0 <= p[0] && p[0] <= 8 && p[1] >=0 && p[1] <= 9 && !PIECES_RED_ALL.contains(""+board[p[0]][p[1]])) {
			result = result + fieldString(p[0],p[1]);
			if(PIECES_RED_ALL.contains(""+board[p[0]][p[1]])) {
				p[0] = - 1;
			}
			p[dim] += dir;
		}
		return result;
	}
	
	/**
	 * lists possible fields in one direction for movement of black rook
	 * @param pos
	 * @param dim
	 * @param dir
	 * @param board
	 * @return
	 */
	public String listPossibleFields4Direction_R(Integer[] pos, Integer dim, Integer dir, char[][] board) {
		String result = "";
		Integer [] p = pos;
		p[dim] = pos[dim] + dir;
		while(0 <= p[0] && p[0] <= 8 && p[1] >=0 && p[1] <= 9 && !PIECES_RED_ALL.contains(""+board[p[0]][p[1]])) {
			result = result + fieldString(p[0],p[1]);
			if(PIECES_RED_ALL.contains(""+board[p[0]][p[1]])) {
				p[0] = - 1;
			}
			p[dim] += dir;
		}
		return result;
	}
	
	/**
	 * lists fields red cannon can move to only considering movement pattern and pieces in the way
	 * @param pos
	 * @param board
	 * @return
	 */
	public String listPossibleFields_c(Integer[] pos, char[][] board) {
		String result = "";
		result = result + listPossibleFields4Direction_c(pos, 0, -1, board);
		result = result + listPossibleFields4Direction_c(pos, 0, 1, board);
		result = result + listPossibleFields4Direction_c(pos, 1, -1, board);
		result = result + listPossibleFields4Direction_c(pos, 1, 1, board);
		return result;
	}
	
	/**
	 * lists fields black cannon can move to only considering movement pattern and pieces in the way
	 * @param pos
	 * @param board
	 * @return
	 */
	public String listPossibleFields_C(Integer[] pos, char[][] board) {
		String result = "";
		result = result + listPossibleFields4Direction_C(pos, 0, -1, board);
		result = result + listPossibleFields4Direction_C(pos, 0, 1, board);
		result = result + listPossibleFields4Direction_C(pos, 1, -1, board);
		result = result + listPossibleFields4Direction_C(pos, 1, 1, board);
		return result;
	}

	/**
	 * lists possible fields in one direction for movement of red cannon
	 * @param pos
	 * @param dim
	 * @param dir
	 * @param board
	 * @return
	 */
	public String listPossibleFields4Direction_c(Integer[] pos, Integer dim, Integer dir, char[][] board) {
		String result = "";
		Integer[] p = pos;
		boolean take = false;
		p[dim] = pos[dim] + dir;
		while(0 <= p[0] && p[0] <= 8 && p[1] >=0 && p[1] <= 9 && (take == false || !PIECES_RED_ALL.contains(""+board[p[0]][p[1]]))) {
			if(take == false && PIECES_ALL.contains(""+board[p[0]][p[1]])) {
				take = true;
			} else if ((take == false && !PIECES_ALL.contains(""+board[p[0]][p[1]])) || (take = true && !PIECES_RED_ALL.contains(""+board[p[0]][p[1]]))){
				result = result + fieldString(p[0],p[1]);
			}
			p[dim] += dir;
		}
		return result;
	}
	
	/**
	 * lists possible fields in one direction for movement of black cannon
	 * @param pos
	 * @param dim
	 * @param dir
	 * @param board
	 * @return
	 */
	public String listPossibleFields4Direction_C(Integer[] pos, Integer dim, Integer dir, char[][] board) {
		String result = "";
		Integer[] p = pos;
		boolean take = false;
		p[dim] = pos[dim] + dir;
		while(0 <= p[0] && p[0] <= 8 && p[1] >=0 && p[1] <= 9 && (take == false || !PIECES_RED_ALL.contains(""+board[p[0]][p[1]]))) {
			if(take == false && PIECES_ALL.contains(""+board[p[0]][p[1]])) {
				take = true;
			} else if ((take == false && !PIECES_ALL.contains(""+board[p[0]][p[1]])) || (take = true && !PIECES_RED_ALL.contains(""+board[p[0]][p[1]]))){
				result = result + fieldString(p[0],p[1]);
			}
			p[dim] += dir;
		}
		return result;
	}
	
	/**
	 * lists fields red soldier can move to only considering movement pattern and pieces in the way
	 * @param pos
	 * @param board
	 * @return
	 */
	public String listPossibleFields_s(Integer[] pos, char[][] board) {
		String result = "";
		Integer[] p = pos;
		if(areCoordsWithinArea(pos, "rh") && !PIECES_RED_ALL.contains(""+board[pos[0]][pos[1]+1])) {
			result = result + fieldString(pos[0], pos[1]+1);
		} else if(areCoordsWithinArea(pos, "bh")) {
			if(pos[1] < 9 && !PIECES_RED_ALL.contains(""+board[pos[0]][pos[1]+1])) {
				result = result + fieldString(pos[0], pos[1]+1);
			}
			if(pos[0] > 0 && !PIECES_RED_ALL.contains(""+board[pos[0]-1][pos[1]])) {
				result = result + fieldString(pos[0]-1, pos[1]);
			}
			if(pos[0] < 8 && !PIECES_RED_ALL.contains(""+board[pos[0]+1][pos[1]])) {
				result = result + fieldString(pos[0]+1, pos[1]);
			}
		}
		return result;
	}
	
	/**
	 * lists fields black soldier can move to only considering movement pattern and pieces in the way
	 * @param pos
	 * @param board
	 * @return
	 */
	public String listPossibleFields_S(Integer[] pos, char[][] board) {
		String result = "";
		Integer[] p = pos;
		if(areCoordsWithinArea(pos, "bh") && !PIECES_RED_ALL.contains(""+board[pos[0]][pos[1]-1])) {
			result = result + fieldString(pos[0], pos[1]-1);
		} else if(areCoordsWithinArea(pos, "rh")) {
			if(pos[1] > 0 && !PIECES_RED_ALL.contains(""+board[pos[0]][pos[1]-1])) {
				result = result + fieldString(pos[0], pos[1]-1);
			}
			if(pos[0] > 0 && !PIECES_RED_ALL.contains(""+board[pos[0]-1][pos[1]])) {
				result = result + fieldString(pos[0]-1, pos[1]);
			}
			if(pos[0] < 8 && !PIECES_RED_ALL.contains(""+board[pos[0]+1][pos[1]])) {
				result = result + fieldString(pos[0]+1, pos[1]);
			}
		}
		return result;
	}
	
}
