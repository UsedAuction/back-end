package com.ddang.usedauction.annotation;

import com.ddang.usedauction.security.auth.PrincipalDetails;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithCustomMockUserSecurityContextFactory implements
    WithSecurityContextFactory<WithCustomMockUser> {

    @Override
    public SecurityContext createSecurityContext(WithCustomMockUser annotation) {

        String memberId = annotation.memberId();
        String email = annotation.email();
        String password = annotation.password();
        String role = annotation.role();

        PrincipalDetails principalDetails = new PrincipalDetails(memberId, email, password, role);

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
            principalDetails, "",
            List.of(new SimpleGrantedAuthority(role)));
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(token);

        return context;
    }
}
