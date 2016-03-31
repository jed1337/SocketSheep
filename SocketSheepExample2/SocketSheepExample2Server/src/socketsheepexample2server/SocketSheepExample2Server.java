package socketsheepexample2server;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class SocketSheepExample2Server {
   private final static int PORT = 4096;
   private static HashMap<String, Coordinates> sheep;
   
   private static BufferedImage sheepImage;
   private static BufferedImage canvas;

   public static void main(String[] args) {
      try {
         sheep      = new HashMap<>();
         sheepImage = ImageIO.read(new File("src\\images\\Sheep.jpg"));
         canvas     = new BufferedImage(1000, 1000, BufferedImage.TYPE_3BYTE_BGR);
         
         ServerSocket serverSocket = new ServerSocket(PORT);
         System.out.println("Server started!\n");
         while (true) {
            new Thread(new Handler(serverSocket.accept())).start();
            System.out.println("Client accepted");
         }
      } catch (IOException ex) {
         System.err.println(ex.getMessage());
      }
   }

   private static void render(String[] clientInput) {
      String name = clientInput[0];
      String direction = clientInput[1];
      
      Coordinates sheepCoor = sheep.get(name);
      if(sheepCoor==null){
         System.err.println("Sheep "+name+" has been removed!");
      }else{
         sheepCoor.updateLocation(Constants.valueOf(direction));
      }
      
      //Implement Remove images from canvas
      Graphics2D tempImage = canvas.createGraphics();
      sheep.values().stream().forEach((v)->{
         tempImage.drawImage(sheepImage, v.getX(), v.getY(), null);
      });
   }
   private static String render2(String[] clientInput) {
      String name = clientInput[0];
      String direction = clientInput[1];
      
      Coordinates sheepCoor = sheep.get(name);
      if(sheepCoor==null){
         System.err.println("Sheep "+name+" has been removed!");
      }else{
         sheepCoor.updateLocation(Constants.valueOf(direction));
      }
      
      //Implement Remove images from canvas
      StringBuilder sb = new StringBuilder();
      
      sheep.values().stream().forEach((v)->{
//         tempImage.drawImage(sheepImage, v.getX(), v.getY(), null);
         sb.append(v.getX());
         sb.append(":");
         sb.append(v.getY());
         sb.append(",");
      });
      
      return sb.toString();
   }

   private static class Handler implements Runnable {
      private final Socket socket;

      private BufferedReader input;
      private OutputStream outputStream;
      private PrintWriter pw;

      public Handler(Socket socket) {
         this.socket = socket;
      }

      @Override
      public void run() {
         try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outputStream = socket.getOutputStream();
            pw = new PrintWriter(outputStream, true);

            while (!getValidClientName(input, pw)) {}
            
            pw.println("NAMEACCEPTED");

            while (true) {
               String clientInput = input.readLine();
               int i = clientInput.lastIndexOf(":");
               
               OutputStream out = socket.getOutputStream();
//               FileInputStream file = new FileInputStream(new File("src/images/Sheep.jpg"));

//               byte buffer[] = new byte[1024];
//               int LENGTH = buffer.length;
//               int count;

               out.write("IMAGE".getBytes());
               
//               while ((count = file.read(buffer)) != -1){
//                   out.write(buffer);
//               }
               
               out.write(render2(new String[]{clientInput.substring(0, i), clientInput.substring(i+2)})
                  .getBytes());
               
               out.flush();
               close(out);
            }

//            byte[] sizeAr = new byte[4];
//            input.read(sizeAr);
//            int size = ByteBuffer.wrap(sizeAr).asIntBuffer().get();
//            byte[] imageAr = new byte[size];
//            input.read(imageAr);
//            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageAr));
//            System.out.println("Received " + image.getHeight() + "x" + image.getWidth() + ": " + System.currentTimeMillis());
//            ImageIO.write(image, "jpg", new File("Image-" + System.currentTimeMillis() + ".jpg"));
//            input.close();
         } catch (IOException ex) {
            System.err.println(ex.getMessage());
         }
      }

      private void close(Closeable c) {
         try {
            if (c != null) {
               c.close();
            }
         } catch (IOException ex) {
            System.err.println(ex.getMessage());
         }
      }

      private boolean getValidClientName(BufferedReader input, PrintWriter pw) throws IOException {
         // Request a name from this client.  Keep requesting until
         // a name is submitted that is not already used.  Note that
         // checking for the existence of a name and adding the name
         // must be done while locking the set of names.
         boolean valid = false;
         while (!valid) {
            valid = true;
            pw.println("SUBMITNAME");
            String name = input.readLine();
            if (name.isEmpty() || name == null) {
               return false;
            }
            synchronized (name) {
               if (sheep.containsKey(name)) {
                  valid = false;
               }
            }
            if (valid) {
               sheep.put(name, new Coordinates(0, 0));
               break;
            }
         }
         return true;
      }
   }
}
