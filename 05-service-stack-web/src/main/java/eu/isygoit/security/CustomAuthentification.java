package eu.isygoit.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * The type Custom authentification.
 */
public class CustomAuthentification extends UsernamePasswordAuthenticationToken {

    private String userName;
    private String domain;
    private String application;

    /**
     * Instantiates a new Custom authentification.
     *
     * @param principal   the principal
     * @param credentials the credentials
     */
    public CustomAuthentification(Object principal, Object credentials) {
        super(principal, credentials);
    }

    /**
     * Instantiates a new Custom authentification.
     *
     * @param principal   the principal
     * @param credentials the credentials
     * @param authorities the authorities
     */
    public CustomAuthentification(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
    }

    /**
     * Gets user name.
     *
     * @return the user name
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets user name.
     *
     * @param userName the user name
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Gets domain.
     *
     * @return the domain
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Sets domain.
     *
     * @param domain the domain
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     * Gets application.
     *
     * @return the application
     */
    public String getApplication() {
        return application;
    }

    /**
     * Sets application.
     *
     * @param application the application
     */
    public void setApplication(String application) {
        this.application = application;
    }
}
