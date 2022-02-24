package de.tuberlin.sese.swtpp.gameserver.model.xiangqi;

import de.tuberlin.sese.swtpp.gameserver.model.*;
//TODO: more imports from JVM allowed here


import java.io.Serializable;

public class XiangqiGame extends Game implements Serializable{

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
	private String state;
	/************************
	 * constructors
	 ***********************/

	public XiangqiGame() {
		super();

		// TODO: initialization of game state can go here 
		this.state = "rheagaehr/9/1c5c1/s1s1s1s1s/9/9/S1S1S1S1S/1C5C1/9/RHEAGAEHR";
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
		this.state = state;
	}

	@Override
	public String getBoard() {
		// TODO: implement
		return this.state;
	}

	@Override
	public boolean tryMove(String moveString, Player player) {
		// TODO: implement
		boolean result = true;
		char[][] board = matricise(getBoard());
		char piece = board[fieldInt(moveString.substring(0,2))[0]][fieldInt(moveString.substring(0,2))[1]];
		if(player == redPlayer && !isMovePossibleRed(piece, fieldInt(moveString.substring(0,2)), matricise(getBoard()))) {
			result = false;
		}
		if(player == blackPlayer && !isMovePossibleBlack(piece, fieldInt(moveString.substring(0,2)), matricise(getBoard()))) {
			result = false;
		}
		board[fieldInt(moveString.substring(0,2))[0]][fieldInt(moveString.substring(0,2))[1]] = ' ';
		board[fieldInt(moveString.substring(2,2))[0]][fieldInt(moveString.substring(2,2))[1]] = piece;
		if(player == redPlayer && isRedInCheck(board)) {
			result = false;
		}
		if(player == blackPlayer && isBlackInCheck(board)) {
			result = false;
		}
		if(result) {
			setBoard(stringify(board));
		}
		return result;
	}	
	
	//turns FEN-Notation into 9x10 matrix
	public char[][] matricise(String state) {
		char [][] result = new char[9][10];
		Integer c = 0;
		Integer r = 0;
		for(Integer i = 0; i < state.length(); i++) {
			if(state.charAt(i) == '/') {
				r++;
				c = 0;
			} else if("gaehrcsGAEHRCS".contains(state.substring(i,1))) {
				result[c][r] = state.charAt(i);
				c++;
			} else if("0123456789".contains(state.substring(i,1))) {
				c += (int) state.charAt(i);
			}
		}
		return result;
	}

	//turns the board matrix into compact FEN-String
	public String stringify(char[][] matrix) {
		String result = "";
		Integer i = 0;
		for(Integer r=0;r<10;r++) {
			for(Integer c=0;c<9;c++) {
				if("gaehrcsGAEHRCS".contains("" + matrix[c][r])) {
					result = result + matrix[c][r];
				} else if("gaehrcsGAEHRCS".contains("" + matrix[c+1][r]) || c == 9) {
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
	
	//converts "a1" into {0,1}
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
	
	//checks whether coordinates are within board b, red palace rp, black palace bp, red half rh oder black half bh
	public boolean withinArea(Integer[] pos, String area) {
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
	
	//checks whether red is put in check
		public boolean isRedInCheck(char[][] board) {
			boolean result = false;
			Integer[] gen = generalPositions(board);
			String threatened = threatenedByBlack(board);
			String generalPos = fieldString(gen[0],gen[1]);
			if(threatened.contains(generalPos)) {
				result = true;
			}
			return result;
		}
	
	//checks whether black is put in check
	public boolean isBlackInCheck(char[][] board) {
		boolean result = false;
		Integer[] gen = generalPositions(board);
		String threatened = threatenedByRed(board);
		String generalPos = fieldString(gen[2],gen[3]);
		if(threatened.contains(generalPos)) {
			result = true;
		}
		return result;
	}
	
	//lists fields threatened by black including evil eye
	public String threatenedByBlack(char[][] board) {
		String result = "";
		Integer[] pos = {0,0};
		for(Integer c=0;c<9;c++) {
			for(Integer r=0;r<10;r++) {
				pos[0] = c;
				pos[1] = r;
				if(board[c][r] == 'G') {
					result = result + evilEye_G(pos, board);
				}
				if(board[c][r] == 'A') {
					result = result + possibleFields_A(pos, board);
				}
				if(board[c][r] == 'E') {
					result = result + possibleFields_E(pos, board);
				}
				if(board[c][r] == 'H') {
					result = result + possibleFields_H(pos, board);
				}
				if(board[c][r] == 'R') {
					result = result + possibleFields_R(pos, board);
				}
				if(board[c][r] == 'C') {
					result = result + possibleFields_C(pos, board);
				}
				if(board[c][r] == 'S') {
					result = result + possibleFields_S(pos, board);
				}
			}
		}
		return result;
	}
	
	//lists fields threatened by red including evil eye
	public String threatenedByRed(char[][] board) {
		String result = "";
		Integer[] pos = {0,0};
		for(Integer c=0;c<9;c++) {
			for(Integer r=0;r<10;r++) {
				pos[0] = c;
				pos[1] = r;
				if(board[c][r] == 'g') {
					result = result + evilEye_g(pos, board);
				}
				if(board[c][r] == 'a') {
					result = result + possibleFields_a(pos, board);
				}
				if(board[c][r] == 'e') {
					result = result + possibleFields_e(pos, board);
				}
				if(board[c][r] == 'h') {
					result = result + possibleFields_h(pos, board);
				}
				if(board[c][r] == 'r') {
					result = result + possibleFields_r(pos, board);
				}
				if(board[c][r] == 'c') {
					result = result + possibleFields_c(pos, board);
				}
				if(board[c][r] == 's') {
					result = result + possibleFields_s(pos, board);
				}
			}
		}
		return result;
	}
	
	//finds both generals
	public Integer[] generalPositions(char[][] board) {
		Integer[] result = {4,0,4,9}; 
		for(Integer c=0;c<9;c++) {
			for(Integer r=0;r<10;r++) {
				if(board[c][r]== 'g') {
					result[0]=c;
					result[1]=r;
				}
				if(board[c][r]== 'G') {
					result[2]=c;
					result[3]=r;
				}
			}
		}
		return result;
	}
	
	//checks whether move is possible considering only movement pattern and friendly pieces in the way
	public boolean isMovePossibleRed(char piece, Integer[] pos, char[][]board) {
		boolean result = false;
		if(piece=='g' && possibleFields_g(pos,board).contains(fieldString(pos[0],pos[1]))) {
			result = true;
		}
		if(piece=='a' && possibleFields_a(pos,board).contains(fieldString(pos[0],pos[1]))) {
			result = true;
		}
		if(piece=='e' && possibleFields_e(pos,board).contains(fieldString(pos[0],pos[1]))) {
			result = true;
		}
		if(piece=='h' && possibleFields_h(pos,board).contains(fieldString(pos[0],pos[1]))) {
			result = true;
		}
		if(piece=='r' && possibleFields_r(pos,board).contains(fieldString(pos[0],pos[1]))) {
			result = true;
		}
		if(piece=='c' && possibleFields_c(pos,board).contains(fieldString(pos[0],pos[1]))) {
			result = true;
		}
		if(piece=='s' && possibleFields_s(pos,board).contains(fieldString(pos[0],pos[1]))) {
			result = true;
		}
		return result;
	}
	
	//checks whether move is possible considering only movement pattern and friendly pieces in the way
	public boolean isMovePossibleBlack(char piece, Integer[] pos, char[][]board) {
		boolean result = false;
		if(piece=='G' && possibleFields_G(pos,board).contains(fieldString(pos[0],pos[1]))) {
			result = true;
		}
		if(piece=='A' && possibleFields_A(pos,board).contains(fieldString(pos[0],pos[1]))) {
			result = true;
		}
		if(piece=='E' && possibleFields_E(pos,board).contains(fieldString(pos[0],pos[1]))) {
			result = true;
		}
		if(piece=='H' && possibleFields_H(pos,board).contains(fieldString(pos[0],pos[1]))) {
			result = true;
		}
		if(piece=='R' && possibleFields_R(pos,board).contains(fieldString(pos[0],pos[1]))) {
			result = true;
		}
		if(piece=='C' && possibleFields_C(pos,board).contains(fieldString(pos[0],pos[1]))) {
			result = true;
		}
		if(piece=='S' && possibleFields_S(pos,board).contains(fieldString(pos[0],pos[1]))) {
			result = true;
		}
		return result;
	}
	
	//lists fields red general can move to only considering movement pattern, palace and pieces in the way
	public String possibleFields_g(Integer[] pos, char[][] board) {
		String result = "";
		if(pos[1] > 0 && !"gaehrcs".contains(""+board[pos[0]][pos[1]-1])) {
			result = result + fieldString(pos[0], pos[1]-1);
		}
		if(pos[1] < 2 && !"gaehrcs".contains(""+board[pos[0]][pos[1]+1])) {
			result = result + fieldString(pos[0], pos[1]+1);
		}
		if(pos[0] > 3 && !"gaehrcs".contains(""+board[pos[0]-1][pos[1]])) {
			result = result + fieldString(pos[0]-1, pos[1]);
		}
		if(pos[0] < 5 && !"gaehrcs".contains(""+board[pos[0]+1][pos[1]])) {
			result = result + fieldString(pos[0]+1, pos[1]);
		}
		return result;
	}
	
	//lists fields black general can move to only considering movement pattern, palace and pieces in the way
	public String possibleFields_G(Integer[] pos, char[][] board) {
		String result = "";
		if(pos[1] > 7 && !"GAEHRCS".contains(""+board[pos[0]][pos[1]-1])) {
			result = result + fieldString(pos[0], pos[1]-1);
		}
		if(pos[1] < 9 && !"GAEHRCS".contains(""+board[pos[0]][pos[1]+1])) {
			result = result + fieldString(pos[0], pos[1]+1);
		}
		if(pos[0] > 3 && !"GAEHRCS".contains(""+board[pos[0]-1][pos[1]])) {
			result = result + fieldString(pos[0]-1, pos[1]);
		}
		if(pos[0] < 5 && !"GAEHRCS".contains(""+board[pos[0]+1][pos[1]])) {
			result = result + fieldString(pos[0]+1, pos[1]);
		}
		return result;
	}

	//lists fields "threatened" by red generals evil eye
	public String evilEye_g(Integer[] pos, char[][] board) {
		String result = "";
		Integer[] p = pos;
		p[1]++;
		while(withinArea(p, "b") && !"gaehrcsGAEHRCS".contains(""+board[p[0]][p[1]])) {
			result = result + fieldString(p[0],p[1]);
			p[1]++;
		}
		return result;
	}

	//lists fields "threatened" by black generals evil eye
	public String evilEye_G(Integer[] pos, char[][] board) {
		String result = "";
		Integer[] p = pos;
		p[1]--;
		while(withinArea(p, "b") && !"gaehrcsGAEHRCS".contains(""+board[p[0]][p[1]])) {
			result = result + fieldString(p[0],p[1]);
			p[1]--;
		}
		return result;
	}
	
	//lists fields red advisor can move to only considering movement pattern, palace and pieces in the way
	public String possibleFields_a(Integer[] pos, char[][] board) {
		String result = "";
		if(pos[0] > 3 && pos[1] > 0 && !"gaehrcs".contains(""+board[pos[0]-1][pos[1]-1])) {
			result = result + fieldString(pos[0]-1, pos[1]-1);
		}
		if(pos[0] > 3 && pos[1] < 2 && !"gaehrcs".contains(""+board[pos[0]-1][pos[1]+1])) {
			result = result + fieldString(pos[0]-1, pos[1]+1);
		}
		if(pos[0] < 5 && pos[1] > 0 && !"gaehrcs".contains(""+board[pos[0]+1][pos[1]-1])) {
			result = result + fieldString(pos[0]+1, pos[1]-1);
		}
		if(pos[0] < 5 && pos[1] < 2 && !"gaehrcs".contains(""+board[pos[0]+1][pos[1]+1])) {
			result = result + fieldString(pos[0]+1, pos[1]+1);
		}
		return result;
	}
	
	//lists fields black advisor can move to only considering movement pattern, palace and pieces in the way
	public String possibleFields_A(Integer[] pos, char[][] board) {
		String result = "";
		if(pos[0] > 3 && pos[1] > 7 && !"GAEHRCS".contains(""+board[pos[0]-1][pos[1]-1])) {
			result = result + fieldString(pos[0]-1, pos[1]-1);
		}
		if(pos[0] > 3 && pos[1] < 9 && !"GAEHRCS".contains(""+board[pos[0]-1][pos[1]+1])) {
			result = result + fieldString(pos[0]-1, pos[1]+1);
		}
		if(pos[0] < 5 && pos[1] > 7 && !"GAEHRCS".contains(""+board[pos[0]+1][pos[1]-1])) {
			result = result + fieldString(pos[0]+1, pos[1]-1);
		}
		if(pos[0] < 5 && pos[1] < 9 && !"GAEHRCS".contains(""+board[pos[0]+1][pos[1]+1])) {
			result = result + fieldString(pos[0]+1, pos[1]+1);
		}
		return result;
	}
	
	//lists fields red advisor can move to only considering movement pattern, river and pieces in the way
	public String possibleFields_e(Integer[] pos, char[][] board) {
		String result = "";
		if(pos[0] > 1 && pos[1] > 1 && !"gaehrcsGAEHRCS".contains(""+board[pos[0]-1][pos[1]-1]) && !"gaehrcs".contains(""+board[pos[0]-2][pos[1]-2])) {
			result = result + fieldString(pos[0]-2, pos[1]-2);
		}
		if(pos[0] > 1 && pos[1] < 4 && !"gaehrcsGAEHRCS".contains(""+board[pos[0]-1][pos[1]+1]) && !"gaehrcs".contains(""+board[pos[0]-2][pos[1]+2])) {
			result = result + fieldString(pos[0]-2, pos[1]+2);
		}
		if(pos[0] < 7 && pos[1] > 1 && !"gaehrcsGAEHRCS".contains(""+board[pos[0]+1][pos[1]-1]) && !"gaehrcs".contains(""+board[pos[0]+2][pos[1]-2])) {
			result = result + fieldString(pos[0]+2, pos[1]-2);
		}
		if(pos[0] < 7 && pos[1] < 4 && !"gaehrcsGAEHRCS".contains(""+board[pos[0]+1][pos[1]+1]) && !"gaehrcs".contains(""+board[pos[0]+2][pos[1]+2])) {
			result = result + fieldString(pos[0]+2, pos[1]+2);
		}
		return result;
	}
	
	//lists fields black advisor can move to only considering movement pattern, river and pieces in the way
	public String possibleFields_E(Integer[] pos, char[][] board) {
		String result = "";
		if(pos[0] > 1 && pos[1] > 5 && !"gaehrcsGAEHRCS".contains(""+board[pos[0]-1][pos[1]-1]) && !"GAEHRCS".contains(""+board[pos[0]-2][pos[1]-2])) {
			result = result + fieldString(pos[0]-2, pos[1]-2);
		}
		if(pos[0] > 1 && pos[1] < 8 && !"gaehrcsGAEHRCS".contains(""+board[pos[0]-1][pos[1]+1]) && !"GAEHRCS".contains(""+board[pos[0]-2][pos[1]+2])) {
			result = result + fieldString(pos[0]-2, pos[1]+2);
		}
		if(pos[0] < 7 && pos[1] > 5 && !"gaehrcsGAEHRCS".contains(""+board[pos[0]+1][pos[1]-1]) && !"GAEHRCS".contains(""+board[pos[0]+2][pos[1]-2])) {
			result = result + fieldString(pos[0]+2, pos[1]-2);
		}
		if(pos[0] < 7 && pos[1] < 8 && !"gaehrcsGAEHRCS".contains(""+board[pos[0]+1][pos[1]+1]) && !"GAEHRCS".contains(""+board[pos[0]+2][pos[1]+2])) {
			result = result + fieldString(pos[0]+2, pos[1]+2);
		}
		return result;
	}
	
	//lists fields red horse can move to only considering movement pattern and pieces in the way
	public String possibleFields_h(Integer[] pos, char[][] board) {
		String result = "";
		result = result + listDir_h(pos, 0, -1, board);
		result = result + listDir_h(pos, 0, 1, board);
		result = result + listDir_h(pos, 1, -1, board);
		result = result + listDir_h(pos, 1, 1, board);
		return result;
	}
	
	//lists fields black horse can move to only considering movement pattern and pieces in the way
	public String possibleFields_H(Integer[] pos, char[][] board) {
		String result = "";
		result = result + listDir_H(pos, 0, -1, board);
		result = result + listDir_H(pos, 0, 1, board);
		result = result + listDir_H(pos, 1, -1, board);
		result = result + listDir_H(pos, 1, 1, board);
		return result;
	}
	
	//lists possible fields in one direction for movement of red horse
	public String listDir_h(Integer[] pos, Integer dim, Integer dir, char[][] board) {
		String result = "";
		Integer [] p1 = pos;
		Integer [] p2 = pos;
		p1[dim] = pos[dim] + dir;
		p2[dim] = p1[dim] + dir;
		if(withinArea(p2, "b") && !"gaehrcsGAEHRCS".contains(""+board[p1[0]][p1[1]])) {
			p1 = p2;
			p1[dim-1]++;
			p2[dim-1]--;
			if(withinArea(p1, "b") && !"gaehrcs".contains(""+board[p1[0]][p1[1]])) {
				result = result + fieldString(p1[0],p1[1]);
			}
			if(withinArea(p2, "b") && !"gaehrcs".contains(""+board[p2[0]][p2[1]])) {
				result = result + fieldString(p2[0],p2[1]);
			}
		}
		return result;
	}
	
	//lists possible fields in one direction for movement of black horse
	public String listDir_H(Integer[] pos, Integer dim, Integer dir, char[][] board) {
		String result = "";
		Integer [] p1 = pos;
		Integer [] p2 = pos;
		p1[dim] = pos[dim] + dir;
		p2[dim] = p1[dim] + dir;
		if(withinArea(p2, "b") && !"gaehrcsGAEHRCS".contains(""+board[p1[0]][p1[1]])) {
			p1 = p2;
			p1[dim-1]++;
			p2[dim-1]--;
			if(withinArea(p1, "b") && !"GAEHRCS".contains(""+board[p1[0]][p1[1]])) {
				result = result + fieldString(p1[0],p1[1]);
			}
			if(withinArea(p2, "b") && !"GAEHRCS".contains(""+board[p2[0]][p2[1]])) {
				result = result + fieldString(p2[0],p2[1]);
			}
		}
		return result;
	}
	
	//lists fields red rook can move to only considering movement pattern and pieces in the way
	public String possibleFields_r(Integer[] pos, char[][] board) {
		String result = "";
		result = result + checkDir_r(pos, 0, -1, board);
		result = result + checkDir_r(pos, 0, 1, board);
		result = result + checkDir_r(pos, 1, -1, board);
		result = result + checkDir_r(pos, 1, 1, board);
		return result;
	}
	
	//lists fields red rook can move to only considering movement pattern and pieces in the way
	public String possibleFields_R(Integer[] pos, char[][] board) {
		String result = "";
		result = result + checkDir_R(pos, 0, -1, board);
		result = result + checkDir_R(pos, 0, 1, board);
		result = result + checkDir_R(pos, 1, -1, board);
		result = result + checkDir_R(pos, 1, 1, board);
		return result;
	}
	
	//lists possible fields in one direction for movement of red rook
	public String checkDir_r(Integer[] pos, Integer dim, Integer dir, char[][] board) {
		String result = "";
		Integer [] p = pos;
		p[dim] = pos[dim] + dir;
		while(0 <= p[0] && p[0] <= 8 && p[1] >=0 && p[1] <= 9 && !"gaehrcs".contains(""+board[p[0]][p[1]])) {
			result = result + fieldString(p[0],p[1]);
			if("GAEHRCS".contains(""+board[p[0]][p[1]])) {
				p[0] = - 1;
			}
			p[dim] += dir;
		}
		return result;
	}
	
	//lists possible fields in one direction for movement of black rook
	public String checkDir_R(Integer[] pos, Integer dim, Integer dir, char[][] board) {
		String result = "";
		Integer [] p = pos;
		p[dim] = pos[dim] + dir;
		while(0 <= p[0] && p[0] <= 8 && p[1] >=0 && p[1] <= 9 && !"GAEHRCS".contains(""+board[p[0]][p[1]])) {
			result = result + fieldString(p[0],p[1]);
			if("gaehrcs".contains(""+board[p[0]][p[1]])) {
				p[0] = - 1;
			}
			p[dim] += dir;
		}
		return result;
	}
	
	//lists fields red cannon can move to only considering movement pattern and pieces in the way
	public String possibleFields_c(Integer[] pos, char[][] board) {
		String result = "";
		result = result + checkDir_c(pos, 0, -1, board);
		result = result + checkDir_c(pos, 0, 1, board);
		result = result + checkDir_c(pos, 1, -1, board);
		result = result + checkDir_c(pos, 1, 1, board);
		return result;
	}
	
	//lists fields black cannon can move to only considering movement pattern and pieces in the way
	public String possibleFields_C(Integer[] pos, char[][] board) {
		String result = "";
		result = result + checkDir_C(pos, 0, -1, board);
		result = result + checkDir_C(pos, 0, 1, board);
		result = result + checkDir_C(pos, 1, -1, board);
		result = result + checkDir_C(pos, 1, 1, board);
		return result;
	}

	//lists possible fields in one direction for movement of red cannon
	public String checkDir_c(Integer[] pos, Integer dim, Integer dir, char[][] board) {
		String result = "";
		Integer[] p = pos;
		boolean take = false;
		p[dim] = pos[dim] + dir;
		while(0 <= p[0] && p[0] <= 8 && p[1] >=0 && p[1] <= 9 && (take == false || !"gaehrcs".contains(""+board[p[0]][p[1]]))) {
			if(take == false && "gaehrcsGAEHRCS".contains(""+board[p[0]][p[1]])) {
				take = true;
			} else if ((take == false && !"gaehrcsGAEHRCS".contains(""+board[p[0]][p[1]])) || (take = true && !"gaehrcs".contains(""+board[p[0]][p[1]]))){
				result = result + fieldString(p[0],p[1]);
			}
			p[dim] += dir;
		}
		return result;
	}
	
	//lists possible fields in one direction for movement of black cannon
	public String checkDir_C(Integer[] pos, Integer dim, Integer dir, char[][] board) {
		String result = "";
		Integer[] p = pos;
		boolean take = false;
		p[dim] = pos[dim] + dir;
		while(0 <= p[0] && p[0] <= 8 && p[1] >=0 && p[1] <= 9 && (take == false || !"GAEHRCS".contains(""+board[p[0]][p[1]]))) {
			if(take == false && "gaehrcsGAEHRCS".contains(""+board[p[0]][p[1]])) {
				take = true;
			} else if ((take == false && !"gaehrcsGAEHRCS".contains(""+board[p[0]][p[1]])) || (take = true && !"GAEHRCS".contains(""+board[p[0]][p[1]]))){
				result = result + fieldString(p[0],p[1]);
			}
			p[dim] += dir;
		}
		return result;
	}
	
	//lists fields red soldier can move to only considering movement pattern and pieces in the way
	public String possibleFields_s(Integer[] pos, char[][] board) {
		String result = "";
		Integer[] p = pos;
		if(withinArea(pos, "rh") && !"gaehrcs".contains(""+board[pos[0]][pos[1]+1])) {
			result = result + fieldString(pos[0], pos[1]+1);
		} else if(withinArea(pos, "bh")) {
			if(pos[1] < 9 && !"gaehrcs".contains(""+board[pos[0]][pos[1]+1])) {
				result = result + fieldString(pos[0], pos[1]+1);
			}
			if(pos[0] > 0 && !"gaehrcs".contains(""+board[pos[0]-1][pos[1]])) {
				result = result + fieldString(pos[0]-1, pos[1]);
			}
			if(pos[0] < 8 && !"gaehrcs".contains(""+board[pos[0]+1][pos[1]])) {
				result = result + fieldString(pos[0]+1, pos[1]);
			}
		}
		return result;
	}
	
	//lists fields black soldier can move to only considering movement pattern and pieces in the way
	public String possibleFields_S(Integer[] pos, char[][] board) {
		String result = "";
		Integer[] p = pos;
		if(withinArea(pos, "bh") && !"GAEHRCS".contains(""+board[pos[0]][pos[1]-1])) {
			result = result + fieldString(pos[0], pos[1]-1);
		} else if(withinArea(pos, "rh")) {
			if(pos[1] > 0 && !"GAEHRCS".contains(""+board[pos[0]][pos[1]-1])) {
				result = result + fieldString(pos[0], pos[1]-1);
			}
			if(pos[0] > 0 && !"GAEHRCS".contains(""+board[pos[0]-1][pos[1]])) {
				result = result + fieldString(pos[0]-1, pos[1]);
			}
			if(pos[0] < 8 && !"GAEHRCS".contains(""+board[pos[0]+1][pos[1]])) {
				result = result + fieldString(pos[0]+1, pos[1]);
			}
		}
		return result;
	}
	
}
