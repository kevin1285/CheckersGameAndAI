import java.util.*;
import java.util.Map.Entry;

public class AIPlayer {
	int maxDepth;
	Board board;
	Move bestMove;
	int tradeThreshold, playstyle;
	public AIPlayer(Board b, int depth, int style) {
		maxDepth = depth;
		board = b;
		playstyle = style;
		if(depth>0) 
			tradeThreshold = -2*playstyle; //tradeThreshold means the material difference required after the AI starts making trades
	}

	public Move calculateMove() throws Exception {
		if(maxDepth != 0) //use minimax algorithm if mode is medium+
			minimax(0, 2, Integer.MIN_VALUE, Integer.MAX_VALUE);
		else
			findEasyMove(); //otherwise find easy move
		return bestMove; 
	}
	
	private void findEasyMove() {
		List<Move> possibleMoves = getPossibleMoves(2); 
		//since possibleMoves is sorted in reverse, we can use the value of the move (indicated by its position in the list) to simulate the playstyle
		//easy is the only mode where its playstyle drastically affects its move accuracy
		if(playstyle == -1) { //passive -> equally likely to play any move in possibleMoves (however there are usually more possible moves with walks than jumps, so a walk is more likely)
			bestMove = possibleMoves.get((int)(Math.random())*possibleMoves.size()); 
			return;
		}
		int[] weights = new int[possibleMoves.size()];
		if(playstyle == 1){ //aggressive ->  way more likely to play higher value move
			for(int i=0; i<weights.length; i++) {
				weights[i] = (weights.length-i) * (weights.length-i);
			}
		}
		else if(playstyle == 0) { //normal ->  a bit more likely to play higher value move
			for(int i=0; i<weights.length; i++) {
				weights[i] = (weights.length-i);
			}
		}
		int idx = weightedRandIdx(weights);
		bestMove = possibleMoves.get(idx);
	}
	private int weightedRandIdx(int[] weights) {//uses bucket method to find random idx based on weights https://www.youtube.com/watch?v=fWS0TCcr-lE
		int[] cumsum = new int[weights.length];
		cumsum[0] = weights[0];
		for(int i=1; i<weights.length; i++) {
			cumsum[i] = cumsum[i-1] + weights[i];
		}
		
		//binary search for idx
		int l = 0, r = weights.length-1, rand = (int)(cumsum[cumsum.length-1]*Math.random()) + 1;
		while(l<r) {
			int mid = (l+r)/2;
			if(cumsum[mid] == rand)
				return mid;
			else if(cumsum[mid] < rand)
				l = mid+1;
			else
				r = mid;
		}
		return l;
		
	}
	private int minimax(int depth, int turn, int alpha, int beta) throws Exception {
		List<Move> possibleMoves = getPossibleMoves(turn);
		if(turn==1 && board.lostByPosition()) return 999999999-depth; 
		else if (turn==2 && board.lostByPosition()) return -999999999+depth;
		else if(board.draw() != 0) return getMaterialScore()>0 ? -1 : 1;
		else if(depth == maxDepth) return getHeuristicScore();
		
		if (turn==2) { //AI's turn - want to maximize score
			int maxScore = Integer.MIN_VALUE;
			for (Move possibleMove : possibleMoves) {
				board.executeMove(possibleMove, true); //explore new position
				int score = minimax(depth+1, 1, alpha, beta);
				board.takeback(true); //backtrack to previous position				
				if (score > maxScore) {
					maxScore = score; 
					if(depth == 0)
						bestMove = possibleMove; //we only need to find the next single move for AI, so only update bestMove when depth=0
				}			
				alpha = Math.max(alpha, score); //alpha beta pruning to kill branch if guarenteed better move in earlier branch
				if(beta <= alpha) break;
			}
			return maxScore;
		} else { //AI's turn - want to minimize score
			int minScore = Integer.MAX_VALUE;
			for (Move possibleMove : possibleMoves) {
				board.executeMove(possibleMove, true);
				int score = minimax(depth+1, 2, alpha, beta);
				board.takeback(true);
				minScore = Math.min(score, minScore);		
				beta = Math.min(beta, score); 
				if(beta <= alpha) break;
			}
			return minScore;
		}

	}
	
	
	
	
	
	private int getHeuristicScore() { 
		//gets score based on material, trading, and positions each with different weighting
		return 1000000 * getMaterialScore() + getBackRankDistanceScore() + 1000*getTradeScore() + getProximityScore();
	}
	
	
	private int getMaterialScore() { //material score is the difference between the amount of material for both players
		return board.getPlayer2().getMaterial() - board.getPlayer1().getMaterial();
	}
	
	
	
	private int getTradeScore() { //makes winning player look for trades, but takes in account the playstyle which affects whether trades are made or not
		int materialSum = board.getPlayer2().getMaterial() + board.getPlayer1().getMaterial();
		return getMaterialScore() > tradeThreshold ? -materialSum : materialSum;
	}
	private int getBackRankDistanceScore() {
		int score = 0; // add if player2 (AI), subtract if player1
		//encourages pawns to promote
		for(int r=0; r<8; r++) {
			for(int c=0; c<8; c++) {
				if(board.getG()[r][c] != null && board.getG()[r][c] instanceof Pawn) {
					if(board.getG()[r][c].getPlayer() == 2) {
						score += r*r; 
					}else {
						score -= (7-r)*(7-r);
					}
				}
			}
		}
		return score;
	}

	private int getProximityScore() {//if a player is winning in endgame, they want to get their pieces closer to the enemy
		if(board.getPlayer1().getPawns()+board.getPlayer2().getPawns()>=5 || Math.abs(getMaterialScore()) <= 2) 
			return 0;
	
		List<Piece> pieces1 = new ArrayList<>(), pieces2 = new ArrayList<>(); //populate 2 arraylists each with the pieces of its player
		for(Piece[] row : board.getG()) {
			for(Piece piece : row) {
				if(piece != null)
					(piece.getPlayer()==1 ? pieces1 : pieces2).add(piece);
			}
		}
		//AI wants to trap piece when winning in endgame. After the enemy piece is trapped, they do not need to worry about getting pieces close to it, so remove it from list
		int trapScore = 0;
		for(int i=pieces1.size()-1; i>=0; i--) {
			int trapVal = trapValue(pieces1.get(i));
			if(trapVal > 0) {
				trapScore += 100*trapVal;
				pieces1.remove(i);
			}
		}
		
		//winning player wants to engage losing player by bringing pieces closer
		int total = 0;
		for(Piece p2 : pieces2) {
			int distToClosestPiece = minDiagonalDist(p2, pieces1);
			total += distToClosestPiece;
		}

		int distScore = total; 
		return (getMaterialScore()>=1 ? -distScore  : distScore) + trapScore;
	}
	private int trapValue(Piece p) { //1-> partial trap (p1 can escape due to another piece blocking capture) 2-> complete trap (any available move made would lead to piece being captured)
		//*these are just some of the common trapping positions, there are way more
		//sides
		if(p.getLegalSquares().isEmpty())
			return 2;
		if(p.getR() != 1 && p.getR() != 7) {
			if(p.getC() == 0 && hasK2(p.getR(), 2)) 
				return blocked(p.getR(), p.getC()) ? 1 : 2;
			if(p.getC() == 7 && hasK2(p.getR(), 5))
				return blocked(p.getR(), p.getC()) ? 1 : 2;
		}
		if(p.getC() != 1 && p.getR() != 7) {
			if(p.getR() == 0 && hasK2(2, p.getC()))
				return blocked(p.getR(), p.getC()) ? 1 : 2;
			if(p.getR() == 7 && hasK2(5, p.getC()))
				return blocked(p.getR(), p.getC()) ? 1 : 2;
		}
		
		//corners
		if(p.getR() == 7 && p.getC() == 0) {
			if(hasK2(5, 2))
				return hasP(p.getR()-1, p.getC()+1) ? 1 : 2;
			if(hasK2(7, 2))
				return hasP(5,0) ? 1 : 2;
			if(hasK2(5, 0))
				return hasP(7,2) ? 1 : 2;
		}
		if(p.getR() == 0 && p.getC() == 7) {
			if(hasK2(2, 5))
				return hasP(p.getR()-1, p.getC()+1) ? 1 : 2;
			if(hasK2(2, 7))
				return hasP(0,5) ? 1 : 2;
			if(hasK2(0, 5))
				return hasP(2,7) ? 1 : 2;
		}
		return 0;
	}

	private boolean hasK2(int r, int c) {
		return board.getG()[r][c] != null && board.getG()[r][c].getPlayer() == 2 && board.getG()[r][c] instanceof King;
	}
	private boolean blocked(int r, int c) {//return whether the trap is blocked by another piece
		if(r==0 || r==7) 
			return hasP(r, c+2) || hasP(r, c-2);
		return hasP(r+2, c) || hasP(r-2, c);
	}
	private boolean hasP(int r, int c) {//has piece at cell
		return r>=0&&r<8&&c>=0&&c<8 && board.getG()[r][c] != null;
	}
	private int diagonalDist(Piece p1, Piece p2) {
		int d = Math.max(Math.abs(p1.getR()-p2.getR()), Math.abs(p1.getC()-p2.getC()));
		return d;
	}
	private int minDiagonalDist(Piece from, List<Piece> pieces) {
		int minDist = 8;
		for(Piece to : pieces) {
			if(to != null)
				minDist = Math.min(minDist, diagonalDist(from, to));
		}
		return minDist;
	}

	private List<Move> getPossibleMoves(int player) { //gets possible moves for indicated player
		board.populateLegalSquares(player);
		List<Move> possibleMoves = new ArrayList<>();
		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++) {
				if (Board.isValidPiece(board.getG()[r][c], player)) {
					for (Entry<String, JumpPath> entry : board.getG()[r][c].getLegalSquares().entrySet()) {
						int toR = entry.getKey().charAt(0) - '0', toC = entry.getKey().charAt(1) - '0';
						possibleMoves.add(new Move(r, c, toR, toC, entry.getValue(), board.getG()[r][c].isPromotedAfterMove(toR)));
					}
				}
			}
		}
		Collections.sort(possibleMoves, Collections.reverseOrder()); //decreases runtime by checking better moves first
		return possibleMoves;
	}
}