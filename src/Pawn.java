import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.*;
public class Pawn extends Piece {
	
	public Pawn(int r, int c, int player, Board board, BufferedImage[] imgPair) {
		super(r, c, player, board, imgPair);
		dirPairs = new int[][]{{1,1}, {1,-1}};
	}
	
	protected void jumpSquaresHelper(int jr, int jc, List<String> path, List<Piece> takes) { //use backtracking to explore all possible paths, adding the valid ones in legalSquares
		for(int[] pair : dirPairs) {
			int rMult = pair[0]*playerYDir, cMult = pair[1]; //take into account direction
			String toCoord = (jr+2*rMult)+""+(jc-2*cMult);
			if(hasJump(jr, jc, pair[0], pair[1], takes)) {				
				if(isPromotedAfterMove(jr+2*rMult))
					dirPairs = new int[][]{{1,1}, {1,-1}, {-1,1}, {-1,-1}}; //promoted pieces move in 4 directions
				recordEventsOnSquare(toCoord, jr+rMult, jc-cMult, path, takes, dirPairs.length==4 && this instanceof Pawn);
				jumpSquaresHelper(jr+2*rMult, jc-2*cMult, path, takes); //explore possible branch
				//undo change, backtracking to previous state
				path.remove(path.size()-1);
				takes.remove(takes.size()-1);
				dirPairs = new int[][]{{1,1}, {1,-1}};
			}
		}
	}
	
	public void drawPiece(Graphics g) {
		g.drawImage(imgPair[0], Board.toX(c) + 7, Board.toY(r) + 5, null);
	}
}