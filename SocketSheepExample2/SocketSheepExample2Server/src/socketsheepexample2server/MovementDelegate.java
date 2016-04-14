package socketsheepexample2server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class MovementDelegate implements Runnable {
   private final long SEND_INTERVAL;
   private final LinkedBlockingQueue<String> movedClients;
   private final ArrayList<Server> servers;
   
   private StringBuilder sb;
   
   public MovementDelegate(long sendInterval) {
      this.SEND_INTERVAL = sendInterval;
      this.movedClients  = new LinkedBlockingQueue<>();
      this.servers       = new ArrayList<>();
   }
   
   public void addToServers(Server server){
      this.servers.add(server);
   }
   
   public void addToMessage(String clientAndCoor) throws InterruptedException {
      this.movedClients.put(clientAndCoor);
   }

   @Override
   public void run() {
      while (true) {
         try {
            Thread.sleep(this.SEND_INTERVAL);

            int size = movedClients.size();
            if (size == 0) {
               continue;
            }

            sb = new StringBuilder("IMAGE");
            while (size > 0) {
               sb.append(this.movedClients.take());
               sb.append(",");
               size--;
//<editor-fold defaultstate="collapsed" desc="Old Code">
//               int cID = this.movedClients.take();
//               sb.append(cID);
//               sb.append(":");
//               sb.append(allServerSheep.get(cID));
//               sb.append(",");
//</editor-fold>
            }

            for (int i = 0; i < servers.size(); i++) {
               servers.get(i).sendOutputAll(sb.toString());
            }
//            sendOutputAll(sb.toString());

         } catch (IOException | InterruptedException ex) {
//            printErrors(ex);
            System.err.println(ex.getMessage());
         }
      }
   }
}
