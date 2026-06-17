package fr.miage.toulouse.callme.competitionms.config;

import fr.miage.toulouse.callme.competitionms.security.RoleHeaderFilter;
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
                        .requestMatchers(HttpMethod.GET, "/competitions/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/competitions").hasAnyRole("ENSEIGNANT", "PRESIDENT")
                        .requestMatchers(HttpMethod.POST, "/competitions/*/resultats").hasAnyRole("ENSEIGNANT", "PRESIDENT")
                        .requestMatchers(HttpMethod.PATCH, "/competitions/**").hasAnyRole("ENSEIGNANT", "PRESIDENT")
                        .requestMatchers(HttpMethod.DELETE, "/competitions/**").hasRole("PRESIDENT")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(roleHeaderFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
