import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.JButton;

public class GameOverPopUp {//popup that appears when game is over
	private final int x = 100, y = 300, w=850, h=400;
	private String message;
	private JButton closeButton = new JButton(), playAgainButton = new JButton(), newGameButton = new JButton();
	
	public JButton getCloseButton() {
		return closeButton;
	}
	public JButton getPlayAgainButton() {
		return playAgainButton;
	}
	public JButton getNewGameButton() {
		return newGameButton;
	}
	public GameOverPopUp(String s) {
		message = s;
		//3 options after game is over: play again, new game, or view board
		int bW = 250, bH = 200, gap = 30, bY = y+(h-bH)-gap;
		playAgainButton.setBounds(x+gap, bY, bW, bH);
		playAgainButton.setText("Play again");
		playAgainButton.setFont(new Font("Verdana", Font.BOLD, 30));
		
		newGameButton.setBounds(playAgainButton.getX()+bW+gap, bY, bW, bH);
		newGameButton.setText("New Game");
		newGameButton.setFont(new Font("Verdana", Font.BOLD, 30));
		
		closeButton.setBounds(newGameButton.getX()+bW+gap, bY, bW, bH);
		closeButton.setText("View Board");
		closeButton.setFont(new Font("Verdana", Font.BOLD, 30));
		
	}

	public void draw(Graphics g) {
		g.setColor(Color.BLACK);
		g.fillRect(x, y, w, h);
		g.setColor(Color.WHITE);
		g.drawRect(x, y, w, h);
		g.setFont(new Font("Verdana", Font.BOLD, 50));
		FontMetrics fontMetrics = g.getFontMetrics();
	    int messageW = fontMetrics.stringWidth(message);
	    
	    // Calculate the x and y coordinates to center the message
	    int centerX = x + (w - messageW) / 2;
		g.drawString(message, centerX, y+80);
	}
	
}