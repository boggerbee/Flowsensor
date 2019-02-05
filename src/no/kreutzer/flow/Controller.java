/*
 * Main class of the water controller
 * 
 * 
 */
package no.kreutzer.flow;

import org.apache.logging.log4j.LogManager;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import no.kreutzer.oled.AbstractDisplay;
import no.kreutzer.oled.Font;
import no.kreutzer.oled.MockOLEDDisplay;
import no.kreutzer.oled.OLEDDisplay;

import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.json.JsonObject;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.json.Json;

import no.kreutzer.utils.ConfigService;
import no.kreutzer.utils.WebSocketService;

@Dependent
public class Controller {
    private static final Logger logger = LogManager.getLogger(Controller.class);
    private @Inject ConfigService conf;
    private @Inject WebSocketService ws;

    private FlowMeter flow;
    private ScheduledExecutorService scheduledPool;
    private long lastCount;
    private static final int INTERVAL = 10;
    private AbstractDisplay oled;

    private FlowHandler flowHandler = new FlowHandler() {
        @Override
        public void onCount(long total, int current) {
            if ((total-lastCount) > INTERVAL) {
                //logger.info("Count:"+total+":"+current);
                updateDisplay(current);
                lastCount = total;
                // store total
                conf.getConfig().setTotalFlow(total);
                conf.writeConfig();
                // call websock
                // TODO: Use timer to keep track of open and close.. maybe put logic in service class
                //sendFlowMessage(total,current); // TODO: this may take too much time, fire timer..
            } 
        }

    };
    
    private void updateDisplay(int current) {
        oled.clearRect(0, 0, oled.getWidth(), 5, false);
        oled.drawString("FLOW: "+current ,Font.FONT_4X5, 1,1, true);
        try {
            oled.update();
        } catch (IOException e) {
            logger.error(e);
        }     
    }

    private void init() {
        flow = conf.getFlowSensorImpl(flowHandler);
        oled = conf.getDisplayImpl();
        flow.setTotal(conf.getConfig().getTotalFlow());
        flow.setPPL(conf.getConfig().getPulsesPerLitre());
        lastCount = flow.getTotalCount();
        
        ws.checkAlive();
        
        oled.drawString(getInetAddress() ,Font.FONT_4X5, 1,oled.getHeight()-6, true);
        oled.drawStringRight("X",Font.FONT_4X5, oled.getHeight()-6, true);
        display("HELLO!");

        logger.info("Init done!");
    }
    
    private void display(String s) {
        oled.drawStringCentered(s ,Font.FONT_5X8, 25, true);
        //oled.drawTestLine();
        try {
            oled.update();
        } catch (IOException e) {
            logger.error(e);
        }        
    }
    
    private void sendFlowMessage(long total, int current) {
        JsonObject json = Json.createObjectBuilder()
                .add("flow", Json.createObjectBuilder().add("total", total).add("current", current).build())
                .build();
        ws.sendMessage(json.toString());
    }    
    
    private String runSystemCmd(String cmd) {
        String s,r=null;
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            r = stdInput.readLine();
            
            // read any errors from the attempted command
            while ((s = stdError.readLine()) != null) {
                logger.error(s);
            }
        } catch (IOException e) {
            logger.error(e);
        }        
        return r;
    }

    public String getInetAddress() {
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getLocalHost();
            return inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            logger.error(e);
            return "UnknownHostException";
        }
    }
    
    public static void main(String args[]) {
        logger.info("Hello, flowmeter!");
        // Bootstrapping CDI
        Weld weld = new Weld();
        try (WeldContainer container = weld.initialize()) {
            container.select(Controller.class).get().init();
            while (true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    logger.error("Main loop interrupted!!", e);
                }
            }
        }
    }
    
}
