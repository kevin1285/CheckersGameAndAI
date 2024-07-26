import java.io.IOException;
import javax.swing.JFrame;

public class Driver{
	public static void main(String[] args) throws IOException {
		JFrame frame = new JFrame();
		frame.setSize(1108, 900);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		ColorPanel panel = new ColorPanel();
		frame.getContentPane().add(panel);
		frame.setVisible(true);
	}
}