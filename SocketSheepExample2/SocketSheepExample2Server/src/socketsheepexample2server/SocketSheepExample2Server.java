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
         System.out.println("Server started!");
         
         while (true) {
            new Thread(new Handler(serverSocket.accept())).start();
            System.out.println("Client accepted");
         }
      } catch (IOException ex) {
         System.err.println(ex.getMessage());
      }
   }

   private static void updateSheepLocation(String[] clientInput) {
      String name = clientInput[0];
      String direction = clientInput[1];
      
      sheep.get(name).updateLocation(Constants.valueOf(direction));
   }
   
   private static String updateImageProtocol(){
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
      
      private String clientName;

      public Handler(Socket socket) {
         this.socket = socket;
      }

      @Override
      public void run() {
         try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            pw    = new PrintWriter(socket.getOutputStream(), true);
            clientPrintWriters.add(pw);
            
            clientName = getValidClientName(input, pw);
            
            pw.println("NAMEACCEPTED");
            
            while (true) {
               String clientInput = input.readLine();
               System.out.println("Got input from client: "+clientInput);
               
               sendUpdatedImageToAllClients(clientInput, clientInput.lastIndexOf(":"));
            }
         } catch (IOException ex) {
            System.err.println(ex.getMessage());
            removeSheep(clientName);
         }
      }

      private void sendUpdatedImageToAllClients(String clientInput, int i) {
         updateSheepLocation(new String[]{clientInput.substring(0, i), clientInput.substring(i+2)});
         sendImageProtocolToClients();
      }
      
      private void removeSheep(String clientName) {
         sheep.remove(clientName);
         sendImageProtocolToClients();
      }
      
      private void sendImageProtocolToClients(){
         String newImage = updateImageProtocol();
         clientPrintWriters.stream().forEach((printWriter)->{
            printWriter.println(newImage);
         });
      }
      
      private String getValidClientName(BufferedReader input, PrintWriter pw) throws IOException {
         // Request a name from this client.  Keep requesting until
         // a name is submitted that is not already used.  Note that
         // checking for the existence of a name and adding the name
         // must be done while locking the set of names.
         boolean valid = false;
         String name="";
         
         while (!valid) {
            valid = true;
            pw.println("SUBMITNAME");
            name = input.readLine();
            if (name.isEmpty() || name == null) {
               valid = false;
            }
            synchronized (name) {
               if (sheep.containsKey(name)) {
                  valid = false;
               }
            }
         }
         sheep.put(name, new Coordinates(0, 0));
         return name;
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
   }
}
