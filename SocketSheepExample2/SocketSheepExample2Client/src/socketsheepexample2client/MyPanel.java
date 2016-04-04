package socketsheepexample2client;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class MyPanel extends JPanel{
   private final BufferedImage sheepImage;
   private int[] nCoordinates;
   private String[] clientNames;

   public MyPanel(String filePath) throws IOException {
      this.sheepImage   = ImageIO.read(new File(filePath));
      this.nCoordinates = new int[0];
      this.clientNames  = new String[0];
      
      this.setBackground(Color.WHITE);
   }
   
   public void updateCoordinates(String[] clientNames, int[] clientCoordinates){
      this.clientNames  = clientNames;
      this.nCoordinates = clientCoordinates;
      this.repaint();
   }

   @Override
   public void paintComponent(Graphics g){
      super.paintComponent(g);

      for(int i=0;i<clientNames.length;i++){
         int x = nCoordinates[(i*2)];
         int y = nCoordinates[(i*2)+1];
         
         g.drawImage(sheepImage, x, y, this);
         g.drawString(clientNames[i], x, y);
      }
      
//      for (int i = 0; i < nCoordinates.length; i += 2) {
//         int x = nCoordinates[i];
//         int y = nCoordinates[i + 1];
//         
//         g.drawImage(sheepImage,x, y , this);
//         g.drawString(clientName, x, y);
//      }
   }
}
