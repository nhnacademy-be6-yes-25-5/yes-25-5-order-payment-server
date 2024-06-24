package com.yes25.yes255orderpaymentserver.common.jwt;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtUserDetailService implements UserDetailsService {

    private final JwtProvider jwtProvider;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String token = (String) SecurityContextHolder.getContext().getAuthentication().getCredentials();

        String userId = jwtProvider.getUserNameFromToken(token);
        List<String> roles = jwtProvider.getRolesFromToken(token);

        return JwtUserDetails.of(userId, roles);
    }
}
