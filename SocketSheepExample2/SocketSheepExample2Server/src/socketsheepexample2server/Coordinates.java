package socketsheepexample2server;

public class Coordinates {
   private int x;
   private int y;
   
   private int move = 10;
   
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
   
   public void update(Constants direction){
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
}
