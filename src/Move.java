public class Move implements Comparable<Move>{
	private int toR, toC, fromR, fromC;
	private JumpPath jumpPath;
	boolean promoted;
	
	public boolean isPromoted() {
		return promoted;
	}
	public void setPromoted(boolean promoted) {
		this.promoted = promoted;
	}
	public JumpPath getJumpPath() {
		return jumpPath;
	}
	public void setJumpPath(JumpPath jumpPath) {
		this.jumpPath = jumpPath;
	}
	public int getFromR() {
		return fromR;
	}
	public void setFromR(int fromR) {
		this.fromR = fromR;
	}
	public int getFromC() {
		return fromC;
	}
	public void setFromC(int fromC) {
		this.fromC = fromC;
	}


	public int getToR() {
		return toR;
	}
	public void setToR(int toR) {
		this.toR = toR;
	}
	public int getToC() {
		return toC;
	}
	public void setToC(int toC) {
		this.toC = toC;
	}
	
	public Move(int fR, int fC, int tR, int tC, JumpPath jp, boolean promotedAtEnd) {
		fromR = fR;
		fromC = fC;
		toR = tR;
		toC = tC;
		jumpPath = jp;
		promoted = promotedAtEnd || promotedInMiddleOfJump();
	}
	private boolean promotedInMiddleOfJump() {//returns true if a pawn is promoted and continues to jump in the same move after 
		if(jumpPath == null || jumpPath.getIntermediateSquares() == null)
			return false;
		return jumpPath.isPromotedInJump();
	}
	
	
	public int compareTo(Move o) {//compare two moves based on their score
		return getScore() - o.getScore();
	}
	
	public int getScore() { //calculates score of move based on how long it is and if there is a promotion 
		int score = jumpPath == null ? 0 : jumpPath.countJumps();
		if(promoted)
			score++;
		return score;
	}
	public boolean madeCapture() {
		return jumpPath != null && jumpPath.getTakes() != null && !jumpPath.getTakes().isEmpty();
	}
	
	
}