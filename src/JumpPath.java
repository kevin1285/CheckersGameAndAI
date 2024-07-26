import java.util.*;
public class JumpPath{//stores information about the jump path in move that has 1+ jump
	private List<Piece> takes;
	private List<String> intermediateSquares;
	private boolean promotedInJump;
	
	public List<Piece> getTakes() {
		return takes;
	}
	public void setTakes(List<Piece> takes) {
		this.takes = takes;
	}
	public boolean isPromotedInJump() {
		return promotedInJump;
	}
	public void setPromotedInJump(boolean promotedInJump) {
		this.promotedInJump = promotedInJump;
	}
	public List<String> getIntermediateSquares() {
		return intermediateSquares;
	}
	public void setIntermediateSquares(List<String> intermediateSquares) {
		this.intermediateSquares = intermediateSquares;
	}

	public JumpPath(List<String> interSquares, List<Piece> t, boolean promotedInJump) {
		intermediateSquares = interSquares;
		takes = t;
	}
	
	public int countJumps() { //counts number of jumps used
		int jumps = takes == null ? 0 : 1;
		if(intermediateSquares==null)
			return jumps;
		jumps += intermediateSquares.size();
		return jumps;
	}
	
}