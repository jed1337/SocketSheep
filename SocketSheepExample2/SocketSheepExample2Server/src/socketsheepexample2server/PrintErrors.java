package socketsheepexample2server;

import java.util.Arrays;

public class PrintErrors {
   public static void log(Exception ex) {
      System.err.println(ex.getMessage());
      Arrays.stream(ex.getStackTrace()).forEach(System.err::println);
   }
}
