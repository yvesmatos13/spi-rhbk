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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class CustomAuthProvider implements Authenticator {

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthProvider.class);

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        logger.debug("::authenticate");

        if (context.getAuthenticatorConfig() == null) {
            logger.error("[CUSTOM-AUTH] Ausência de configuração detectada!");
            context.failure(AuthenticationFlowError.INVALID_USER);
            return;
        } else if (!isValidConfig(context)) {
            context.failure(AuthenticationFlowError.INVALID_USER);
            return;
        }

        UserModel user = context.getUser();
        String username = user.getUsername();

        logger.info("Iniciando autenticação para o usuário: {}", username);

        String apiUrl = context.getAuthenticatorConfig().getConfig().get(CustomAuthProviderFactory.PROPERTY_URL);
        String apiUsername = context.getAuthenticatorConfig().getConfig().get(CustomAuthProviderFactory.PROPERTY_USERNAME);
        String apiPassword = context.getAuthenticatorConfig().getConfig().get(CustomAuthProviderFactory.PROPERTY_PASSWORD);
        int connectTimeout = Integer.parseInt(context.getAuthenticatorConfig().getConfig().getOrDefault(CustomAuthProviderFactory.PROPERTY_TIMEOUT_CONNECT, "5"));
        int responseTimeout = Integer.parseInt(context.getAuthenticatorConfig().getConfig().getOrDefault(CustomAuthProviderFactory.PROPERTY_TIMEOUT_RESPONSE, "5"));

        boolean isAuthenticated = callExternalAPI(username, apiUrl, apiUsername, apiPassword, connectTimeout, responseTimeout);

        if (isAuthenticated) {
            logger.info("Autenticação bem-sucedida para o usuário: {}", username);
            context.success();
        } else {
            logger.warn("Falha na autenticação para o usuário: {}", username);
            context.failure(AuthenticationFlowError.INVALID_USER);
        }
    }

    private boolean isValidConfig(AuthenticationFlowContext context) {
        logger.debug("::isValidConfig");

        boolean valid = true;

        String apiUrl = context.getAuthenticatorConfig().getConfig().get(CustomAuthProviderFactory.PROPERTY_URL);
        String apiUsername = context.getAuthenticatorConfig().getConfig().get(CustomAuthProviderFactory.PROPERTY_USERNAME);
        String apiPassword = context.getAuthenticatorConfig().getConfig().get(CustomAuthProviderFactory.PROPERTY_PASSWORD);
        String connectTimeoutStr = context.getAuthenticatorConfig().getConfig().get(CustomAuthProviderFactory.PROPERTY_TIMEOUT_CONNECT);
        String responseTimeoutStr = context.getAuthenticatorConfig().getConfig().get(CustomAuthProviderFactory.PROPERTY_TIMEOUT_RESPONSE);

        if (isNullOrEmpty(apiUrl)) {
            logger.error("[CUSTOM-AUTH] Configuração inválida! " + CustomAuthProviderFactory.PROPERTY_URL + " não foi preenchido.");
            valid = false;
        }

        if (isNullOrEmpty(apiUsername)) {
            logger.error("[CUSTOM-AUTH] Configuração inválida! " + CustomAuthProviderFactory.PROPERTY_USERNAME + " não foi preenchido.");
            valid = false;
        }

        if (isNullOrEmpty(apiPassword)) {
            logger.error("[CUSTOM-AUTH] Configuração inválida! " + CustomAuthProviderFactory.PROPERTY_PASSWORD + " não foi preenchido.");
            valid = false;
        }

        if (isNullOrEmpty(connectTimeoutStr) || !isNumeric(connectTimeoutStr)) {
            logger.error("[CUSTOM-AUTH] Configuração inválida! " + CustomAuthProviderFactory.PROPERTY_TIMEOUT_CONNECT + " não foi preenchido ou não é um número.");
            valid = false;
        }

        if (isNullOrEmpty(responseTimeoutStr) || !isNumeric(responseTimeoutStr)) {
            logger.error("[CUSTOM-AUTH] Configuração inválida! " + CustomAuthProviderFactory.PROPERTY_TIMEOUT_RESPONSE + " não foi preenchido ou não é um número.");
            valid = false;
        }

        return valid;
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isNumeric(String str) {
        return str.matches("\\d+");
    }

    private boolean callExternalAPI(String username, String apiUrl, String apiUsername, String apiPassword, int connectTimeout, int responseTimeout) {
        String url = apiUrl + "?username=" + username;

        // Configuração de timeout
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(connectTimeout))  // Timeout de conexão
                .setResponseTimeout(Timeout.ofSeconds(responseTimeout)) // Timeout de resposta
                .build();

        // Credenciais para autenticação Basic
        String auth = apiUsername + ":" + apiPassword;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        logger.debug("Enviando requisição para a URL: {}", url);

        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .build()) {

            HttpGet request = new HttpGet(url);
            request.setHeader("Authorization", "Basic " + encodedAuth);  // Configura autenticação Basic

            logger.debug("Requisição executada com o header Authorization: Basic *****");

            try (CloseableHttpResponse response = client.execute(request)) {
                int statusCode = response.getCode();
                logger.debug("Recebido status code: {}", statusCode);

                if (statusCode == HttpStatus.SC_OK) {
                    logger.info("Usuário {} autenticado com sucesso na API externa.", username);
                    return true;
                } else if (statusCode == HttpStatus.SC_BAD_REQUEST || statusCode == HttpStatus.SC_NOT_FOUND) {
                    logger.warn("Erro de autenticação na API externa para o usuário {}. Status code: {}", username, statusCode);
                    return false;  // Erros como 400 ou 404 retornam false
                }
            }
        } catch (IOException e) {
            logger.error("Erro ao chamar a API externa para o usuário {}. Mensagem: {}", username, e.getMessage());
            return false;
        }

        return false;
    }

    @Override
    public void action(AuthenticationFlowContext context) {
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
    }

    @Override
    public void close() {
    }
}
