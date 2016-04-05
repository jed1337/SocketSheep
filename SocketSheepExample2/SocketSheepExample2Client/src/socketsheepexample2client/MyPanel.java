package socketsheepexample2client;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class MyPanel extends JPanel{
   private final BufferedImage sheepImage;
   private int[] nCoordinates;
   private String[] clientNames;
   private long startTime;
   private boolean clientMoved;

   public MyPanel(String filePath) throws IOException {
      this.sheepImage   = ImageIO.read(new File(filePath));
      this.nCoordinates = new int[0];
      this.clientNames  = new String[0];
      this.startTime    = -1;
      this.clientMoved  = false;
      
      this.setBackground(Color.WHITE);
   }
   
   public void updateCoordinates(String[] clientNames, int[] clientCoordinates, long startTime, boolean clientMoved){
      this.clientNames  = clientNames;
      this.nCoordinates = clientCoordinates;
      this.clientMoved  = clientMoved;
      
      if(clientMoved){
         this.startTime = startTime;
      }
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
      
//      try {
//         Thread.sleep(1000);
//      } catch (InterruptedException ex) {
//         System.out.println(ex.getMessage());
//      }
      
      if(clientMoved){
         System.out.println("Latency: "+ (System.currentTimeMillis()-this.startTime));
         this.clientMoved = false;
         this.startTime   = -1;
      }
   }
}
