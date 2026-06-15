package com.optimagrowth.organization.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.optimagrowth.organization.events.model.ActionType;
import com.optimagrowth.organization.events.source.OrganizationChangePublisher;
import com.optimagrowth.organization.model.Organization;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Service
public class OrganizationService {

    private List<Organization> organizations;

    private final OrganizationChangePublisher organizationChangePublisher;

    public OrganizationService(OrganizationChangePublisher organizationChangePublisher) {
        this.organizationChangePublisher = organizationChangePublisher;
    }

    /**
     * The {@code @PostConstruct} annotation is used to mark a method that should be
     * executed once immediately after the Spring container has created the bean
     * and completed dependency injection.
     * <p>
     * This provides a safe place to run initialization logic that depends on
     * injected fields or configuration values, unlike constructors where such
     * dependencies may not yet be available.
     * </p>
     *
     * <p><strong>Typical use cases include:</strong></p>
     * <ul>
     *     <li>Loading data from files (e.g., JSON, YAML) into memory caches</li>
     *     <li>Initializing maps, lists, or in-memory databases</li>
     *     <li>Preloading configuration or reference data required by the service</li>
     *     <li>Running setup logic that must occur after bean creation</li>
     * </ul>
     *
     * <p>
     * In this project, it is used to load organization data from a JSON file when
     * the microservice starts. Spring initializes the bean, injects all
     * dependencies, and <em>then</em> calls the annotated method exactly once
     * during application startup.
     * </p>
     *
     * <p><strong>Execution order:</strong></p>
     * <ol>
     *     <li>Spring creates the bean instance</li>
     *     <li>Spring injects all {@code @Autowired} and {@code @Value} fields</li>
     *     <li>The method annotated with {@code @PostConstruct} is invoked</li>
     * </ol>
     *
     * <h3>🧠 Why not use the constructor?</h3>
     * <p>
     * A constructor runs <em>before</em> Spring performs dependency injection.
     * This means any injected components (e.g., services, repositories, configuration
     * properties) will still be {@code null} inside the constructor.
     * </p>
     *
     * <p>
     * {@code @PostConstruct}, however, guarantees that:
     * </p>
     *
     * <ul>
     *     <li>All dependencies have been injected</li>
     *     <li>{@code @Value} properties are resolved</li>
     *     <li>Environment and configuration beans are ready</li>
     * </ul>
     *
     * <p>
     * Therefore, it is the correct place to perform initialization that depends on
     * resources managed by the Spring container.
     * </p>
     */
    @PostConstruct
    public void loadOrganizations() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = getClass().getResourceAsStream("/data/organizations.json");
        organizations = mapper.readValue(inputStream, new TypeReference<List<Organization>>() {
        });
    }

    public Organization getOrganizationById(String orgId) {
        return organizations.stream()
                .filter(org -> org.getOrganizationId().equalsIgnoreCase(orgId))
                .filter(org -> "ACT".equalsIgnoreCase(org.getStatus()))
                .findFirst()
                .orElse(null);
    }

    public List<Organization> getAllOrganizations() {
        return organizations.stream()
                .filter(org -> "ACT".equalsIgnoreCase(org.getStatus()))
                .toList();
    }

    public Organization create(Organization organization) {

        boolean organizationExists = organizations.stream()
                .anyMatch(org -> org.getOrganizationId()
                        .equalsIgnoreCase(organization.getOrganizationId()));

        if (organizationExists) {
            throw new RuntimeException("Organization already exists with ID: "
                    + organization.getOrganizationId());
        }

        organization.setStatus("ACT");
        organizations.add(organization);

        organizationChangePublisher.publishOrganizationChange(ActionType.CREATE, organization.getOrganizationId());

        return organization;
    }

    public Organization update(String orgId, Organization updatedOrganization) {

        Organization existingOrganization = organizations.stream()
                .filter(org -> org.getOrganizationId().equalsIgnoreCase(orgId))
                .filter(org -> "ACT".equalsIgnoreCase(org.getStatus()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Organization not found with ID: " + orgId));

        existingOrganization.setName(updatedOrganization.getName());
        existingOrganization.setContactName(updatedOrganization.getContactName());
        existingOrganization.setContactEmail(updatedOrganization.getContactEmail());
        existingOrganization.setContactPhone(updatedOrganization.getContactPhone());

        organizationChangePublisher.publishOrganizationChange(ActionType.UPDATED, existingOrganization.getOrganizationId());

        return existingOrganization;
    }

    public void delete(String orgId) {

        Organization existingOrganization = organizations.stream()
                .filter(org -> org.getOrganizationId().equalsIgnoreCase(orgId))
                .filter(org -> "ACT".equalsIgnoreCase(org.getStatus()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Organization not found with ID: " + orgId));

        existingOrganization.setStatus("INA");

        organizationChangePublisher.publishOrganizationChange(ActionType.DELETED, existingOrganization.getOrganizationId());
    }
}
