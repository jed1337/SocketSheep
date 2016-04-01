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
   private String clientName;

   public MyPanel(String filePath) throws IOException {
      this.sheep        = ImageIO.read(new File(filePath));
      this.nCoordinates = new int[0];
      this.clientName   = "No Name";
      
      this.setBackground(Color.WHITE);
   }
   
   public void setClientName(String name){
      this.clientName = name;
   }
   
   public void updateCoordinates(int[] nCoordinates){
      this.nCoordinates = nCoordinates;
      this.repaint();
   }

   @Override
   public void paintComponent(Graphics g){
      super.paintComponent(g);

      for (int i = 0; i < nCoordinates.length; i += 2) {
         int x = nCoordinates[i];
         int y = nCoordinates[i + 1];
         
         g.drawImage(sheep,x, y , this);
         g.drawString(clientName, x, y);
      }
   }
}
