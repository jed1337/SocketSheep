package socketsheepexample2server;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Handler implements Runnable {
   private Socket socket;
   
   private InputStream input;
   private OutputStream outputStream;
   private PrintWriter pw;

   public Handler(Socket socket) {
      this.socket = socket;
   }

   @Override
   public void run() {
      try {
         input = socket.getInputStream();
         outputStream = socket.getOutputStream();
         pw = new PrintWriter(outputStream, true);
         
         
      } catch (IOException ex) {
         Logger.getLogger(Handler.class.getName()).log(Level.SEVERE, null, ex);
      }
   }
   
   private void close(Closeable c){
      try {
         if(c!=null)
         c.close();
      } catch (IOException ex) {
         System.err.println(ex.getMessage());
      }
   }
}
