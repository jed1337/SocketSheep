package socketsheepexample2client;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.InputMismatchException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class MyPanel extends JPanel{
   private final BufferedImage SHEEP_IMAGE;
   private final int clientID;
   
   private long startTime;
   
   private final HashMap<Integer, int[]> allSheep;

   public MyPanel(String filePath, int clientID) throws IOException {
      this.SHEEP_IMAGE = ImageIO.read(new File(filePath));
      this.clientID    = clientID;
      
      this.allSheep   = new HashMap<>();
      this.startTime  = -1;
      
      this.setBackground(Color.WHITE);
   }
   
   public void addSheep(int clientID, int[] coordinates){
      checkCoordinateLength(coordinates);
      allSheep.put(clientID, coordinates);
   }
   
   public void removeSheep(int clientID){
      checkIfContainsSheep(clientID);
      allSheep.remove(clientID);
      this.repaint();
   }

   public void updateCoordinates(Object[] protocolDetails, long startTime){
      checkProtocolLength(protocolDetails);
      updateCoordinates((int[])protocolDetails[1], startTime);
   }
   
   public void updateCoordinates(int[] cProc, long startTime){
      for(int i=0; i<cProc.length; i++){
         int id = cProc[(i*3)+0];
         int x  = cProc[(i*3)+1];
         int y  = cProc[(i*3)+2];
         
//         if(!checkIfContainsSheep(cNames[i])){
//            this.allSheep.put(cNames[i], new int[]{x,y});
//         }
         
         //What I want to happen:
         //If the client's not there, he's put there
         //If he is, his coordinates is simply updated
         this.allSheep.put(id, new int[]{x,y});
         
         if(this.clientID == id){
            this.startTime = startTime;
         }
      }
      
      this.repaint();
   }

   @Override
   public void paintComponent(Graphics g){
      super.paintComponent(g);

      allSheep.forEach((Integer k,int[] v)->{
         g.drawImage(SHEEP_IMAGE, v[0], v[1], null);
         g.drawString(Integer.toString(k), v[0], v[1]);
      });
      
      if(this.startTime>0){
         System.out.println(String.format("Latency of %s is %dms ", 
            this.clientID, (System.currentTimeMillis()-this.startTime)));
         this.startTime   = -1;
      }
   }
      
   //<editor-fold defaultstate="collapsed" desc="Checkers">
   private boolean checkIfContainsSheep(int clientID){
//         throw new NoSuchElementException("Does not contain the name "+name);      
      return allSheep.containsKey(clientID);
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
