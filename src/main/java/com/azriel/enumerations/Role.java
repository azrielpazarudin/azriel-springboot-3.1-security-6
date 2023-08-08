package com.azriel.enumerations;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
@RequiredArgsConstructor
public enum Role {
    ADMIN(
        Set.of(Privilage.READ,Privilage.WRITE,Privilage.UPDATE,Privilage.DELETE)
    ),
    USER(
        Set.of(Privilage.READ)
    );
    @Getter
    private final Set<Privilage> privilages;

    public List<SimpleGrantedAuthority>getAuthority(){
        List<SimpleGrantedAuthority> authorities = getPrivilages().stream()
        .map(privilage->new SimpleGrantedAuthority(privilage.name()))
        .collect(Collectors.toList());
        authorities.add(new SimpleGrantedAuthority("ROLE_"+this.name()));
        return authorities;
    }
}
