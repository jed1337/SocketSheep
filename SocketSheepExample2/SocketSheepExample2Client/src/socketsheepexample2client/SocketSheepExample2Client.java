package socketsheepexample2client;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;
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
    String direc = "";
    JButton up = new JButton("UP");
    JButton down = new JButton("DOWN");
    JButton left = new JButton("LEFT");
    JButton right = new JButton("RIGHT");
    
    public SocketSheepExample2Client(){
        super("SHEEP");
        
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        
        up.addActionListener(this);
        down.addActionListener(this);
        left.addActionListener(this);
        right.addActionListener(this);
        
        c.fill = GridBagConstraints.HORIZONTAL;
	c.weightx = 0.5;
	c.gridx = 1;
	c.gridy = 0;
        c.gridheight = 1;
	c.gridwidth = 1;
	add(up, c);
        
        c.fill = GridBagConstraints.HORIZONTAL;
	c.weightx = 0.5;
	c.gridx = 1;
	c.gridy = 1;
        c.gridheight = 1;
	c.gridwidth = 1;
	add(down, c);
        
        c.fill = GridBagConstraints.VERTICAL;
	c.weightx = 0.5;
	c.gridx = 0;
	c.gridy = 0;
	c.gridheight = 2;
        c.gridwidth = 1;
	add(left, c);
        
        c.fill = GridBagConstraints.VERTICAL;
	c.weightx = 0.5;
	c.gridx = 2;
	c.gridy = 0;
	c.gridheight = 2;
        c.gridwidth = 1;
	add(right, c);
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setPreferredSize(new Dimension(250,150));
        pack();
	setLocationRelativeTo(null);
        setVisible(true);
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
            while(true){
                direc = returnDirec();
                if(!direc.isEmpty()){
                    System.out.println("UP IS ON!");
                    byte[] buffer = direc.getBytes();
                    out.write(buffer);
                    direc = "";
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
    
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == up){
            direc = UP;
            System.out.println(returnDirec());
        }
        else if(e.getSource() == down){
            direc = DOWN;
            System.out.println(returnDirec());
        }
        else if(e.getSource() == left){
            direc = LEFT;
            System.out.println(returnDirec());
        }
        else if(e.getSource() == right){
            direc = RIGHT;
            System.out.println(returnDirec());
        }
    }
    
    public String returnDirec(){
        return direc;
    }
}
