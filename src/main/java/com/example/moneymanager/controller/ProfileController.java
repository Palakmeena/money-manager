package com.example.moneymanager.controller;

import com.example.moneymanager.dto.AuthDTO;
import com.example.moneymanager.dto.ProfileDTO;
import com.example.moneymanager.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
//@RequestMapping("/casho")
@CrossOrigin(origins = "*")
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping("/register")
    public ResponseEntity<ProfileDTO> responseProfile(@RequestBody ProfileDTO profileDTO){
        ProfileDTO registeredProfile=profileService.registerProfile(profileDTO);
        return  ResponseEntity.status(HttpStatus.CREATED).body(registeredProfile);
    }

    @GetMapping("/activate")
    public ResponseEntity<String> activateProfile(@RequestParam String token) {
        try {
            boolean isActivated = profileService.activateProfile(token);
            if (isActivated) {
                return ResponseEntity.ok("Profile activated successfully");
            } else {
                return ResponseEntity.status(HttpStatus.GONE) // 410 is more appropriate than 404
                        .body("Activation token not found or already used");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Activation failed due to server error");
        }
    }

//    @PostMapping("/login")
//    public  ResponseEntity<Map<String,Object>> login(@RequestBody AuthDTO authDTO){
//          try {
//              if(!profileService.isAccountActive(authDTO.getEmail())) {
//                  return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
//                          "Message","Account is not active. Please active your account first."
//
//                  ));
//              }
//             Map<String,Object> response= profileService.authenticationAndGeneratedToken(authDTO);
//              return ResponseEntity.ok(response);
//          }
//          catch (Exception e){
//              return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
//                      "message", e.getMessage()
//              ));
//
//          }
//
//    }

    @PostMapping("/login")
    public ResponseEntity<Map<String,Object>> login(@RequestBody AuthDTO authDTO){
        try {
            // Normalize email input
            authDTO.setEmail(authDTO.getEmail().toLowerCase().trim());

            Map<String,Object> response = profileService.authenticationAndGeneratedToken(authDTO);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "message", "Invalid email or password"  // Fixed typo here
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "Login failed: " + e.getMessage()
            ));
        }
    }




    @GetMapping("/test")
    public String test(){
        return "Test successful";
    }
}
