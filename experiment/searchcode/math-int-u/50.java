/* ===========================================================
 * $Id: UserServiceImpl.java 547 2009-10-28 04:55:15Z bitorb $
 * This file is part of Micrite
 * ===========================================================
 *
 * (C) Copyright 2009, by Gaixie.org and Contributors.
 * 
 * Project Info:  http://micrite.gaixie.org/
 *
 * Micrite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Micrite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Micrite.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.gaixie.micrite.security.service.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.gaixie.micrite.beans.Role;
import org.gaixie.micrite.beans.Setting;
import org.gaixie.micrite.beans.Token;
import org.gaixie.micrite.beans.User;
import org.gaixie.micrite.security.SecurityException;
import org.gaixie.micrite.security.dao.IRoleDAO;
import org.gaixie.micrite.security.dao.ISettingDAO;
import org.gaixie.micrite.security.dao.ITokenDAO;
import org.gaixie.micrite.security.dao.IUserDAO;
import org.gaixie.micrite.security.service.IUserService;
import org.gaixie.micrite.mail.IEmailSender;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.providers.dao.UserCache;
import org.springframework.security.providers.encoding.PasswordEncoder;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;

/**
 * ??????
 * 
 */
public class UserServiceImpl implements IUserService, UserDetailsService {
    
    @Autowired
    private IUserDAO userDAO;
    @Autowired
    private IRoleDAO roleDAO;
    @Autowired
    private ISettingDAO settingDAO;    
    @Autowired
    private ITokenDAO tokenDAO;    

    @Autowired
    private IEmailSender emailSender;
    
    //  ?????????
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserCache userCache;
    
    // ~~~~~~~~~~~~~~~~~~~~~~~  UserDetailsService Implement ~~~~~~~~~~~~~~~~~~~~~~~~~~//
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException, DataAccessException {
        User user = userDAO.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("User " + username
            + " has no GrantedAuthority");
        }
        Set<Role> roles = user.getRoles();
        for (Role role : roles) {
                role.getName();
        }
        
        return user;
    }
    
    // ~~~~~~~~~~~~~~~~~~~~~~~  IUserService Implement ~~~~~~~~~~~~~~~~~~~~~~~~~~//
    public void add(User user) throws SecurityException {
        if(isExistedByUsername(user.getUsername())) {
            throw new SecurityException("error.user.add.userNameInUse");
        }        

        //  ????
        String plainpassword = user.getPlainpassword();
        //  ??????
        String cryptpassword = passwordEncoder.encodePassword(plainpassword, null);
        user.setCryptpassword(cryptpassword);
        user.setEnabled(true);
        userDAO.save(user);
        
        String emailText =  "Dear " + user.getFullname() + ": \r\n"+
                            "A new account for "+user.getLoginname() +" has been created. \r\n"+
                            "\r\n"+                               
                            "This is an automatically generated message. Replies are not monitored or answered. \r\n"+
                            "\r\n"+     
                            "Sincerely \r\n"+
                            "The Micrite Support Team";        
        emailSender.sendEmail(null,user.getEmailaddress(), "New Account create Confirmation",emailText);        
    }

    public boolean isExistedByUsername(String username) {
        User user = userDAO.findByUsername(username);
        if (user != null) {
            return true;
        }
        return false;
    }
    
    public void updateMe(User u) throws SecurityException {
        //  ????
        User user = userDAO.get(u.getId());
        
        // ??????????????
        // ???????????????SecurityContext??????
        // ???loadUserByUsername???????????????UserDetails??(loadUserByUsername??????)?
        // ??acl_sid??????username???acl??(Principal?true)???security??????username????
        

        //  ?cache??????????User??
        if (userCache != null) {
            userCache.removeUserFromCache(user.getUsername());
        }
        
        // ????
        user.setFullname(u.getFullname());
        user.setEmailaddress(u.getEmailaddress());
        //  ??????????????
        if (!"".equals(u.getPlainpassword()) && u.getPlainpassword() != null) {
        	// ????????????????????????
			if (!passwordEncoder.isPasswordValid(user.getCryptpassword(), u.getOldPlainpassword(), null)) {
				throw new SecurityException("error.user.update.oldPasswordNotCorrect");
			}

            String cryptpassword = passwordEncoder.encodePassword(u.getPlainpassword(), null);
            user.setCryptpassword(cryptpassword);
        }
        
        if (u.getSettings() != null) {
            // ???????????TreeSet????????
            Set<Setting> settings = new HashSet<Setting>();
    
            //????????setting,??????????,????
            for (Setting s:u.getSettings()){
            	Setting setting = settingDAO.get(s.getId());
            	settings.add(setting);
            }
            user.setSettings(settings);
        }
        
        //  ?????????????????????createNewAuthentication()?????????????????
        userDAO.update(user);
        
        Authentication currentAuthentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityContextHolder.getContext().setAuthentication(createNewAuthentication(currentAuthentication));
        
        String emailText =  "Dear " + user.getFullname() + ": \r\n"+
                            "Your Account Settings have been modified. \r\n"+
                            "\r\n"+                               
                            "This is an automatically generated message. Replies are not monitored or answered. \r\n"+
                            "\r\n"+     
                            "Sincerely \r\n"+
                            "The Micrite Support Team";
        
        emailSender.sendEmail(null,user.getEmailaddress(), "Account Settings Change Confirmation",emailText);
    }
    
    public void update(User u) throws SecurityException {
        //  ????
        User user = userDAO.get(u.getId());
 
        // ???????????updateMe()?????
        //  ?cache??????????User??
        if (userCache != null) {
            userCache.removeUserFromCache(user.getUsername());
        }
        
        // ????
        user.setFullname(u.getFullname());
        user.setEmailaddress(u.getEmailaddress());
        
        //  ?????????????????????createNewAuthentication()?????????????????
        userDAO.update(user);
        
        Authentication currentAuthentication = SecurityContextHolder.getContext().getAuthentication();
        //  ???????????????????SecurityContext?????
        if (user.getUsername().equals(currentAuthentication.getName())) {
            SecurityContextHolder.getContext().setAuthentication(createNewAuthentication(currentAuthentication));
        }
        
        String emailText =  "Dear " + user.getFullname() + ": \r\n"+
                            "Your Account Settings have been modified. \r\n"+
                            "\r\n"+                               
                            "This is an automatically generated message. Replies are not monitored or answered. \r\n"+
                            "\r\n"+     
                            "Sincerely \r\n"+
                            "The Micrite Support Team";
        
        emailSender.sendEmail(null,user.getEmailaddress(), "Account Settings Change Confirmation",emailText);
        
    }    
    private Authentication createNewAuthentication(Authentication currentAuth) {
        UserDetails user = loadUserByUsername(currentAuth.getName());

        UsernamePasswordAuthenticationToken newAuthentication =
                new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
        newAuthentication.setDetails(currentAuth.getDetails());

        return newAuthentication;
    }

    public Integer findByFullnameVagueCount(String fullname) {
        return userDAO.findByFullnameVagueCount(fullname);
    }

    public List<User> findByFullnameVaguePerPage(String fullname, int start, int limit) {
        return userDAO.findByFullnameVaguePerPage(fullname, start, limit);
    }
    
    public Set<Setting> getSettings(int userId){
        User user = userDAO.get(userId);
        // ??TreeSet?settings????Setting??compareTo???
        Set<Setting> set = new TreeSet<Setting>();
        if (user.getSettings().size()>0){   
            // org.hibernate.LazyInitializationException
            for (Setting setting:user.getSettings()){
                setting.getName();
                set.add(setting);
            }
        }else{
            List<Setting> settings = settingDAO.findAllDefault();
            for (Setting setting:settings){
                set.add(setting);
            }
        }
        return set;
    }

    public List<Setting> findSettingByName(String name) {
        return settingDAO.findSettingByName(name);
    }   	
    
    public Set<Setting> findSettingByList(List<Setting> settings){
        Set<Setting> set = new TreeSet<Setting>();
        for (Setting setting:settings){
            set.add(settingDAO.get(setting.getId()));
        }    
        return set;
    }
    
    public void delete(int[] userIds) {
        for (int i = 0; i < userIds.length; i++) {
            User user = userDAO.get(userIds[i]);
            userDAO.delete(user);
            
            String emailText =  "Dear " + user.getFullname() + ": \r\n"+
                                "Your Account has been deleted. \r\n"+
                                "\r\n"+                               
                                "This is an automatically generated message. Replies are not monitored or answered. \r\n"+
                                "\r\n"+     
                                "Sincerely \r\n"+
                                "The Micrite Support Team";
            emailSender.sendEmail(null,user.getEmailaddress(), "Account delete Confirmation",emailText);
        }
    }
    
    public void updateStatus(int[] userIds) {
        for (int i = 0; i < userIds.length; i++) {
            User user = userDAO.get(userIds[i]);
            user.setEnabled(!user.isEnabled());
            //  ?cache????????
            if (userCache != null) {
                userCache.removeUserFromCache(user.getLoginname());
            }
        }
    }
    
    public List<User> findUsersByRoleIdPerPage(int roleId, int start, int limit) {
        List<User> users = userDAO.findByRoleIdPerPage(roleId,start,limit);
        return users;
    }    

    public Integer findUsersByRoleIdCount(int roleId) {
        return userDAO.findByRoleIdCount(roleId);
    }  
    
    public void bindUsers(int[] userIds, int roleId) {
        Role role = roleDAO.get(roleId);
        for (int i = 0; i < userIds.length; i++) {
            User user = userDAO.get(userIds[i]);
            Set<Role> roles =  user.getRoles();
            roles.add(role);
            user.setRoles(roles);
            //  ?cache????????
            if (userCache != null) {
                userCache.removeUserFromCache(user.getLoginname());
            }
        }
    }    
    
    public void unBindUsers(int[] userIds, int roleId) {
        Role role = roleDAO.get(roleId);
        for (int i = 0; i < userIds.length; i++) {
            User user = userDAO.get(userIds[i]);
            Set<Role> roles =  user.getRoles();
            roles.remove(role);
            user.setRoles(roles);
            //  ?cache????????
            if (userCache != null) {
                userCache.removeUserFromCache(user.getLoginname());
            }
        }
    }      
    
    public void forgotPasswordStepOne(String username, String baseUrl, String locale) throws SecurityException {
        User user = userDAO.findByUsername(username);

        if (user == null) {
            throw new SecurityException("forgotPassword.step1.usernameNotFound");
        }
        
        Random randomGenerator = new Random();
        long randomLong = randomGenerator.nextLong();
        
        Date date = new Date();  
        Calendar calendar = Calendar.getInstance();  
        calendar.setTime(date);  
        calendar.add(calendar.DAY_OF_MONTH, +1);  
        date = calendar.getTime(); 
        
        String key = passwordEncoder.encodePassword(Long.toString(randomLong) + date + username, null);
        String actionUrl = baseUrl +"forgotPasswordStepTwo.action?key="+key+"&request_locale="+locale;
        String emailText =  "Dear " + user.getFullname() + ": \r\n"+
                            "We've received a password reset request for "+user.getLoginname() +" .\r\n"+
                            "To initiate the process, please click the following link: \r\n"+
                            "\r\n"+
                            actionUrl+" \r\n"+
                            "\r\n"+
                            "If clicking the link above does not work, copy and paste the URL in \r\n"+
                            "a new browser window instead. The URL will expire in 24 hours for security \r\n"+
                            "reasons. \r\n"+
                            "\r\n"+                           
                            "Please disregard this message if you did not make a password reset request. \r\n"+
                            "\r\n"+                               
                            "This is an automatically generated message. Replies are not monitored or answered. \r\n"+
                            "\r\n"+     
                            "Sincerely \r\n"+
                            "The Micrite Support Team";
        
        emailSender.sendEmail(null,user.getEmailaddress(), "Reset Password Assistance",emailText);
        
        Token token = new Token(key,"password",date,user);
        tokenDAO.save(token);

    }
    
    public User findByTokenKey(String key) throws SecurityException {
        Token token = tokenDAO.findByKey(key);
        
        if (token == null) 
            throw new SecurityException("forgotPassword.step2.tokenNotAvailable");
        
        Date date = new Date(); 
        if(date.after(token.getExpireTime()))
            throw new SecurityException("forgotPassword.step2.tokenNotAvailable");
        
        return token.getUser();
        
    }
    
    public void forgotPasswordStepTwo(String key, String password) throws SecurityException{
        User user = findByTokenKey(key);
        
        //  ?cache??????????User??
        if (userCache != null) {
            userCache.removeUserFromCache(user.getUsername());
        }
        
        //  ??????????????
        if (!"".equals(password)) {
            String cryptpassword = passwordEncoder.encodePassword(password, null);
            user.setCryptpassword(cryptpassword);
        }
        
        userDAO.update(user);
    }
}

