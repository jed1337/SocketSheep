package socketsheepexample2server;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class SocketSheepExample2Server {
   private final static int PORT = 4096;
   private static ConcurrentHashMap<String, Coordinates> sheep;
   private static ArrayList<PrintWriter> clientPrintWriters;

   public static void main(String[] args) {
      try {
         sheep = new ConcurrentHashMap<>();
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
            pw = new PrintWriter(socket.getOutputStream(), true);
            
            synchronized(clientPrintWriters){
               clientPrintWriters.add(pw);
            }

            clientName = getValidClientName(input, pw);

            pw.println("NAME_ACCEPTED"+sheep.get(clientName).toString());

            while (true) {
               String clientInput = input.readLine();
               sendUpdatedImageToAllClients(clientInput, clientInput.lastIndexOf(":"));
            }
         } catch (IOException ex) {
            System.err.println(ex.getMessage());
            synchronized(clientPrintWriters){
               removeSheep(clientName);
            }
            clientPrintWriters.remove(pw);
         }
      }

      private void sendUpdatedImageToAllClients(String clientInput, int i) {
         updateSheepLocation(new String[]{clientInput.substring(0, i), clientInput.substring(i + 2)});
         sendImageProtocolToClients();
      }

      private void removeSheep(String clientName) {
         sheep.remove(clientName);
         sendImageProtocolToClients();
      }

      private void sendImageProtocolToClients() {
         String newImage = updateImageProtocol();
         System.out.println(newImage);
//         System.out.println("---");
         synchronized(clientPrintWriters){
            clientPrintWriters.stream().forEach((printWriter)->{
               printWriter.println(newImage);
            });
         }
      }

      private String getValidClientName(BufferedReader input, PrintWriter pw) throws IOException {
         // Request a name from this client.  Keep requesting until
         // a name is submitted that is not already used.  Note that
         // checking for the existence of a name and adding the name
         // must be done while locking the set of names.
         boolean valid = false;
         String name = "";

         while (!valid) {
            valid = true;
            pw.println("SUBMITNAME");
            name = input.readLine();
            if (name == null || name.isEmpty()) {
               valid = false;
            }
            synchronized (name) {
               if (sheep.containsKey(name)) {
                  valid = false;
               }
            }
         }
         sheep.put(name, new Coordinates());
         return name;
      }

      private void updateSheepLocation(String[] clientInput) {
         String name = clientInput[0];
         String direction = clientInput[1];

         sheep.get(name).updateLocation(Constants.valueOf(direction));
      }

      private String updateImageProtocol() {
         StringBuilder sb = new StringBuilder();
         sb.append("IMAGE");
         
         sheep.forEach((k,v)->{
            sb.append(k);
            sb.append(":");
            
            sb.append(v.getX());
            sb.append(":");
            sb.append(v.getY());
            
            sb.append(",");
         });

         return sb.toString();
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
