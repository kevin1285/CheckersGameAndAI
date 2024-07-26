
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.Map.Entry;
public abstract class Piece{
	protected int r, c;
	protected final int player, playerYDir; //taken into account that the two players move in opposite y directions
	protected Map<String, JumpPath> legalSquares = new HashMap<>(); // rc -> piecesTaken 
	protected Board board;
	protected int[][] dirPairs;
	protected final BufferedImage[] imgPair;
	protected int maxJumps;

	public int getPlayerYDir() {
		return playerYDir;
	}
	public int getR() {
		return r;
	}
	public int getC() {
		return c;
	}
	public void setCoords(int newR, int newC) {
		r = newR;
		c = newC;
	}
	public int getPlayer() {
		return player;
	}
	public Map<String, JumpPath> getLegalSquares() {
		return legalSquares;
	}
	public Board getBoard() {
		return board;
	}
	public int[][] getDirPairs() {
		return dirPairs;
	}
	
	public Piece(int r, int c, int player, Board board, BufferedImage[] imgPair) {
		this.r = r;
		this.c = c;
		this.player = player;
		this.board = board;
		this.imgPair = imgPair;
		this.playerYDir = player == 1 ? -1 : 1;//p1 moves toward top, p2 moves toward bottom
	}

	public static boolean onBackRank(int r, int player) {//uses row and player to check whether a piece is on backrank
		return (player == 1 && r == 0) || (player == 2 && r == 7);
	}
	public void promote() {
		board.getG()[r][c] = new King(r, c, player, board, imgPair);
	}
	public void demote() {
		board.getG()[r][c] = new Pawn(r, c, player, board, imgPair);
	}
	
	public void addAdjSquares() {
		//loop through directions, adding any squares the piece can walk to
		for(int[] pair : dirPairs) {
			int ar = r + pair[0] * playerYDir, ac = c - pair[1];
			if(hasAdj(ar, ac))
				legalSquares.put(ar+""+ac, null);
		}
	}
	
	public void addJumpSquares() {
		jumpSquaresHelper(r, c, new ArrayList<String>(), new ArrayList<Piece>());
	}

	public int getMaxPieceJumps() {//get max number of jumps for piece
		int maxJumps = 0;
		for(Entry<String, JumpPath> entry : legalSquares.entrySet())
			maxJumps = Math.max(maxJumps, entry.getValue().countJumps());
		return maxJumps;
	}
	public void removeSquares(int maxJumps) {//remove moves that have less jumps than maxJumps (this is used when forced move is on)
		Iterator<Map.Entry<String, JumpPath>> iter = legalSquares.entrySet().iterator();
		while(iter.hasNext()) {
			if(iter.next().getValue().countJumps() < maxJumps)
				iter.remove();
		}
	}
	
	public Piece copy() {
		if(this instanceof Pawn)
			return new Pawn(r, c, player, board, imgPair);
		else
			return new King(r, c, player, board, imgPair);
	}
	
	
	protected abstract void jumpSquaresHelper(int jr, int jc, List<String> path, List<Piece> takes); //helper method to populate jump squares
	
	protected boolean hasAdj(int ar, int ac) {//helper method of addAdjSquares to check if square is valid 
		return Board.inBounds(ar, ac) && board.getG()[ar][ac] == null;
	}
	
	protected void recordEventsOnSquare(String toCoord, int takeR, int takeC, List<String> path, List<Piece> takes, boolean promoted) { //update variables when finding jumps
		path.add(toCoord);
		takes.add(board.getG()[takeR][takeC].copy());
		JumpPath newPath = new JumpPath(path.size() > 1 ? new ArrayList<>(path).subList(0, path.size()-1) : null, new ArrayList<>(takes), promoted);
		if(!legalSquares.containsKey(toCoord) || newPath.countJumps() > legalSquares.get(toCoord).countJumps()) 
			legalSquares.put(toCoord, newPath);
	}
	
	protected boolean hasJump(int jr, int jc, int forward, int left, List<Piece> takes) {
		int rMult = forward * playerYDir, cMult = left;
		if(Board.inBounds(jr+2*rMult, jc-2*cMult) && (board.getG()[jr+2*rMult][jc-2*cMult] == null || (r==jr+2*rMult && c == jc-2*cMult)))//edge case: piece can jump back to its own square
			return Board.inBounds(jr+rMult, jc-cMult) && !takenDuringMove(jr+rMult, jc-cMult, takes) && board.getG()[jr+rMult][jc-cMult] != null && board.getG()[jr+rMult][jc-cMult].getPlayer() != player;
		return false;
	}
	
	protected boolean takenDuringMove(int jr, int jc, List<Piece> takes) {//returns true if the piece at the square is taken during a move
		for(Piece take : takes) {
			if(take.r == jr && take.c == jc)
				return true;
		}
		return false;
	}
	
	public boolean isPromotedAfterMove(int toR) {
		return this instanceof Pawn && onBackRank(toR, player); 
	}
	
	public abstract void drawPiece(Graphics g);
	
	public String toString() {
		//b -> black piece, r -> red piece, capitalized if king
		return player == 1 ? "b" : "r";
	}
	
}
