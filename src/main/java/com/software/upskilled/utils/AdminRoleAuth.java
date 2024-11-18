package com.software.upskilled.utils;

import com.software.upskilled.Entity.Users;
import com.software.upskilled.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Component that provides a method to check if a user has an "ADMIN" role.
 * The `checkUserForAdminRole` method verifies if the provided user has the "ADMIN" role by fetching the user from the database
 * and comparing the role field.
 * Returns true if the user is an admin, otherwise returns false.
 */
@Component
public class AdminRoleAuth
{
    @Autowired
    UserService userService;

    public boolean checkUserForAdminRole( Users user ) {
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
