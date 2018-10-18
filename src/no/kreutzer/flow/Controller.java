/*
 * Main class of the water controller
 * 
 * 
 */
package no.kreutzer.flow;

import org.apache.logging.log4j.LogManager;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.apache.logging.log4j.Logger;

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

    private FlowMeter flow;
    private ScheduledExecutorService scheduledPool;
    private WebSocketService ws;
    private long lastCount;
    private static final int INTERVAL = 10;

    private FlowHandler flowHandler = new FlowHandler() {
        @Override
        public void onCount(long total, int current) {
            if ((total-lastCount) > INTERVAL) {
                logger.info("Count:"+total+":"+current);
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

    private void init() {
        ws = new WebSocketService(conf.getConfig().getWsEndPoint());
        flow = conf.getFlowSensorImpl(flowHandler);
        flow.setTotal(conf.getConfig().getTotalFlow());
        flow.setPPL(conf.getConfig().getPulsesPerLitre());
        lastCount = flow.getTotalCount();

        logger.info("Init done!");
    }
    
    private void sendFlowMessage(long total, int current) {
        JsonObject json = Json.createObjectBuilder()
                .add("flow", Json.createObjectBuilder().add("total", total).add("current", current).build())
                .build();
        ws.sendMessage(json.toString());
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
