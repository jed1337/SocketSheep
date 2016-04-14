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
      if(this.movedClients.contains(clientAndCoor)){
         this.movedClients.remove();
      }
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

            sb = new StringBuilder("IMG");
            while (size > 0) {
               sb.append(this.movedClients.take());
               sb.append(",");
               size--;
            }

            for (int i = 0; i < servers.size(); i++) {
               servers.get(i).sendOutputAll(sb.toString());
            }
         } catch (IOException | InterruptedException ex) {
            PrintErrors.log(ex);
         }
      }
   }
}
