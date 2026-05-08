package com.optimagrowth.organization.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
/*
 * Spring Security now recommends @EnableMethodSecurity for method-level security.
 * @EnableGlobalMethodSecurity(jsr250Enabled = true): old
 * */
@EnableMethodSecurity(jsr250Enabled = true)
/*
 * public class SecurityConfig extends KeycloakWebSecurityConfigurerAdapter: old
 * */
public class SecurityConfig {

    /**
     * Keycloak client ID used to read client-specific roles from the JWT
     * {@code resource_access} claim.
     *
     * <p>
     * This value must match the client name in the JWT. For example:
     * </p>
     *
     * <pre>
     * {@code
     * "resource_access": {
     *   "ostock": {
     *     "roles": ["ADMIN"]
     *   }
     * }
     * }
     * </pre>
     */
    private static final String CLIENT_ID = "ostock";

    /*
     * old:
     * @Override
     * protected void configure(HttpSecurity http) throws Exception
     * - Spring Security’s modern configuration is bean-based using SecurityFilterChain.
     * */

    /**
     * Configures the application as an OAuth2 Resource Server that validates incoming
     * JWT access tokens and converts Keycloak client roles into Spring Security
     * authorities.
     *
     * <p>
     * By default, Spring Security does not automatically map Keycloak roles from
     * {@code resource_access.{client-id}.roles} into {@link GrantedAuthority}
     * instances. This configuration provides a custom JWT authentication converter
     * so that Keycloak client roles such as {@code ADMIN} are converted into Spring
     * authorities such as {@code ROLE_ADMIN}.
     * </p>
     *
     * <p>
     * This allows method-level security annotations such as:
     * </p>
     *
     * <pre>
     * {@code
     * @RolesAllowed("ADMIN")
     * }
     * </pre>
     *
     * <p>
     * to correctly authorize users whose JWT contains:
     * </p>
     *
     * <pre>
     * {@code
     * "resource_access": {
     *   "ostock": {
     *     "roles": ["ADMIN"]
     *   }
     * }
     * }
     * </pre>
     *
     * @param http the {@link HttpSecurity} object used to configure web security
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if an error occurs while building the security filter chain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
//                         .anyRequest().permitAll() // Let every request enter the service, even if the user is not logged in.
                                .anyRequest().authenticated() // Only allow requests if the caller has a valid authenticated Keycloak token/session.
                )
                .oauth2ResourceServer(oauth2 -> oauth2
//                        .jwt(Customizer.withDefaults())
                                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );

        return http.build();
    }


    /**
     * Creates a custom {@link JwtAuthenticationConverter} that converts Keycloak
     * client roles from the JWT into Spring Security authorities.
     *
     * <p>
     * Spring Security stores roles as authorities with the {@code ROLE_} prefix.
     * For example, a Keycloak role named {@code ADMIN} should be converted into
     * {@code ROLE_ADMIN}.
     * </p>
     *
     * @return a configured {@link JwtAuthenticationConverter} that extracts
     * Keycloak client roles and maps them to Spring Security authorities
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(keycloakRoleConverter());
        return converter;
    }

    /**
     * Extracts client-specific roles from the Keycloak JWT {@code resource_access}
     * claim and converts them into Spring Security {@link GrantedAuthority}
     * instances.
     *
     * <p>
     * This method reads roles from:
     * </p>
     *
     * <pre>
     * {@code
     * resource_access.{CLIENT_ID}.roles
     * }
     * </pre>
     *
     * <p>
     * For each role found, the method adds the {@code ROLE_} prefix because Spring
     * Security role-based checks such as {@code @RolesAllowed("ADMIN")} expect the
     * underlying authority to be named {@code ROLE_ADMIN}.
     * </p>
     *
     * <p>
     * Example conversion:
     * </p>
     *
     * <pre>
     * {@code
     * Keycloak role: ADMIN
     * Spring authority: ROLE_ADMIN
     * }
     * </pre>
     *
     * @return a converter that maps a {@link Jwt} into a collection of
     * {@link GrantedAuthority} objects
     */
    private Converter<Jwt, Collection<GrantedAuthority>> keycloakRoleConverter() {
        return jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();

            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");

            if (resourceAccess != null && resourceAccess.containsKey(CLIENT_ID)) {
                Map<String, Object> clientAccess =
                        (Map<String, Object>) resourceAccess.get(CLIENT_ID);

                List<String> clientRoles =
                        (List<String>) clientAccess.get("roles");

                if (clientRoles != null) {
                    clientRoles.forEach(role ->
                            authorities.add(new SimpleGrantedAuthority("ROLE_" + role))
                    );
                }
            }

            return authorities;
        };
    }

    /*
     * Also, with this setup, you can remove these old methods completely:
     * - configureGlobal
     * - sessionAuthenticationStrategy
     * - KeycloakConfigResolver
     * They are only for the old Keycloak Spring adapter.
     * */

    /*
     * NOTE:
     * .anyRequest().permitAll()
     * means every endpoint is publicly accessible. The JWT token will not actually be required for access.
     * For a real secured microservice, use this instead:
     * .authorizeHttpRequests(auth -> auth
     * .requestMatchers("/actuator/**").permitAll()
     * .anyRequest().authenticated()
     * )
     * */
}
