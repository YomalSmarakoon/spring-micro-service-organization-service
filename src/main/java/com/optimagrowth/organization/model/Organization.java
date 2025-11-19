package com.optimagrowth.organization.model;

import lombok.Data;

@Data
public class Organization {
    private String organizationId;
    private String name;
    private String contactName;
    private String contactEmail;
    private String contactPhone;
}
