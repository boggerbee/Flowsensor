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
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
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
    private static final int INTERVAL = 10;
    private AbstractDisplay oled;
    private long lastCount;

    private FlowHandler flowHandler = new FlowHandler() {
        @Override
        public void onCount(long total, int current) {
            if ((total-lastCount) > INTERVAL) {
                lastCount = total;
                //logger.info("Count:"+total+":"+current);
                // store total
                conf.getConfig().setTotalFlow(total);
                conf.writeConfig();
                // call websock
                // TODO: Use timer to keep track of open and close.. maybe put logic in service class
                //sendFlowMessage(total,current); // TODO: this may take too much time, fire timer..
            } 
        }

    };
    
    private void updateDisplay() {
        oled.clearRect(0, oled.getHeight()/2 -8, oled.getWidth(),10 , false);
        oled.drawStringCentered(flow.getPulsesPerSecond() + " pps" ,Font.FONT_5X8, 25, true);
        
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String s = now.format(formatter);
        oled.clearRect(0, 0, oled.getWidth(),10 , false);
        oled.drawString(s ,Font.FONT_4X5, 1,1, true);
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
        
        ws.checkAlive();
        
        oled.drawString(getFirstNonLoopbackAddress(true, false).getHostAddress() ,Font.FONT_4X5, 1,oled.getHeight()-6, true);
        oled.drawStringRight("X",Font.FONT_4X5, oled.getHeight()-6, true);
        
        Executors.newScheduledThreadPool(2).scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                updateDisplay();
            }
        }, 500,1000, TimeUnit.MILLISECONDS);     
        

        logger.info("Init done!");
    }
    
    
    private void sendFlowMessage(long total, int current) {
        JsonObject json = Json.createObjectBuilder()
                .add("flow", Json.createObjectBuilder().add("total", total).add("current", current).build())
                .build();
        ws.sendMessage(json.toString());
    }    
/*
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
 */   
    private static InetAddress getFirstNonLoopbackAddress(boolean preferIpv4, boolean preferIPv6) {
        Enumeration<NetworkInterface> en;
        try {
            en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface i = (NetworkInterface) en.nextElement();
                for (Enumeration<InetAddress> en2 = i.getInetAddresses(); en2.hasMoreElements();) {
                    InetAddress addr = (InetAddress) en2.nextElement();
                    if (!addr.isLoopbackAddress()) {
                        if (addr instanceof Inet4Address) {
                            if (preferIPv6) {
                                continue;
                            }
                            return addr;
                        }
                        if (addr instanceof Inet6Address) {
                            if (preferIpv4) {
                                continue;
                            }
                            return addr;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            logger.error(e);
        }
        return null;
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
