package socketsheepexample2server;

import java.io.IOException;
import java.net.ServerSocket;

public class SocketSheepExample2Server {
   private final static int PORT = 4096;

   public static void main(String[] args) {
      try {
         ServerSocket serverSocket = new ServerSocket(PORT);
         System.out.println("Server started!\n");
         while(true){
            new Thread(new Handler(serverSocket.accept())).start();
            System.out.println("Client accepted");
         }
         
      } catch (IOException ex) {
         System.err.println(ex.getMessage());
      }
   }
}
