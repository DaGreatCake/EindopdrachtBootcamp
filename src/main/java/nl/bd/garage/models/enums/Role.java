package nl.bd.garage.models.enums;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    ASSISTANT(Code.ASSISTANT),
    MECHANIC(Code.MECHANIC),
    CASHIER(Code.CASHIER),
    BACKOFFICE(Code.BACKOFFICE),
    ADMIN(Code.ADMIN);

    private final String authority;

    Role(String authority) {
        this.authority = authority;
    }

    @Override
    public String getAuthority() {
        return this.authority;
    }

    public class Code {
        public static final String ASSISTANT = "ROLE_ASSISTANT";
        public static final String MECHANIC = "ROLE_MECHANIC";
        public static final String CASHIER = "ROLE_CASHIER";
        public static final String BACKOFFICE = "ROLE_BACKOFFICE";
        public static final String ADMIN = "ROLE_ADMIN";
    }
}
