import java.awt.*;
import java.io.*;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.plaf.basic.BasicButtonUI;

import java.awt.event.*;
import java.awt.image.BufferedImage;

public class ColorPanel extends JPanel{
	
	private Board board;
	private TitleScreen title = new TitleScreen();;
	private OptionScreen options;
	private boolean drawTitle = true, drawOptions;
	private boolean playerVsAI, forcedJump, takebacks;
	private boolean gameOver;
	private int firstTurn=1, startTime=-1, AIDifficulty=1, AIPlaystyle = -2;
	private TakebackArrow backArrow;
	private PiecesTakenDisplay takenDisplay1, takenDisplay2;
	private GameOverPopUp gg;
	public static final int CX = 540; //x coord of center
	private final BufferedImage[][] pieceImgs = new BufferedImage[2][2];
	public Board getBoard() {
		return board;
	}
	private void resetVariables() {//resets necessary variables for "New Game"
		board = null;
		title = new TitleScreen();
		options = null;
		drawTitle = true; drawOptions = false;
		playerVsAI = false; forcedJump = false; takebacks = false;
		gameOver = false;
		firstTurn=1; startTime=-1; AIDifficulty=1; AIPlaystyle = -2;
		backArrow = null;
		takenDisplay1 = null; takenDisplay2 = null;
		gg = null;
	}
	public ColorPanel() throws IOException {
		readImages();
		setLayout(null);
		setBackground(new Color(153, 102, 51));
	}
	private void setUpGame() throws Exception {
		forcedJump = options.getForcedJumpToggle().isOn();
		takebacks = options.getTakebackToggle().isOn();
		addMouseListener(new GameMouseListener());
		if(takebacks)
			backArrow = new TakebackArrow("backarrow.png", Board.getEndX()+68, 396);
		takenDisplay1 = new PiecesTakenDisplay(pieceImgs[2-firstTurn][0], 660);
		takenDisplay2 = new PiecesTakenDisplay(pieceImgs[firstTurn-1][0], 100);
		if(playerVsAI) { //with AI
			board = new Board(firstTurn, forcedJump, this, pieceImgs, null, true, AIDifficulty, AIPlaystyle);
			if(firstTurn == 2) {
				Timer timer = new Timer(100, new ActionListener() {//tiny pause before AI executes first move
			        public void actionPerformed(ActionEvent e) {
			        	try {
							board.AIMove();
						} catch (Exception e1) {
							e1.printStackTrace();
						}
			            repaint();
			        }
			    });
			    timer.setRepeats(false);
			    timer.start();
			}
		}else { //without AI
			if(startTime != -1) {//with clock
				int clockX = Board.getEndX() + 40;
				Clock[] clocks = new Clock[]{new Clock(startTime*60000, clockX, 760), new Clock(startTime*60000, clockX, 0)};
				board = new Board(firstTurn, forcedJump, this, pieceImgs, clocks, false, -1, -1);
				for(Clock clock : clocks) {
					clock.prepareClock(); 
					add(clock.getLabel());
					clock.setBoard(board);
				}
			}else {//without clock
				board = new Board(firstTurn, forcedJump, this, pieceImgs, null, false, -1, -1);
			}
		}	
	}
	private void readImages() throws IOException {
		pieceImgs[0][0] = ImageIO.read(new File("blackpawn.png"));
		pieceImgs[0][1] = ImageIO.read(new File("blackking.png"));
		pieceImgs[1][0] = ImageIO.read(new File("redpawn.png"));
		pieceImgs[1][1] = ImageIO.read(new File("redking.png"));
	}
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if(drawTitle) {
			title.draw(g);
		}else if(drawOptions){
			options.draw(g);
		}else if(gameOver) {
			updateBoard(g);		
			gg.draw(g);
			add(gg.getCloseButton());
			add(gg.getPlayAgainButton());
			add(gg.getNewGameButton());
		}else {
			updateBoard(g);
		}
	}
	private void updateBoard(Graphics g) {//updates board and number of pieces taken labels on side of screen
		board.drawBoard(g); 
		takenDisplay1.draw(g, board.getPlayer2().numPiecesTaken());
		takenDisplay2.draw(g, board.getPlayer1().numPiecesTaken());
		if(takebacks)
			backArrow.draw(g);
	}
	
	public void gameOver(String result) {//shows popup that states the result of the game and allows the user to choose to play again, play a new game, or view board
		removeAllMouseListeners();
		gg = new GameOverPopUp(result);
		gameOver=true;
		repaint();
		gg.getCloseButton().addActionListener(e->{
			remove(gg.getPlayAgainButton());
			remove(gg.getCloseButton());
			remove(gg.getNewGameButton());
			gameOver=false;
			repaint();
		});
		gg.getPlayAgainButton().addActionListener(e->{
			gameOver=false;
			resetGame();
		});
		gg.getNewGameButton().addActionListener(e->{
			gameOver=false;
			removeEverything();
			repaint();
			resetVariables();
		});
	
	
	}
	private void removeAllMouseListeners() {
		for(MouseListener ml : getMouseListeners()) {
		    removeMouseListener(ml);
		}
	}
	private void removeEverything() {
	    removeAll();
	    revalidate();
	}
	public void resetGame() {
		removeEverything();
	    try {
	        setUpGame();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    repaint();
	}
	//note: all these classes are private nested classes of ColorPanel- this makes it easier to assign values to all the vairables related to user option in ColorPanel
	private class OptionScreen{ //gives users many options after they select whether they want to play with AI
		private ToggleOptionButton forcedJumpToggle = new ToggleOptionButton(CX-190, 200, "Forced Jump"), takebackToggle = new ToggleOptionButton(CX-190, 310, "Takebacks");
		private JButton startGameButton = new JButton();
		private FirstTurnClicker firstTurnClicker = new FirstTurnClicker();
		TriangleChooser timeChooser, AIDiffChooser, AIPSChooser;
		public ToggleOptionButton getForcedJumpToggle() {
			return forcedJumpToggle;
		}
		public ToggleOptionButton getTakebackToggle() {
			return takebackToggle;
		}
		public OptionScreen() {
			if(playerVsAI) {
				AIDiffChooser = new AIDifficultyChooser(CX-100, 470);
				AIPSChooser = new AIPlaystyleChooser(CX-100, 620);
			}else
				timeChooser = new TimeChooser(CX-100, 550);
			add(forcedJumpToggle.getButton());
			add(takebackToggle.getButton());
	        setUpStartGameButton();
		}
		
		private void draw(Graphics g) {
			firstTurnClicker.draw(g);
			if(playerVsAI) {
				AIDiffChooser.draw(g);
				AIPSChooser.draw(g);
			}else
				timeChooser.draw(g);
		}
		private class ToggleOptionButton{//alterantes between on/off when clicked by user, is used for forcedJump and takebakcs
			JToggleButton button = new JToggleButton();
			boolean on = false;
			
			public JToggleButton getButton() {
				return button;
			}
			public boolean isOn() {
				return on;
			}

			public ToggleOptionButton(int x, int y, String str){
				button.setUI(new BasicButtonUI());
				button.setSelected(false);
				button.setBounds(x, y, 380, 90);
				button.setFont(new Font("Verdana", Font.BOLD, 30));
				button.setText(str + ": OFF");
				button.addActionListener(new ActionListener() {
		            public void actionPerformed(ActionEvent e) {
		                // Update forcedJump based on toggle state
		                on = button.isSelected();
		                if(on) {
		                	button.setText(str + ": ON");
		                }else {
		                	button.setText(str + ": OFF");
		                }
		            }
		        });
			}
		}
	
		private void setUpStartGameButton() { //starts game when clicked
			startGameButton = new JButton("Start game");
			startGameButton.setFont(new Font("Verdana", Font.BOLD, 40));
	        startGameButton.setBounds(CX-150, 750, 300, 100);
	        startGameButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {  	
                	removeAll();
            	    revalidate();
                	drawOptions = false;
                	try {
                		removeAllMouseListeners();
						setUpGame();
					} catch (Exception e1) {
						e1.printStackTrace();
					}
                    repaint(); 
                }
            });
	        add(startGameButton);
		}
		private abstract class TriangleChooser{ //uses triangles on left and right of a rectangle to go through an array of options
			protected final int x, y;
			protected int[] options;
			protected JLabel label;
			protected int curIdx = 0;
			protected Polygon backTriangle, nextTriangle;
			protected boolean clickedBack, clickedNext; 
			protected String message;
			public TriangleChooser(int xc, int yc, String msg, int[] optionsA) {
				x = xc; y = yc;
				message = msg;
				options = optionsA;
				label = new JLabel(toText(options[0]));
				label.setBounds(x, y, 200, 100);
				label.setBackground(Color.white);
				label.setOpaque(true);
				label.setHorizontalAlignment(SwingConstants.CENTER);
				label.setVerticalAlignment(SwingConstants.CENTER);
		        label.setFont(new Font("Arial", Font.BOLD, 30));
				add(label);
				
				
				backTriangle = makeTriangle(x, y, false);
				nextTriangle = makeTriangle(x+200, y, true);
				addMouseListener(new MouseAdapter() {
					public void mousePressed(MouseEvent e) {
						int mouseX = e.getX(),  mouseY = e.getY();

				        if (backTriangle.contains(mouseX, mouseY)) {
				            updateChoice(-1);
				            flash(false);
				        } else if (nextTriangle.contains(mouseX, mouseY)) {
				            updateChoice(1);
				            flash(true);
				        }
				    }
				});
			}
			protected void flash(boolean next) { //triangle flashes when clicked
				if (next)
			        clickedNext = true;
			    else
			        clickedBack = true;
			    repaint();
				Timer timer = new Timer(100, new ActionListener() {
			        @Override
			        public void actionPerformed(ActionEvent e) {
			            if (next)
			                clickedNext = false;
			            else
			                clickedBack = false;
			            repaint();
			        }
			    });
			    timer.setRepeats(false);
			    timer.start();
			}
			protected Polygon makeTriangle(int x, int y, boolean faceRight) {
				final int side = 100;
				int[] xPoints = {x, x + (faceRight ? side : -side), x};
				int[] yPoints = {y, y + side/2, y + side};
		        return new Polygon(xPoints, yPoints, 3);
		    }
			protected void draw(Graphics g) {
				g.setColor(Color.BLACK);
				g.setFont(new Font("Verdana", Font.ITALIC, 36));
				g.drawString(message, CX-g.getFontMetrics().stringWidth(message)/2, y-10);
				
				g.setColor(clickedBack ? Color.GRAY: Color.BLACK);
				g.fillPolygon(backTriangle);
				g.setColor(clickedNext ? Color.GRAY: Color.BLACK);
				g.fillPolygon(nextTriangle);
			}
			protected void shift(int dir) {//updates curIdx of array
		        curIdx += dir;
		        if (curIdx < 0) {
		            curIdx = options.length - 1;
		        } else if (curIdx >= options.length) {
		            curIdx = 0;
		        }
		    }
			protected abstract void updateChoice(int dir);
			protected abstract String toText(int n);
		}
		//children of TriagleChooser:
		private class TimeChooser extends TriangleChooser{
			public TimeChooser(int x, int y) {
				super(x, y, "Time control:", new int[]{-1, 1, 3, 5, 10});
			}

			@Override
			protected String toText(int time) {
				if(time == -1)
					return "NO CLOCK";
				return String.format("%02d", time) + ":00";
			}

			@Override
			protected void updateChoice(int dir) {
				shift(dir);
				startTime = options[curIdx];
		        label.setText(toText(startTime));
			}
		}
		private class AIDifficultyChooser extends TriangleChooser{
			public AIDifficultyChooser(int x, int y) {
				super(x, y, "Difficulty:", new int[]{1, 2, 3, 4});
			}
			@Override
			protected void updateChoice(int dir) {
				shift(dir);
				AIDifficulty = options[curIdx];
				label.setText(toText(AIDifficulty));
			}
			@Override
			protected String toText(int difficulty) {
				switch(difficulty) {
					case 1: return "EASY";
					case 2: return "MEDIUM";
					case 3: return "HARD";
					default: return "IMPOSSIBLE";
				}
			}
			
		}
		private class AIPlaystyleChooser extends TriangleChooser{
			public AIPlaystyleChooser(int x, int y) {
				super(x, y, "Playstyle:", new int[]{0, -1, 1});
			}
			@Override
			protected void updateChoice(int dir) {
				shift(dir);
				AIPlaystyle = options[curIdx];
				label.setText(toText(AIPlaystyle));
			}
			@Override
			protected String toText(int style) {
				switch(style) {
					case 0: return "NORMAL";
					case -1: return "PASSIVE";
					default: return "AGGRESSIVE";
				}
			}
			
		}
		
		private class FirstTurnClicker{//choose whether to go first (play with black) or go second (play with red)
			private final BufferedImage bp=pieceImgs[0][0], rp=pieceImgs[1][0];
			private final int r = bp.getWidth()/2, x = CX-r, y=70; 
			public FirstTurnClicker() {
				addMouseListener(new MouseAdapter() {
					public void mousePressed(MouseEvent e) {
				        double dist = Math.sqrt(Math.pow(e.getX() - (x+r), 2) + Math.pow(e.getY() - (y+r), 2));
				        if (dist <= r) {
				           firstTurn = 3 - firstTurn;
				           repaint();
				        }
				    }
				});
			}
			private void draw(Graphics g) {
				g.setFont(new Font("Verdana", Font.BOLD, 30));
				g.setColor(Color.BLACK);
				String s = "I want to play with:";
				g.drawString(s, CX-g.getFontMetrics().stringWidth(s)/2, y-10);
				g.drawImage(firstTurn==1 ? bp : rp, x, y, null);
			}
			
		}
	}
	
	private class TitleScreen {//titiel screen that displays "CHECKERS" and allows uer to pick bewteen pvp or pvAI
		private PlayButton playerButton, AIButton;
		public TitleScreen() {
			final int buttonX = (CX - 200);
			playerButton = new PlayButton(buttonX, 500, true);
			add(playerButton.getButton());
			AIButton = new PlayButton(buttonX, 700, false);
			add(AIButton.getButton());
		}
	    private void draw(Graphics g) {
			g.setFont(new Font("Comic Sans MS", Font.BOLD, 150));
			g.setColor(Color.RED);
			FontMetrics fontMetrics = g.getFontMetrics();

            int textW = fontMetrics.stringWidth("CHECKERS");
			g.drawString("CHECKERS", CX-textW/2, 200);
	    }
	    
		private class PlayButton{
	    	JButton button;
	    	
	    	public JButton getButton() {
				return button;
			}

			public PlayButton(int x, int y, boolean AI) {//for having the user choose whcih mode they want to play in: pvp or pvAI
		    	button = new JButton(AI? "Player vs AI" : "Player vs Player");
				button.setFont(new Font("Verdana", Font.BOLD, 40));
				button.setBounds(x, y, 500, 100);
				
				button.setIcon(new Icon() {//add triangle icon on button
					private final int size = 30;
					public void paintIcon(Component c, Graphics g, int x, int y) {
						int[] Xs = {x, x+size, x}, Ys = {y, y+size/2, y+size};
						g.setColor(Color.BLACK);;
						g.fillPolygon(Xs, Ys, 3);
					}
		
					public int getIconWidth() {
						return size;
					}
					public int getIconHeight() {
						return size;
					}	
				});
				
				button.addActionListener(new ActionListener() {
	                public void actionPerformed(ActionEvent e) {  	
	                	removeAll();
	            	    revalidate();
	                	playerVsAI = AI;
	                	options = new OptionScreen();
	                	drawTitle = false;
	                	drawOptions = true;
	                    repaint(); 
	                }
	            });
	    	}
	     }
	}
	
	
	private class GameMouseListener extends MouseAdapter{//mouse input for piece moves and takebacks
		public void mousePressed(MouseEvent e) {
			if(playerVsAI && board.getTurn()==2) //don't allow mouse presses when it's AI;s turn
				return;
			if(takebacks && backArrow.isTouching(e.getX(), e.getY())) {//takeback
				if(board.getMovesMade().size() > 0) {
					try {
						board.takeback(false);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
			int r = Board.toR(e.getY()), c = Board.toC(e.getX());
			if(!Board.inBounds(r, c)) { 
				if(board.getSelectedPiece() != null) { //unselect piece if square is out of bounds
					board.setSelectedPiece(null);
					repaint();
				}
			}		
			else if(board.getSelectedPiece() == null) {//empty square
				selectPieceIfValid(r, c);
			}else{//selected a certain piece
				if(board.getSelectedPiece().getLegalSquares().containsKey(r+""+c)) {//valid move
					try {
						makeMove(r, c);
					} catch (Exception e1) {
						e1.printStackTrace();
					}		
				}else { //if the new piece is the same color as selected piece, change that to the new selected piece, otherwise, set to null
					board.setSelectedPiece(board.getG()[r][c] != null && board.getG()[r][c].getPlayer()==board.getTurn() ? board.getG()[r][c] : null);
					repaint();
				}
			}
		}

		private void selectPieceIfValid(int r, int c) {
			if(Board.isValidPiece(board.getG()[r][c], board.getTurn())) { //unselect piece if already selected
				board.setSelectedPiece(board.getG()[r][c]);
				repaint();
			}
		}
		private void makeMove(int r, int c) throws Exception {	//calls board's executeMove() with a new Move object based on the coordinate selected by the mouse
			Move move = new Move(board.getSelectedPiece().getR(), board.getSelectedPiece().getC(), r, c, board.getSelectedPiece().getLegalSquares().get(r+""+c), board.getSelectedPiece().isPromotedAfterMove(r));
			board.setSelectedPiece(null);
			board.executeMove(move, false);
		}
	}
}