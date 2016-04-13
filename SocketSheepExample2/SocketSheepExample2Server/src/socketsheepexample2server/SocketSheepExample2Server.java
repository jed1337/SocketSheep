package socketsheepexample2server;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class SocketSheepExample2Server {
   private final static int PORT = 4096;
   private static ConcurrentHashMap<Integer, Coordinates> allSheep;
   private static List<DataOutputStream> allDOuts;

   public static void main(String[] args) {
      try {
         allSheep = new ConcurrentHashMap<>();
         allDOuts = new CopyOnWriteArrayList<>();

         ServerSocket serverSocket = new ServerSocket(PORT);
         System.out.println("Server started!");
                  
//         MovementDelegate movementDelegate = new MovementDelegate(100);
//         new Thread().start();

         for(int i=1 ;; i++){
            new Thread(new ClientHandler(serverSocket.accept())).start();
            System.out.println("Client " + i + " accepted");
         }
      } catch (IOException ex) {
         System.err.println(ex.getMessage());
      }
   }
   
   private static class MovementDelegate implements Runnable{
      private String message;

      private final char COMMA = ',';

      private final long SEND_INTERVAL;

      public MovementDelegate(long sendInterval){
         this.message       = "";
         this.SEND_INTERVAL = sendInterval;
      }

      public synchronized void addToMessage(String name, Coordinates coor){
         addToMessage(name, coor.getX(), coor.getY());
      }

      public synchronized void addToMessage(String name, int x, int y){
         if(message.isEmpty()) {
            message = "IMAGE";
         }

         message += String.format("%s:%d:%d%c", name, x, y, COMMA);
      }

      @Override
      public void run(){
         while(true){
            try {
               Thread.sleep(this.SEND_INTERVAL);

               if (message.isEmpty()) {
                  continue;
               }

               allOutputStreams.forEach((os)->{
                  try {
                     os.write(message.getBytes());
                     os.flush();
                  } catch (IOException ex) {
                     System.err.println(ex.getMessage());
                  }
               });

               message = "";

            } catch (InterruptedException ex) {
               printError(ex);
            }
         }
      }
   }
   
   private static class ClientHandler implements Runnable {
      private final Socket socket;
      private final DataOutputStream dOut;
      private final DataInputStream dIn;
      
      public ClientHandler(Socket socket) throws IOException {
         this.socket = socket;
         this.dIn    = new DataInputStream(socket.getInputStream());
         this.dOut   = new DataOutputStream(socket.getOutputStream());
         
         allDOuts.add(dOut);
      }

      @Override
      public void run() {
         try {
            sendOutput(dOut, "SUBMITNAME");
            allSheep.put(Integer.parseInt(getInput()), new Coordinates());
            sendOutput(dOut, "NEW_USER");
            
//            sendOutput("IMAGE-1337:400:450");
//<editor-fold defaultstate="collapsed" desc="Tester messages">
//            String message = "Test";
//            sendOutput(message);
//
//            message = "Second";
//            sendOutput(message);
//</editor-fold>
            
            while(true){
               String input = getInput();
               System.out.println("input = " + input);
            }
            
         } catch (IOException ex) {
            System.err.println(ex.getMessage());
//            removeClient();
         } finally{
            closeSafely(dIn);
            closeSafely(dOut);
            closeSafely(socket);
         }
      }

      private void sendOutputAll(String message) throws IOException{
         for (DataOutputStream dataOut : allDOuts) {
            sendOutput(dataOut, message);
         }
      }
      
      private void sendOutput(DataOutputStream dOut, String message) throws IOException {
         dOut.writeShort(message.length());
         dOut.writeBytes(message);
         dOut.flush();
      }

      private String getInput() throws IOException {
         short procLength = dIn.readShort();
         System.out.println("procLength = " + procLength);
         byte[] bProcData = new byte[procLength];
         dIn.readFully(bProcData);
         String input = new String(bProcData, StandardCharsets.UTF_8);
         return input;
      }
      
////<editor-fold defaultstate="collapsed" desc="Old code">
//      private void sendUpdatedImageToAllClients(String clientInput, int i) {
//         updateSheepLocation(new String[]{clientInput.substring(0, i), clientInput.substring(i + 2)});
////         MOVEMENT_DELEGATE.addToMessage(clientName, allSheep.get(clientName));
//      }
//
//      private void removeClient() {
//         allSheep.remove(clientName);
//         allOutputStreams.remove(os);
//         close(os);
//         sendToAllClients("REMOVE_USER"+clientName);
//      }
//
////      private void sendImageProtocolToClients() {
////         String newImage = updateImageProtocol();
////         sendToAllClients(newImage);
////      }
//
//      private void sendToAllClients(String protocol){
//         synchronized(allOutputStreams){
//            Iterator<OutputStream> pwIterator = allOutputStreams.iterator();
//            while(pwIterator.hasNext()){
//               try {
//                  OutputStream os = pwIterator.next();
//                  os.write(protocol.getBytes());
//                  os.flush();
//               } catch (IOException ex) {
//                  Logger.getLogger(SocketSheepExample2Server.class.getName()).log(Level.SEVERE, null, ex);
//               }
//            }
//         }
//      }
//
//      private String getAllUsers(){
//         StringBuilder sb = new StringBuilder();
//
//         allSheep.forEach((k,v)->{
//            updateImageProtocol(sb, k, v);
//            sb.append(",");
//         });
//         return sb.toString();
//      }
//</editor-fold>
      
//<editor-fold defaultstate="collapsed" desc="Updaters">
      private void updateSheepLocation(String[] clientInput) {
         int cNumber      = Integer.parseInt(clientInput[0]);
         String direction = clientInput[1];
         
         allSheep.get(cNumber).updateLocation(Constants.valueOf(direction));
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
   }

   private static void printErrors(Exception ex) {
      System.err.println(ex.getMessage());
      Arrays.stream(ex.getStackTrace()).forEach(System.err::println);
   }

   private static void closeSafely(Closeable c) {
      try{
         if (c != null) {
            c.close();
         }
      } catch (IOException ex) {
         System.err.println(ex.getMessage());
      }
   }
}
