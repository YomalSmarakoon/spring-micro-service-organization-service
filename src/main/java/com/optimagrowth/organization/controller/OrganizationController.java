package com.optimagrowth.organization.controller;

import com.optimagrowth.organization.model.Organization;
import com.optimagrowth.organization.service.OrganizationService;
import com.optimagrowth.organization.utils.UserContextHolder;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/v1/organization")
public class OrganizationController {

    private static final Logger LOG = LoggerFactory.getLogger(OrganizationController.class);


    @Autowired
    private OrganizationService organizationService;

    @RolesAllowed({"ADMIN", "USER"})
    @GetMapping("/{organizationId}")
    public ResponseEntity<Organization> getOrganization(@PathVariable("organizationId") String orgId) {

        LOG.info("getLicense; Correlation id: {}", UserContextHolder.getContext().getCorrelationId());

        Organization org = organizationService.getOrganizationById(orgId);
        if (org == null) {
//            throw new RuntimeException("Organization not found: " + orgId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(org);
    }

    @RolesAllowed({"ADMIN", "USER"})
    @PostMapping
    public ResponseEntity<Organization> saveOrganization(@RequestBody Organization organization) {
        return ResponseEntity.ok(organizationService.create(organization));
    }

    @RolesAllowed({"ADMIN", "USER"})
    @PutMapping("/{organizationId}")
    public ResponseEntity<Organization> updateOrganization(
            @PathVariable("organizationId") String organizationId,
            @RequestBody Organization organization) {

        Organization updatedOrganization = organizationService.update(organizationId, organization);

        return ResponseEntity.ok(updatedOrganization);
    }

    @RolesAllowed("ADMIN")
    @DeleteMapping("/{organizationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrganization(@PathVariable("organizationId") String organizationId) {
        organizationService.delete(organizationId);
    }

    @GetMapping("/debug-authorities")
    public Collection<? extends GrantedAuthority> debugAuthorities(Authentication authentication) {
        return authentication.getAuthorities();
    }
}