package com.optimagrowth.organization.events.model;

import java.time.Instant;

public record OrganizationChangeModel(
        String correlationId,
        String type,
        String action,
        String organizationId,
        Instant timestamp
) {}
