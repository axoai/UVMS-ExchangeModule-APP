package eu.europa.ec.fisheries.uvms.exchange.model.constant;

public enum FaultCode {
	EXCHANGE_MESSAGE(3700),
	EXCHANGE_TOPIC_MESSAGE(3701),
	EXCHANGE_EVENT_SERVICE(3201),
	EXCHANGE_PLUGIN_EVENT(3202);
	
	private final int code;
	
	private FaultCode(int code) {
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}
}
