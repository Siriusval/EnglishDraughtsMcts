package fr.istic.ia.tp1;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation of the English Draughts game.
 * @author vdrevell
 *
 */
public class EnglishDraughts extends Game {
	/** 
	 * The checker board
	 */
	CheckerBoard board;
	
	/** 
	 * The {@link PlayerId} of the current player
	 * {@link PlayerId#ONE} corresponds to the whites
	 * {@link PlayerId#TWO} corresponds to the blacks
	 */
	PlayerId playerId;
	
	/**
	 * The current game turn (incremented each time the whites play)
	 */
	int nbTurn;
	
	/**
	 * The number of consecutive moves played only with kings and without capture
	 * (used to decide equality)
	 */
	int nbKingMovesWithoutCapture;
	
	/**
	 * Class representing a move in the English draughts game
	 * A move is an ArrayList of Integers, corresponding to the successive tile numbers (Manouri notation)
	 * toString is overrided to provide Manouri notation output.
	 * @author vdrevell
	 *
	 */
	class DraughtsMove extends ArrayList<Integer> implements Game.Move {

		private static final long serialVersionUID = -8215846964873293714L;

		@Override
		public String toString() {
			Iterator<Integer> it = this.iterator();
			Integer from = it.next();
			StringBuffer sb = new StringBuffer();
			sb.append(from);
			while (it.hasNext()) {
				Integer to = it.next();
				if (board.neighborDownLeft(from)==to || board.neighborUpLeft(from)==to
						|| board.neighborDownRight(from)==to || board.neighborUpRight(from)==to) {
					sb.append('-');
				}
				else {
					sb.append('x');
				}
				sb.append(to);
				from = to;
			}
			return sb.toString();
		}

		/**
		 * Get first element of DraughtsMove
		 * @return the first element
		 */
		public int getStartPosition(){
			return this.get(0);
		}

		/**
		 * Get last element of DraughtsMove
		 * @return the last element
		 */
		public int getEndPosition(){
			return this.get(this.size()-1);
		}
	}
	
	/**
	 * The default constructor: initializes a game on the standard 8x8 board.
	 */
	public EnglishDraughts() {
		this(8);
	}
	
	/**
	 * Constructor with custom boardSize (to play on a boardSize x boardSize checkerBoard).
	 * @param boardSize See {@link CheckerBoard#CheckerBoard(int)} for valid board sizes. 
	 */
	public EnglishDraughts(int boardSize) {
		this.board = new CheckerBoard(boardSize);
		this.playerId = PlayerId.ONE;
		this.nbTurn = 1;
		this.nbKingMovesWithoutCapture = 0;
	}
	
	/**
	 * Copy constructor
	 * @param d The game to copy
	 */
	EnglishDraughts(EnglishDraughts d) {
		this.board = d.board.clone();
		this.playerId = d.playerId;
		this.nbTurn = d.nbTurn;
		this.nbKingMovesWithoutCapture = d.nbKingMovesWithoutCapture;
	}
	
	@Override
	public EnglishDraughts clone() {
		return new EnglishDraughts(this);
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(nbTurn);
		sb.append(". ");
		sb.append(this.playerId==PlayerId.ONE?"W":"B");
		sb.append(":");
		sb.append(board.toString());
		return sb.toString();
	}
	
	@Override
	public String playerName(PlayerId playerId) {
		switch (playerId) {
		case ONE:
			return "Player with the whites";
		case TWO:
			return "Player with the blacks";
		case NONE:
		default:
			return "Nobody";
		}
	}
	
	@Override
	public String view() {
		return board.boardView() + "Turn #" + nbTurn + ". " + playerName(playerId) + " plays.\n";
	}
	
	/**
	 * Check if a tile is empty
	 * @param square Tile number
	 * @return
	 */
	boolean isEmpty(int square) {
		return this.board.isEmpty(square);
	}
	
	/** 
	 * Check if a tile is owned by adversary
	 * @param square Tile number
	 * @return
	 */
	boolean isAdversary(int square) {
		return !this.isEmpty(square) && !this.isMine(square);
	}
	
	/** 
	 * Check if a tile is owned by the current player
	 * @param square Tile number
	 * @return
	 */
	boolean isMine(int square) {
		if(this.playerId.equals(PlayerId.ONE)){
			return this.board.isWhite(square);
		}
		else{
			return this.board.isBlack(square);
		}
	}
	
	/** 
	 * Retrieve the list of positions of the pawns owned by the current player
	 * @return The list of current player pawn positions
	 */
	ArrayList<Integer> myPawns() {
		if(this.playerId.equals(PlayerId.ONE)){
			return this.board.getWhitePawns();
		}
		else{
			return this.board.getBlackPawns();
		}
	}
	
	
	/**
	 * Generate the list of possible moves
	 * - first check moves with captures
	 * - if no capture possible, return displacement moves
	 */
	@Override
	public List<Move> possibleMoves() {
		//
		// TODO generate the list of possible moves
		//
		// Advice: 
		// create two auxiliary functions :
		// - one for jump moves from a given position, with capture (and multi-capture).
		//    Use recursive calls to explore all multiple capture possibilities
		// - one function that returns the displacement moves from a given position (without capture)
		//
		ArrayList<Move> moves = new ArrayList<>();
		return moves;
	}


	/**
	 * Generate all possible displacement moves for the player
	 * @return the list of possible displacement moves
	 */
	private List<Move> displacementMoves() {
		ArrayList<Move> moves = new ArrayList<>();

		for(int pawn : this.myPawns()){

			//If white (or if king), check move up
			if(this.board.isWhite(pawn) || this.board.isKing(pawn)){
				int upLeftTile = this.board.neighborUpLeft(pawn);
				int upRightTile = this.board.neighborUpRight(pawn);

				//if free (check != 0 if in bounds)
				if(this.board.tileExist(upLeftTile) && this.board.isEmpty(upLeftTile)){
					//Create a move	& add it to list
					moves.add(createAMove(pawn, upLeftTile));
				}

				if(this.board.tileExist(upRightTile) && this.board.isEmpty(upRightTile)){
					//Create a move	& add it to list
					moves.add(createAMove(pawn, upRightTile));
				}

			}
			//If black (or if king), check move down
			if(this.board.isBlack(pawn) || this.board.isKing(pawn)){
				int downLeftTile = this.board.neighborDownLeft(pawn);
				int downRightTile = this.board.neighborDownRight(pawn);

				//if free
				if(this.board.tileExist(downLeftTile) && this.board.isEmpty(downLeftTile)){
					//Create a move	& add it to list
					moves.add(createAMove(pawn, downLeftTile));
				}

				//if free
				if(this.board.tileExist(downRightTile) && this.board.isEmpty(downRightTile)){
					//Create a move	& add it to list
					moves.add(createAMove(pawn, downRightTile));
				}
			}
		}
		return moves;
	}

	/**
	 * Create a move will create a DraughtMove Object
	 * @param start, the start position
	 * @param destination, the destination position
	 */
	private Move createAMove( int start, int destination) {
		DraughtsMove dMove = new DraughtsMove();
		dMove.add(start);
		dMove.add(destination);
		return dMove;
	}

	/*
	NOTE
	Si j'ai bien compris, le move doit etre possible avant qu'il soit passé en parametre
	 */

	/**
	 * Play method
	 * Check if (in that order) :
	 * - Player is valid
	 * - Convert chosen move to DraughtsMove
	 * - Move pawn
	 * - Check if adversary pawn(s) captured
	 * - (optional) Promote king if needed
	 * - Change playerId for next player
	 * - Increment number of turn
	 * - (optional) Increment nbKingMovesWithoutCapture if needed
	 * @param aMove
	 */
	@Override
	public void play(Move aMove) {
		// Player should be valid
		if (playerId == PlayerId.NONE)
			return;
		// We will cast Move to DraughtsMove (kind of ArrayList<Integer>
		if (!(aMove instanceof DraughtsMove))
			return;
		// Cast and apply the move
		DraughtsMove move = (DraughtsMove) aMove;

		//init state to check "nbKingMovesWithoutCapture" later
		boolean hasBeenCaptured = false; //Keep track if a pawn was captured
		boolean isKing = this.board.isKing(move.getStartPosition()); //check if the pawn is king before turn

		// Move pawn and capture opponents
		for(int i = 1; i < move.size(); i++){
			int startTile = move.get(i-1);
			int destTile = move.get(i);
			//move
			this.board.movePawn(startTile,destTile);

			//check if pawn exist in between
			int inBetweenTile = this.board.squareBetween(startTile,destTile);
			//take it if exist
			if (this.board.tileExist(inBetweenTile) && !this.board.isEmpty(inBetweenTile)){
				this.board.removePawn(inBetweenTile);
				hasBeenCaptured = true;
			}
		}


		// Promote to king if the pawn ends on the opposite of the board
		int currentTile = move.getEndPosition();
		if(this.playerId.equals(PlayerId.ONE) && this.board.lineOfSquare(currentTile) == 0){
			this.board.crownPawn(currentTile);
		}
		else if(this.playerId.equals(PlayerId.TWO) && this.board.lineOfSquare(currentTile) == this.board.size-1){
			this.board.crownPawn(currentTile);
		}
		
		// Next player
		this.playerId = getAdversaryId();
		
		// Update nbTurn
		nbTurn++;
		
		// Keep track of successive moves with kings without capture
		if(isKing && !hasBeenCaptured){
			nbKingMovesWithoutCapture++;
		}

	}

	@Override
	public PlayerId player() {
		return playerId;
	}

	/**
	 * Get the winner (or null if the game is still going)
	 * Victory conditions are :
	 * - adversary with no more pawns or no move possibilities
	 * Null game condition (return PlayerId.NONE) is
	 * - more than 25 successive moves of only kings and without any capture
	 */
	@Override
	public PlayerId winner() {

		// return the winner ID if possible
		if(this.myPawns().size() == 0 || this.possibleMoves().size()==0){
			return getAdversaryId();
		}

		// return PlayerId.NONE if the game is null
		else if (this.nbKingMovesWithoutCapture == 25){
			return PlayerId.NONE;
		}

		// Return null is the game has not ended yet
		return null;
	}

	/**
	 * Get the id of the next player
	 * @return ONE if TWO, and TWO if ONE
	 */
	private PlayerId getAdversaryId(){
		return this.playerId == PlayerId.ONE ? PlayerId.TWO : PlayerId.ONE;
	}
}
