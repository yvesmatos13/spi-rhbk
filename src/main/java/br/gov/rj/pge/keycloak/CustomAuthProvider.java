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
import java.nio.charset.StandardCharsets;
import java.util.Base64;

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
        String url = "https://api.example.com/user/" + username; // Ajuste conforme necessário

        // Configuração de timeout
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(5)) // Timeout de conexão
                .setResponseTimeout(Timeout.ofSeconds(5)) // Timeout de resposta
                .build();

        // Credenciais para autenticação Basic
        String apiUsername = "username";  // Substitua com seu username
        String apiPassword = "password";     // Substitua com sua senha
        String auth = apiUsername + ":" + apiPassword;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .build()) {

            HttpGet request = new HttpGet(url);
            request.setHeader("Authorization", "Basic " + encodedAuth);  // Configura autenticação Basic

            try (CloseableHttpResponse response = client.execute(request)) {
                int statusCode = response.getCode();

                if (statusCode == HttpStatus.SC_OK) {
                    // Lógica de sucesso com base na resposta da API
                    return true;
                } else if (statusCode == HttpStatus.SC_BAD_REQUEST || statusCode == HttpStatus.SC_NOT_FOUND) {
                    return false;  // Erros como 400 ou 404 retornam false
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
        // Cleanup se necessário
    }
}
