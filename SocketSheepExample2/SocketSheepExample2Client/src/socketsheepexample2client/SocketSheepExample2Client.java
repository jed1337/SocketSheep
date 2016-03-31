package socketsheepexample2client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.net.Socket;
import javax.swing.JButton;
import javax.swing.JFrame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.border.LineBorder;

public class SocketSheepExample2Client extends JFrame implements ActionListener {
   private final static int PORT = 4096;
   private final static String UP = "UP";
   private final static String DOWN = "DOWN";
   private final static String LEFT = "LEFT";
   private final static String RIGHT = "RIGHT";

   private String clientName;
   
   private final JButton jbUp   = new JButton(UP);
   private final JButton jbDown = new JButton(DOWN);
   private final JButton jbLeft = new JButton(LEFT);
   private final JButton jbRight = new JButton(RIGHT);
   private final ArrayList<JButton> buttons;
   
   private BufferedReader in;
   private PrintWriter pw;
   private final MyPanel myPanel;
   private static String OS = System.getProperty("os.name");
   
   public SocketSheepExample2Client() throws IOException {
      super("SHEEP");
      
        String s = "";
        if(OS.contains("Windows")){
            s = "\\";
        } else if(OS.contains("Mac")){
            s = "/";
        }

        buttons    = new ArrayList<>();
        myPanel    = new MyPanel("src"+s+"images"+s+"Sheep.jpg");

        setupGUI();
        setupProtocolReceiver();
   }
   
//<editor-fold defaultstate="collapsed" desc="Gui Things">
   
   private void setupGUI() {
      setLayout(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      myPanel.setPreferredSize(new Dimension(500,420));
      myPanel.setBorder(new LineBorder(Color.black, 1));
      
      addComponent(c, myPanel, GridBagConstraints.VERTICAL, 0, 0, 1, 3);
      
      addButton(c, jbUp, GridBagConstraints.HORIZONTAL, 1, 1, 1, 1);
      addButton(c, jbDown, GridBagConstraints.HORIZONTAL, 1, 2, 1, 1);
      addButton(c, jbLeft, GridBagConstraints.VERTICAL, 0, 1, 2, 1);
      addButton(c, jbRight, GridBagConstraints.VERTICAL, 2, 1, 2, 1);
      
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setResizable(false);
      setPreferredSize(new Dimension(600, 600));
      pack();
      setLocationRelativeTo(null);
      setVisible(true);
      
      System.out.println("");
   }
   private void addComponent(GridBagConstraints c, Component component,
                                                   int fill, int x, int y, int height, int width) {
      c.fill = fill;
      c.weightx = 0.5;
      c.gridx = x;
      c.gridy = y;
      c.gridheight = height;
      c.gridwidth = width;
      
      add(component, c);
   }
   private void addButton(GridBagConstraints c, JButton button,
                                                int fill, int x, int y, int height, int width) {
      
      addComponent(c, button, fill, x, y, height, width);
      
      button.addActionListener(this);
      button.setEnabled(false);
      buttons.add(button);
   }
//</editor-fold>
   
   private void setupProtocolReceiver() throws IOException {
      try {
         Socket socket = new Socket("::1", PORT);
         in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         pw = new PrintWriter(socket.getOutputStream(), true);
         
         System.out.println("");
         while (true) {
            String input = in.readLine();
            System.out.println(input);
            
            if(input!=null){
               if (input.startsWith("SUBMITNAME")) {
                   pw.println(getClientName());
               } else if (input.startsWith("NAMEACCEPTED")) {
                   enableButtons();
               } 
               else if (input.startsWith("IMAGE")){
                  handleImages(input);
               }
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
      clientName = JOptionPane.showInputDialog(this,
              "Choose a screen name:",
              "Screen name selection",
              JOptionPane.PLAIN_MESSAGE);
      return clientName;
   }

   private void handleImages(String input) throws NumberFormatException {
      String[] sCoordinates = input.substring(5).split(",");
      System.out.println("Size: "+sCoordinates.length);
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
      
      pw.println(clientName+": "+direction);
   }

   private void enableButtons() {
      buttons.stream().forEach((JButton button)->{
         button.setEnabled(true);
      });
      jbUp.doClick();
      jbDown.doClick();
   }
   
   public static void main(String[] args) throws IOException {
      new SocketSheepExample2Client();
   }
}
