import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;

public class TakebackArrow{ //allow user to click to takeback move if they wish
	BufferedImage backArrowImg;
	int x, y;
	int w, h;

	public TakebackArrow(String fileName, int xc, int yc) throws IOException {
		backArrowImg = ImageIO.read(new File(fileName));
		x = xc;
		y = yc;
		w = backArrowImg.getWidth();
		h = backArrowImg.getHeight();
	}

	public void draw(Graphics g) {
		g.setColor(Color.WHITE);
		g.fillRect(x, y, w, h);
		g.drawImage(backArrowImg, x, y, null);
	}

	public boolean isTouching(int mouseX, int mouseY) {
		return (mouseX >= x && mouseX <= x + w) && (mouseY >= y && mouseY <= y + h);
	}
}