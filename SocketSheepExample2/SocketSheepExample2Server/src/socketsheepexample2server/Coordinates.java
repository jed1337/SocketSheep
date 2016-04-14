package socketsheepexample2server;

public class Coordinates {
   private int xy;
   private final int PARTITION = 10;
   
   public Coordinates(){
      this(55);
   }

   public Coordinates(int xy) {
      this.xy = xy;
   }

   public int getXY() {
      return xy;
   }

   public void updateLocation(Constants direction){
      switch(direction){
         case UP:
            xy -= PARTITION;
            break;
         case DOWN:
            xy += PARTITION;
            break;
         case RIGHT:
            xy++;
            break;
         case LEFT:
            xy--;
            break;
      }
//<editor-fold defaultstate="collapsed" desc="Old code">
//      switch(direction){
//         case UP:
//            y-=moveDistance;
//            break;
//         case DOWN:
//            y+=moveDistance;
//            break;
//         case RIGHT:
//            x+=moveDistance;
//            break;
//         case LEFT:
//            x-=moveDistance;
//            break;
//      }
//</editor-fold>
   }
}
