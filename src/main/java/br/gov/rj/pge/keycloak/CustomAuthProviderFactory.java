package br.gov.rj.pge.keycloak;

import org.jboss.logging.Logger;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel.Requirement;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.Config;

import java.util.ArrayList;
import java.util.List;

public class CustomAuthProviderFactory implements AuthenticatorFactory {

    public static final String PROVIDER_ID = "custom-auth-provider";
    private static final Authenticator AUTHENTICATOR = new CustomAuthProvider();

    private static final Logger logger = Logger.getLogger(CustomAuthProviderFactory.class);

    // Configurable properties
    public static final String PROPERTY_URL = "api.url";
    public static final String PROPERTY_URL_LABEL = "URL do Serviço";
    public static final String PROPERTY_URL_HELP_TEXT = "URL do serviço (API) externo para autenticação";

    public static final String PROPERTY_USERNAME = "api.username";
    public static final String PROPERTY_USERNAME_LABEL = "Nome de usuário";
    public static final String PROPERTY_USERNAME_HELP_TEXT = "Nome de usuário para autenticação na API externa";

    public static final String PROPERTY_PASSWORD = "api.password";
    public static final String PROPERTY_PASSWORD_LABEL = "Senha";
    public static final String PROPERTY_PASSWORD_HELP_TEXT = "Senha para autenticação na API externa";

    public static final String PROPERTY_TIMEOUT_CONNECT = "timeout.connect";
    public static final String PROPERTY_TIMEOUT_CONNECT_LABEL = "Timeout de Conexão (segundos)";
    public static final String PROPERTY_TIMEOUT_CONNECT_HELP_TEXT = "Tempo limite de conexão para a API externa";

    public static final String PROPERTY_TIMEOUT_RESPONSE = "timeout.response";
    public static final String PROPERTY_TIMEOUT_RESPONSE_LABEL = "Timeout de Resposta (segundos)";
    public static final String PROPERTY_TIMEOUT_RESPONSE_HELP_TEXT = "Tempo limite de resposta da API externa";

    private final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    @Override
    public Authenticator create(KeycloakSession session) {
        logger.debug("Criando instância de CustomAuthProvider");
        return AUTHENTICATOR;
    }

    @Override
    public void init(Config.Scope config) {
        logger.info("Inicializando CustomAuthProviderFactory");

        ProviderConfigProperty property;

        // URL do serviço
        property = new ProviderConfigProperty();
        property.setName(PROPERTY_URL);
        property.setLabel(PROPERTY_URL_LABEL);
        property.setHelpText(PROPERTY_URL_HELP_TEXT);
        property.setType(ProviderConfigProperty.STRING_TYPE);
        configProperties.add(property);

        // Username
        property = new ProviderConfigProperty();
        property.setName(PROPERTY_USERNAME);
        property.setLabel(PROPERTY_USERNAME_LABEL);
        property.setHelpText(PROPERTY_USERNAME_HELP_TEXT);
        property.setType(ProviderConfigProperty.STRING_TYPE);
        configProperties.add(property);

        // Password (mantido como STRING_TYPE para entrada segura, o ideal seria FIELD_TYPE_PASSWORD para ocultar a entrada)
        property = new ProviderConfigProperty();
        property.setName(PROPERTY_PASSWORD);
        property.setLabel(PROPERTY_PASSWORD_LABEL);
        property.setHelpText(PROPERTY_PASSWORD_HELP_TEXT);
        property.setType(ProviderConfigProperty.PASSWORD);
        configProperties.add(property);

        // Timeout de conexão
        property = new ProviderConfigProperty();
        property.setName(PROPERTY_TIMEOUT_CONNECT);
        property.setLabel(PROPERTY_TIMEOUT_CONNECT_LABEL);
        property.setHelpText(PROPERTY_TIMEOUT_CONNECT_HELP_TEXT);
        property.setType(ProviderConfigProperty.STRING_TYPE);
        configProperties.add(property);

        // Timeout de resposta
        property = new ProviderConfigProperty();
        property.setName(PROPERTY_TIMEOUT_RESPONSE);
        property.setLabel(PROPERTY_TIMEOUT_RESPONSE_LABEL);
        property.setHelpText(PROPERTY_TIMEOUT_RESPONSE_HELP_TEXT);
        property.setType(ProviderConfigProperty.STRING_TYPE);
        configProperties.add(property);
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        logger.debug("PostInit de CustomAuthProviderFactory");
    }

    @Override
    public void close() {
        logger.info("Fechando CustomAuthProviderFactory");
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "Custom Authenticator SPI";
    }

    @Override
    public String getReferenceCategory() {
        return "Custom Authenticator";
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public Requirement[] getRequirementChoices() {
        return new Requirement[] { Requirement.REQUIRED, Requirement.DISABLED };
    }

    @Override
    public String getHelpText() {
        return "Autenticador customizado que consome uma API externa com autenticação baseada em nome de usuário e senha.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }
}
