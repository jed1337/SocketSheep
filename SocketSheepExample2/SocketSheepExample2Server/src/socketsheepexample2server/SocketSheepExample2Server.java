package socketsheepexample2server;

import java.io.IOException;

public class SocketSheepExample2Server {
   private static final int START_PORT = 4096;

   private static void singleServer(int port, MovementDelegate mDelegate) throws IOException, InterruptedException {
      System.out.println("Started Server at "+port);

      new Thread(new Server(port, mDelegate)).start();
   }

   private static void multiServer(MovementDelegate mDelegate, int serverCount) throws IOException, InterruptedException {
      for (int i = 0; i < serverCount; i++) {
         singleServer(START_PORT+i, mDelegate);
      }
   }

   public static void main(String[] args) throws InterruptedException, IOException {
      MovementDelegate mDelegate = new MovementDelegate(300);
      new Thread(mDelegate).start();

      multiServer(mDelegate, 2);
   }
}
