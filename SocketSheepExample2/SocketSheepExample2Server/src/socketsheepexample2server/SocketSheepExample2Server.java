package socketsheepexample2server;

import java.io.IOException;

public class SocketSheepExample2Server {
   private static final int START_PORT = 4096;

   private static void singleServer(int port, MovementDelegate mDelegate) throws IOException, InterruptedException {
      System.out.println("Started Server at "+port);

      new Thread(new Server(port, mDelegate)).start();
   }

   private static void multiServer(int port, MovementDelegate mDelegate, int serverCount) throws IOException, InterruptedException {
      for (int i = 0; i < serverCount; i++) {
         singleServer(port+i, mDelegate);
      }
   }

   public static void main(String[] args) throws InterruptedException, IOException {
      MovementDelegate mDelegate = new MovementDelegate(250);
      new Thread(mDelegate).start();

//      singleServer(PORT, mDelegate);
      multiServer(START_PORT, mDelegate, 2);
      
//<editor-fold defaultstate="collapsed" desc="Old main code">
//         for(int i=1 ;; i++)
//         {
//            if(i%2==1){
//                new Thread(new ClientHandler(firstSocket.accept(), mDelegate)).start();
//                System.out.println("Client " + i + " accepted in Server Socket 1");
//            } else {
//                new Thread(new ClientHandler(secondSocket.accept(), mDelegate)).start();
//                System.out.println("Client " + i + " accepted in Server Socket 2");
//            }
//         }
//</editor-fold>
   }
}
