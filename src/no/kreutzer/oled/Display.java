package no.kreutzer.oled;

import java.io.IOException;

public interface Display {
    public void setPixel(int x, int y, boolean on);
    public void update() throws IOException;

}
