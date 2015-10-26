package eu.europa.ec.fisheries.uvms.exchange.rest.dto.exchange;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class SendingGroupLog {

	@XmlElement(required = true)
	private String recipient;
	@XmlElement(required = true)
	private List<SendingLog> sendingLogList;
	public String getRecipient() {
		return recipient;
	}
	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}
	public List<SendingLog> getSendingLogList() {
		return sendingLogList;
	}
	public void setSendingLogList(List<SendingLog> sendingLogList) {
		this.sendingLogList = sendingLogList;
	}
	
}
