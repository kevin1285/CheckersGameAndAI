import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.*;

public class King extends Piece{
	public King(int r, int c, int player, Board board, BufferedImage[] imgPair) {
		super(r, c, player, board, imgPair);
		dirPairs = new int[][]{{1,1}, {1,-1}, {-1,1}, {-1,-1}}; //king can move in 4 directions
	}
	
	protected void jumpSquaresHelper(int jr, int jc, List<String> path, List<Piece> takes) {//use backtracking to explore all possible paths, adding the valid ones in legalSquares
		for(int[] pair : dirPairs) {//iterate thru directions
			int rMult = pair[0]*playerYDir, cMult = pair[1];
			String toCoord = (jr+2*rMult)+""+(jc-2*cMult);
			if(hasJump(jr, jc, pair[0], pair[1], takes)) {
				recordEventsOnSquare(toCoord, jr+rMult, jc-cMult, path, takes, false);
				//explore branch
				jumpSquaresHelper(jr+2*rMult, jc-2*cMult, path, takes);
				//undo change
				path.remove(path.size()-1);
				takes.remove(takes.size()-1);
			}
		}
	}
	
	public void drawPiece(Graphics g) {
		g.drawImage(imgPair[1], Board.toX(c) + 7, Board.toY(r) + 5, null);
	}
	
	public String toString() {
		return super.toString().toUpperCase();
	}
}