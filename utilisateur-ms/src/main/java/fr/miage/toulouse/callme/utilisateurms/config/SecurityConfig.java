package fr.miage.toulouse.callme.utilisateurms.config;

import fr.miage.toulouse.callme.utilisateurms.security.RoleHeaderFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/utilisateurs/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/utilisateurs").permitAll()
                        .requestMatchers(HttpMethod.GET, "/utilisateurs/*/apte").permitAll()
                        .requestMatchers(HttpMethod.GET, "/utilisateurs/*/niveau").permitAll()
                        .requestMatchers(HttpMethod.GET, "/utilisateurs/*/role").permitAll()
                        .requestMatchers(HttpMethod.GET, "/utilisateurs/*/exists").permitAll()
                        .requestMatchers(HttpMethod.GET, "/utilisateurs/**").hasAnyRole("MEMBRE", "ENSEIGNANT", "SECRETAIRE", "PRESIDENT")
                        .requestMatchers(HttpMethod.PATCH, "/utilisateurs/*/admin").hasAnyRole("SECRETAIRE", "PRESIDENT")
                        .requestMatchers(HttpMethod.PATCH, "/utilisateurs/**").hasAnyRole("MEMBRE", "ENSEIGNANT", "SECRETAIRE", "PRESIDENT")
                        .requestMatchers(HttpMethod.DELETE, "/utilisateurs/**").hasRole("PRESIDENT")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(roleHeaderFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
