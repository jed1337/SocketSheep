package socketsheepexample2server;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

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
                  
         MovementDelegate mDelegate = new MovementDelegate(250);
         new Thread(mDelegate).start();

         for(int i=1 ;; i++){
            new Thread(new ClientHandler(serverSocket.accept(), mDelegate)).start();
            System.out.println("Client " + i + " accepted");
         }
      } catch (IOException ex) {
         System.err.println(ex.getMessage());
      }
   }
   
   private static class MovementDelegate implements Runnable{
      private final long SEND_INTERVAL;
      private final LinkedBlockingQueue<Integer> movedClients;
      private StringBuilder sb;
      
      public MovementDelegate(long sendInterval){
         this.SEND_INTERVAL = sendInterval;
         this.movedClients  = new LinkedBlockingQueue<>();
      }

      public void addToMessage(Integer clientID) throws InterruptedException{
         this.movedClients.put(clientID);
      }

      @Override
      public void run(){
         while(true){
            try {
               Thread.sleep(this.SEND_INTERVAL);

               int size = movedClients.size();
               if (size == 0) {
                  continue;
               }

               sb = new StringBuilder("IMAGE");
               while(size > 0){
                  int cID = this.movedClients.take();
                  sb.append(cID);
                  sb.append(":");
                  sb.append(allSheep.get(cID));
                  sb.append(",");
                  size--;
               }
               sendOutputAll(sb.toString());

            } catch (IOException | InterruptedException ex) {
               printErrors(ex);
            }
         }
      }
   }
   
   private static class ClientHandler implements Runnable {
      private final Socket socket;
      private final DataOutputStream dOut;
      private final DataInputStream dIn;
      
      private final MovementDelegate mDelegate;
      
      private Integer clientID;
      
      public ClientHandler(Socket socket, MovementDelegate mDelegate) throws IOException {
         this.socket = socket;
         this.dIn    = new DataInputStream(socket.getInputStream());
         this.dOut   = new DataOutputStream(socket.getOutputStream());
         allDOuts.add(dOut);
         
         this.mDelegate = mDelegate;
      }

      @Override
      public void run() {
         try {
            sendOutput(dOut, "SUBMITNAME");
            this.clientID = Integer.parseInt(getInput());
            allSheep.put(this.clientID, new Coordinates());
            sendOutput(dOut, "NEW_USER");
            
//<editor-fold defaultstate="collapsed" desc="Tester messages">
//            String message = "Test";
//            sendOutput(message);
//
//            message = "Second";
//            sendOutput(message);
//</editor-fold>
            
            while(true){
               String input = getInput();
//               System.out.println("input = " + input);
               updateSheepLocation(input);
               this.mDelegate.addToMessage(clientID);
            }
         } catch (IOException | InterruptedException ex) {
            printErrors(ex);
         } finally{
            allSheep.remove(clientID);
            closeSafely(dIn);
            closeSafely(dOut);
            closeSafely(socket);
         }
      }

      private String getInput() throws IOException {
         short procLength = dIn.readShort();
//         System.out.println("procLength = " + procLength);
         byte[] bProcData = new byte[procLength];
         dIn.readFully(bProcData);
         String input = new String(bProcData, StandardCharsets.UTF_8);
         return input;
      }
      
      private void updateSheepLocation(String clientInput) {
         String[] temp    = clientInput.split(":");
         int cNumber      = Integer.parseInt(temp[0]);
         String direction = temp[1];
         
         allSheep.get(cNumber).updateLocation(Constants.getDirection(direction));
//         System.out.println("direction = " + direction);
      }
   }

   private static void sendOutputAll(String message) throws IOException {
      for(int i=0;i<allDOuts.size();i++){
         sendOutput(allDOuts.get(i), message);
      }
   }

   private static void sendOutput(DataOutputStream dOut, String message) throws IOException {
      dOut.writeShort(message.length());
      dOut.writeBytes(message);
      dOut.flush();
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
