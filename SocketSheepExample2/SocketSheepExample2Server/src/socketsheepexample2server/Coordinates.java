package socketsheepexample2server;

public class Coordinates {
   private int x;
   private int y;
   
   private final int move = 50;
   
   public Coordinates(){
      this(0,0);
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
            y+=move;
            break;
         case DOWN:
            y-=move;
            break;
         case RIGHT:
            x+=move;
            break;
         case LEFT:
            x-=move;
            break;
      }
   }
   
   @Override
   public String toString(){
      return String.format("x: %d, y: %d\n", x, y);
   }
}
