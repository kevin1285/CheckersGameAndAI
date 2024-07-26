import java.util.*;

public class Player{
	private int pawns = 12, kings = 0;
	private Clock clock;
	private List<Piece> pieces;
	public List<Piece> getPieces() {
		return pieces;
	}
	public void setPieces(List<Piece> pieces) {
		this.pieces = pieces;
	}
	public int getPawns() {
		return pawns;
	}
	public void incrementPawns() {
		pawns++;
	}
	public void decrementPawns() {
		pawns--;
	}
	public int getKings() {
		return kings;
	}
	public void setPawns(int pawns) {
		this.pawns = pawns;
	}
	public void setKings(int kings) {
		this.kings = kings;
	}
	public void incrementKings() {
		kings++;
	}
	public void decrementKings() {
		kings--;
	}
	public int getPieceCount() {
		return kings + pawns;
	}
	public Clock getClock() {
		return clock;
	}
	public void setClock(Clock clock) {
		this.clock = clock;
	}

	public int getMaterial() {//calculates combined points of all its pieces on the board- king is worth 3 pts
		return kings*3 + pawns;
	}
	
	public int numPiecesTaken() {
		return 12 - (kings+pawns);
	}
	public void updateCountIfPromotion(boolean isPromoted) {//in a promotion, the pawn count decreases while king count increases
		if(isPromoted) {
			kings++;
			pawns--;
		}
	}
	public void subtractTakenPieces(List<Piece> taken) {//subtract material points of the pieces that were taken
		if(taken != null) {
			for(Piece piece : taken) {
				if(piece instanceof Pawn)
					pawns--;
				else
					kings--;
			}
		}
	}
	
	public String toString() {
		return "k: " + kings + " p: " + pawns;
	}
	
	
	
}