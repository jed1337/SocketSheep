package socketsheepexample2server;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class SocketSheepExample2Server {
   private final static int PORT = 4096;
   private static HashMap<String, Coordinates> sheep;

   public static void main(String[] args) {
      sheep = new HashMap<>();
      
      try {
         ServerSocket serverSocket = new ServerSocket(PORT);
         System.out.println("Server started!\n");
         while (true) {
            new Thread(new Handler(serverSocket.accept())).start();
         BufferedImage image = ImageIO.read(new File("src\\images\\Sheep.jpg"));   
//            sheep.put("test", new Coordinates());
//            Coordinates a =sheep.get("test");
//            a.update(Constants.valueOf("DOWN"));
            
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
         sheepCoor.update(Constants.valueOf(direction));
      }
      
      sheep.entrySet().stream().forEach((Map.Entry<String, Coordinates> entry)->{
         try {
//            BufferedImage image = ImageIO.read(getClass().getResourceAsStream("src\\images\\Sheep.jpg"));
         } catch (IOException ex) {
            Logger.getLogger(SocketSheepExample2Server.class.getName()).log(Level.SEVERE, null, ex);
         }
      });
         
   }

   private static class Handler implements Runnable {
      private Socket socket;

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
            
            while(!getValidClientName(input, pw)){}

            while (true) {
               String clientInput = input.readLine();
               int i = clientInput.lastIndexOf(":");
               render(new String[]{clientInput.substring(0, i), clientInput.substring(i)});
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
            if (name == null) {
               return false;
            }
            synchronized (name) {
               if(sheep.containsKey(name))
                  valid = false;
            }
            if(valid){
               sheep.put(name, new Coordinates(0, 0));
               break;
            }
         }
         return true;
      }
   }
}
