package com.shuangqi.aiagent7.config;

import com.shuangqi.aiagent7.filter.*;
import com.shuangqi.aiagent7.service.AccountUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    private static final String[] URL_WHITELIST = {"/user/login", "/favicon.ico"};

    private final AccountUserDetailsService accountUserDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtLogoutSuccessHandler jwtLogoutSuccessHandler;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    public SecurityConfig(AccountUserDetailsService accountUserDetailsService, JwtAuthenticationFilter jwtAuthenticationFilter, JwtLogoutSuccessHandler jwtLogoutSuccessHandler, JwtAccessDeniedHandler jwtAccessDeniedHandler, LoginSuccessHandler loginSuccessHandler, LoginFailureHandler loginFailureHandler, JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.accountUserDetailsService = accountUserDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtLogoutSuccessHandler = jwtLogoutSuccessHandler;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
        this.loginSuccessHandler = loginSuccessHandler;
        this.loginFailureHandler = loginFailureHandler;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    /**
     @Bean public PasswordEncoder passwordEncoder() {
     return new BCryptPasswordEncoder();
     }
     **/

    /**
     * @return 身份校验机制、身份验证提供程序
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        // 创建一个用户认证提供者
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        // 设置用户相关信息，可以从数据库中读取、或者缓存、或者配置文件
        authProvider.setUserDetailsService(accountUserDetailsService);
        // 设置加密机制，用于对用户进行身份验证
        //authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * 基于用户名和密码或使用用户名和密码进行身份验证
     *
     * @param config
     * @return
     * @throws Exception
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用csrf(防止跨站请求伪造攻击)
                .csrf(csrf -> csrf.disable())
                // 登录操作
                .formLogin(form -> form.successHandler(loginSuccessHandler).failureHandler(loginFailureHandler))
                // 登出操作
                .logout(logout -> logout.logoutSuccessHandler(jwtLogoutSuccessHandler))
                // 使用无状态session，即不使用session缓存数据
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 设置白名单
                .authorizeHttpRequests(auth -> auth.requestMatchers(URL_WHITELIST).permitAll().anyRequest().authenticated())
                // 异常处理器
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint).accessDeniedHandler(jwtAccessDeniedHandler))
                // 添加jwt过滤器
                .authenticationProvider(authenticationProvider()).addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}