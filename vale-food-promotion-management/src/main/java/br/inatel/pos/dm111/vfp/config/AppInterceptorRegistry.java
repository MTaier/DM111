package br.inatel.pos.dm111.vfp.config;

import br.inatel.pos.dm111.vfp.api.core.interceptor.AuthenticationInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AppInterceptorRegistry implements WebMvcConfigurer {

    private final AuthenticationInterceptor authenticationInterceptor;

    public AppInterceptorRegistry(AuthenticationInterceptor authenticationInterceptor) {
        this.authenticationInterceptor = authenticationInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor);
    }
}