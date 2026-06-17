package fr.miage.toulouse.callme.coursms.config;

import fr.miage.toulouse.callme.coursms.security.RoleHeaderFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final RoleHeaderFilter roleHeaderFilter;

    public SecurityConfig(RoleHeaderFilter roleHeaderFilter) {
        this.roleHeaderFilter = roleHeaderFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/cours/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/cours").hasAnyRole("ENSEIGNANT", "SECRETAIRE", "PRESIDENT")
                        .requestMatchers(HttpMethod.PATCH, "/cours/**").hasAnyRole("ENSEIGNANT", "SECRETAIRE", "PRESIDENT")
                        .requestMatchers(HttpMethod.DELETE, "/cours/**").hasRole("PRESIDENT")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(roleHeaderFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
