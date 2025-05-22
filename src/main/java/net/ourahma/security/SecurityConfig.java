package net.ourahma.security;

import net.ourahma.security.service.UserDetailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)// au niveau de controleur je dois proteget les endpoints moi meme
public class SecurityConfig {
    @Autowired
    private UserDetailServiceImpl userDetailServiceImpl;

    // définir les utilisateurs qui ont droit d 'accéder à l'application
    //@Bean
    public InMemoryUserDetailsManager inMemoryUserDetailsManager(PasswordEncoder passwordEncoder){
        String encodedPassword = passwordEncoder.encode("1234");
        System.out.println(encodedPassword);
        return new InMemoryUserDetailsManager(
                User.withUsername("user1").password(encodedPassword).roles("USER").build(),
                User.withUsername("user2").password(encodedPassword).roles("USER").build(),
                User.withUsername("admin").password(encodedPassword).roles("USER","ADMIN").build()
        );
    }
    //JDBC authentication
    //@Bean
    public JdbcUserDetailsManager jdbcUserDetailsManager(DataSource dataSource){
        // spécifier le data source, où on a les rôles et les tables
        return new JdbcUserDetailsManager(dataSource);
    }
    @Bean // Exécuter au démarrage
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        return httpSecurity
                .formLogin(form ->
                    form.loginPage("/login")
                            .defaultSuccessUrl("/user/index")
                            .permitAll()
                ).rememberMe(remember -> remember
                        .key("my-unique-key")
                        .tokenValiditySeconds(1209600)
                )
                .authorizeHttpRequests(ar ->ar.requestMatchers("/webjars/**").permitAll()
                        //.requestMatchers("/deletePatient/").hasRole("ADMIN")
                        //.requestMatchers("/admin/**").hasRole("ADMIN")
                        //.requestMatchers("/user/**").hasRole("USER")
                        .anyRequest().authenticated())
                        .userDetailsService(userDetailServiceImpl)
                        .exceptionHandling(exception ->{
                            exception.accessDeniedPage("/notAuthorized");
                        })
                        .build();
    }
}
