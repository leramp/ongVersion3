package com.alkemy.ong.auth;

import com.alkemy.ong.auth.filter.JwtAuthenticationFilter;
import com.alkemy.ong.service.impl.CustomUserDetailsService;
import com.alkemy.ong.utils.RoleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)

public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private CustomUserDetailsService customUserDetailsService;


    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    public void configure(AuthenticationManagerBuilder managerBuilder) throws Exception {
        managerBuilder.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .exceptionHandling()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                //auth
                .antMatchers(HttpMethod.POST, "/auth/login").permitAll()
                .antMatchers(HttpMethod.POST, "/auth/register").permitAll()
                .antMatchers(HttpMethod.GET, "/auth/me").hasAnyRole(RoleEnum.ADMIN.getSimpleRoleName(), RoleEnum.USER.getSimpleRoleName())
                //organization
                .antMatchers(HttpMethod.POST, "/organization/public").hasRole(RoleEnum.ADMIN.getSimpleRoleName())
                .antMatchers(HttpMethod.GET, "/organization/public").permitAll()
                //users
                .antMatchers(HttpMethod.GET, "/users").hasRole(RoleEnum.ADMIN.getSimpleRoleName())
                .antMatchers(HttpMethod.DELETE, "/users/{id}").hasAnyRole(RoleEnum.ADMIN.getSimpleRoleName(), RoleEnum.USER.getSimpleRoleName())
                .antMatchers(HttpMethod.PATCH, "/users/{id}").hasAnyRole(RoleEnum.ADMIN.getSimpleRoleName(), RoleEnum.USER.getSimpleRoleName())
                //slides
                .antMatchers(HttpMethod.POST, "/slides").hasRole(RoleEnum.ADMIN.getSimpleRoleName())
                .antMatchers(HttpMethod.GET, "/slides").hasRole(RoleEnum.ADMIN.getSimpleRoleName())
                .antMatchers(HttpMethod.GET, "/slides/{id}").hasRole(RoleEnum.ADMIN.getSimpleRoleName())
                /*agregar autorizaciones a los endpoints pendientes en desarrollo

                 *EJEMPLO:
                 * PARA TODOS:
                 * .antMatchers(HttpMethod.<TIPO>, "<endpoint>").permitAll()
                 * PARA USER:
                 * .antMatchers(HttpMethod.<TIPO>, "<endpoint>").hasRole(RoleEnum.USER.getSimpleRoleName);
                 * PARA ADMIN:
                 * .antMatchers(HttpMethod.<TIPO>, "<endpoint>").hasRole(RoleEnum.ADMIN.getSimpleRoleName);
                 * PARA USER Y ADMIN:
                 * .antMatchers(HttpMethod.<TIPO>, ">endpoint>").hasAnyRole(RoleEnum.ADMIN.getSimpleRoleName(), RoleEnum.USER.getSimpleRoleName())
                 */
                .anyRequest()
                .authenticated();
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }
}
