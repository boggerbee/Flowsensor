package no.kreutzer.flow;

/*
 * FlowMeter measures the flow of water 
 * 
 * The sensor sends pulses with a frequency equal to the flow rate.
 * Needs to set up listening to changes and measure freq.
 * */
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class HallEffectFlowMeter implements FlowMeter {
    private static final Logger logger = LogManager.getLogger(HallEffectFlowMeter.class);
	final GpioController gpio = GpioFactory.getInstance();
	final GpioPinDigitalInput flowPin = gpio.provisionDigitalInputPin(RCMPin.GPIO_17,"Flow sensor",PinPullResistance.PULL_UP);
	
	private AtomicInteger pulseIncrement = new AtomicInteger();
	private AtomicInteger pulseTotal = new AtomicInteger();
	
	private int pulsesPerLitre = 585; // calibrated number; 5846 on 10 L
	
	private int pulsesPerSecond=0;
	private FlowHandler flowHandler;
	
	/* Constructor */
	public HallEffectFlowMeter (FlowHandler f) {
		logger.info("HallEffectFlowMeter created");  
		flowHandler = f;
		
        flowPin.addListener(new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                if (event.getState().isHigh()) {
					//logger.info(event.getPin() + " = " + event.getState() + " cnt:"+pulseIncrement);  
					pulseIncrement.incrementAndGet();
					pulseTotal.incrementAndGet();
					if (flowHandler!=null) flowHandler.onCount(pulseTotal.get(),pulsesPerSecond);
				}
            }
        });
        Executors.newScheduledThreadPool(2).scheduleAtFixedRate(new Runnable() {
	    		@Override
	    		public void run() {
	    			reset();
	    		}
	    	}, 1,1, TimeUnit.SECONDS);
        
	}
	
	// Return flow in liter/min
	public float getFlow() {
		return ((float)pulsesPerSecond*60)/(float)pulsesPerLitre;
	}
	public int getTotalCount() {
		return pulseTotal.get();
	}
	public void setPPL(int ppl) {
		pulsesPerLitre = ppl;
	}
	
	// to be called every second
	private void reset() {
		pulsesPerSecond = pulseIncrement.getAndSet(0);
	}

	@Override
	public void setTotal(long total) {
		pulseTotal.set((int)total);
	}

    @Override
    public int getPulsesPerSecond() {
        // TODO Auto-generated method stub
        return pulsesPerSecond;
    }
}
