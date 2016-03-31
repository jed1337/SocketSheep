package socketsheepexample2client;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class MyPanel extends JPanel{
   private final BufferedImage sheep;
   private int[] nCoordinates;

   public MyPanel(String filePath) throws IOException {
      this.sheep        = ImageIO.read(new File("src\\images\\Sheep.jpg"));
      this.nCoordinates = new int[0];
      
      this.setBackground(Color.WHITE);
   }
   
   public void updateCoordinates(int[] nCoordinates){
      this.nCoordinates = nCoordinates;
      this.repaint();
   }

   @Override
   public void paintComponent(Graphics g){
      super.paintComponent(g);

      for (int i = 0; i < nCoordinates.length; i += 2) {
         g.drawImage(sheep, nCoordinates[i], nCoordinates[i + 1], this);
      }
   }
}
