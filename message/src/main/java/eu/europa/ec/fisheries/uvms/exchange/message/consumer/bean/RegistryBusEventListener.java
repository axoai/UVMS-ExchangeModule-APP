/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.europa.ec.fisheries.uvms.exchange.message.consumer.bean;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ec.fisheries.schema.exchange.registry.v1.ExchangeRegistryBaseRequest;
import eu.europa.ec.fisheries.uvms.exchange.message.event.carrier.PluginMessageEvent;
import eu.europa.ec.fisheries.uvms.exchange.message.event.registry.PluginErrorEvent;
import eu.europa.ec.fisheries.uvms.exchange.message.event.registry.RegisterServiceEvent;
import eu.europa.ec.fisheries.uvms.exchange.message.event.registry.UnRegisterServiceEvent;
import eu.europa.ec.fisheries.uvms.exchange.message.exception.ExchangeMessageException;
import eu.europa.ec.fisheries.uvms.exchange.model.constant.ExchangeModelConstants;
import eu.europa.ec.fisheries.uvms.exchange.model.constant.FaultCode;
import eu.europa.ec.fisheries.uvms.exchange.model.exception.ExchangeModelMarshallException;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.ExchangePluginResponseMapper;
import eu.europa.ec.fisheries.uvms.exchange.model.mapper.JAXBMarshaller;

/**
 *
 * @author jojoha
 */
@MessageDriven(mappedName = ExchangeModelConstants.EVENTBUS, activationConfig = {
    @ActivationConfigProperty(propertyName = "messageSelector", propertyValue = ExchangeModelConstants.SERVICE_NAME + " = '" + ExchangeModelConstants.EXCHANGE_REGISTER_SERVICE + "'"),
    @ActivationConfigProperty(propertyName = "messagingType", propertyValue = ExchangeModelConstants.CONNECTION_TYPE),
    @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = ExchangeModelConstants.DESTINATION_TYPE_TOPIC),
    @ActivationConfigProperty(propertyName = "destination", propertyValue = ExchangeModelConstants.EVENTBUS_NAME),
    @ActivationConfigProperty(propertyName = "subscriptionName", propertyValue = ExchangeModelConstants.EXCHANGE_REGISTER_SERVICE),
    @ActivationConfigProperty(propertyName = "clientId", propertyValue = ExchangeModelConstants.EXCHANGE_REGISTER_SERVICE)
})
public class RegistryBusEventListener implements MessageListener {

    final static Logger LOG = LoggerFactory.getLogger(RegistryBusEventListener.class);

    @Inject
    @RegisterServiceEvent
    Event<PluginMessageEvent> registerServiceEvent;

    @Inject
    @UnRegisterServiceEvent
    Event<PluginMessageEvent> unregisterServiceEvent;

    @Inject
    @PluginErrorEvent
    Event<PluginMessageEvent> errorEvent;

    @Override
    public void onMessage(Message message) {

        LOG.info("Eventbus listener for Exchange Registry (ExchangeModelConstants.EXCHANGE_REGISTER_SERVICE): {}", ExchangeModelConstants.EXCHANGE_REGISTER_SERVICE);

        TextMessage textMessage = (TextMessage) message;
        String responseTopicMessageSelector = null;
        try {

            ExchangeRegistryBaseRequest request = JAXBMarshaller.unmarshallTextMessage(textMessage, ExchangeRegistryBaseRequest.class);
            responseTopicMessageSelector = request.getResponseTopicMessageSelector();
            
            switch (request.getMethod()) {
                case REGISTER_SERVICE:
                    registerServiceEvent.fire(new PluginMessageEvent(textMessage, responseTopicMessageSelector));
                    break;
                case UNREGISTER_SERVICE:
                	unregisterServiceEvent.fire(new PluginMessageEvent(textMessage, responseTopicMessageSelector));
                    break;
                default:
                	LOG.error("[ Not implemented method consumed: {} ]", request.getMethod());
                	throw new ExchangeMessageException("[ Not implemented method consumed: " + request.getMethod() +" ]");
            }
        } catch (ExchangeMessageException | ExchangeModelMarshallException | NullPointerException e) {
            LOG.error("[ Error when receiving message on topic in exchange: ]");
            errorEvent.fire(new PluginMessageEvent(textMessage, responseTopicMessageSelector, ExchangePluginResponseMapper.mapToPluginFaultResponse(FaultCode.EXCHANGE_TOPIC_MESSAGE.getCode(), "Error when receiving message in exchange " + e.getMessage())));
        }
    }

}
