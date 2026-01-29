package com.example.didong2jv.service.impl; //

import java.util.Optional; //
import org.springframework.beans.factory.annotation.Autowired; //
import org.springframework.security.core.userdetails.UserDetails; //
import org.springframework.security.core.userdetails.UserDetailsService; //
import org.springframework.security.core.userdetails.UsernameNotFoundException; //
import org.springframework.stereotype.Service; //

import com.example.didong2jv.config.UserInfoConfig; //
import com.example.didong2jv.entity.User; //
import com.example.didong2jv.exceptions.ResourceNotFoundException; //
import com.example.didong2jv.repository.UserRepo; //

@Service //
public class UserDetailsServiceImpl implements UserDetailsService { //

    @Autowired //
    private UserRepo userRepo; //

    @Override //
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException { //
        Optional<User> user = userRepo.findByEmail(username); //
        
        return user.map(UserInfoConfig::new) //
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", username)); //
    }
}