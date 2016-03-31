package socketsheepexample2client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import javax.swing.JButton;
import javax.swing.JFrame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
//import java.awt.event.KeyAdapter;
//import java.awt.event.KeyEvent;
//import java.awt.event.KeyListener;

public class SocketSheepExample2Client extends JFrame implements ActionListener {
   private final static int PORT = 4096;
   private final static String UP = "UP";
   private final static String DOWN = "DOWN";
   private final static String LEFT = "LEFT";
   private final static String RIGHT = "RIGHT";

   private String clientName;
   
   private final JButton jbUp = new JButton(UP);
   private final JButton jbDown = new JButton(DOWN);
   private final JButton jbLeft = new JButton(LEFT);
   private final JButton jbRight = new JButton(RIGHT);
   
   private final JPanel pnl = new JPanel();
   
   private final ArrayList<JButton> buttons;
   
   private BufferedReader in;
   private PrintWriter pw;
   
   private BufferedImage sheep;
   private JLabel sheepLabel;
   
   
   public SocketSheepExample2Client() throws IOException {
      super("SHEEP");

//<editor-fold defaultstate="collapsed" desc="Add GUI">
      buttons = new ArrayList<>();
      
      setLayout(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      pnl.setPreferredSize(new Dimension(500,420));
      pnl.setBorder(new LineBorder(Color.black, 1));
      
      c.fill = GridBagConstraints.HORIZONTAL;
      c.weightx = 0.5;
      c.gridx = 0;
      c.gridy = 0;
      c.gridheight = 1;
      c.gridwidth = 3;
      add(pnl, c);

      sheep      = ImageIO.read(new File("src\\images\\Sheep.jpg"));
      sheepLabel = new JLabel(new ImageIcon(sheep));
      
      addButton(c, jbUp, GridBagConstraints.HORIZONTAL, 1, 1, 1, 1);
      addButton(c, jbDown, GridBagConstraints.HORIZONTAL, 1, 2, 1, 1);
      addButton(c, jbLeft, GridBagConstraints.VERTICAL, 0, 1, 2, 1);
      addButton(c, jbRight, GridBagConstraints.VERTICAL, 2, 1, 2, 1);

      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setResizable(false);
      setPreferredSize(new Dimension(500, 500));
      pack();
      setLocationRelativeTo(null);
      setVisible(true);
//</editor-fold>
      
      setup();
   }

   private void addButton(GridBagConstraints c, JButton button, 
                           int fill, int x, int y, int height, int width) {
      button.addActionListener(this);
      button.setEnabled(false);
      
      c.fill = fill;
      c.weightx = 0.5;
      c.gridx = x;
      c.gridy = y;
      c.gridheight = height;
      c.gridwidth = width;
      add(button, c);
      
      buttons.add(button);
   }

   private void setup() throws IOException {
      try {
         Socket socket = new Socket("::1", PORT);
         OutputStream outputStream = socket.getOutputStream();
         
         in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         pw = new PrintWriter(socket.getOutputStream(), true);
//         
         while (true) {
            String input = in.readLine();
            System.out.println(input);
            if (input.startsWith("SUBMITNAME")) {
                pw.println(getClientName());
            } else if (input.startsWith("NAMEACCEPTED")) {
                enableButtons();
            } else if (input.startsWith("IMAGE")){
               System.out.println("IMAGE");
               String[] sCoordinates = input.substring(5).split(",");
               int[] nCoordinates = new int[sCoordinates.length*2];
               
               for(int i=0;i<sCoordinates.length;i++){
                  String[] split = sCoordinates[i].split(":");
                  nCoordinates[i*2]     = Integer.parseInt(split[0]);
                  nCoordinates[(i*2)+1] = Integer.parseInt(split[1]);
               }
               
               for(int i=0;i<nCoordinates.length;i+=2){
                  
               }
               
//               for(String coordinate : sCoordinates){
//                  String[] split = coordinate.split(":");
//                  int x = Integer.parseInt(split[0]);
//                  int y = Integer.parseInt(split[1]);
//               }
//               BufferedImage img = ImageIO.read(new File(input.substring("IMAGE".length())));
//               pnl.add(new JLabel(new ImageIcon(img)));
            }
         }
      } catch (Exception ex) {
         System.err.println(ex.getMessage());
      }
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
      direction = "";
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

   private void enableButtons() {
      buttons.stream().forEach((JButton button)->{
         button.setEnabled(true);
      });
   }
   
   public static void main(String[] args) throws IOException {
      new SocketSheepExample2Client();
//      client.run();
   }
}
