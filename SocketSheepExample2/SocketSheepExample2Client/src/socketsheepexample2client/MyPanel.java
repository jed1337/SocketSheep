package socketsheepexample2client;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class MyPanel extends JPanel{
   private final BufferedImage sheepImage;
//   private int[] nCoordinates;
//   private String[] clientNames;
   private String clientName;
   private long startTime;
   
   private HashMap<String, int[]> allSheep;

   public MyPanel(String filePath) throws IOException {
      this.sheepImage = ImageIO.read(new File(filePath));
      this.allSheep   = new HashMap<>();
      this.startTime  = -1;
      
//      this.nCoordinates   = new int[0];
//      this.clientNames    = new String[0];
//      this.clientMoved    = false;
      
      this.setBackground(Color.WHITE);
   }
   
   public void setClientName(String clientName){
      this.clientName = clientName;
   }
   
   public void addSheep(String name, int[] coordinates){
      checkCoordinateLength(coordinates);
      allSheep.put(name, coordinates);
   }
   
   public void removeSheep(String name){
      checkIfContainsSheep(name);
      allSheep.remove(name);
      this.repaint();
   }

   public void updateCoordinates(Object[] protocolDetails, long startTime){
      checkProtocolLength(protocolDetails);
      updateCoordinates((String[])protocolDetails[0], (int[])protocolDetails[1], startTime);
   }
   
   public void updateCoordinates(String[] cNames, int[] cCoor, long startTime){
//      this.clientNames  = clientNames;
//      this.nCoordinates = clientCoordinates;
//      this.clientMoved  = clientMoved;
      
//      if(clientMoved){
//         this.startTime = startTime;
//      }
      for(int i=0; i<cNames.length; i++){
         checkIfContainsSheep(cNames[i]);
         int x = cCoor[(i*2)];
         int y = cCoor[(i*2)+1];
         
         this.allSheep.put(cNames[i], new int[]{x,y});
         
         if(this.clientName.equals(cNames[i])){
            this.startTime = startTime;
         }
      }
      
      this.repaint();
   }

   @Override
   public void paintComponent(Graphics g){
      super.paintComponent(g);

//      try {
//         Thread.sleep(1000);
//      } catch (InterruptedException ex) {
//         System.out.println(ex.getMessage());
//      }
      
      allSheep.forEach((k,v)->{
         g.drawImage(sheepImage, v[0], v[1], null);
         g.drawString(k, v[0], v[1]);
      });
      
      if(this.startTime>0){
         System.out.println(String.format("Latency of %s is %dms ", 
            this.clientName, (System.currentTimeMillis()-this.startTime)));
         this.startTime   = -1;
      }
   }
      
   //<editor-fold defaultstate="collapsed" desc="Checkers">
   private void checkIfContainsSheep(String name){
      if(!allSheep.containsKey(name)){
         throw new NoSuchElementException("Does not contain the name "+name);
      }
   }
   
   private void checkProtocolLength(Object[] p){
      if(p.length!=2){
         throwInputMismatchError("protocolDetails", 2, p.length);
      }
      
      String[] cNames = (String[]) p[0];
      int[]    cCoor  = (int[]) p[1];
      
      if(cCoor.length!=cNames.length*2){
         throwInputMismatchError("Coordinate Array", cNames.length*2, cCoor.length);
      }
   }
   
   private void checkCoordinateLength(int[] coordinates){
      if(coordinates.length!=2){
         throwInputMismatchError("Coordinates", 2, coordinates.length);
      }
   }
   
   private void throwInputMismatchError(String message, int reqLength, int acLength) throws InputMismatchException{
      throw new InputMismatchException(
         String.format("%s must be of length %d, length received is %d", 
         message, reqLength, acLength));
   }
//</editor-fold>
}
