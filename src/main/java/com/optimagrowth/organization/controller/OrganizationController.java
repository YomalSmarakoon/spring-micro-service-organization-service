package com.optimagrowth.organization.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/organization")
public class OrganizationController {

    @GetMapping("/{organizationId}")
    public String getOrganization(@PathVariable("organizationId") String orgId) {
        return "Organization " + orgId;
    }
}