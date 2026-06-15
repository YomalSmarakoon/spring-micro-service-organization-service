package com.optimagrowth.organization.events.source;

import com.optimagrowth.organization.events.model.ActionType;
import com.optimagrowth.organization.events.model.OrganizationChangeModel;
import com.optimagrowth.organization.utils.UserContext;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Modern replacement for SimpleSourceBean (ch.10 Spring Microservices in Action).
 * StreamBridge replaces the deprecated @EnableBinding + Source pattern — no
 * binding interface needed; the destination is resolved at runtime from the
 * binding name passed to send().
 */
@Component
public class OrganizationChangePublisher {

    private final StreamBridge streamBridge;

    public OrganizationChangePublisher(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void publishOrganizationChange(ActionType action, String organizationId) {
        OrganizationChangeModel event = new OrganizationChangeModel(
                UserContext.getCorrelationId(),
                "ORGANIZATION_CHANGE",
                action.name(),
                organizationId,
                Instant.now()
        );

        streamBridge.send(
                "organizationChange-out-0",
                MessageBuilder
                        .withPayload(event)
                        .setHeader("eventType", "ORGANIZATION_CHANGE")
                        .build()
        );
    }
}
