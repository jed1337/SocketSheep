package socketsheepexample2client;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;
//import java.awt.event.KeyAdapter;
//import java.awt.event.KeyEvent;
//import java.awt.event.KeyListener;

public class SocketSheepExample2Client {
    private final static int PORT = 4096;
    private char keyCode; 
    
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("::1", PORT);
            OutputStream outputStream = socket.getOutputStream();

            File f = new File("src/images/Sheep.jpg");
            BufferedImage image = ImageIO.read(f);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", byteArrayOutputStream);
            byte[] size = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();

            outputStream.write(size);
            outputStream.write(byteArrayOutputStream.toByteArray());
            outputStream.flush();
            socket.close();
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }
    
    /*class KeyChecker implements KeyListener{
        @Override
        public void keyPressed(KeyEvent event) {
            keyCode = event.getKeyChar();
            System.out.println(event.getKeyChar());
        }

        @Override
        public void keyTyped(KeyEvent e) {
            throw new UnsupportedOperationException(""); 
        }

        @Override
        public void keyReleased(KeyEvent e) {
            throw new UnsupportedOperationException("");
        }
    }*/
}
