import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import javax.sound.sampled.*;
import javax.swing.Timer;
public class Board{
	private Piece[][] G = new Piece[8][8];
	private static final int squareSize = 108, startX = 0, endX = startX+108*8, startY = 0;
	private final Color emptySquareColor= new Color(212, 204, 196), pieceSquareColor = new Color(92, 64, 51);
	private Stack<Move> movesMade = new Stack<>();
	private Stack<Integer> movesWithCaptureOrPromotion = new Stack<>();
	private final boolean forcedJump;
	private Map<String, Integer> positionFreq = new HashMap<>();
	private ColorPanel panel;
	private String curPosition; 
	private boolean foundMove;
	private Stack<Integer> msLeftBeforeMoveStk;
	private Piece selectedPiece;
	private Player player1, player2;
	private AIPlayer AI;
	private int turn;
	private final int firstTurn;
	private BufferedImage[][] pieceImgs;
	private Clip movePieceAudio, takePieceAudio;
	private int AIDepth;
	
	public Stack<Move> getMovesMade(){
		return movesMade;
	}
	public static int getEndX() {
		return endX;
	}
	public Player getPlayer1() {
		return player1;
	}
	public Player getPlayer2() {
		return player2;
	}

	public int getTurn() {
		return turn;
	}
	public Piece getSelectedPiece() {
		return selectedPiece;
	}
	public void setSelectedPiece(Piece selectedPiece) {
		this.selectedPiece = selectedPiece;
	}

	public ColorPanel getPanel() {
		return panel;
	}

	public static int getSquaresize() {
		return squareSize;
	}
	public static int getSquareSize() {
		return squareSize;
	}
	public Piece[][] getG() {
		return G;
	}

	public Board(int ft, boolean fj, ColorPanel p, BufferedImage[][] pImgs, Clock[] C, boolean playWithAI, int AIDifficulty, int playstyle) throws Exception {
		forcedJump = fj;
		player1 = new Player();
		player2 = new Player();
		turn = firstTurn = ft;
		panel = p;
		pieceImgs = pImgs;
		setUpAudio();
		if(C != null) {//set up clocks if needed
			player1.setClock(C[0]);
			player2.setClock(C[1]);
			msLeftBeforeMoveStk = new Stack<>();
			msLeftBeforeMoveStk.push(C[0].getMsLeft());
		}
		resetG();
		populateLegalSquares(turn);
		if(playWithAI) { //sets AI depth based on difficulty (easy mode doesnt use minimax)
			if(AIDifficulty == 2)
				AIDepth = 2;
			else if(AIDifficulty == 3)
				AIDepth = 8;
			else if(AIDifficulty == 4)
				AIDepth = 10;
			AI = new AIPlayer(this, AIDepth, playstyle);
		}
		//store initial position 
		curPosition = toString(); 
		positionFreq.put(curPosition, positionFreq.getOrDefault(curPosition, 0) + 1);
	}
	private void setUpAudio() throws UnsupportedAudioFileException, IOException, LineUnavailableException {//get audios for moving and taking piece
		AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File("movepiece.wav"));
		movePieceAudio = AudioSystem.getClip();
		movePieceAudio.open(audioStream);
		
		audioStream = AudioSystem.getAudioInputStream(new File("takepiece.wav"));
		takePieceAudio = AudioSystem.getClip();
		takePieceAudio.open(audioStream);
	}
	private void playAudio(Clip clip) {
		clip.setMicrosecondPosition(0);
		clip.start();
	}
	private void resetG() {//resets board to starting state
		for(int r=0; r<3; r++) {
			resetRow(r, 2, pieceImgs);
			resetRow(r+5, 1, pieceImgs);
		}
	}
	private void resetRow(int r, int player, BufferedImage[][] pieceImgs) {//populates indicated row with initial pieces
		int imgIdx = firstTurn == 1 ? player-1 : 2-player;
		for(int c=(r+1)%2; c<8; c+=2)
			G[r][c] = new Pawn(r, c, player, this, pieceImgs[imgIdx]);
	}
	public void drawBoard(Graphics g){
		//draw each sqaure and piece
		for(int r=0; r<8; r++) {
			for(int c=0; c<8; c++) {
				drawSquare(g, r, c);
				if(G[r][c] != null) 
					G[r][c].drawPiece(g);
			}
		}
		if(selectedPiece != null) {
			//indicate square of selected piece and available moves
			outlineSquareOfSelectedPiece(g);
			highLightLegalSquares(g);
		}
	}
	public void AIMove() throws Exception {
		executeMove(AI.calculateMove(), false);
	}
	private void drawSquare(Graphics g, int r, int c) {//draw square with appropriate colro and black outline
		g.setColor((r+c)%2==0 ? emptySquareColor : pieceSquareColor);	
		g.fillRect(toX(c), toY(r), squareSize, squareSize);
		g.setColor(Color.black);
		g.drawRect(toX(c), toY(r), squareSize, squareSize);
	}
	private void highLightLegalSquares(Graphics g) {//highlight legal squares of selected piece in green
		g.setColor(Color.green);	
		for(String coord : selectedPiece.getLegalSquares().keySet()) {
			int r = coord.charAt(0)-'0', c = coord.charAt(1)-'0';
			g.fillRect(toX(c), toY(r), squareSize, squareSize);
			if(selectedPiece.getR() == r && selectedPiece.getC() == c) //edge case: piece jumps back to its own square, need to redraw piece
				G[r][c].drawPiece(g);
		}
	}
	private void outlineSquareOfSelectedPiece(Graphics g) {//indicate square of selected piece 
		g.setColor(Color.white);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setStroke(new BasicStroke());
		g.drawRect(toX(selectedPiece.getC())+2, toY(selectedPiece.getR())+2, squareSize-4, squareSize-4);
		g2d.setStroke(new BasicStroke(1));
	}

	public void nextTurn() throws Exception {
		turn = turn==1 ? 2 : 1;
		if(player1.getClock() != null) {//stop timer of player whose move it just was, start the other
			if(turn == 1) {
				player1.getClock().getTimer().start();
				player2.getClock().getTimer().stop();
				msLeftBeforeMoveStk.push(player1.getClock().getMsLeft());
			}else {
				player2.getClock().getTimer().start();
				player1.getClock().getTimer().stop();
				msLeftBeforeMoveStk.push(player2.getClock().getMsLeft());
			}
		}
		populateLegalSquares(turn); //find legal squares for current turn
		//checks if game over, if so, call gameOver() in colorpanel and stop clock
		int drawStatus = draw();
		if (drawStatus != 0) { 
			if(player1.getClock() != null)
				(turn==1 ? player1 : player2).getClock().getTimer().stop();
			panel.gameOver(drawStatus == 1 ? "DRAW BY REPITION" : "DRAW BY NO C/P");
			return;
		} else if (lostByPosition()) {
			if(player1.getClock() != null)
				(turn==1 ? player1 : player2).getClock().getTimer().stop();
			panel.gameOver("PLAYER " + (3 - turn) + " HAS WON");
			return;
		}
		
		if (turn == 2 && AI != null) {//move for AI
			if(AIDepth >=8)
				AIMove();
			else {
				Timer timer = new Timer(400, new ActionListener() {//AI with low depths move almost instantaneously, so add slight delay
			        public void actionPerformed(ActionEvent e) {
			        	try {
							AIMove();
						} catch (Exception e1) {
							e1.printStackTrace();
						}
			        }
			    });
			    timer.setRepeats(false);
			    timer.start();
			}
		}
	}
	

	public void executeMove(Move move, boolean isAICalculation) throws Exception {
		if(!isAICalculation) {
			movePieceAudio.stop();
			takePieceAudio.stop();
		}
		if(move.getJumpPath() == null || move.getJumpPath().getIntermediateSquares() == null || isAICalculation) {//move piece from start square to final square
			movePiece(G[move.getFromR()][move.getFromC()], move.getToR(), move.getToC());
			if(move.getJumpPath() != null) {
				removeTakenPieces(move.getJumpPath().getTakes());
			}
			if(!isAICalculation) {
				playAudio(move.getJumpPath() == null ? movePieceAudio : takePieceAudio);
				panel.paintImmediately(panel.bounds()); 
			}
		}else {//when move has 2+ jumps, add a pause as it jumps one square at a time
			int fromR = move.getFromR(), fromC = move.getFromC();
			String coord = move.getJumpPath().getIntermediateSquares().get(0);
			int toR = coord.charAt(0) - '0', toC = coord.charAt(1) - '0';
			for (int i = 0; i < move.getJumpPath().getIntermediateSquares().size(); i++) {
				movePiece(G[fromR][fromC], toR, toC);
				decrementPieceAfterTake(move.getJumpPath().getTakes().get(i));
				
				G[move.getJumpPath().getTakes().get(i).getR()][move.getJumpPath().getTakes().get(i).getC()] = null; //set taken piece to null
				fromR = toR;
				fromC = toC;

				if (i + 1 < move.getJumpPath().getIntermediateSquares().size()) {
					coord = move.getJumpPath().getIntermediateSquares().get(i + 1);
					toR = coord.charAt(0) - '0';
					toC = coord.charAt(1) - '0';
				} else {
					toR = move.getToR();
					toC = move.getToC();
				}
				
				playAudio(takePieceAudio);
				panel.paintImmediately(panel.bounds());
				try {
					Thread.sleep(600);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if(!isAICalculation)
					takePieceAudio.stop();
			}
			//move to final square
			movePiece(G[fromR][fromC], toR, toC); 
			int lastTakeIdx = move.getJumpPath().getTakes().size() - 1;
			decrementPieceAfterTake(move.getJumpPath().getTakes().get(lastTakeIdx));
			G[move.getJumpPath().getTakes().get(lastTakeIdx).getR()][move.getJumpPath().getTakes().get(lastTakeIdx).getC()] = null;
			
			playAudio(takePieceAudio);
			
		}
		movesMade.push(move);
		if(move.isPromoted()) {//udpate piece counts of player when promoted
			if(G[move.getToR()][move.getToC()].getPlayer() == 1) {
				player1.decrementPawns();
				player1.incrementKings();
			}else {
				player2.decrementPawns();
				player2.incrementKings();
			}	
		}
		if(move.madeCapture() || move.isPromoted()) //adds the num of the current move to movesWithCaptureOrPromotion
			movesWithCaptureOrPromotion.push(movesMade.size());
		curPosition = toString(); 
		positionFreq.put(curPosition, positionFreq.getOrDefault(curPosition, 0) + 1);
		if(!isAICalculation) { //redraw screen and move on to next turn if not when AI's minimax is running
			panel.paintImmediately(panel.bounds());
			nextTurn();	
		}
	}
	
	private void decrementPieceAfterTake(Piece taken) {//decrement piece counts of player after take
		if(taken instanceof Pawn)
			(taken.getPlayer()==1 ? player1 : player2).decrementPawns();
		else
			(taken.getPlayer()==1 ? player1 : player2).decrementKings();
	}
	
	private void movePiece(Piece piece, int toR, int toC) {//moves piece to new square, setting taken piece to null and promoting if necessary
		Piece temp = piece;
		G[piece.getR()][piece.getC()] = null;
		temp.setCoords(toR, toC);
		G[toR][toC] = temp;
		if(Piece.onBackRank(temp.getR(), temp.getPlayer()) && temp instanceof Pawn) 
			temp.promote();
	}
	private void removeTakenPieces(List<Piece> taken) {//set pieces that are taken during move to null
		Player playerTaken = taken.get(0).getPlayer()==1 ? player1 : player2;
		for(Piece take : taken) {
			G[take.getR()][take.getC()] = null;
			if(take instanceof Pawn)
				playerTaken.decrementPawns();
			else
				playerTaken.decrementKings();
		}
		
	}
	
	public void takeback(boolean isAICalculation) throws Exception {
		undoMove();
		boolean afterAIMoves = !isAICalculation && AI != null && turn == 1 && !movesMade.isEmpty();
		if(afterAIMoves) //if it's AI v player and it's player's turn, AI's mvoe is taken back also
			undoMove();

		if(player1.getClock() != null) {//replenish the lost time
			int msLeftBeforeMove = msLeftBeforeMoveStk.pop();
			if(turn == 1) {
				player1.getClock().setMsLeft(msLeftBeforeMove);
				player2.getClock().setMsLeft(msLeftBeforeMoveStk.pop());
			}else {
				player2.getClock().setMsLeft(msLeftBeforeMove);
				player1.getClock().setMsLeft(msLeftBeforeMoveStk.pop());
			}
			player1.getClock().calculateMinSec();
			player2.getClock().calculateMinSec();
			player1.getClock().getLabel().setText(player1.getClock().getMinSecStr());
			player2.getClock().getLabel().setText(player2.getClock().getMinSecStr());
		}
		if(!isAICalculation) {
			panel.repaint();
			if(afterAIMoves)
				populateLegalSquares(turn);
			else 
				nextTurn();
		}
	}
	private void undoMove() {
		String pos = toString();
		positionFreq.put(pos, positionFreq.get(pos) - 1); //decrement number of times that current postion was visited
		Move move = movesMade.pop();
		if(move.getJumpPath() != null)
			replenishTakenPieces(move.getJumpPath().getTakes()); //recover lost pieces
		movePiece(G[move.getToR()][move.getToC()], move.getFromR(), move.getFromC()); //move piece back to square it came from
		if(move.isPromoted()) { //demote piece if promoted in move
			G[move.getFromR()][move.getFromC()].demote();
			if(G[move.getFromR()][move.getFromC()].getPlayer() == 1) {
				player1.incrementPawns();
				player1.decrementKings();
			}else {
				player2.incrementPawns();
				player2.decrementKings();
			}
		}
		if(!movesWithCaptureOrPromotion.isEmpty() && (move.madeCapture() || move.isPromoted()) && movesMade.size()+1 == movesWithCaptureOrPromotion.peek())
			movesWithCaptureOrPromotion.pop();//if current move is promotion/capture, pop it from movesWithCaptureOrPromotion
	}
	private void replenishTakenPieces(List<Piece> taken) { //recover taken pieces of a specific player
		if(taken == null)
			return;
		Player playerTaken = taken.get(0).getPlayer()==1 ? player1 : player2;
		for(Piece piece : taken) {
			G[piece.getR()][piece.getC()] = piece.copy();
			if(piece instanceof Pawn)
				playerTaken.incrementPawns();
			else
				playerTaken.incrementKings();
		}
		
		
	}
	
	public void populateLegalSquares(int player) {
		foundMove = false;
		int maxJumps = 0;
		for(int r=0; r<8; r++) {//populate jump squares first
			for(int c=0; c<8; c++) {
				if(isValidPiece(G[r][c], player)) {
					G[r][c].getLegalSquares().clear();
					G[r][c].addJumpSquares();
					if(!G[r][c].getLegalSquares().isEmpty())
						foundMove = true;
					maxJumps = Math.max(maxJumps, G[r][c].getMaxPieceJumps());
				}
			}
		}
		//remove jumps with shorter jump lengths when forcedJump and foundJump are true
		if(forcedJump && foundMove) {
			for(int r=0; r<8; r++) {
				for(int c=0; c<8; c++) {
					if(isValidPiece(G[r][c], player)) 
						G[r][c].removeSquares(maxJumps);
				}
			}
		}else {//otherwise, populate legal squares with adj squares
			for(int r=0; r<8; r++) {
				for(int c=0; c<8; c++) {
					if(isValidPiece(G[r][c], player)) {
						G[r][c].addAdjSquares();
						if(!G[r][c].getLegalSquares().isEmpty())
							foundMove = true;
					}
				}
			}
		}
	}
	
	public boolean lostByPosition() {//checks if current player lost by position (meaning not time)
		if(!foundMove)
			return true; //no move -> player whose turn it is loses
		return false; //no one has won
	}
	public int draw() {
		//draw when 3 repeated positions or 80 moves (move as in each player's turn) without capture
		if(positionFreq.get(curPosition) == 3)
			return 1;
		else if(movesMade.size() - (movesWithCaptureOrPromotion.isEmpty() ? 0 : movesWithCaptureOrPromotion.peek()) >= 80)
			return 2;
		return 0;
	}
	
	public void lostByTime() {//if player runs outta time, other player wins
		panel.gameOver("PLAYER " + (3-turn) + " HAS WON");
	}
	public static int toY(int r) {
		return r*squareSize + startY;
	}
	public static int toX(int c) {
		return c*squareSize + startX;
	}
	public static int toR(int y) {
		if(y<startY)
			return -1;
		return (y-startY)/squareSize;
	}
	public static int toC(int x) {
		if(x<startX)
			return -1;
		return (x-startX)/squareSize;
	}
	
	public static boolean inBounds(int r, int c) { //check if square (r,c) is inside grid
		return r>=0 && r<8 && c>=0 && c<8;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(Piece[] Ps : G) {
			for(Piece p : Ps) {
				sb.append(p==null ? "_" : p.toString());			
			}
		}
		return sb.toString();
	}
	public static boolean isValidPiece(Piece piece, int player) {
		return piece != null && piece.getPlayer() == player;
	}	
}
