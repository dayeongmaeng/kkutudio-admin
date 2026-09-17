package com.kkutudio.admin.integration;

import com.kkutudio.admin.application.AppConnection;
import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.core.env.Environment;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Component
public class AppAdminClientFactory {

    private final Environment environment;

    public AppAdminClientFactory(Environment environment) {
        this.environment = environment;
    }

    public AppAdminClient create(AppConnection connection) {
        String credential = environment.getProperty(connection.getCredentialEnvKey());
        Duration timeout = Duration.ofMillis(connection.getTimeoutMs());

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(timeout)
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(timeout);

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(connection.getBaseUrl())
                .requestFactory(requestFactory);
        if (credential != null) {
            builder.defaultHeader("X-Admin-Api-Key", credential);
        }

        RestClientAdapter adapter = RestClientAdapter.create(builder.build());
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(AppAdminClient.class);
    }
}
