package com.example.moneymanager.security;

import com.example.moneymanager.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {
    private final UserDetailsService userDetailsService;
    private final JWTUtil jwtUtil;

  
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)throws ServletException, IOException{
          System.out.println("Request URI: " + request.getRequestURI());
    System.out.println("Servlet Path: " + request.getServletPath());
    System.out.println("HTTP Method: " + request.getMethod());

        final String authHeader=request.getHeader("Authorization");
        String email=null;
        String jwt=null;

        String path = request.getRequestURI();

    // Skip JWT check for public endpoints
    if (path.equals("/casho/home") ||
        path.equals("/casho/register") ||
        path.equals("/casho/login") ||
        path.equals("/casho/activate")) {
        filterChain.doFilter(request, response);
        return;
    }

        if(authHeader!=null && authHeader.startsWith("Bearer")){
            jwt=authHeader.substring(7);
            email= jwtUtil.extractUsername(jwt);

        }

        if(email!=null && SecurityContextHolder.getContext().getAuthentication()==null){
            UserDetails userDetails= this.userDetailsService.loadUserByUsername(email);
            if(jwtUtil.validateToken(jwt,userDetails)){
                UsernamePasswordAuthenticationToken authToken=new UsernamePasswordAuthenticationToken(
                        userDetails,null,userDetails.getAuthorities()

                        );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

        }

        filterChain.doFilter(request,response);
    }
}
