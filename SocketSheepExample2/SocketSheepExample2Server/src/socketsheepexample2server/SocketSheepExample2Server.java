package socketsheepexample2server;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

public class SocketSheepExample2Server {
   private final static int PORT = 4096;
   private static ConcurrentHashMap<String, Coordinates> allSheep;
   private static List<PrintWriter> allPrintWriters;

   public static void main(String[] args) {
      try {
         allSheep        = new ConcurrentHashMap<>();
         allPrintWriters = new CopyOnWriteArrayList<>();

         ServerSocket serverSocket = new ServerSocket(PORT);
         System.out.println("Server started!");
         
         MovementDelegate movementDelegate = new MovementDelegate(50);
         new Thread(movementDelegate).start();

         for(int i=1 ;; i++){
            new Thread(new ClientHandler(serverSocket.accept(), movementDelegate)).start();
            System.out.println("Client " + i + " accepted");
         }
      } catch (IOException ex) {
         System.err.println(ex.getMessage());
      }
   }
   
   private static class MovementDelegate implements Runnable{
      private String message;
      
      private final char COMMA = ',';
//      private final Semaphore LOCK;
//      private final Semaphore CAN_SEND_LOCK;
      
      private final long SEND_INTERVAL;
      
      public MovementDelegate(long sendInterval){
         this.message       = "";
         this.SEND_INTERVAL = sendInterval;
         
//         this.LOCK          = new Semaphore(1);
//         this.CAN_SEND_LOCK = new Semaphore(0);
      }
      
      public synchronized void addToMessage(String name, Coordinates coor){
         addToMessage(name, coor.getX(), coor.getY());
      }
      
      public synchronized void addToMessage(String name, int x, int y){
//         try {
//            LOCK.acquire();
               if(message.isEmpty()){
                  message="IMAGE";
               }

               message += String.format("%s:%d:%d%c", name, x, y, COMMA);
//            LOCK.release();
//         } catch (InterruptedException ex) {
//            printError(ex);
//         }
      }
      
      @Override
      public void run(){
         while(true){
            try {
//               LOCK.acquire();
                  Thread.sleep(this.SEND_INTERVAL);
                  
                  if(message.isEmpty()){
//                     LOCK.release();
                     continue;
                  }

                  allPrintWriters.forEach((pw)->{
                     pw.println(message);
                  });

                  message = "";
               
//               LOCK.release();
            } catch (InterruptedException ex) {
               printError(ex);
            }
         }
      }
      private static void printError(InterruptedException ex) {
         System.err.println(ex.getMessage());
         System.err.println(Arrays.toString(ex.getStackTrace()));
      }
   }

   private static class ClientHandler implements Runnable {
      private final Socket SOCKET;
      private final MovementDelegate MOVEMENT_DELEGATE;

      private BufferedReader bufferedReader;
      private PrintWriter printWriter;

      private String clientName;

      public ClientHandler(Socket socket, MovementDelegate movementDelegate) {
         this.SOCKET = socket;
         this.MOVEMENT_DELEGATE = movementDelegate;
      }

      @Override
      public void run() {
         try {
            bufferedReader = new BufferedReader(new InputStreamReader(SOCKET.getInputStream()));
            printWriter    = new PrintWriter(SOCKET.getOutputStream(), true);
            
            allPrintWriters.add(printWriter);

            clientName = getValidClientName(bufferedReader, printWriter);

            sendToAllClients("NEW_USER"+clientName+":"+allSheep.get(clientName).toString());
            printWriter.println("GET_CURRENT_USERS"+getAllUsers());
            
            while (true) {
               if(bufferedReader.ready()){
                  String clientInput = bufferedReader.readLine();
                  sendUpdatedImageToAllClients(clientInput, clientInput.lastIndexOf(":"));
               }
            }
         } catch (IOException ex) {
            System.err.println(ex.getMessage());
            removeClient();
         }
      }

      private void sendUpdatedImageToAllClients(String clientInput, int i) {
         updateSheepLocation(new String[]{clientInput.substring(0, i), clientInput.substring(i + 2)});
         MOVEMENT_DELEGATE.addToMessage(clientName, allSheep.get(clientName));
      }

      private void removeClient() {
         allSheep.remove(clientName);
         allPrintWriters.remove(printWriter);
         close(printWriter);
         sendToAllClients("REMOVE_USER"+clientName);
      }

//      private void sendImageProtocolToClients() {
//         String newImage = updateImageProtocol();
//         sendToAllClients(newImage);
//      }
      
      private void sendToAllClients(String protocol){
         synchronized(allPrintWriters){
            Iterator<PrintWriter> pwIterator = allPrintWriters.iterator();
            while(pwIterator.hasNext()){
               PrintWriter pw = pwIterator.next();
               pw.println(protocol);
            }
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

      private String getAllUsers(){
         StringBuilder sb = new StringBuilder();
         
         allSheep.forEach((k,v)->{
            updateImageProtocol(sb, k, v);
            sb.append(",");
         });
         return sb.toString();
      }

//<editor-fold defaultstate="collapsed" desc="Updaters">
      private void updateSheepLocation(String[] clientInput) {
         String name      = clientInput[0];
         String direction = clientInput[1];
         
         allSheep.get(name).updateLocation(Constants.valueOf(direction));
      }
      
//      private String updateImageProtocol() {
//         StringBuilder sb  = new StringBuilder();
//         Coordinates cCoor = allSheep.get(clientName);
//         
//         sb.append("IMAGE");
//         updateImageProtocol(sb, clientName, cCoor);
//         
//         return sb.toString();
//      }
      
      private void updateImageProtocol(StringBuilder sb, String key, Coordinates value){
         sb.append(key);
         sb.append(":");
         sb.append(value.getX());
         sb.append(":");
         sb.append(value.getY());
      }
//</editor-fold>

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
