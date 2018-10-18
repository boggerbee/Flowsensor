package no.kreutzer.utils;

import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("config")
public class ConfigPOJO {
	private String id = "Almedalen25";
	private String restEndPoint = "http://data.kreutzer.no/dataserver";
	private String wsEndPoint = "ws://data.kreutzer.no/dataserver/websocket";
	private long totalFlow = 0;
	private boolean liveFlow = false;
	private String flowSensorClassName = "no.kreutzer.flow.HallEffectFlowMeter";
	private int pulsesPerLitre = 585;
	
	public int getPulsesPerLitre() {
        return pulsesPerLitre;
    }
    public void setPulsesPerLitre(int pulsesPerLitre) {
        this.pulsesPerLitre = pulsesPerLitre;
    }
	
	public String getFlowSensorClassName() {
		return flowSensorClassName;
	}
	public void setFlowSensorClassName(String flowSensorClassName) {
		this.flowSensorClassName = flowSensorClassName;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getRestEndPoint() {
		return restEndPoint;
	}
	public void setRestEndPoint(String restEndPoint) {
		this.restEndPoint = restEndPoint;
	}
	public String getWsEndPoint() {
		return wsEndPoint;
	}
	public void setWsEndPoint(String wsEndPoint) {
		this.wsEndPoint = wsEndPoint;
	}
	public long getTotalFlow() {
		return totalFlow;
	}
	public void setTotalFlow(long totalFlow) {
		this.totalFlow = totalFlow;
	}
	public void setLiveFlow(boolean b) {
		liveFlow = b;
	}
	public boolean isLiveFlow() {
		return liveFlow;
	}


}
