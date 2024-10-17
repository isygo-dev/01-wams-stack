package eu.isygoit.security;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * The type Custom user details.
 */
@Data
@SuperBuilder
public class CustomUserDetails implements UserDetails {

    @Builder.Default
    private boolean domainEnabled = false;
    @Builder.Default
    private boolean accountEnabled = false;
    @Builder.Default
    private boolean accountExpired = true;
    @Builder.Default
    private boolean accountLocked = true;
    @Builder.Default
    private boolean passwordExpired = true;

    private Collection<? extends GrantedAuthority> authorities;
    private String password;
    private String username;
    private Boolean isAdmin;


    /**
     * Instantiates a new Custom user details.
     *
     * @param username        the username
     * @param password        the password
     * @param isAdmin         the is admin
     * @param authorities     the authorities
     * @param domainEnabled   the domain enabled
     * @param accountEnabled  the account enabled
     * @param accountExpired  the account expired
     * @param passwordExpired the password expired
     * @param accountLocked   the account locked
     */
    public CustomUserDetails(String username,
                             String password,
                             Boolean isAdmin,
                             Collection<? extends GrantedAuthority> authorities,
                             boolean domainEnabled,
                             boolean accountEnabled,
                             boolean accountExpired,
                             boolean passwordExpired,
                             boolean accountLocked) {
        this.username = username;
        this.isAdmin = isAdmin;
        this.password = password;
        this.authorities = authorities;
        this.domainEnabled = domainEnabled;
        this.accountEnabled = accountEnabled;
        this.accountExpired = accountExpired;
        this.passwordExpired = passwordExpired;
        this.accountLocked = accountLocked;
    }

    @Override
    public boolean isAccountNonExpired() {
        return !this.accountExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !this.accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !passwordExpired;
    }

    @Override
    public boolean isEnabled() {
        return this.domainEnabled && this.accountEnabled;
    }
}
