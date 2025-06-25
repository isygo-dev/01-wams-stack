package eu.isygoit.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * The type Custom authentification.
 */
@Getter
@Setter
public class CustomAuthentification extends UsernamePasswordAuthenticationToken {

    /**
     * The constant serialVersionUID.
     */
    public static final long serialVersionUID = -1378977978987113579L;

    private String userName;
    private String tenant;
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
}
