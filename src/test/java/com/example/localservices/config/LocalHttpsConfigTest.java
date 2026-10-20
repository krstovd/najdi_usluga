package com.example.localservices.config;

import org.apache.catalina.core.StandardContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalHttpsConfigTest {
    @Test
    void redirectsEveryPathToConfiguredHttpsPort() {
        var factory = new TomcatServletWebServerFactory();
        new LocalHttpsConfig().localHttpsRedirect(8081, 8080).customize(factory);
        assertThat(factory.getAdditionalTomcatConnectors()).singleElement().satisfies(connector -> {
            assertThat(connector.getPort()).isEqualTo(8081);
            assertThat(connector.getRedirectPort()).isEqualTo(8080);
            assertThat(connector.getSecure()).isFalse();
            assertThat(connector.getProperty("address")).isInstanceOfSatisfying(
                    java.net.InetAddress.class,
                    address -> assertThat(address.getHostAddress()).isEqualTo("127.0.0.1"));
        });
        var context = new StandardContext();
        factory.getTomcatContextCustomizers().forEach(customizer -> customizer.customize(context));
        assertThat(context.findConstraints()).singleElement().satisfies(constraint -> {
            assertThat(constraint.getUserConstraint()).isEqualTo("CONFIDENTIAL");
            assertThat(constraint.findCollections()[0].findPatterns()).containsExactly("/*");
        });
    }

    @Test
    void rejectsConflictingOrEphemeralPorts() {
        var config = new LocalHttpsConfig();
        assertThatThrownBy(() -> config.localHttpsRedirect(8080, 8080))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> config.localHttpsRedirect(0, 8080))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void redirectConfigurationIsOptInAndExcludedFromProduction() {
        var runner = new ApplicationContextRunner().withUserConfiguration(LocalHttpsConfig.class);
        runner.withPropertyValues("spring.profiles.active=local")
                .run(context -> assertThat(context).doesNotHaveBean("localHttpsRedirect"));
        runner.withPropertyValues("spring.profiles.active=local,local-https")
                .run(context -> assertThat(context).hasBean("localHttpsRedirect"));
        runner.withPropertyValues("spring.profiles.active=prod,local-https")
                .run(context -> assertThat(context).doesNotHaveBean("localHttpsRedirect"));
    }
}
