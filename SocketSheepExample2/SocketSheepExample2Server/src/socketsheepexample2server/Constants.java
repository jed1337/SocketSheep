package socketsheepexample2server;

public enum Constants {
   UP,DOWN,LEFT,RIGHT,INVALID;
   
   public static Constants getDirection(String direction){
      switch(direction){
         case "U":
            return UP;
         case "D":
            return DOWN;
         case "L":
            return LEFT;
         case "R":
            return RIGHT;
         default:
            return INVALID;
      }
   }
}
