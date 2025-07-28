package com.example.moneymanager.service;

import com.example.moneymanager.dto.AuthDTO;
import com.example.moneymanager.dto.ProfileDTO;
import com.example.moneymanager.entity.ProfileEntity;
import com.example.moneymanager.repository.ProfileRepository;
import com.example.moneymanager.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final  ProfileRepository profileRepository;
    private  final EmailService emailService;
    private  final  PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private  final JWTUtil jwtUtil;

    @Value("${app.activation.url}")
    private String activationURL;

   
    public ProfileDTO registerProfile(ProfileDTO profileDTO) {
        ProfileEntity newProfile = toEntity(profileDTO);
        newProfile.setActivationToken(UUID.randomUUID().toString());
        newProfile = profileRepository.save(newProfile);

        String activationLink = activationURL+"/casho/activate?token=" + newProfile.getActivationToken();
        String subject = "Activate your money manager account";
        String body = "Click on the following link to acitvate your account:" + activationLink;
        emailService.sendEmail(newProfile.getEmail(), subject, body);


        return toDTO(newProfile);

    }


    public ProfileEntity toEntity(ProfileDTO profileDTO){
        return ProfileEntity.builder()
                .id(profileDTO.getId())
                .fullName(profileDTO.getFullName())
                .email(profileDTO.getEmail())
                .password(passwordEncoder.encode(profileDTO.getPassword()))
                .profileImageUrl(profileDTO.getProfileImageUrl())
                .createdAt(profileDTO.getCreatedAt())
                .updatedAt(profileDTO.getUpdatedAt())
                .build();
    }

    public ProfileDTO toDTO(ProfileEntity profileEntity){
        return ProfileDTO.builder()
               .id(profileEntity.getId())
               .fullName(profileEntity.getFullName())
                .email(profileEntity.getEmail())
                .password(profileEntity.getPassword())
               .profileImageUrl(profileEntity.getProfileImageUrl())
                .createdAt(profileEntity.getCreatedAt())
               .updatedAt(profileEntity.getUpdatedAt())
               .build();
  }

    public boolean activateProfile(String activationToken){return profileRepository.findByActivationToken(activationToken)
                .map(profile->{
                    profile.setIsActive(true);
                    profileRepository.save(profile);
                    return  true;
                })
                .orElse(false);
    }

    public boolean isAccountActive(String email){
        return profileRepository.findByEmail(email)
                .map(ProfileEntity::getIsActive)
                .orElse(false);
    }

    public ProfileEntity getCurrentProfile(){

        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();

  return profileRepository.findByEmail(authentication.getName())
          .orElseThrow(()->new UsernameNotFoundException("Profile not found with email:"+ authentication.getName()));
    }

//    public ProfileDTO getPublicProfile(String email){
//        ProfileEntity currentUser=null;
//
//        if(email==null){
//           currentUser= getCurrentProfile();
//        }else{
//            profileRepository.findByEmail(email)
//                    .orElseThrow(()->new UsernameNotFoundException("Profile not found with email:"+  email));
//
//        }

    public ProfileDTO getPublicProfile(String email) {
        ProfileEntity currentUser;

        if(email == null) {
            currentUser = getCurrentProfile();
        } else {
            currentUser = profileRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Profile not found with email: "+ email));
        }

        return  ProfileDTO.builder()
                .id(currentUser.getId())
                .fullName(currentUser.getFullName())
                .email(currentUser.getEmail())
                .createdAt(currentUser.getCreatedAt())
                .updatedAt(currentUser.getUpdatedAt())
                .build();



    }

//    public Map<String,Object> authenticationAndGeneratedToken(AuthDTO authDTO){
//        try{
//            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authDTO.getEmail(),authDTO.getPassword()));
//            String token=jwtUtil.generateToken(authDTO.getEmail());
//            return  Map.of(
//                    // "token",token,
//                    "token",token,
//                    "user",getPublicProfile(authDTO.getEmail())
//            );
//        }
//        catch (Exception e){
//            throw new RuntimeException("Invalis email or password");
//        }
//    }

    public Map<String, Object> authenticationAndGeneratedToken(AuthDTO authDTO) {
        try {
            // 1. Normalize email and trim password
            String normalizedEmail = authDTO.getEmail().toLowerCase().trim();
            String trimmedPassword = authDTO.getPassword().trim();

            // 2. Debug output (remove in production)
            System.out.println("Login attempt for: " + normalizedEmail);
            System.out.println("Password received: [" + trimmedPassword + "]");

            // 3. Find user first to verify account status
            ProfileEntity user = profileRepository.findByEmail(normalizedEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // 4. Debug stored hash (remove in production)
            System.out.println("Stored password hash: " + user.getPassword());
            System.out.println("Password matches? " +
                    passwordEncoder.matches(trimmedPassword, user.getPassword()));

            // 5. Check if account is active
            if (!user.getIsActive()) {
                throw new RuntimeException("Account not activated. Please check your email.");
            }

            // 6. Authenticate with Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            normalizedEmail,
                            trimmedPassword
                    )
            );

            // 7. Generate JWT token
            String token = jwtUtil.generateToken(normalizedEmail);

            // 8. Return response
            return Map.of(
                    "token", token,
                    "user", getPublicProfile(normalizedEmail)
            );

        } catch (UsernameNotFoundException e) {
            throw new RuntimeException("User not found", e);
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Invalid email or password", e);
        } catch (Exception e) {
            throw new RuntimeException("Login failed: " + e.getMessage(), e);
        }
    }



}




























