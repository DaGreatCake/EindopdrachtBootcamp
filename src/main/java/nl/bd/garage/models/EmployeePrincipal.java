package nl.bd.garage.models;

import nl.bd.garage.models.entities.Employee;
import nl.bd.garage.models.enums.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;

// Handles the authorities of the currently logged in user.
public class EmployeePrincipal implements UserDetails {
    private Employee employee;

    public EmployeePrincipal(Employee employee) {
        this.employee = employee;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        HashSet<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(employee.getRole().getAuthority()));

        // Admin gets all authorities
        if (employee.getRole() == Role.ADMIN) {
            authorities.add(new SimpleGrantedAuthority(Role.ASSISTANT.getAuthority()));
            authorities.add(new SimpleGrantedAuthority(Role.MECHANIC.getAuthority()));
            authorities.add(new SimpleGrantedAuthority(Role.CASHIER.getAuthority()));
            authorities.add(new SimpleGrantedAuthority(Role.BACKOFFICE.getAuthority()));
        }

        return authorities;
    }

    @Override
    public String getPassword() {
        return employee.getPassword();
    }

    @Override
    public String getUsername() {
        return employee.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
