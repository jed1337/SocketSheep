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
   private static ConcurrentHashMap<String, Coordinates> allSheep;
   private static ArrayList<PrintWriter> clientPrintWriters;

   public static void main(String[] args) {
      try {
         allSheep = new ConcurrentHashMap<>();
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

      private BufferedReader bufferedReader;
      private PrintWriter printWriter;

      private String clientName;

      public Handler(Socket socket) {
         this.socket = socket;
      }

      @Override
      public void run() {
         try {
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            printWriter = new PrintWriter(socket.getOutputStream(), true);
            
            synchronized(clientPrintWriters){
               clientPrintWriters.add(printWriter);
            }

            clientName = getValidClientName(bufferedReader, printWriter);

            sendToAllClients("NEW_USER"+clientName+":"+allSheep.get(clientName).toString());
            printWriter.println("GET_CURRENT_USERS"+getAllUsers());
            
            while (true) {
               String clientInput = bufferedReader.readLine();
               sendUpdatedImageToAllClients(clientInput, clientInput.lastIndexOf(":"));
            }
         } catch (IOException ex) {
            System.err.println(ex.getMessage());
            synchronized(clientPrintWriters){
               removeSheep(clientName);
            }
            clientPrintWriters.remove(printWriter);
         }
      }

      private void sendUpdatedImageToAllClients(String clientInput, int i) {
         updateSheepLocation(new String[]{clientInput.substring(0, i), clientInput.substring(i + 2)});
         sendImageProtocolToClients();
      }

      private void removeSheep(String clientName) {
         allSheep.remove(clientName);
         sendToAllClients("REMOVE_USER"+clientName);
//         sendImageProtocolToClients();
      }

      private void sendImageProtocolToClients() {
         String newImage = updateImageProtocol();
//         System.out.println(newImage);
//         System.out.println("---");
         
         sendToAllClients(newImage);
//         synchronized(clientPrintWriters){
//            clientPrintWriters.stream().forEach((printWriter)->{
//               printWriter.println(newImage);
//            });
//         }
      }
      
      private void sendToAllClients(String protocol){
         synchronized(clientPrintWriters){
            clientPrintWriters.forEach((pw)->{
               pw.println(protocol);
            });
         }
      }
         
      /**
      * Request a name from this client.  Keep requesting until
      * a name is submitted that is not already used.  Note that
      * checking for the existence of a name and adding the name
      * must be done while locking the set of names.
      */
      private String getValidClientName(BufferedReader input, PrintWriter pw) throws IOException {
         boolean valid = false;
         String name = "";

         while (!valid) {
            valid = true;
            pw.println("SUBMITNAME");
            name = input.readLine();
            if (name == null || name.isEmpty()) {
               valid = false;
            }
            if (allSheep.containsKey(name)) {
               valid = false;
            }
         }
         allSheep.put(name, new Coordinates());
         return name;
      }

      private void updateSheepLocation(String[] clientInput) {
         String name      = clientInput[0];
         String direction = clientInput[1];

         allSheep.get(name).updateLocation(Constants.valueOf(direction));
      }
      
      private String getAllUsers(){
         StringBuilder sb = new StringBuilder();
         
         allSheep.forEach((k,v)->{
            sb.append(k);
            sb.append(":");
            sb.append(v.getX());
            sb.append(":");
            sb.append(v.getY());
            sb.append(",");
         });
         return sb.toString();
      }

      private String updateImageProtocol() {
         StringBuilder sb = new StringBuilder();
         Coordinates cCoor = allSheep.get(clientName);
         
         sb.append("IMAGE");
         sb.append(clientName);
         sb.append(":");
         sb.append(cCoor.getX());
         sb.append(":");
         sb.append(cCoor.getY());
//         sheep.forEach((k,v)->{
//            sb.append(k);
//            sb.append(":");
//            
//            sb.append(v.getX());
//            sb.append(":");
//            sb.append(v.getY());
//            
//            sb.append(",");
//         });

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
