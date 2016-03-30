package socketsheepexample2client;

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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.function.Consumer;
import javax.swing.JOptionPane;
//import java.awt.event.KeyAdapter;
//import java.awt.event.KeyEvent;
//import java.awt.event.KeyListener;

public class SocketSheepExample2Client extends JFrame implements ActionListener {
   private final static int PORT = 4096;
   private final static String UP = "UP";
   private final static String DOWN = "DOWN";
   private final static String LEFT = "LEFT";
   private final static String RIGHT = "RIGHT";
   String direction = "";

   private final JButton jbUp = new JButton(UP);
   private final JButton jbDown = new JButton(DOWN);
   private final JButton jbLeft = new JButton(LEFT);
   private final JButton jbRight = new JButton(RIGHT);
   
   private ArrayList<JButton> buttons;
   
   private BufferedReader in;
   private PrintWriter pw;

   public SocketSheepExample2Client() {
      super("SHEEP");

//<editor-fold defaultstate="collapsed" desc="Add GUI">
      buttons = new ArrayList<>();
      
      setLayout(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();

      addButton(c, jbUp, GridBagConstraints.HORIZONTAL, 1, 0, 1, 1);
      addButton(c, jbDown, GridBagConstraints.HORIZONTAL, 1, 1, 1, 1);
      addButton(c, jbLeft, GridBagConstraints.VERTICAL, 0, 0, 2, 1);
      addButton(c, jbRight, GridBagConstraints.VERTICAL, 2, 0, 2, 1);

      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setResizable(false);
      setPreferredSize(new Dimension(250, 150));
      pack();
      setLocationRelativeTo(null);
      setVisible(true);
//</editor-fold>
      
   }

   private void addButton(GridBagConstraints c, JButton button, 
                           int fill, int x, int y, int height, int weight) {
      button.addActionListener(this);
      button.setEnabled(false);
      
      c.fill = fill;
      c.weightx = 0.5;
      c.gridx = x;
      c.gridy = y;
      c.gridheight = height;
      c.gridwidth = weight;
      add(button, c);
      
      buttons.add(button);
   }

   private void run() throws IOException {
      try {
         Socket socket = new Socket("::1", PORT);
         OutputStream outputStream = socket.getOutputStream();
            // <editor-fold desc="Old Code">
                /*Socket socket = new Socket("::1", PORT);
          OutputStream outputStream = socket.getOutputStream();

          File f = new File("src/images/Sheep.jpg");
          BufferedImage image = ImageIO.read(f);
          ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
          ImageIO.write(image, "jpg", byteArrayOutputStream);
          byte[] size = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();

          outputStream.write(size);
          outputStream.write(byteArrayOutputStream.toByteArray());
          outputStream.flush();
          socket.close();*/
         //</editor-fold>
         in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         pw = new PrintWriter(socket.getOutputStream(), true);
         
         while (true) {
            String input = in.readLine();
            System.out.println(input);
            if (input.startsWith("SUBMITNAME")) {
                pw.println(getClientName());
            } else if (input.startsWith("NAMEACCEPTED")) {
                enableButtons();
            }
            
//                direction = returnDirec();
//            System.out.println("Direction is '"+direction+"'");
//            if (!direction.isEmpty()) {
//               System.out.println("UP IS ON!");
//               byte[] buffer = direction.getBytes();
//               out.write(buffer);
//               System.out.println(direction);
////                  direction = "";
//            }
         }
      } catch (Exception ex) {
         System.err.println(ex.getMessage());
      }
   }

   public static void main(String[] args) throws IOException {
      SocketSheepExample2Client client = new SocketSheepExample2Client();
      client.run();
   }

   @Override
   public void actionPerformed(ActionEvent e) {
      if (e.getSource() == jbUp) {
         direction = UP;
      } else if (e.getSource() == jbDown) {
         direction = DOWN;
      } else if (e.getSource() == jbLeft) {
         direction = LEFT;
      } else if (e.getSource() == jbRight) {
         direction = RIGHT;
      }
   }

   /**
    * Prompt for and return the desired screen name.
    */
   private String getClientName() {
      //Save name somewhere
      return JOptionPane.showInputDialog(
              this,
              "Choose a screen name:",
              "Screen name selection",
              JOptionPane.PLAIN_MESSAGE);
   }

   private void enableButtons() {
      buttons.stream().forEach((JButton button)->{
         button.setEnabled(true);
      });
   }
}
