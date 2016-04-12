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
   private final static String UP    = "UP";
   private final static String DOWN  = "DOWN";
   private final static String LEFT  = "LEFT";
   private final static String RIGHT = "RIGHT";

   private final JButton JB_UP    = new JButton(UP);
   private final JButton JB_DOWN  = new JButton(DOWN);
   private final JButton JB_LEFT  = new JButton(LEFT);
   private final JButton JB_RIGHT = new JButton(RIGHT);
   private final ArrayList<JButton> BUTTONS;
   
   private final Random RAND;
   private final int clientNumber;
   
   private final MyPanel MY_PANEL;
   
   private final boolean RANDOM_MOVEMENTS;
   
   private long start;

   private final Socket socket;
   private final DataOutputStream dOut;
   private final DataInputStream dIn;
   
   public SocketSheepExample2Client(boolean randomMovements, int clientNumber) throws IOException {
      super("SHEEP");
      this.RANDOM_MOVEMENTS = randomMovements;
      this.clientNumber     = clientNumber;

      this.socket = new Socket("::1", PORT);
      this.dIn    = new DataInputStream(socket.getInputStream());
      this.dOut   = new DataOutputStream(socket.getOutputStream());
      
      String filePath = "src\\images\\Sheep.png";
      if(System.getProperty("os.name").contains("Mac")){
         filePath = filePath.replaceAll("\\\\", "/");
      }
      
      this.MY_PANEL = new MyPanel(filePath);
      this.BUTTONS = new ArrayList<>();
      this.RAND    = new Random();
      setupGUI();
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
//      setLocationRelativeTo(null);
//      setVisible(true);
      //System.out.println("");
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
               sendOutput(Integer.toString(clientNumber));
            }
            System.out.println("input = " + input);
         }
         
//         while (true) {//<editor-fold defaultstate="collapsed" desc="Old Implementation">
         
//
//            if (in.ready()) {
//               String input = in.readLine();
//
//               if (input != null) {
//                  if (input.startsWith("SUBMITNAME")) {
//                     printWriter.println(getClientName());
//                  } else if (input.startsWith("NEW_USER")) {
//                     String[] sCoor = input.substring(8).split(":");
//                     String cName   = sCoor[0];
//                     int cXCoor     = Integer.parseInt(sCoor[1]);
//                     int cYCoor     = Integer.parseInt(sCoor[2]);
//
//                     MY_PANEL.addSheep(cName, new int[]{cXCoor, cYCoor});
//
//                     enableButtons();
//                  } else if (input.startsWith("GET_CURRENT_USERS")) {
//                     Object[] pDetails = parseClientAndCoordinates(input.substring(17).split(","));
//                     String[] cNames = (String[]) pDetails[0];
//                     int[] cCoor = (int[]) pDetails[1];
//
//                     for (int i = 0; i < cNames.length; i++) {
//                        if (cNames[i].equals(clientName)) {
//                           continue;
//                        }
//
//                        int cXCoor = cCoor[(i * 2)];
//                        int cYCoor = cCoor[(i * 2) + 1];
//
//                        MY_PANEL.addSheep(cNames[i], new int[]{cXCoor, cYCoor});
//                     }
//                  } else if (input.startsWith("REMOVE_USER")) {
//                     MY_PANEL.removeSheep(input.substring(11));
//                  } else if (input.startsWith("IMAGE")) {
//                     handleImages(input);
//                  }
//               }
//               if (RANDOM_MOVEMENTS) {
//                  randomMovement();
//               }
//             }
//          }
//</editor-fold>

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
      System.out.println("procLength = " + procLength);
      byte[] bProcData = new byte[procLength];
      dIn.readFully(bProcData);
      String input = new String(bProcData, StandardCharsets.UTF_8);
      return input;
   }
   
   private void printErrors(Exception ex) {
      System.err.println(ex.getMessage());
      System.err.println(Arrays.toString(ex.getStackTrace()));
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
   
//   //<editor-fold defaultstate="collapsed" desc="Old implementation functions">
//   private Object[] parseClientAndCoordinates(String[] movedClients){
//      String[] cNames    = new String[movedClients.length];
//      int[] cCoordinates = new int[movedClients.length*2];
//
//      for(int i=0;i<movedClients.length;i++){
//         String[] split        = movedClients[i].split(":");
//         cNames[i]             = split[0];
//         cCoordinates[(i*2)]   = Integer.parseInt(split[1]);
//         cCoordinates[(i*2)+1] = Integer.parseInt(split[2]);
//      }
//      return new Object[]{cNames, cCoordinates};
//   }
//
//   private void handleImages(String input) throws NumberFormatException {
//      Object[] protocolDetails = parseClientAndCoordinates(input.substring(5).split(","));
//      String[] cNames          = (String[])protocolDetails[0];
//
//      boolean moved   = Arrays.stream(cNames).anyMatch((cn)->cn.equals(clientName));
//
//      MY_PANEL.updateCoordinates(protocolDetails, moved? start : -1);
//   }
//
//
//   /**
//    * Prompt for and return the desired screen clientName.
//    */
//   private String getClientName() {
//      if(clientName==null){
//         clientName = JOptionPane.showInputDialog(this,
//            "Choose a screen name:",
//            "Screen name selection",
//            JOptionPane.PLAIN_MESSAGE);
//      }
//      this.setTitle(this.getTitle()+": "+clientName);
//      this.MY_PANEL.setClientName(clientName);
//      return clientName;
//   }
//</editor-fold>

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
//      printWriter.println(clientNumber+": "+direction);
   }
   
   private void randomMovement(){
      BUTTONS.get(RAND.nextInt(BUTTONS.size())).doClick();
   }
   
   private void enableButtons() {
      BUTTONS.stream().forEach((JButton button)->{
         button.setEnabled(true);
      });
      JB_UP.doClick();
      JB_DOWN.doClick();
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
      singleClient();
//      multiClient(20);
//      multiClient(20, 1000);
   }
}
