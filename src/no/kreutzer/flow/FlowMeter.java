package no.kreutzer.flow;

public interface FlowMeter {
	public float getFlow() ;
	public int getTotalCount();
	public void setPPL(int ppl);
	public void setTotal(long total);
	public int getPulsesPerSecond();
}
