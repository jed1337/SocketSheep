package socketsheepexample2server;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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
                  
//<editor-fold defaultstate="collapsed" desc="Movement delegate">
//         Timer timer = new Timer();
//         timer.schedule(movementDelegate, 100);
//         MovementDelegate movementDelegate = new MovementDelegate(100);
//         new Thread().start();
//</editor-fold>

         for(int i=1 ;; i++){
            new Thread(new ClientHandler(serverSocket.accept())).start();
            System.out.println("Client " + i + " accepted");
         }
      } catch (IOException ex) {
         System.err.println(ex.getMessage());
      }
   }
// //<editor-fold defaultstate="collapsed" desc="MovementDelegate">
   
//   private static class MovementDelegate implements Runnable{
//      private String message;
//
//      private final char COMMA = ',';
//
//      private final long SEND_INTERVAL;
//
//      public MovementDelegate(long sendInterval){
//         this.message       = "";
//         this.SEND_INTERVAL = sendInterval;
//
//      }
//
//      public synchronized void addToMessage(String name, Coordinates coor){
//         addToMessage(name, coor.getX(), coor.getY());
//      }
//
//      public synchronized void addToMessage(String name, int x, int y){
//         if(message.isEmpty()) {
//            message = "IMAGE";
//         }
//
//         message += String.format("%s:%d:%d%c", name, x, y, COMMA);
//      }
//
//      @Override
//      public void run(){
//         while(true){
//            try {
//               Thread.sleep(this.SEND_INTERVAL);
//
//               if (message.isEmpty()) {
//                  continue;
//               }
//
//               allOutputStreams.forEach((os)->{
//                  try {
//                     os.write(message.getBytes());
//                     os.flush();
//                  } catch (IOException ex) {
//                     System.err.println(ex.getMessage());
//                  }
//               });
//
//               message = "";
//
//            } catch (InterruptedException ex) {
//               printError(ex);
//            }
//         }
//      }
//      private static void printError(InterruptedException ex) {
//         System.err.println(ex.getMessage());
//         System.err.println(Arrays.toString(ex.getStackTrace()));
//      }
//   }
//
//</editor-fold>
   
   private static class ClientHandler implements Runnable {
      private final Socket socket;
      private final DataOutputStream dOut;
      private final DataInputStream dIn;
      
      public ClientHandler(Socket socket) throws IOException {
         this.socket = socket;
         this.dIn    = new DataInputStream(socket.getInputStream());
         this.dOut   = new DataOutputStream(socket.getOutputStream());
      }

      @Override
      public void run() {
         try {
            String message = "Test";
            sendOutput(message);
            
            message = "Second";
            sendOutput(message);
            
         } catch (IOException ex) {
            closeSafely(dIn);
            closeSafely(dOut);
            closeSafely(socket);
            System.err.println(ex.getMessage());
//            removeClient();
         }
      }

      private void sendOutput(String message) throws IOException {
         dOut.writeShort(message.length());
         dOut.writeBytes(message);
         dOut.flush();
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
//      /**
//      * Request a name from this client.  Keep requesting until
//      * a name is submitted that is not already used.  Note that
//      * checking for the existence of a name and adding the name
//      * must be done while locking the set of names.
//      */
//      private String getValidClientName(BufferedReader input, OutputStream os) throws IOException {
//         boolean valid = false;
//         String name = "";
//
//         while (!valid) {
//            valid = true;
//            os.write("SUBMITNAME".getBytes());
//            os.flush();
//            name = input.readLine();
//            if (name == null || name.isEmpty()) {
//               valid = false;
//            }
//            if (allSheep.containsKey(name)) {
//               valid = false;
//            }
//         }
//         allSheep.put(name, new Coordinates());
//         return name;
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

      private void closeSafely(Closeable c) {
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
