package no.kreutzer.flow;

public interface FlowHandler {
	/**
	 * 
	 * @param total		Total pulse count
	 * @param current	Current pulses per second 
	 */
	void onCount(long total, int current);

}
