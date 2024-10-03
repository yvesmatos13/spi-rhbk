package br.gov.rj.pge.keycloak;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.util.Timeout;
import java.io.IOException;

public class CustomAuthProvider implements Authenticator {

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        UserModel user = context.getUser();
        String username = user.getUsername();

        boolean isAuthenticated = callExternalAPI(username);

        if (isAuthenticated) {
            context.success();
        } else {
            context.failure(AuthenticationFlowError.INVALID_USER);
        }
    }

    private boolean callExternalAPI(String username) {
        String url = "https://api.example.com/user/" + username; // URL a ser ajustada conforme o OpenAPI

        // Configurando o timeout
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(10)) // Timeout de conexão
                .setResponseTimeout(Timeout.ofSeconds(10)) // Timeout de resposta
                .build();

        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(config) // Define o config no cliente
                .build()) {

            HttpGet request = new HttpGet(url);
            request.setHeader("Authorization", "Bearer <your-api-key>"); // Configurar com sua chave ou autenticação

            try (CloseableHttpResponse response = client.execute(request)) {
                int statusCode = response.getCode();

                if (statusCode == HttpStatus.SC_OK) {
                    // Lógica de sucesso, baseada na resposta da API
                    return true;
                } else if (statusCode == HttpStatus.SC_BAD_REQUEST || statusCode == HttpStatus.SC_NOT_FOUND) {
                    return false;  // Erros tratados como false
                }
            }
        } catch (IOException e) {
            // Timeout ou erro de conexão
            return false;
        }

        return false;
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        // Nenhuma ação necessária
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // Nenhuma ação adicional necessária
    }

    @Override
    public void close() {
        // Cleanup if necessary
    }
}
