package socketsheepexample2server;

public class Coordinates {
   private int x;
   private int y;
   
   private final int moveDistance = 50;
   
   public Coordinates(){
      this(300,300);
   }

   public Coordinates(int x, int y) {
      this.x = x;
      this.y = y;
   }

   public int getX() {
      return x;
   }

   public int getY() {
      return y;
   }
   
   public void updateLocation(Constants direction){
      switch(direction){
         case UP:
            y-=moveDistance;
            break;
         case DOWN:
            y+=moveDistance;
            break;
         case RIGHT:
            x+=moveDistance;
            break;
         case LEFT:
            x-=moveDistance;
            break;
      }
   }
   
   @Override
   public String toString(){
      return String.format("%d:%d", x, y);
   }
}
