package socketsheepexample2client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import javax.swing.border.LineBorder;
import javax.swing.JButton;
import javax.swing.JFrame;

public class SocketSheepExample2Client extends JFrame implements ActionListener, Runnable {
   private final static int PORT     = 4096;
   private final static String UP    = "U";
   private final static String DOWN  = "D";
   private final static String LEFT  = "L";
   private final static String RIGHT = "R";

   private final JButton JB_UP    = new JButton("Up");
   private final JButton JB_DOWN  = new JButton("Down");
   private final JButton JB_LEFT  = new JButton("Left");
   private final JButton JB_RIGHT = new JButton("Right");
   private final ArrayList<JButton> BUTTONS;
   
   private final MyPanel MY_PANEL;
   
   private final Random RAND;
   private final int clientID;
   
   private final boolean RANDOM_MOVEMENTS;
   
   private long start;
   private static int num = 1;

   private final Socket socket;
   private final DataOutputStream dOut;
   private final DataInputStream dIn;
   
   public SocketSheepExample2Client(boolean randomMovements, int clientID) throws IOException {
      super("SHEEP");
      this.RANDOM_MOVEMENTS = randomMovements;
      this.clientID         = clientID;
      
      if(num%2==1){
        this.socket = new Socket("::1", PORT);
        System.out.println("PORT 4096 entered : " + num);
        num++;
      } else {
        this.socket = new Socket("::1", PORT+1); 
        System.out.println("PORT 4097 entered : " + num);
        num++;
      }
      
//        this.socket = new Socket("::1", PORT);
      
      this.dIn    = new DataInputStream(socket.getInputStream());
      this.dOut   = new DataOutputStream(socket.getOutputStream());
      
      String filePath = "src\\images\\Sheep.png";
      if(System.getProperty("os.name").contains("Mac")){
         filePath = filePath.replaceAll("\\\\", "/");
      }
      
      this.MY_PANEL = new MyPanel(filePath, clientID);
      this.BUTTONS = new ArrayList<>();
      this.RAND    = new Random();
      setupGUI();
      
      this.MY_PANEL.updateSize();
   }
   
//<editor-fold defaultstate="collapsed" desc="Gui Things">
   
   private void setupGUI() {
      setLayout(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      MY_PANEL.setPreferredSize(new Dimension(800,520));
      MY_PANEL.setBorder(new LineBorder(Color.black, 1));
      
      addComponent(c, MY_PANEL, GridBagConstraints.VERTICAL, 0, 0, 1, 3);
      
      addButton(c, JB_UP, GridBagConstraints.HORIZONTAL, 1, 1, 1, 1);
      addButton(c, JB_DOWN, GridBagConstraints.HORIZONTAL, 1, 2, 1, 1);
      addButton(c, JB_LEFT, GridBagConstraints.VERTICAL, 0, 1, 2, 1);
      addButton(c, JB_RIGHT, GridBagConstraints.VERTICAL, 2, 1, 2, 1);
      
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setResizable(false);
      setPreferredSize(new Dimension(850, 600));
      pack();
//      setVisible(true);
   }
   private void addComponent(GridBagConstraints c, Component component,int fill, 
                              int x, int y, int height, int width) {
      c.fill = fill;
      c.weightx = 0.5;
      c.gridx = x;
      c.gridy = y;
      c.gridheight = height;
      c.gridwidth = width;
      
      add(component, c);
   }
   private void addButton(GridBagConstraints c, JButton button,int fill, 
                              int x, int y, int height, int width) {
      
      addComponent(c, button, fill, x, y, height, width);
      
      button.addActionListener(this);
      button.setEnabled(false);

      BUTTONS.add(button);
   }
//</editor-fold>

   @Override
   public void run(){
      try {
         
         while (true) {
            String input = getInput();

            if(input.startsWith("SUBMITNAME")){
               sendOutput(Integer.toString(clientID));
            } 
            else if (input.startsWith("NEW_USER")){
               enableButtons();
            } 
            else if (input.startsWith("IMG")) {
               handleImages(input);
            }
            if(RANDOM_MOVEMENTS){
               randomMovement();
            }
//            System.out.println("input = " + input);
         }
         
      } catch (IOException | NumberFormatException ex) {
         printErrors(ex);
      } finally{
         closeSafely(dIn);
         closeSafely(dOut);
         closeSafely(socket);
      }
   }

   private void sendOutput(String message) throws IOException {
      dOut.writeShort(message.length());
      dOut.writeBytes(message);
      dOut.flush();
   }
   
   private String getInput() throws IOException {
      short procLength = dIn.readShort();
//      System.out.println("procLength = " + procLength);
      byte[] bProcData = new byte[procLength];
      dIn.readFully(bProcData);
      String input = new String(bProcData, StandardCharsets.UTF_8);
      return input;
   }
   
   private void printErrors(Exception ex) {
      System.err.println(ex.getMessage());
      Arrays.stream(ex.getStackTrace()).forEach(System.err::println);
   }

   private void closeSafely(Closeable c) {
      try {
         if (c != null) {
            c.close();
         }
      } catch (IOException ex) {
         System.err.println(ex.getMessage());
      }
   }
   
   private int[] parseClientAndCoordinates(String[] movedClients){
      int[] nProc = new int[movedClients.length*2];

      for(int i=0;i<movedClients.length;i++){
         String[] split = movedClients[i].split(":");
         int index      = 0;

         try {
            nProc[(i*2) + index] = Integer.parseInt(split[(index++)]); //ID
            nProc[(i*2) + index] = Integer.parseInt(split[(index++)]); //XY
         } catch (NumberFormatException ex) {
            System.err.println("Moved clients is: "+Arrays.toString(movedClients));
            System.err.println("Split is: "+ Arrays.toString(split));
            System.err.println("NProc is: "+ Arrays.toString(nProc));
            printErrors(ex);
         }
      }
      return nProc;
   }
   
   private void handleImages(String input) throws NumberFormatException {
      int[] procDetails = parseClientAndCoordinates(input.substring(3).split(","));

      boolean moved = false;
      for(int i=0;i<procDetails.length;i+=2){
         if(procDetails[i] == clientID){
            moved = true;
            break;
         }
      }

      this.MY_PANEL.updateCoordinates(procDetails, moved? start : -1);
   }

//<editor-fold defaultstate="collapsed" desc="Movement and set StartTime">
   @Override
   public void actionPerformed(ActionEvent e) {
      String direction = "";
      if (e.getSource() == JB_UP) {
         direction = UP;
      } else if (e.getSource() == JB_DOWN) {
         direction = DOWN;
      } else if (e.getSource() == JB_LEFT) {
         direction = LEFT;
      } else if (e.getSource() == JB_RIGHT) {
         direction = RIGHT;
      }
      start = System.currentTimeMillis();
      try {
         sendOutput(clientID+":"+direction);
      } catch (IOException ex) {
         printErrors(ex);
      }
   }
   
   private void randomMovement(){
      BUTTONS.get(RAND.nextInt(BUTTONS.size())).doClick();
   }
   
   private void enableButtons() {
      BUTTONS.stream().forEach((JButton button)->{
         button.setEnabled(true);
      });
      JB_UP.doClick();
      JB_UP.doClick();
   }
//</editor-fold>
   
//<editor-fold defaultstate="collapsed" desc="Single or Multi client">
   public static void singleClient() throws IOException, InterruptedException{
      System.out.println("Started single client");
      
      SocketSheepExample2Client sheep = new SocketSheepExample2Client(false, -1337);
      sheep.setVisible(true);
      new Thread(sheep).start();
   }
   
   private static void multiClient(int SHEEP_LIMIT) throws IOException, InterruptedException {
       multiClient(SHEEP_LIMIT, 0);
   }
   
   private static void multiClient(int SHEEP_LIMIT, long TIME) throws IOException, InterruptedException{
      singleClient();
      
      System.out.println("Started multi client");
      
      for (int i = 1; i<=SHEEP_LIMIT; i++) {
         new Thread(new SocketSheepExample2Client(true, i)).start();
         Thread.sleep(TIME);
      }
   }
//</editor-fold>
   
   public static void main(String[] args) throws IOException, InterruptedException {
//      singleClient();
//      multiClient(10);
//      multiClient(50);
      multiClient(100);
//      multiClient(150);
//      multiClient(200);
   }
}
