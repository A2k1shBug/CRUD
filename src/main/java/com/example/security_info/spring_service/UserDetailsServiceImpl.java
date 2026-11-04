package com.example.security_info.spring_service;


import com.example.user_info.repo.UserDao;
import com.example.user_info.model.PostUser;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserDao userDao;

    public UserDetailsServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        PostUser byUserName = userDao.findByName(username);
        if (byUserName != null) {
            return User.builder()
                    .username(byUserName.getName())
                    .password(byUserName.getPassword())
                    .roles(byUserName.getRoles().toArray(new String[0]))
                    .build();
        }
        throw new UsernameNotFoundException("User not found: " + username);
    }
}
