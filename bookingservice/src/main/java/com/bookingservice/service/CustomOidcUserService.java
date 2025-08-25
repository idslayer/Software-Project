// src/main/java/com/bookingservice/security/CustomOAuth2UserService.java
package com.bookingservice.service;

import com.bookingservice.entities.User;
import com.bookingservice.enums.AuthProvider;
import com.bookingservice.enums.UserRole;
import com.bookingservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.GrantedAuthority;
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
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;

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

        // keep authorities & tokens, but replace userInfo with our merged claims
        Collection<? extends GrantedAuthority> auths = oidc.getAuthorities();
        OidcUserInfo userInfo = new OidcUserInfo(claims);

        // name attribute key "sub" is fine for Google
        return new DefaultOidcUser(auths, oidc.getIdToken(), userInfo, "sub");
    }
}

