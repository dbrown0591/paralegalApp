package com.paralegal.paralegalApp.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwt;

    private final UserDetailsService uds;

    public JwtAuthFilter(JwtService jwt, UserDetailsService uds) {
        this.jwt = jwt;
        this.uds = uds;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException{
        String header = request.getHeader("Authorization");
        String token = null;
        String subject = null;

        if(StringUtils.hasText(header) && header.startsWith("Bearer ")){
            token = header.substring(7);

            try{
                subject = jwt.extractSubject(token);
            }catch (Exception e){
                subject = null;
            }
        }
        if(subject != null && SecurityContextHolder.getContext().getAuthentication() == null ){

            try{
                UserDetails userDetails = uds.loadUserByUsername(subject);
                if(jwt.isValid(token, userDetails.getUsername())){
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }catch (UsernameNotFoundException ex) {
                // Token subject not in DB -> treat as unauthenticated (no 500)
            } catch (Exception ex) {
                // Any other token/user check problem -> unauthenticated
            }
        }
        chain.doFilter(request, response);
    }

}
