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
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server implements Runnable{
//   private final static int PORT = 4096;
   private final ServerSocket serverSocket;
   
   private final ConcurrentHashMap<Integer, Coordinates> serverSheep;
   private final List<DataOutputStream> serverDOut;
   private final MovementDelegate mDelegate;

   public Server(int port, MovementDelegate mDelegate) throws IOException {
      this.serverSocket = new ServerSocket(port);
      
      this.serverSheep  = new ConcurrentHashMap<>();
      this.serverDOut   = new CopyOnWriteArrayList<>();
      
      this.mDelegate    = mDelegate;
      this.mDelegate.addToServers(this);
      
//      System.out.println("Server started!");
   }
   
   @Override
   public void run(){
      for (int i = 1;; i++) {
         try {
            new Thread(new ClientHandler(serverSocket.accept(), mDelegate)).start();
            System.out.println("Client " + i + " accepted in ServerSocket port "+serverSocket.getLocalPort());
         } catch (IOException ex) {
            Logger.getLogger(SocketSheepExample2Server.class.getName()).log(Level.SEVERE, null, ex);
         }
      }
   }
   
   private class ClientHandler implements Runnable {
      private final Socket socket;
      private final DataOutputStream dOut;
      private final DataInputStream dIn;
      
      private final MovementDelegate mDelegate;
      
      private int clientID;
      
      public ClientHandler(Socket socket, MovementDelegate mDelegate) throws IOException {
         this.socket = socket;
         this.dIn    = new DataInputStream(socket.getInputStream());
         this.dOut   = new DataOutputStream(socket.getOutputStream());
//         allServerDOut.add(dOut);
         
         this.mDelegate = mDelegate;
      }

      @Override
      public void run() {
         try {
            sendOutput(dOut, "SUBMITNAME");
            this.clientID = Integer.parseInt(getInput());
            sendOutput(dOut, "NEW_USER");
            
            //Logic
            //Only do the following once it has finished setting up
            //To avoid concurrently writing to the same stream
            serverSheep.put(this.clientID, new Coordinates());
            serverDOut.add(dOut);
            
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
               this.mDelegate.addToMessage(clientID+":"+serverSheep.get(clientID));
            }
         } catch (IOException | InterruptedException ex) {
            printErrors(ex);
         } finally{
            serverSheep.remove(clientID);
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
         
         serverSheep.get(cNumber).updateLocation(Constants.getDirection(direction));
//         System.out.println("direction = " + direction);
      }
   }
   
   public void sendOutputAll(String message) throws IOException {
      for (int i = 0; i < serverDOut.size(); i++) {
         sendOutput(serverDOut.get(i), message);
      }
   }
   
   private void sendOutput(DataOutputStream dOut, String message) throws IOException {
      dOut.writeShort(message.length());
      dOut.writeBytes(message);
      dOut.flush();
   }
   
   private void printErrors(Exception ex) {
      System.err.println(ex.getMessage());
      Arrays.stream(ex.getStackTrace()).forEach(System.err::println);
   }

   private void closeSafely(Closeable c) {
      try{
         if (c != null) {
            c.close();
         }
      } catch (IOException ex) {
         System.err.println(ex.getMessage());
      }
   }
}
