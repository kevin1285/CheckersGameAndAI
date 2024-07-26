import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Clock {
	private JLabel label;
	private final int startMs;
	private int msLeft;
	private String minStr, secStr;
	private Timer timer; 
	private Board board;
	private final int x, y;
	public void setBoard(Board b) {
		board = b;
	}
	public String getMinSecStr() {
		return minStr + ":" + secStr;
	}

	public int getMsLeft() {
		return msLeft;
	}
	public void setMsLeft(int milisecsLeft) {
		this.msLeft = milisecsLeft;
	}
	public int getStartMs() {
		return startMs;
	}
	public JLabel getLabel() {
		return label;
	}
	public void setLabel(JLabel label) {
		this.label = label;
	}
	public Timer getTimer() {
		return timer;
	}
	public void setTimer(Timer timer) {
		this.timer = timer;
	}
	
	public Clock(int startTime, int xc, int yc) {
		x = xc; y = yc;
		startMs = startTime;
		msLeft = startTime;
	}
	public void prepareClock() {
		calculateMinSec();
		setupClock();
		setUpLabel(x, y);
	}
	
	
	private void setUpLabel(int x, int y) {//graphics of clock
		label = new JLabel();
		label.setLocation(x, y);
		label.setSize(150, 100);
		label.setFont(new Font("Verdana", Font.PLAIN, 35));
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setVerticalAlignment(SwingConstants.CENTER);
		label.setBorder(BorderFactory.createBevelBorder(1));
		label.setOpaque(true);
		label.setText(getMinSecStr());
	}
	private void setupClock() {
		//updates clock every 10th of a second
		timer = new Timer(100, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(msLeft <= 0) {//player runs out of time
					timer.stop();
					board.lostByTime();
				}
				msLeft -= 100;
				calculateMinSec();
				label.setText(getMinSecStr());
			}
		});
	}
	public void calculateMinSec() { //converts miliseconds left to minutes:seconds left
		int sec= (int)Math.ceil(msLeft/1000.0);
		int min = sec/60;
		sec %= 60;
		
		minStr = String.format("%02d", min);
		secStr = String.format("%02d", sec);
	}
}