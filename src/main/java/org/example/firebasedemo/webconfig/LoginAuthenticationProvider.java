package org.example.firebasedemo.webconfig;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LoginAuthenticationProvider implements AuthenticationProvider, UserDetailsPasswordService {

    private static class AuthenticationBlocker {
        boolean authenticated = false;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        final AuthenticationBlocker authBlocker = new AuthenticationBlocker();

        FirebaseDatabase.getInstance().getReference("admin/")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {

                    //grand admin privileges to the client with the correct name
                    //TIP: don't do this in a real app!!!!!
                    String username = authentication.getPrincipal().toString();
                    String adminName = snapshot.getValue().toString();
                    if(adminName.equals(username)) {
                        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                    } else {
                        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_GUEST"));
                    }

                    //firebase responded, unlock the blocker
                    synchronized(authBlocker) {
                        authBlocker.authenticated = true;
                        authBlocker.notifyAll();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {

                }
            });

        //wait for firebase to give a response
        synchronized(authBlocker) {
            try {
                authBlocker.wait(2000);
            } catch (InterruptedException e) {
                return null;
            }
        }

        if(!authBlocker.authenticated) {
            return null;
        }

        Authentication auth = new UsernamePasswordAuthenticationToken(authentication.getPrincipal(), authentication.getCredentials(), grantedAuthorities);
        return auth;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.isAssignableFrom(UsernamePasswordAuthenticationToken.class);
    }

    @Override
    public UserDetails updatePassword(UserDetails user, String newPassword) {
        //not yet implemented
        return null;
    }

}
