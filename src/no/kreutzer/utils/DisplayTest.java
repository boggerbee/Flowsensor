package no.kreutzer.utils;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import no.kreutzer.oled.Font;
import no.kreutzer.oled.OLEDDisplay;

public class DisplayTest {
    private static final Logger logger = LogManager.getLogger(DisplayTest.class);
    private OLEDDisplay oled; 

    public DisplayTest() {
        try {
            oled = new OLEDDisplay();
        } catch (IOException | UnsupportedBusNumberException e) {
            logger.error(e);
        }
    }
    
    private void display(String s) {
        oled.drawStringCentered(s ,Font.FONT_5X8, 25, true);
        try {
            oled.update();
        } catch (IOException e) {
            logger.error(e);
        }        
    }
    
    public static void main(String[] args) {
       new DisplayTest().display("Flowsensor: 0000");
    }
}
