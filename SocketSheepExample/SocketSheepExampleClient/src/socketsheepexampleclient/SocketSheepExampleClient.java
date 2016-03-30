package socketsheepexampleclient;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;
/**
 * Source : http://stackoverflow.com/questions/33606499/move-the-images-from-client-to-server-using-java
 */

public class SocketSheepExampleClient {
   public static void main(String[] args) throws Exception {
      Socket socket = new Socket("localhost", 2048);
      OutputStream outputStream = socket.getOutputStream();
      
      //File f = new File("src//images//Sheep.jpg");
      File f = new File("src/images/Sheep.jpg");
      BufferedImage image = ImageIO.read(f);
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      ImageIO.write(image, "jpg", byteArrayOutputStream);
      byte[] size = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();
      
      outputStream.write(size);
      outputStream.write(byteArrayOutputStream.toByteArray());
      outputStream.flush();
      socket.close();
   }
}
