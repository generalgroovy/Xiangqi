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

	//adds up neighboring numbers in state
	public String compact(String state) {
		String result = state;
		String numbers = "0123456789";
		Integer i = 0;
		while (i < result.length()) {
			if(numbers.contains(result.substring(i,1)) && numbers.contains(result.substring(i+1,1))) {
				Integer s = (int) result.charAt(i) + (int) result.charAt(i+1);
				result = result.substring(0,i) + s + result.substring(i+2,result.length()-i-2);
			} else {
				i++;
			}
		}
		return result;
	}
	
	//turns numbers above 1 into consecutive 1s for ease of readability
	public String decompact(String state) {
		String result = "";
		String numbers = "0123456789";
		Integer i = 0;
		while(i < 81 && i < state.length()) {
			if(numbers.contains(state.substring(i,1)) && 1 < (int) state.charAt(i)) {
				for(Integer j = 0; j < (int) state.charAt(i); j++) {
					result = result + "1";
					i++;
				}
			} else {
				result = result + state.substring(i,1);
				i++;
			}
		}
		return state;
	}
	
	//finds Index of "[column][row]" in decompacted state
	public Integer findIndex(String positionString, String state) {
		String dec = decompact(state);
		Integer r = (int) (positionString.charAt(1));
		Integer c = (int) (positionString.charAt(0)) - 97; //turns 'a'into 1,...,'i' into 9
		Integer i = 0;
		while(r > 0 && i <= dec.length()) {
			if(dec.substring(i,1) == "/") {
				r--;
			}
			i++;
		}
		return i+c;
	}
	
	//returns state with piece removed, unaltered if field is empty
	public String removePiece(String moveString, String state) {
		String result = decompact(state);
		Integer index = findIndex(moveString.substring(0,2), state);
		result = result.substring(0,index) + "1" + result.substring(index+1,result.length()-index-1);
		return compact(result);
	}
	
	//returns state with piece added, unaltered if unsuccessful
	public String addPiece(String moveString, String state, String piece) {
		String result = decompact(state);
		Integer index = findIndex(moveString.substring(2,2), state);
		result = result.substring(0,index) + piece + result.substring(index+1,result.length()-index-1);
		return compact(result);
	}
	
	
	//checks whether moveString adheres to "[char][integer]-[char][integer]" format
	public boolean isMove(String moveString) {
		boolean check = true;
		String columns = "abcdefghi";
		String rows = "0123456789";
		if(!columns.contains(moveString.substring(0,1)) || !columns.contains(moveString.substring(3,1))) {
			check = false;
		}
		if(!rows.contains(moveString.substring(1,1)) || !rows.contains(moveString.substring(4,1))) {
			check = false;
		}
		if(moveString.substring(2,1) != "-") {
			check = false;
		}
		
		return check;
	}
	
	public boolean validMoveg(String moveString, String state) {
		boolean result = true;
		String friendlies = "gaehrcs";				
		Integer currentRow = (int) moveString.charAt(1);
		Integer currentColumn = (int) moveString.charAt(0) - 96;
		Integer targetRow = (int) moveString.charAt(4);
		Integer targetColumn = (int) moveString.charAt(3) - 96;
		if(friendlies.contains(decompact(state).substring(findIndex(moveString.substring(3,2),(state)),1))) {
			result = false;
		}
		if( targetColumn < 4 || targetColumn > 6) {
			result = false;
		}
		if(targetRow > 2) {
			result = false;
		}
		if(currentRow - targetRow != 0 && currentColumn - targetColumn != 0) {
			result = false;
		}
		return result;
	}

}
