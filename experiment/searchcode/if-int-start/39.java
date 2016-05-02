/* ===========================================================
 * $Id: IRoleService.java 515 2009-08-23 16:09:30Z bitorb $
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

package org.gaixie.micrite.security.service;

import java.util.List;

import org.gaixie.micrite.beans.Role;
import org.gaixie.micrite.security.SecurityException;

/**
 * ?????????????????????????
 * 
 */
public interface IRoleService {

    /**
     * ?????????????????????
     * 
     * @param name ???
     * @param start ????
     * @param limit ???
     */
    public List<Role> findByNameVaguePerPage(String name, int start, int limit);

    /**
     * ???????????????????
     * 
     * @param name ???
     */    
    public int findByNameVagueCount(String name);

    /**
     * ???????
     * 
     * @param roleIds ??id??
     */      
    public void deleteRoles(int[] roleIds) throws SecurityException;

    /**
     * ?????
     * 
     * @param role ??
     */      
    public void delete(Role role);
    
    /**
     * ??????
     * 
     * @param role ????
     * @throws SecurityException ?????????
     */
    public void add(Role role) throws SecurityException;
    
    /**
     * ?????
     * 
     * @param role ????
     * @throws SecurityException ?????????
     */
    public void update(Role role) throws SecurityException;    
    
    /**
     * ????id???????????
     * 
     * @param userId ??id
     * @param start ????
     * @param limit ???
     * @return ????
     */
    public List<Role> findByUserIdPerPage(int userId, int start, int limit);
    
    /**
     * ????id????????
     * 
     * @param userId ??id
     * @return ?????
     */
    public int findByUserIdCount(int userId);
    
    /**
     * ?????????
     * 
     * @param roleIds ??id??
     * @param userId ??id
     */
    public void bindRolesToUser(int[] roleIds, int userId);    
    
    /**
     * ????????????
     * 
     * @param roleIds ??id??
     * @param userId ??id
     */
    public void unBindRolesFromUser(int[] roleIds, int userId);

    /**
     * ????id???????????
     * 
     * @param authorityId ??id
     * @param start ????
     * @param limit ???
     * @return ????
     */
    public List<Role> findByAuthorityIdPerPage(int authorityId, int start, int limit);
    
    /**
     * ????id????????
     * 
     * @param authorityId ??id
     * @return ?????
     */
    public int findByAuthorityIdCount(int authorityId);

    /**
     * ?????????
     * 
     * @param roleIds ??id??
     * @param authorityId ??id
     */
    public void bindRolesToAuthority(int[] roleIds, int authorityId);
    
    /**
     * ????????????
     * 
     * @param roleIds ??id??
     * @param authorityId ??id
     */
    public void unBindRolesFromAuthority(int[] roleIds, int authorityId);

}

