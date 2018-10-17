package no.kreutzer.flow;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MockFlowMeter implements FlowMeter {
    private static final Logger logger = LogManager.getLogger(MockFlowMeter.class);
	private FlowHandler flowHandler;
	private AtomicInteger pulseTotal = new AtomicInteger();
    private int pps = 200;
    private int pulsesPerLitre = 585; // calibrated number; 5846 on 10 L
	
	
	public MockFlowMeter(FlowHandler f) {
		logger.info("MockFlowMeter created");  
		flowHandler = f;

        Executors.newScheduledThreadPool(2).scheduleAtFixedRate(new Runnable() {
    		@Override
    		public void run() {
				pulseTotal.incrementAndGet();
				if (Math.random() > 0.5) {
				    if (pps<400) pps++;
				} else {
				    if (pps>0) pps--;
				}
				if (flowHandler!=null) flowHandler.onCount(pulseTotal.get(),pps);
    		}
    	}, 1000,50, TimeUnit.MILLISECONDS);		
	}

	@Override
	public float getFlow() {
		return ((float)pps*60)/(float)pulsesPerLitre;
	}

	@Override
	public int getTotalCount() {
		return pulseTotal.get();
	}

	@Override
	public void setPPL(int ppl) {
		// TODO Auto-generated method stub
	}
	@Override
	public void setTotal(long total) {
		pulseTotal.set((int)total);
	}

    @Override
    public int getPulsesPerSecond() {
        return pps;
    }

}
