package socketsheepexample2server;

public enum Constants {
   UP,DOWN,LEFT,RIGHT,INVALID;
   
   public static Constants getDirection(String direction){
      switch(direction){
         case "UP":
            return UP;
         case "DOWN":
            return DOWN;
         case "LEFT":
            return LEFT;
         case "RIGHT":
            return RIGHT;
         default:
            return INVALID;
      }
   }
}
