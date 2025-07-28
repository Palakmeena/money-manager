package com.example.moneymanager.service;

import com.example.moneymanager.entity.ProfileEntity;
import com.example.moneymanager.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {
    private final ProfileRepository profileRepository;
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String normalizedEmail = email.toLowerCase().trim();  // NEW: Normalize email
          ProfileEntity existingProfile= profileRepository.findByEmail(normalizedEmail)
                    .orElseThrow(()-> new UsernameNotFoundException("Profile not found with email:"+email));
          return User.builder()
                  .username(existingProfile.getEmail())
                  .password(existingProfile.getPassword())
                  .authorities(Collections.emptyList())
                  .build();



    }
}
