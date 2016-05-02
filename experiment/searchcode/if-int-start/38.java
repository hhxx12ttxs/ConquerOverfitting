/* ===========================================================
 * $Id: IRoleDAO.java 406 2009-07-13 02:43:35Z bitorb $
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

package org.gaixie.micrite.security.dao;

import java.util.List;

import org.gaixie.micrite.beans.Role;
import org.gaixie.micrite.dao.IGenericDAO;

/**
 * ??? <code>Role</code> ?????DAO???
 * 
 */
public interface IRoleDAO extends IGenericDAO<Role, Integer>{
    
    /**
     * ?????????????????????
     * 
     * @see org.gaixie.micrite.beans.Role
     * @param name ???
     * @param start ????
     * @param limit ???
     * @return <code>Role</code>????
     */
    public List<Role> findByNameVaguePerPage(String name, int start, int limit);
    
    /**
     * ????????????????
     * 
     * @see org.gaixie.micrite.beans.Role
     * @param name ???
     */
    public Integer findByNameVagueCount(String name);

    /**
     * ??????????
     * 
     * @see org.gaixie.micrite.beans.Role
     * @param rolename ???
     * @return <code>Role</code>??
     */
    public Role findByRolename(String rolename);
    
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
    public Integer findByUserIdCount(int userId);
    
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
    public Integer findByAuthorityIdCount(int authorityId);

}

