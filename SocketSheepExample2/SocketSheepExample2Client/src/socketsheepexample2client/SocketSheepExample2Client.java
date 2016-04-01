package socketsheepexample2client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.border.LineBorder;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class SocketSheepExample2Client extends JFrame implements ActionListener, Runnable {
   private final static int PORT = 4096;
   private final static String UP = "UP";
   private final static String DOWN = "DOWN";
   private final static String LEFT = "LEFT";
   private final static String RIGHT = "RIGHT";

   private final JButton jbUp   = new JButton(UP);
   private final JButton jbDown = new JButton(DOWN);
   private final JButton jbLeft = new JButton(LEFT);
   private final JButton jbRight = new JButton(RIGHT);
   private final ArrayList<JButton> buttons;
   
   private final Random rand;
   private String clientName;
   
   private final MyPanel myPanel;
   private BufferedReader in;
   private PrintWriter pw;
   
   private final boolean randomMovements;
   
   private long start;
   
   public SocketSheepExample2Client(boolean randomMovements, String clientName) throws IOException {
      super("SHEEP");
      this.randomMovements = randomMovements;
      this.clientName      = clientName;

      String filePath = "src\\images\\Sheep.png";
      if(System.getProperty("os.name").contains("Mac")){
         filePath = filePath.replaceAll("\\\\", "/");
      }
      myPanel = new MyPanel(filePath);

      buttons = new ArrayList<>();
      rand = new Random();

      setupGUI();
   }
   
   public SocketSheepExample2Client(boolean randomMovements) throws IOException {
      this(randomMovements, null);
   }

//<editor-fold defaultstate="collapsed" desc="Gui Things">
   
   private void setupGUI() {
      setLayout(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      myPanel.setPreferredSize(new Dimension(800,520));
      myPanel.setBorder(new LineBorder(Color.black, 1));
      
      addComponent(c, myPanel, GridBagConstraints.VERTICAL, 0, 0, 1, 3);
      
      addButton(c, jbUp, GridBagConstraints.HORIZONTAL, 1, 1, 1, 1);
      addButton(c, jbDown, GridBagConstraints.HORIZONTAL, 1, 2, 1, 1);
      addButton(c, jbLeft, GridBagConstraints.VERTICAL, 0, 1, 2, 1);
      addButton(c, jbRight, GridBagConstraints.VERTICAL, 2, 1, 2, 1);
      
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setResizable(false);
      setPreferredSize(new Dimension(900, 600));
      pack();
      setLocationRelativeTo(null);
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
      buttons.add(button);
   }
//</editor-fold>

   @Override
   public void run(){
      try {
         Socket socket = new Socket("::1", PORT);
         in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         pw = new PrintWriter(socket.getOutputStream(), true);
         
         while (true) {
            String input = in.readLine();
            //System.out.println(input);
            
            if(input!=null){
               if (input.startsWith("SUBMITNAME")) {
                  pw.println(getClientName());
               } else if (input.startsWith("NAMEACCEPTED")) {
                  enableButtons();
               } else if (input.startsWith("IMAGE")){
                  int startParen = input.indexOf("(")+1;
                  int endParen = input.indexOf(")");
                  
                  handleImages(input);
                  
                  if(clientName.equals(input.substring(startParen, endParen))){
                     System.out.println(clientName+" latency: "+ (System.currentTimeMillis() - start)+" ms");
                  }
               }
            }
            if(randomMovements){
               randomMovement();
            }
         }
      } catch (IOException | NumberFormatException ex) {
         System.err.println(ex.getMessage());
      }
   }
   
   /**
    * Prompt for and return the desired screen clientName.
    */
   private String getClientName() {
      if(clientName==null){
         clientName = JOptionPane.showInputDialog(this,
                 "Choose a screen name:",
                 "Screen name selection",
                 JOptionPane.PLAIN_MESSAGE);
      }
      this.setTitle(this.getTitle()+": "+clientName);
      this.myPanel.setClientName(clientName);
      return clientName;
   }

   private void handleImages(String input) throws NumberFormatException {
      String[] sCoordinates = input.substring(input.indexOf(")")+1).split(",");
      int[] nCoordinates = new int[sCoordinates.length*2];
      
      for(int i=0;i<sCoordinates.length;i++){
         String[] split = sCoordinates[i].split(":");
         nCoordinates[i*2]     = Integer.parseInt(split[0]);
         nCoordinates[(i*2)+1] = Integer.parseInt(split[1]);
      }

      myPanel.updateCoordinates(nCoordinates);
   }

   @Override
   public void actionPerformed(ActionEvent e) {
      String direction = "";
      if (e.getSource() == jbUp) {
         direction = UP;
      } else if (e.getSource() == jbDown) {
         direction = DOWN;
      } else if (e.getSource() == jbLeft) {
         direction = LEFT;
      } else if (e.getSource() == jbRight) {
         direction = RIGHT;
      }
      start = System.currentTimeMillis();
      pw.println(clientName+": "+direction);
   }
   
   private void randomMovement(){
      buttons.get(rand.nextInt(buttons.size())).doClick();
   }

   private void enableButtons() {
      buttons.stream().forEach((JButton button)->{
         button.setEnabled(true);
      });
      jbUp.doClick();
      jbDown.doClick();
   }
   
   public static void main(String[] args) throws IOException {
      int SHEEP_LIMIT         = 10;
      boolean randomMovements = true;
      
      SocketSheepExample2Client sheep = new SocketSheepExample2Client(randomMovements, "Client -1");
      sheep.setVisible(true);
      new Thread(sheep).start();
      
      for(int i=0;i<SHEEP_LIMIT;i++){
         new Thread(new SocketSheepExample2Client(randomMovements, "Client "+i)).start();
      }
   }
}
