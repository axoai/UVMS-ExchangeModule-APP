package eu.europa.ec.fisheries.uvms.exchange.rest.unsecured;

import eu.europa.ec.fisheries.uvms.exchange.message.event.HandleProcessedMovementEvent;
import eu.europa.ec.fisheries.uvms.exchange.message.event.carrier.ExchangeMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSink;

@ApplicationScoped
@Path("sse")
public class SSERestResource {
    private final static Logger LOG = LoggerFactory.getLogger(SSERestResource.class);

    Sse sse;
    OutboundSseEvent.Builder eventBuilder;
    SseBroadcaster sseBroadcaster;

    @Context
    public void setSse(Sse sse) {
        this.sse = sse;
        this.eventBuilder = sse.newEventBuilder();
        this.sseBroadcaster = sse.newBroadcaster();
    }

    public void createdMovement(@Observes @HandleProcessedMovementEvent ExchangeMessageEvent message){
        try {
            String s = "Received movement response";
            OutboundSseEvent sseEvent = eventBuilder
                    .name("Movement")
                    .id("" + System.currentTimeMillis())
                    .mediaType(MediaType.APPLICATION_JSON_PATCH_JSON_TYPE)
                    .data(String.class, s)
                    //.reconnectDelay(3000) //this one is optional and governs how long the client should wait b4 attempting to reconnect to this server
                    .comment("Processed Movement")
                    .build();
            sseBroadcaster.broadcast(sseEvent);
        }catch (Exception e){
            LOG.error("Error while broadcasting SSE: ", e);
            throw new RuntimeException(e);
        }
    }


    @GET
    @Path("subscribe")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void listen(@Context SseEventSink sseEventSink) {
        sseEventSink.send(sse.newEvent("Welcome to UVMS SSE notifications."));
        sseBroadcaster.register(sseEventSink);
        sseEventSink.send(sse.newEvent("You are now registered for receiving notifications for new movements."));
    }
}
