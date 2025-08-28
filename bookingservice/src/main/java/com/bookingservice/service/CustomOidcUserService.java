// src/main/java/com/bookingservice/security/CustomOAuth2UserService.java
package com.bookingservice.service;

import com.bookingservice.dto.LoginRequest;
import com.bookingservice.entities.User;
import com.bookingservice.enums.AuthProvider;
import com.bookingservice.enums.UserRole;
import com.bookingservice.repository.UserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest req) throws OAuth2AuthenticationException {
        OidcUser oidc = super.loadUser(req);
        String email = Optional.ofNullable(oidc.getEmail())
            .map(String::toLowerCase)
            .orElseThrow(() -> new OAuth2AuthenticationException("email_not_found"));


        // Tạo mới nếu chưa có, ngược lại cập nhật nhẹ name/picture
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User u = new User();
            u.setEmail(email);
            u.setFullName((String) oidc.getClaims().getOrDefault("name", email));
            u.setPictureUrl((String) oidc.getClaims().get("picture"));
            u.setAuthProvider(AuthProvider.GOOGLE);
            u.setRole(UserRole.USER);
            try {
                return userRepository.save(u);
            } catch (DataIntegrityViolationException ex) {
                // Tránh race condition: nếu 2 request tạo cùng email
                return userRepository.findByEmail(email).orElseThrow(() -> ex);
            }
        });

        // update basic fields if changed
        boolean dirty = false;
        String nm = Optional.ofNullable((String) oidc.getClaims().get("name")).orElse(user.getFullName());
        String pic = (String) oidc.getClaims().get("picture");
        if (!Objects.equals(nm, user.getFullName())) {
            user.setFullName(nm);
            dirty = true;
        }
        if (!Objects.equals(pic, user.getPictureUrl())) {
            user.setPictureUrl(pic);
            dirty = true;
        }
        if (dirty) userRepository.save(user);

        // merge claims + add appUserId so user.getAttribute("appUserId") works
        Map<String, Object> claims = new HashMap<>(oidc.getClaims());
        claims.put("appUserId", user.getId());
        claims.put("role", user.getRole());

        // keep authorities & tokens, but replace userInfo with our merged claims
        Collection<? extends GrantedAuthority> auths = oidc.getAuthorities();
        OidcUserInfo userInfo = new OidcUserInfo(claims);

        // name attribute key "sub" is fine for Google
        return new DefaultOidcUser(auths, oidc.getIdToken(), userInfo, "sub");
    }


    public String login(LoginRequest request) {
        User user = userRepository.findFirstByUsername(request.getUsername()).orElse(null);
        if (user == null) {
            return null;
        }
        if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return jwtService.generateToken(user.getUsername(), user.getRole().toString());
        }
        return null;

    }

    public Map<String, Object> loadAdmin(String token) {
        try {
            Claims claimsJws = jwtService.validateToken(token).getBody();
            log.debug("Claims: {}", claimsJws);
            User user = userRepository.findFirstByUsername(claimsJws.getSubject()).orElse(null);
            if (user == null) return null;
            if (user.getRole() != UserRole.ADMIN) return null;
            return Map.of(
                "name", user.getFullName(),
                "email", user.getEmail(),
                "picture", user.getPictureUrl(),
                "appUserId", user.getId(),
                "role", user.getRole()
            );
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }
}

