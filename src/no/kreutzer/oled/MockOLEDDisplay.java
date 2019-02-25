package no.kreutzer.oled;

import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;


public class MockOLEDDisplay extends AbstractDisplay {

    private static final Logger LOGGER = LogManager.getLogger(MockOLEDDisplay.class.getCanonicalName());
    private BufferedImage image = convertImageBuffer();
    private JFrame frame = new JFrame("128x64 OLED Display");
    private JLabel lbl = new JLabel();
    private int scaleFactor = 3;
    private int width =  DISPLAY_WIDTH*scaleFactor;
    private int height = DISPLAY_HEIGHT*scaleFactor;


    /**
     * creates an oled display object with default
     * i2c bus 1 and default display address of 0x3C
     *
     * @throws IOException
     * @throws com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException
     */
    public MockOLEDDisplay()  throws IOException, UnsupportedBusNumberException {
        LOGGER.info("Created MockOLEDDisplay");
        clear();
        init();
    }


    private void init() {
        ImageIcon icon=new ImageIcon(scale(image,width,height));
        lbl.setIcon(icon);
        
        frame.setLayout(new FlowLayout());
        frame.setSize(width+50,height+50);
        frame.add(lbl);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
    }

    /**
     * sends the current buffer to the display
     * @throws IOException
     */
    public synchronized void update() throws IOException {
        image = convertImageBuffer();
        ImageIcon icon=new ImageIcon(scale(image,width,height));
        lbl.setIcon(icon);
    }

    private static BufferedImage scale(BufferedImage imageToScale, int dWidth, int dHeight) {
        BufferedImage scaledImage = null;
        if (imageToScale != null) {
            scaledImage = new BufferedImage(dWidth, dHeight, imageToScale.getType());
            Graphics2D graphics2D = scaledImage.createGraphics();
            graphics2D.drawImage(imageToScale, 0, 0, dWidth, dHeight, null);
            graphics2D.dispose();
        }
        return scaledImage;
    }    
    
    private BufferedImage convertImageBuffer() {
        BufferedImage tmpImage = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        byte[] pixels = ((DataBufferByte) tmpImage.getRaster().getDataBuffer()).getData();
        
        int index = 0;
        for (int posY = 0; posY < DISPLAY_HEIGHT; posY++) {
            for (int posX = 0; posX < DISPLAY_WIDTH /8; posX++) {
                for (int bit = 0; bit < 8; bit++) {
                    int pixel = getPixel(posX * 8 + bit, posY)?1:0;
                    pixels[index/8] |= (pixel << (7 - bit));
                    index++;
                }
            }
        }
        return tmpImage;
    }
    
    public void drawTestLine() {
        for (int x = 0; x < DISPLAY_WIDTH; x++) {
            setPixel(x, 5, true);
        }

    }
    

    private synchronized void shutdown() {
        // close window
    }


}
