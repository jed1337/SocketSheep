package socketsheepexample2client;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class SocketSheepExample2Client {
   private final static int PORT = 4096;
   
    public static void main(String[] args) {
      try {
         Socket socket = new Socket("::1", PORT);
         InputStream input = socket.getInputStream();
      } catch (IOException ex) {
         System.err.println(ex.getMessage());
      }
    }
}
