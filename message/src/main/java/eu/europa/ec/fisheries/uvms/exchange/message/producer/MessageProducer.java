package eu.europa.ec.fisheries.uvms.exchange.message.producer;

import javax.ejb.Local;
import javax.enterprise.event.Observes;
import javax.jms.TextMessage;

import eu.europa.ec.fisheries.uvms.exchange.message.constants.MessageQueue;
import eu.europa.ec.fisheries.uvms.exchange.message.event.ErrorEvent;
import eu.europa.ec.fisheries.uvms.exchange.message.event.carrier.ExchangeMessageEvent;
import eu.europa.ec.fisheries.uvms.exchange.message.event.carrier.PluginMessageEvent;
import eu.europa.ec.fisheries.uvms.exchange.message.event.registry.PluginErrorEvent;
import eu.europa.ec.fisheries.uvms.exchange.message.exception.ExchangeMessageException;

@Local
public interface MessageProducer {

    public String sendMessageOnQueue(String text, MessageQueue queue) throws ExchangeMessageException;

    public String sendEventBusMessage(String text, String serviceName) throws ExchangeMessageException;
    
    public void sendModuleResponseMessage(TextMessage message, String text);
    
    public void sendModuleErrorResponseMessage(@Observes @ErrorEvent ExchangeMessageEvent event);
    public void sendPluginErrorResponseMessage(@Observes @PluginErrorEvent PluginMessageEvent event);

    void sendModuleAckMessage(String messageId, MessageQueue queue, String text);
}
