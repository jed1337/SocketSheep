package socketsheepexample2server;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class SocketSheepExample2Server {
   private final static int PORT = 4096;
    private static HashMap<String, Coordinates> sheep;
   private static ArrayList<PrintWriter> clientPrintWriters;
   
   public static void main(String[] args) {
      try {
         sheep               = new HashMap<>();
         clientPrintWriters = new ArrayList<>();
         
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

   private static String render(String[] clientInput) {
      String name = clientInput[0];
      String direction = clientInput[1];
      
      Coordinates sheepCoor = sheep.get(name);
      if(sheepCoor==null){
         System.err.println("Sheep "+name+" has been removed!");
      }else{
         sheepCoor.updateLocation(Constants.valueOf(direction));
      }
      
      StringBuilder sb = new StringBuilder();
      sb.append("IMAGE");
      
      sheep.values().stream().forEach((v)->{
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
      private PrintWriter pw;

      public Handler(Socket socket) {
         this.socket = socket;
      }

      @Override
      public void run() {
         try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//            outputStream = socket.getOutputStream();
            pw = new PrintWriter(socket.getOutputStream(), true);
            clientPrintWriters.add(pw);
            while (!getValidClientName(input, pw)) {}
            
            pw.println("NAMEACCEPTED");
            
            while (true) {
               String clientInput = input.readLine();
               System.out.println("Got input from client: "+clientInput);
               
               int i = clientInput.lastIndexOf(":");
               sendImageToAllClients(clientInput, i);
            }
         } catch (IOException ex) {
            System.err.println(ex.getMessage());
         }
      }

      private void sendImageToAllClients(String clientInput, int i) {
         String newImage = render(new String[]{clientInput.substring(0, i), clientInput.substring(i+2)});
         
         clientPrintWriters.stream().forEach((printWriter)->{
            printWriter.println(newImage);
         });
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
