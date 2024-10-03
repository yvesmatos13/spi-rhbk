package br.gov.rj.pge.keycloak;

import java.util.List;

import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel.Requirement;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

public class CustomAuthProviderFactory implements AuthenticatorFactory {

    @Override
    public Authenticator create(KeycloakSession session) {
        return new CustomAuthProvider();
    }

    @Override
    public void init(org.keycloak.Config.Scope config) {
        // Inicialização da factory
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // Pós-inicialização
    }

    @Override
    public void close() {
        // Limpeza se necessário
    }

    @Override
    public String getId() {
        return "custom-auth-provider";
    }

    @Override
    public String getReferenceCategory() {
        return "Custom Authenticator SPI";
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public String getHelpText() {
        return "Autenticador customizado que consome uma API externa.";
    }

	@Override
	public String getDisplayType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Requirement[] getRequirementChoices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isUserSetupAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<ProviderConfigProperty> getConfigProperties() {
		// TODO Auto-generated method stub
		return null;
	}
}
