/* 
 * Persistence4J - Simple library for data persistence using java
 * Copyright (c) 2010, Avdhesh yadav.
 * http://www.avdheshyadav.com
 * Contact: avdhesh.yadav@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.avdheshyadav.p4j.jdbc.service;

import java.util.List;
import java.util.Map;

import com.avdheshyadav.p4j.common.DAOException;
import com.avdheshyadav.p4j.jdbc.model.DTO;

/**
 * 
 * @author Avdhesh Yadav
 *
 */
public interface DataFetcher
{
	public static final String Service_Name = "DataFetcher";
	
	
	/**
	 * 
	 * @param DTO dto
	 * 
	 * @return boolean
	 * 
	 * @throws DAOException
	 */
	boolean isEntityExists(DTO dto) throws DAOException;
	
	
	/**
	 * 
	 * @param dto
	 * @throws RemoteException
	 * @throws DAOException
	 */
	public void save(DTO dto) throws DAOException;
	
	/**
	 * This method first try to persist this entity.if this entity already exist 
	 * in the database then the database entity will be updated.
	 * 
	 * Note:- Method only works for the entities which have the primary key.
	 * 
	 * @param dto DTO 
	 * 
	 * @throws RemoteException
	 * @throws DAOException
	 */
	public void saveOrUpdate(DTO dto) throws DAOException;
	
	/**
	 * 
	 * @param tableName String
	 * @return
	 * @throws RemoteException
	 * @throws DAOException
	 */
	public List<? extends DTO> load(String tableName) throws DAOException;
	
	/**
	 * 
	 * @param tableName String
	 * @param startPosition int
	 * @param rows int
	 * 
	 * @return List<? extends GenericDTO>
	 * 
	 * @throws RemoteException
	 * @throws DAOException
	 */
	public List<? extends DTO> load(String tableName , int startPosition , int rows) throws DAOException;
	
	/**
	 * 
	 * @param tableName String
	 * @param obj
	 * 
	 * @return DTO
	 * 
	 * @throws RemoteException
	 */
	public DTO findByPrimaryKey(String tableName,Object obj[]) throws DAOException;
	
	/**
	 * 
	 * @param tableName String
	 * @param param String
	 * @param value String
	 * @param orderByField String
	 * @param ascDesc
	 * @param startPosition int
	 * @param count int
	 * @return
	 * @throws RemoteException
	 * @throws DAOException
	 */
	public List<? extends DTO> findByAttribute(String tableName,String param, String value, String orderByField, String ascDesc,int startPosition, int count) throws DAOException;
	
	/**
	 * 
	 * @param tableName String
	 * @param query
	 * @param startPosition
	 * @param count
	 * @retur
	 * @throws RemoteException
	 * @throws DAOException
	 */
	public List<? extends DTO> findByQuery(String tableName, String query, int startPosition,int count) throws DAOException;
	
	/**
	 *  
	 * @param vo ValueObject
	 * @param whereClause Map<String,Object>
	 * 
	 * @return ValueObject
	 * 
	 * @throws RemoteException
	 * @throws DAOException
	 */
	public DTO updateEntity(DTO dto,Map<String,Object> whereClause) throws DAOException; 
	
	/**
	 * 
	 * @param vo ValueObject
	 * 
	 * @throws RemoteException
	 * @throws DAOException
	 */
	public void deleteEntity(DTO vo) throws DAOException;
	
		
	/**
	 * 
	 * @param tableName String
	 * @param params
	 * @param values
	 * @param orderByField
	 * @param ascDesc
	 * @param startPosition
	 * @param count
	 * 
	 * @return List<? extends ValueObject>
	 * 
	 * @throws RemoteException
	 * @throws Exception
	 */
	public List<? extends DTO> find(String tableName,String[] params,String values[],String orderByField,String ascDesc,int startPosition,int count ) throws Exception;
	
	/**
	 * 
	 * @param tableName String
	 * @param params String
	 * @param value String
	 * 
	 * @return int
	 * 
	 * @throws RemoteException
	 * @throws DAOException
	 */
	public int countRows(String tableName,String params,String value) throws DAOException;
	
	/**
	 * 
	 * @param dtos List<DTO>
	 * 
	 * @return Map<Integer, DTO>
	 * 
	 * @throws RemoteException
	 * @throws DAOException
	 */
	public Map<Integer, DTO> batchInsert(List<DTO> dtos) throws DAOException;
	
	/**
	 * 
	 * @param tableName String
	 * @param queries List<String>
	 * 
	 * @return List<? extends DTO>
	 * 
	 * @throws RemoteException
	 * @throws DAOException
	 */
	public List<? extends DTO> executeQueries(String tableName, List<String> queries) throws DAOException;
	
	
	/**
	 * 
	 * @param query String
	 * 
	 * @throws RemoteException
	 * @throws DAOException
	 */
	public boolean executeQuery(String query) throws  DAOException;
}

