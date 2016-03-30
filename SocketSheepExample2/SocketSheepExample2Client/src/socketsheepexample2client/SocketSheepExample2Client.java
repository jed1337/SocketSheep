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
//import java.awt.event.KeyAdapter;
//import java.awt.event.KeyEvent;
//import java.awt.event.KeyListener;

public class SocketSheepExample2Client extends JFrame implements ActionListener{
    private final static int PORT = 4096;
    private final static String UP = "UP";
    private final static String DOWN = "DOWN";
    private final static String LEFT = "LEFT";
    private final static String RIGHT = "RIGHT";
    String direction = "";
    
    private JButton jbUp    = new JButton(UP);
    private JButton jbDown  = new JButton(DOWN);
    private JButton jbLeft  = new JButton(LEFT);
    private JButton jbRight = new JButton(RIGHT);
    
   public SocketSheepExample2Client() {
      super("SHEEP");

//<editor-fold defaultstate="collapsed" desc="Add GUI">
      setLayout(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();

      jbUp.addActionListener(this);
      jbDown.addActionListener(this);
      jbLeft.addActionListener(this);
      jbRight.addActionListener(this);

      c.fill = GridBagConstraints.HORIZONTAL;
      c.weightx = 0.5;
      c.gridx = 1;
      c.gridy = 0;
      c.gridheight = 1;
      c.gridwidth = 1;
      add(jbUp, c);

      c.fill = GridBagConstraints.HORIZONTAL;
      c.weightx = 0.5;
      c.gridx = 1;
      c.gridy = 1;
      c.gridheight = 1;
      c.gridwidth = 1;
      add(jbDown, c);

      c.fill = GridBagConstraints.VERTICAL;
      c.weightx = 0.5;
      c.gridx = 0;
      c.gridy = 0;
      c.gridheight = 2;
      c.gridwidth = 1;
      add(jbLeft, c);

      c.fill = GridBagConstraints.VERTICAL;
      c.weightx = 0.5;
      c.gridx = 2;
      c.gridy = 0;
      c.gridheight = 2;
      c.gridwidth = 1;
      add(jbRight, c);

      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setResizable(false);
      setPreferredSize(new Dimension(250, 150));
      pack();
      setLocationRelativeTo(null);
      setVisible(true);
//</editor-fold>
   }

   private void run() throws IOException {
      try {
         Socket socket = new Socket("::1", PORT);
         OutputStream out = socket.getOutputStream();
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
         while (true) {
//                direction = returnDirec();
//            System.out.println("Direction is '"+direction+"'");
            synchronized(direction){
               if (!direction.isEmpty()) {
                  System.out.println("UP IS ON!");
                  byte[] buffer = direction.getBytes();
                  out.write(buffer);
                  System.out.println(direction);
//                  direction = "";
               }
            }
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
}
