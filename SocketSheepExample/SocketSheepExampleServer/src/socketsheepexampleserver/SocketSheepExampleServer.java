package socketsheepexampleserver;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
/**
 * Source : http://stackoverflow.com/questions/33606499/move-the-images-from-client-to-server-using-java
 */

public class SocketSheepExampleServer implements Runnable {
   public static final String dir = "path to store image";
   Socket soc = null;

   public SocketSheepExampleServer(Socket soc) {
      this.soc = soc;
   }

   @Override
   public void run() {
      InputStream inputStream = null;
      try {
         inputStream = this.soc.getInputStream();
         System.out.println("Reading: " + System.currentTimeMillis());
         byte[] sizeAr = new byte[4];
         inputStream.read(sizeAr);
         int size = ByteBuffer.wrap(sizeAr).asIntBuffer().get();
         byte[] imageAr = new byte[size];
         inputStream.read(imageAr);
         BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageAr));
         System.out.println("Received " + image.getHeight() + "x" + image.getWidth() + ": " + System.currentTimeMillis());
         ImageIO.write(image, "jpg", new File(dir + System.currentTimeMillis() + ".jpg"));
         inputStream.close();
      } catch (IOException ex) {
         Logger.getLogger(SocketSheepExampleServer.class.getName()).log(Level.SEVERE, null, ex);
      }

   }

   public static void main(String[] args) throws Exception {
      ServerSocket serverSocket = new ServerSocket(2048);
      System.out.println("Server on");
      while (true) {
         Socket socket = serverSocket.accept();
         Thread thread = new Thread(new SocketSheepExampleServer(socket));
         thread.start();
      }
   }
}
