package fr.istic.ia.tp1;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import fr.istic.ia.tp1.Game.Move;
import fr.istic.ia.tp1.Game.PlayerId;

/**
 * A class implementing a Monte-Carlo Tree Search method (MCTS) for playing two-player games ({@link Game}).
 * @author vdrevell
 *
 */
public class MonteCarloTreeSearch {

	/**
	 * A class to represent an evaluation node in the MCTS tree.
	 * This is a member class so that each node can access the global statistics of the owning MCTS.
	 * @author vdrevell
	 *
	 */
	class EvalNode {
		/** The number of simulations run through this node */
		int n;
		
		/** The number of winning runs */
		double w;
		
		/** The game state corresponding to this node */
		Game game;
		
		/** The children of the node: the games states accessible by playing a move from this node state */
		ArrayList<EvalNode> children;
		
		/** 
		 * The only constructor of EvalNode.
		 * @param game The game state corresponding to this node.
		 */
		EvalNode(Game game) {
			this.game = game;
			children = new ArrayList<EvalNode>();
			w = 0.0;
			n = 0;
		}
		
		/**
		 * Compute the Upper Confidence Bound for Trees (UCT) value for the node.
		 * @return UCT value for the node
		 */
		double uct() {
			return this.score() + Math.sqrt(2 * Math.log(root.n) / this.n); //implementation of the uct formala, see with the teacher for the constan
		}
		
		/**
		 * "Score" of the node, i.e estimated probability of winning when moving to this node
		 * @return Estimated probability of win for the node
		 */
		double score() {
			return (this.w/this.n);	//the winning probability win/loss
		}
		
		/**
		 * Update the stats (n and w) of the node with the provided rollout results
		 * @param res
		 */
		void updateStats(RolloutResults res) {

			this.w = res.nbWins(game.player());
			this.n = res.n;
		}

		boolean isLeaf(){
			return this.children.isEmpty();
		}
	}

	/**
	 * Go through the children of the chosen node to computes their uct in order to choose the next node
	 * @param children list of the nodes to evaluate
	 * @return the node with the best UCT value
	 */
	private EvalNode nodeChoice(ArrayList<EvalNode> children){

		double bestUCT = 0.0; //to store bestUCT in order to compute it only one time
		EvalNode bestNode = null;

		for(EvalNode child : children){
			double childUCT = child.uct();
			if(childUCT>bestUCT){ //if the node's uct is better, if equal we prioritize the left of the tree
				bestUCT = childUCT;	//update bestUCT
				bestNode = child;	//update bestnode
			}
		}

		return bestNode;
	}

	/**
	 * A class to hold the results of the rollout phase
	 * Keeps the number of wins for each player and the number of simulations.
	 * @author vdrevell
	 *
	 */
	static class RolloutResults {
		/** The number of wins for player 1 {@link PlayerId#ONE}*/
		double win1;
		
		/** The number of wins for player 2 {@link PlayerId#TWO}*/
		double win2;
		
		/** The number of playouts */
		int n;
		
		/**
		 * The constructor
		 */
		public RolloutResults() {
			reset();
		}
		
		/**
		 * Reset results
		 */
		public void reset() {
			n = 0;
			win1 = 0.0;
			win2 = 0.0;
		}
		
		/**
		 * Add other results to this
		 * @param res The results to add
		 */
		public void add(RolloutResults res) {
			win1 += res.win1;
			win2 += res.win2;
			n += res.n;
		}
		
		/**
		 * Update playout statistics with a win of the player <code>winner</code>
		 * Also handles equality if <code>winner</code>={@link PlayerId#NONE}, adding 0.5 wins to each player
		 * @param winner
		 */
		public void update(PlayerId winner) {
			n++;

			try {
				switch (winner) {
					case ONE:
						win1++;
						break;
					case TWO:
						win2++;
						break;
					case NONE:
						win1 += 0.5;
						win2 += 0.5;
						break;
					default:
						throw new Exception("RolloutResults.update : PlayerId not supported");
					}
			}
			catch (Exception e){
				e.printStackTrace();
			}

		}
		
		/**
		 * Getter for the number of wins of a player
		 * @param playerId
		 * @return The number of wins of player <code>playerId</code>
		 */
		public double nbWins(PlayerId playerId) {
			switch (playerId) {
			case ONE: return win1;
			case TWO: return win2;
			default: return 0.0;
			}
		}
		
		/**
		 * Getter for the number of simulations
		 * @return The number of playouts
		 */
		public int nbSimulations() {
			return n;
		}
	}
	
	/**
	 * The root of the MCTS tree
	 */
	EvalNode root;
	
	/**
	 * The total number of performed simulations (rollouts)
	 */
	int nTotal;

	
	/**
	 * The constructor
	 * @param game
	 */
	public MonteCarloTreeSearch(Game game) {
		root = new EvalNode(game.clone());
		nTotal = 0;
	}
	
	/**
	 * Perform a single random playing rollout from the given game state
	 * @param game Initial game state. {@code game} will contain an ended game state when the function returns.
	 * @return The PlayerId of the winner (or NONE if equality or timeout).
	 */
	static PlayerId playRandomlyToEnd(Game game) {
		while(game.winner() == null){
			List<Move> moveList = game.possibleMoves();
			Move move = getRandomElement(moveList);
			game.play(move);
		}

		return game.winner();
	}

	/**
	 * Expand step of the MCTS, creates a child and add it to the parent's children
	 * @param parent Node to expand
	 * @return The children node
	 */
	public EvalNode expand(EvalNode parent){

		List<Move> pool =parent.game.possibleMoves();	//takes the parent's state possible moves
		Move toPlay = getRandomElement(pool);			//choose randomly a move

		Game etatEnfant = parent.game.clone();  //gets the parent's game
		etatEnfant.play(toPlay);				//and play the chosen move on it

		EvalNode child = new EvalNode(etatEnfant);	//creates a new Node with the child's state
		parent.children.add(child);					//adds the new child to the parent's children

		return child;
	}

	private static Move getRandomElement(List<Move> list){
		Random r = new Random();
		return list.get(r.nextInt(list.size()));
	}
	
	/**
	 * Perform nbRuns rollouts from a game state, and returns the winning statistics for both players.
	 * @param game The initial game state to start with (not modified by the function)
	 * @param nbRuns The number of playouts to perform
	 * @return A RolloutResults object containing the number of wins for each player and the number of simulations
	 */
	static RolloutResults rollOut(final Game game, int nbRuns) {

		RolloutResults rolloutResults = new RolloutResults();

		for(int i =0; i< nbRuns;i++){
			Game clone = game.clone();
			PlayerId playerId = playRandomlyToEnd(clone);
			rolloutResults.update(playerId);
		}

		return rolloutResults;
	}
	
	/**
	 * Apply the MCTS algorithm during at most <code>timeLimitMillis</code> milliseconds to compute
	 * the MCTS tree statistics.
	 * @param timeLimitMillis Computation time limit in milliseconds
	 */
	public void evaluateTreeWithTimeLimit(int timeLimitMillis) {
		// Record function entry time
		long startTime = System.nanoTime();

		// Evaluate the tree until timeout
		while (TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime) < timeLimitMillis) {
			// Perform one MCTS step
			boolean canStop = evaluateTreeOnce();
			// Stop evaluating the tree if there is nothing more to explore
			if (canStop) {
				break;
			}
		}
		
		// Print some statistics
		System.out.println("Stopped search after " 
		       + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime) + " ms. "
		       + "Root stats is " + root.w + "/" + root.n + String.format(" (%.2f%% loss)", 100.0*root.w/root.n));
	}
	
	/**
	 * Perform one MCTS step (selection, expansion(s), simulation(s), backpropagation
	 * @return <code>true</code> if there is no need for further exploration (to speed up end of games).
	 */
	public boolean evaluateTreeOnce() {

		// List of visited nodes
		List<EvalNode> visitedNodes = new ArrayList<>();
		
		// Start from the root
		EvalNode node = this.root;
		visitedNodes.add(node);
		
		// Selection (with UCT tree policy)
		while(!node.isLeaf()){
			//tree policy to choose a child
			node = nodeChoice(node.children);
			visitedNodes.add(node);
		}

		// Expand node : create a random new child
		EvalNode newChild = expand(node);
		visitedNodes.add(newChild);

		// Simulate from new node(s)
		RolloutResults valeur =  rollOut(newChild.game,1); //1 ?

		// Backpropagate results
		for(EvalNode vNode : visitedNodes){
			vNode.updateStats(valeur);
		}
		// Return false if tree evaluation should continue
		return false;
	}
	
	/**
	 * Select the best move to play, given the current MCTS tree playout statistics
	 * @return The best move to play from the current MCTS tree state.
	 */
	public Move getBestMove() {

		ArrayList<EvalNode> children = this.root.children;
		double bestScore = 0.0;
		Move bestMove = null;

		//Get node with best score
		for(EvalNode child : children){
			double childScore = child.score();
			if(childScore> bestScore){
				bestScore = childScore;

				EnglishDraughts englishDraughts = (EnglishDraughts) child.game;
				bestMove = englishDraughts.getLastMove();
			}
		}
		return bestMove;
	}
	
	
	/**
	 * Get a few stats about the MTS tree and the possible moves scores
	 * @return A string containing MCTS stats
	 */
	public String stats() {
		String str = "MCTS with " + nTotal + " evals\n";
		Iterator<Move> itMove = root.game.possibleMoves().iterator();
		for (EvalNode node : root.children) {
			Move move = itMove.next();
			double score = node.score();
			str += move + " : " + score + " (" + node.w + "/" + node.n + ")\n";
		}
		return str;
	}
}
