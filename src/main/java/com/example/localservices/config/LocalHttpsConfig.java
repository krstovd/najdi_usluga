package com.example.localservices.config;

import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration(proxyBeanMethods = false)
@Profile("local-https & !prod")
public class LocalHttpsConfig {

    @Bean
    WebServerFactoryCustomizer<TomcatServletWebServerFactory> localHttpsRedirect(
            @Value("${app.local-https.http-port:8081}") int httpPort,
            @Value("${server.port:8080}") int httpsPort) {
        if (httpPort <= 0 || httpsPort <= 0 || httpPort == httpsPort) {
            throw new IllegalArgumentException("Local HTTP and HTTPS ports must be positive and different");
        }
        return factory -> {
            Connector http = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
            http.setPort(httpPort);
            http.setProperty("address", "127.0.0.1");
            http.setScheme("http");
            http.setSecure(false);
            http.setRedirectPort(httpsPort);
            factory.addAdditionalTomcatConnectors(http);
            factory.addContextCustomizers(context -> {
                SecurityCollection collection = new SecurityCollection();
                collection.addPattern("/*");
                SecurityConstraint constraint = new SecurityConstraint();
                constraint.setUserConstraint("CONFIDENTIAL");
                constraint.addCollection(collection);
                context.addConstraint(constraint);
            });
        };
    }
}
