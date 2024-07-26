import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;


public class PiecesTakenDisplay {//displays pieces taken of both players on right of srceen
	private BufferedImage img;
	private int x = Board.getEndX() + 64, y;
	public PiecesTakenDisplay(BufferedImage pieceImg, int yc) throws IOException { 
		img = pieceImg;
		y = yc;
	}
	
	public void draw(Graphics g, int numPiecesTaken) {
		g.setColor(Color.WHITE);
		g.drawImage(img, x, y, null);
		g.setFont(new Font("Arial", Font.ROMAN_BASELINE, img.getWidth()*2/3));
		g.drawString(numPiecesTaken+"", numPiecesTaken >= 10 ? x+12 : x+32, y+66);
	}
}