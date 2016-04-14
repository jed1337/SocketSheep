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
   private final HashMap<Integer, Integer> allSheep;
   private final int clientID;
   
   private long startTime;
   private int partitionedWidth;
   private int partitionedHeight;
   
   private final int PARTITION = 10;

   public MyPanel(String filePath, int clientID) throws IOException {
      this.SHEEP_IMAGE = ImageIO.read(new File(filePath));
      this.allSheep    = new HashMap<>();
      this.clientID    = clientID;
      
      this.startTime   = -1;
      
      this.setBackground(Color.WHITE);
   }
   
   public void updateSize(){
      this.partitionedWidth  = getWidth()  / PARTITION;
      this.partitionedHeight = getHeight() / PARTITION;
   }
   
   public void addSheep(int clientID, int coordinates){
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
      for(int i=0; i<cProc.length; i+=2){
         try{
            int id = cProc[i+0];
            int xy  = cProc[i+1];
         
            //What I want to happen:
            //If the client's not there, he's put there
            //If he is, his coordinates is simply updated
            this.allSheep.put(id, xy);

            if(this.clientID == id){
               this.startTime = startTime;
            }
         }catch(ArrayIndexOutOfBoundsException e){
            System.err.println(e);
         }
      }
      
      this.repaint();
   }

   @Override
   public void paintComponent(Graphics g){
      super.paintComponent(g);

//<editor-fold defaultstate="collapsed" desc="Old Code">
//      allSheep.forEach((Integer k,int[] v)->{
//         g.drawImage(SHEEP_IMAGE, v[0], v[1], null);
//         g.drawString(Integer.toString(k), v[0], v[1]);
//      });
//</editor-fold>
      
      allSheep.forEach((k,v)->{
         int x = (v % PARTITION) * partitionedWidth;
         int y = (v / PARTITION) * partitionedHeight;
         
         g.drawImage(SHEEP_IMAGE, x, y, null);
         g.drawString(Integer.toString(k), x, y);
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
   
   private void throwInputMismatchError(String message, int reqLength, int acLength) throws InputMismatchException{
      throw new InputMismatchException(
         String.format("%s must be of length %d, length received is %d", 
         message, reqLength, acLength));
   }
//</editor-fold>
}
