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

    private FlowHandler flowHandler = new FlowHandler() {
        @Override
        public void onCount(long total, int current) {

        }
    };

    private void init() {
        ws = new WebSocketService(conf.getConfig().getWsEndPoint());
        flow = conf.getFlowSensorImpl(flowHandler);
        flow.setTotal(conf.getConfig().getTotalFlow());
        flow.setPPL(conf.getConfig().getPulsesPerLitre());

        scheduledPool = Executors.newScheduledThreadPool(4);
        scheduledPool.schedule(runnableTask, 1, TimeUnit.SECONDS);
        scheduledPool.scheduleAtFixedRate(flowPoller, 1000, 200, TimeUnit.MILLISECONDS);
        
        logger.info("Init done!");
    }

    private Runnable runnableTask = new Runnable() {
        @Override
        public void run() {
        	try {
	            conf.getConfig().setTotalFlow(flow.getTotalCount());
	            conf.writeConfig();
	            
	            ws.checkAlive(); 
	
                scheduledPool.schedule(runnableTask, 1000, TimeUnit.SECONDS);
	        } catch (RuntimeException e){
	            logger.error("Uncaught Runtime Exception",e);
	            return; // Keep working
	        } catch (Throwable e){
	            logger.error("Unrecoverable error",e);
	            throw e;
	        }
        }
    };
    
    private Runnable flowPoller = new Runnable() {
        @Override
        public void run() {
        	try {
	            if (conf.getConfig().isLiveFlow()) {
	                sendFlowMessage(flow.getTotalCount(), flow.getPulsesPerSecond());
	            }    
            } catch (RuntimeException e){
                logger.error("Uncaught Runtime Exception",e);
                return; // Keep working
            } catch (Throwable e){
                logger.error("Unrecoverable error",e);
                throw e;
            }        
        }
    };
    
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
                    e.printStackTrace();
                }
            }
        }
    }
}
