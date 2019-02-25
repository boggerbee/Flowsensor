package no.kreutzer.oled;

public abstract class AbstractFont {
    private final int minChar, maxChar, spaceWidth, charHeight, gapWidth;
    private final byte[] data;
    private final int[][] descriptors;
    final int[][] kerning = null;

    private AbstractFont(int minChar, int maxChar, int spaceWidth, int charHeight, int gapWidth, byte[] data, int[][] desc) {
        this.minChar = minChar;
        this.maxChar = maxChar;
        this.spaceWidth = spaceWidth;
        this.charHeight = charHeight;
        this.gapWidth = gapWidth;
        this.data = data;
        this.descriptors = desc;
    }    
    
    public void drawChar(AbstractDisplay display, char c, int x, int y, boolean on) {
        if (c > maxChar || c < minChar) {
            c = '?';
        }
        int pos = c - minChar;
        int width = descriptors[pos][0];
        int offset = descriptors[pos][1];
        int bytes_per_row = (width + 7) >> 3;
        
        for (int row=0; row < charHeight;row++) {
            int py = y + row;
            int mask = 0x80;
            int p = offset;
            for (int col=0;col<width;col++) {
                int px = x + col;
                if ((data[p] & mask) > 0) {
                    display.setPixel(px, py, on);  // for kerning, never draw black
                }
                mask >>= 1;
                if (mask == 0) {
                    mask = 0x80;
                    p += 1;
                }
            }
            offset += bytes_per_row;        
        }
    }
    
    public void drawString(AbstractDisplay display, String s, int x, int y,boolean on) {
        char prevChar = 256;

        for (char c : s.toCharArray()) {
            int pos = c - minChar;
            int width = descriptors[pos][0];
            int prevWidth = 0;
            if (c < minChar || c > maxChar) {
                x += spaceWidth + gapWidth;
            } else {
                if (prevChar != 256)
                    x += kerning[prevChar][pos] + gapWidth;
                prevChar = (char)pos;
                prevWidth = width;
                drawChar(display, c,x,y,on);
            }
            if (prevChar != 256)
                x += prevWidth;
        }
    }
}
