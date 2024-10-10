package com.software.upskilled.service;


import com.software.upskilled.Entity.Users;
import com.software.upskilled.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UserDetails userDetails = userRepository.findByEmail(username);
        if(userDetails == null) {
            throw new UsernameNotFoundException("User does not exists");
        }
        return userDetails;
        }

    public Long createUser(Users appUser){
        appUser.setPassword(passwordEncoder.encode(appUser.getPassword()));
        userRepository.save(appUser);
        return appUser.getId();
    }
}
