package no.kreutzer.oled;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.util.Arrays;

public abstract class AbstractDisplay {
    protected static final int DISPLAY_WIDTH = 128;
    protected static final int DISPLAY_HEIGHT = 64;
    protected static final int MAX_INDEX = (DISPLAY_HEIGHT / 8) * DISPLAY_WIDTH;

    protected final byte[] imageBuffer = new byte[(DISPLAY_WIDTH * DISPLAY_HEIGHT) / 8];
    
    public synchronized void clear() {
        Arrays.fill(imageBuffer, (byte) 0x00);
    }

    public int getWidth() {
        return DISPLAY_WIDTH;
    }

    public int getHeight() {
        return DISPLAY_HEIGHT;
    }

    public synchronized void setPixel(int x, int y, boolean on) {
        final int pos = x + (y / 8) * DISPLAY_WIDTH;
        if (pos >= 0 && pos < MAX_INDEX) {
            if (on) {
                this.imageBuffer[pos] |= (1 << (y & 0x07));
            } else {
                this.imageBuffer[pos] &= ~(1 << (y & 0x07));
            }
        }
    }
    
    public synchronized boolean getPixel(int x, int y) {
        final int pos = x + (y / 8) * DISPLAY_WIDTH;
        if (pos >= 0 && pos < MAX_INDEX) {
            return ((this.imageBuffer[pos] & (1 << (y & 0x07))) > 0);
        } else return false;
    }

    public synchronized void drawChar(char c, Font font, int x, int y, boolean on) {
        font.drawChar(this, c, x, y, on);
    }
    
    public synchronized void drawChar(char c, LargeFont font, int x, int y, boolean on) {
        font.drawChar(this, c, x, y, on);
    }
    
    public synchronized void drawString(String string, LargeFont font, int x, int y, boolean on) {
        font.drawString(this, string, x, y, on);
    }
    
    public synchronized void drawString(String string, Font font, int x, int y, boolean on) {
        int posX = x;
        int posY = y;
        for (char c : string.toCharArray()) {
            if (c == '\n') {
                posY += font.getOutterHeight();
                posX = x;
            } else {
                if (posX >= 0 && posX + font.getWidth() < this.getWidth()
                        && posY >= 0 && posY + font.getHeight() < this.getHeight()) {
                    drawChar(c, font, posX, posY, on);
                }
                posX += font.getOutterWidth();
            }
        }
    }
    
    public synchronized void drawStringCentered(String string, Font font, int y, boolean on) {
        final int strSizeX = string.length() * font.getOutterWidth();
        final int x = (this.getWidth() - strSizeX) / 2;
        drawString(string, font, x, y, on);
    }
    
    public synchronized void drawStringCentered(String string, LargeFont font, int y, boolean on) {
        font.drawStringCentered(this, string, y, on);
    }
    
    public synchronized void drawStringRight(String string, Font font, int y, boolean on) {
        final int strSizeX = string.length() * font.getOutterWidth();
        final int x = (this.getWidth() - strSizeX -1);
        drawString(string, font, x, y, on);
    }

    public synchronized void clearRect(int x, int y, int width, int height, boolean on) {
        for (int posX = x; posX < x + width; ++posX) {
            for (int posY = y; posY < y + height; ++posY) {
                setPixel(posX, posY, on);
            }
        }
    }

    /**
     * draws the given image over the current image buffer. The image
     * is automatically converted to a binary image (if it not already
     * is).
     * <p/>
     * Note that the current buffer is not cleared before, so if you
     * want the image to completely overwrite the current display
     * content you need to call clear() before.
     *
     * @param image
     * @param x
     * @param y
     */
    public synchronized void drawImage(BufferedImage image, int x, int y) {
        BufferedImage tmpImage = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        tmpImage.getGraphics().drawImage(image, x, y, null);

        int index = 0;
        int pixelval;
        final byte[] pixels = ((DataBufferByte) tmpImage.getRaster().getDataBuffer()).getData();
        for (int posY = 0; posY < DISPLAY_HEIGHT; posY++) {
            for (int posX = 0; posX < DISPLAY_WIDTH / 8; posX++) {
                for (int bit = 0; bit < 8; bit++) {
                    pixelval = (byte) ((pixels[index/8] >>  (8 - bit)) & 0x01);
                    setPixel(posX * 8 + bit, posY, pixelval > 0);
                    index++;
                }
            }
        }
    }

    /**
     * sends the current buffer to the display
     * @throws IOException
     */
    public abstract void update() throws IOException;
}
