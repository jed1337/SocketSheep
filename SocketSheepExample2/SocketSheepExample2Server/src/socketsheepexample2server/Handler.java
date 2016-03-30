package socketsheepexample2server;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class Handler implements Runnable {
    private Socket socket;
    private SocketSheepExample2Server server;

    private InputStream input;
    private OutputStream outputStream;
    private PrintWriter pw;

    public Handler(Socket socket/*, SocketSheepExample2Server server*/) {
       this.socket = socket;
       //this.server = server;
    }

    @Override
    public void run() {
       try {
            input = socket.getInputStream();
            outputStream = socket.getOutputStream();
            pw = new PrintWriter(outputStream, true);
            int count;
            byte[] buffer = new byte[4];
            while ((count = input.read(buffer)) > 0) {
                outputStream.write(buffer, 0, count);
                String s = new String(buffer);
                System.out.println(s);
                System.out.println("hi!");
            }

            input.close();
            // <editor-fold desc="Old Code">
            /*byte[] sizeAr = new byte[4];
            input.read(sizeAr);
            int size = ByteBuffer.wrap(sizeAr).asIntBuffer().get();
            byte[] imageAr = new byte[size];
            input.read(imageAr);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageAr));
            System.out.println("Received " + image.getHeight() + "x" + image.getWidth() + ": " + System.currentTimeMillis());
            ImageIO.write(image, "jpg", new File("Image-" + System.currentTimeMillis() + ".jpg"));
            input.close();*/
            // </editor-fold>
       } catch (IOException ex) {
          Logger.getLogger(Handler.class.getName()).log(Level.SEVERE, null, ex);
       }
    }

    private void close(Closeable c){
       try {
          if(c!=null)
          c.close();
       } catch (IOException ex) {
          System.err.println(ex.getMessage());
       }
    }
}
