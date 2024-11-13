package com.software.upskilled.utils;

import com.software.upskilled.Entity.Users;
import com.software.upskilled.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AdminRoleAuth
{
    @Autowired
    UserService userService;

    public boolean checkUserForAdminRole( Users user )
    {
        //Fetch the user details from the userID
        Users adminUser = userService.findUserById( user.getId() );
        //If no user is found, then send false
        if( adminUser == null )
            return false;
        else {
            //Check for the role of the Admin; If ADMIN then it will return true else False
            return adminUser.getRole().equals("ADMIN");

        }
    }
}
