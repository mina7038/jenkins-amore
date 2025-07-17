package com.apgroup.app.config;

import com.apgroup.app.repository.MemberRepository;
import com.apgroup.app.service.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.client.RestTemplate;

import com.apgroup.app.CustomLoginSuccessHandler;
import com.apgroup.app.service.CustomUserDetailsService;

import lombok.RequiredArgsConstructor;
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

   private final CustomUserDetailsService userDetailsService;
   private final MemberRepository memberRepository;
   private final CustomOAuth2UserService customOAuth2UserService;
   private final ClientRegistrationRepository clientRegistrationRepository;

   @Bean
   public CustomLoginSuccessHandler customLoginSuccessHandler() {
      return new CustomLoginSuccessHandler(memberRepository, passwordEncoder());
   }

   @Bean
   public OAuth2AuthorizationRequestResolver customAuthorizationRequestResolver() {
      return new CustomAuthorizationRequestResolver(clientRegistrationRepository);
   }



   @Bean
   public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      return http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                  .requestMatchers("/", "/goods/**", "/webjars/**", "/.well-known/**", "/member/login","/intro/**", "/member/join/**", "/member/check-username", "/uploads/**", "/member/term", "/common/**", "/js/**", "/images/**", "/ap_css/**", "/ap_img/**", "/il/**", "/il_css/**", "/il_js/**", "/il_img/**", "/os/**", "/os_css/**", "/os_img/**", "/os/notice/**", "/il/order/**", "/reviews/all", "/reviews/detail/**", "/support/notice/**", "/support/chat", "/support/list", "/oauth2/**").permitAll()
                  .requestMatchers("/", "/cart/**", "/member/mypage", "/member/edit", "/member/delete", "/order/**", "/payment/**", "/review/**").hasRole("USER")
                  .requestMatchers("/admin/**").hasRole("ADMIN")
                    .requestMatchers("/support/qna/**", "/reviews/delete/**").hasAnyRole("USER", "ADMIN")
                  .anyRequest().authenticated()
            )
            .formLogin(form -> form
                  .loginPage("/member/login") // 사용자 정의 로그인 페이지
                   .loginProcessingUrl("/member/login") // 로그인 폼의 action URL과 일치해야 함
                    .successHandler(customLoginSuccessHandler())
                   .failureUrl("/member/login?error=true") // 실패 시 이동할 URL
                   .permitAll()
            ).oauth2Login(oauth -> oauth
                      .loginPage("/member/login")
                      .authorizationEndpoint(auth -> auth
                              .authorizationRequestResolver(customAuthorizationRequestResolver()) // ✅ 수정된 Bean 사용
                      )
                      .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                      .successHandler(customLoginSuccessHandler())
                      .failureUrl("/member/login?error=true")
              )
            .logout(logout -> logout
                  .logoutRequestMatcher(new AntPathRequestMatcher("/member/logout", "GET"))
                  .logoutSuccessUrl("/")
                  .invalidateHttpSession(true)
                  .clearAuthentication(true)
                  .permitAll()
            )
            .build();

   }

   @Bean
   public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
      return authConfig.getAuthenticationManager();
   }

   @Bean
   public PasswordEncoder passwordEncoder( ) {
      return new BCryptPasswordEncoder();
   }

   @Bean
   public RestTemplate restTemplate() {
      return new RestTemplate();   //기본 생성자 사용
   }
}
