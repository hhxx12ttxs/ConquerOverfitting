/*
 * Copyright (C) 2014 Mikel Corcuera <mik.corcuera@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.holacampus.api.subresources;

import com.holacampus.api.domain.Permission;
import com.holacampus.api.domain.Stage;
import com.holacampus.api.domain.User;
import com.holacampus.api.exceptions.HTTPErrorException;
import com.holacampus.api.hal.HalList;
import com.holacampus.api.mappers.StageMapper;
import com.holacampus.api.mappers.UserMapper;
import com.holacampus.api.security.AuthenticationRequired;
import com.holacampus.api.security.AuthenticationScheme;
import com.holacampus.api.security.PermissionScheme;
import com.holacampus.api.security.UserPrincipal;
import com.holacampus.api.utils.MyBatisConnectionFactory;
import com.holacampus.api.utils.Utils;
import com.holacampus.api.validators.CreationValid;
import com.theoryinpractise.halbuilder.api.RepresentationFactory;
import java.io.UnsupportedEncodingException;
import java.util.List;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Esta clase se encarga de gestionar las peticiones de la API al subrecurso
 * Stages. Es decir, gestiona las peticiones a la URL 
 * <code>users/{uid}/stages</code>.
 * <br/><br/>
 * Este subrecurso se encarga de gestionar las diferentes etapas dentro de la
 * trayectoria académica del usuario
 * @author Mikel Corcuera <mik.corcuera@gmail.com>
 */
public class StagesResource {
    
    private final User user;
    
    private static final Logger logger = LogManager.getLogger( StagesResource.class.getName());
    
    private static final PermissionScheme permissionScheme        = new PermissionScheme();
    static {
        permissionScheme.addPermissionScheme( PermissionScheme.Action.GET_MULTIPLE, Permission.LEVEL_MEMBER)
                        .addPermissionScheme(PermissionScheme.Action.POST_MULTIPLE, Permission.LEVEL_OWNER)
                        .addPermissionScheme(PermissionScheme.Action.GET_UNIQUE, Permission.LEVEL_MEMBER)
                        .addPermissionScheme(PermissionScheme.Action.PUT_UNIQUE, Permission.LEVEL_OWNER)
                        .addPermissionScheme(PermissionScheme.Action.DELETE_UNIQUE, Permission.LEVEL_OWNER);
    }
    
    /**
     * Constructor del subrecurso de etapas de la trayectoría académica.
     * @param user usuario al que pertenecen las etapas
     */
    public StagesResource( User user)
    {
        this.user = user;
    }
    
   /**
     * Esta función gestiona las peticiones GET al recurso 
     * <code>users/{uid}/stages</code>. Esta operación devuelve un lista 
     * con las etapas de la trayectoría académica del usuario. 
     * @param page página de los resultados
     * @param size tamaño de los resultados
     * @param sc información de seguridad y autenticación de la petición
     * @param uriInfo  información de la URL de la petición
     * @return lista con las etapas de la trayectoria académica del tamaño especificado
     * @throws UnsupportedEncodingException
     */
    @GET
    @AuthenticationRequired( AuthenticationScheme.AUTHENTICATION_SCHEME_TOKEN)
    @Produces( { RepresentationFactory.HAL_JSON})
    public HalList<Stage> getStages( @QueryParam("page") Integer page, 
            @QueryParam( "size") Integer size, @Context UriInfo uriInfo, @Context SecurityContext sc)  throws UnsupportedEncodingException
    {
        logger.info( "[GET] " + uriInfo.getPath());
        
        HalList<Stage> stages;
        Permission userPermission = new Permission();
        page = Utils.getValidPage(page);
        size = Utils.getValidSize(size);
        
        UserPrincipal up = (UserPrincipal) sc.getUserPrincipal();
        
        RowBounds rb = Utils.createRowBounds(page, size);
        
        SqlSession session = MyBatisConnectionFactory.getSession().openSession();

        try {
            StageMapper mapper      = session.getMapper( StageMapper.class);
            UserMapper userMapper   = session.getMapper( UserMapper.class);
            
            userMapper.getPermissions( up.getId(), user.getId(), userPermission);
            
            if( !permissionScheme.isAllowed(PermissionScheme.Action.GET_MULTIPLE, userPermission.getLevel())){
                throw new HTTPErrorException( Status.FORBIDDEN, "You are not allowed here");
            }
            
            List<Stage> stageList   = mapper.getStages(user, rb);
            int total               = mapper.getTotalStages(user);
            
            for( Stage stage : stageList) {
                stage.setPath( uriInfo.getPath() + "/" + stage.getId());
            }
            
            session.commit();
            
            stages = new HalList<Stage> ( stageList, total);
            stages.setResourceRelativePath( uriInfo.getPath());
            stages.setPage(page);
            stages.setSize(size);
         }catch( HTTPErrorException e) {
            throw e;
        }catch( Exception e) {
            logger.info( e +" : " +  e.getMessage());
            throw new HTTPErrorException( Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }finally {
            session.close();
        }

        return stages;
    }
    
   /**
     * Esta función gestiona las peticiones POST al recurso 
     * <code>users/{uid}/stages</code>. Esta operación añade una nueva etapa
     * a la trayectoria académica del usuario
     * @param stage datos de la etapa a añadir
     * @param sc información de seguridad y autenticación de la petición
     * @param uriInfo  información de la URL de la petición
     * @return representación de la etapa ya añadida
     */
    @POST
    @AuthenticationRequired( AuthenticationScheme.AUTHENTICATION_SCHEME_TOKEN)
    @Consumes( { RepresentationFactory.HAL_JSON, MediaType.APPLICATION_JSON})
    @Produces( { RepresentationFactory.HAL_JSON})
    public Stage createStage( @CreationValid @Valid Stage stage, @Context SecurityContext sc, @Context UriInfo uriInfo) {
        
        logger.info( "[POST] " + uriInfo.getPath());
        
        Permission userPermission   = new Permission();
        UserPrincipal up            = (UserPrincipal) sc.getUserPrincipal();
        SqlSession session          = MyBatisConnectionFactory.getSession().openSession(); 
        
        if( stage.getStudy() == null || stage.getStudy().getUniversity() == null)
            throw new HTTPErrorException( Status.BAD_REQUEST, "Your request body is malformed");
        
        try {
            StageMapper mapper      = session.getMapper( StageMapper.class);
            UserMapper userMapper   = session.getMapper( UserMapper.class);
            
            userMapper.getPermissions( up.getId(), user.getId(), userPermission);
            
            if( !permissionScheme.isAllowed(PermissionScheme.Action.POST_MULTIPLE, userPermission.getLevel())){
                throw new HTTPErrorException( Status.FORBIDDEN, "You are not allowed here");
            }
            
            int result              = mapper.createStage( user, stage);
            
            if( result == 0) {
                throw new HTTPErrorException( Response.Status.CONFLICT, "could not create stage");
            }
            
            stage = mapper.getStage( user, stage.getId());
            stage.setPath( uriInfo.getPath() + "/" + stage.getId());
            session.commit();
         }catch( HTTPErrorException e) {
            throw e;
        }catch( Exception e) {
            logger.info( e);
            throw new HTTPErrorException( Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }finally {
            session.close();
        }
        return stage;
    }
    
      /* Particular Study */

    /**
     * Esta función gestiona las peticiones GET al recurso 
     * <code>users/{uid}/stages/{id}</code>. Esta operación obtiene la representación
     * de una etapa en concreto, identificada por <b>id</b>, dentro de la trayectoria
     * académica del usuario
     * @param id identificador de la etapa académica
     * @param sc información de seguridad y autenticación de la petición
     * @param uriInfo  información de la URL de la petición
     * @return representación de la etapa académica
     */
    
    
    @Path( "{id}")
    @GET
    @AuthenticationRequired( AuthenticationScheme.AUTHENTICATION_SCHEME_TOKEN)
    @Produces( { RepresentationFactory.HAL_JSON})
    public Stage getStage( @PathParam( "id") Long id, @Context UriInfo uriInfo, @Context SecurityContext sc) {
        
        logger.info("[GET] " + uriInfo.getPath());
        
        Stage stage                 = null;
        Permission userPermission   = new Permission();
        UserPrincipal up            = (UserPrincipal) sc.getUserPrincipal();
        SqlSession session          = MyBatisConnectionFactory.getSession().openSession();

        try {
            StageMapper mapper      = session.getMapper( StageMapper.class);
            UserMapper userMapper   = session.getMapper( UserMapper.class);
            
            userMapper.getPermissions( up.getId(), user.getId(), userPermission);
            
            if( !permissionScheme.isAllowed(PermissionScheme.Action.GET_UNIQUE, userPermission.getLevel())){
                throw new HTTPErrorException( Status.FORBIDDEN, "You are not allowed here");
            }
            
            stage = mapper.getStage( user, id);
            
            if( stage == null) {
                throw new HTTPErrorException( Status.NOT_FOUND, "stage " + id + " not found");
            }
            stage.setPath( uriInfo.getPath());
            session.commit();
         }catch( HTTPErrorException e) {
            throw e;
        }catch( Exception e) {
            logger.info( e);
            throw new HTTPErrorException( Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }finally {
            session.close();
        }
        return stage;
    }
    
    /**
     * Esta función gestiona las peticiones PUT al recurso 
     * <code>users/{uid}/stages/{id}</code>. Esta operación modifica los datos de
     * una etapa en concreto, identificada por <b>id</b>, dentro de la trayectoria
     * académica del usuario
     * @param stage datos a modificar de la etapa
     * @param id identificador de la etapa académica
     * @param sc información de seguridad y autenticación de la petición
     * @param uriInfo  información de la URL de la petición
     * @return representación de la etapa académica ya modificada
     */
    @Path( "{id}")
    @PUT
    @AuthenticationRequired( AuthenticationScheme.AUTHENTICATION_SCHEME_TOKEN)
    @Produces( { RepresentationFactory.HAL_JSON})
    @Consumes( { RepresentationFactory.HAL_JSON, MediaType.APPLICATION_JSON})
    public Stage updateStage( @Valid Stage stage, @PathParam( "id") Long id,  @Context SecurityContext sc, @Context UriInfo uriInfo) {
        
        logger.info( "[PUT] " + uriInfo.getPath());
        
        stage.setId(id);
        Permission userPermission   = new Permission();
        UserPrincipal up            = (UserPrincipal) sc.getUserPrincipal();
        SqlSession session          = MyBatisConnectionFactory.getSession().openSession();

        try {
            StageMapper mapper      = session.getMapper( StageMapper.class);
            UserMapper userMapper   = session.getMapper( UserMapper.class);
            
            userMapper.getPermissions( up.getId(), user.getId(), userPermission);
            
            if( !permissionScheme.isAllowed(PermissionScheme.Action.PUT_UNIQUE, userPermission.getLevel())){
                throw new HTTPErrorException( Status.FORBIDDEN, "You are not allowed here");
            }
            int result          = mapper.updateStage( user, stage);
            
            if( result == 0) {
                throw new HTTPErrorException( Status.INTERNAL_SERVER_ERROR, "Error updating stage");
            }
            stage = mapper.getStage( user, stage.getId());
            stage.setPath( uriInfo.getPath());
            session.commit();
         }catch( HTTPErrorException e) {
            throw e;
        }catch( Exception e) {
            logger.info( e);
            throw new HTTPErrorException( Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }finally {
            session.close();
        }
        return stage;
    }
    
     /**
     * Esta función gestiona las peticiones DELETE al recurso 
     * <code>users/{uid}/stages/{id}</code>. Esta operación elimina 
     * una etapa en concreto, identificada por <b>id</b>, dentro de la trayectoria
     * académica del usuario
     * @param id identificador de la etapa académica
     * @param sc información de seguridad y autenticación de la petición
     * @param uriInfo  información de la URL de la petición
     */
    @Path( "{id}")
    @DELETE
    @AuthenticationRequired( AuthenticationScheme.AUTHENTICATION_SCHEME_TOKEN)
    public void deleteStage( @PathParam( "id") Long id,  @Context SecurityContext sc, @Context UriInfo uriInfo) {
        
        logger.info( "[DELETE] " + uriInfo.getPath());
        
        Permission userPermission   = new Permission();
        UserPrincipal up            = (UserPrincipal) sc.getUserPrincipal();
        SqlSession session          = MyBatisConnectionFactory.getSession().openSession();

        try {
            StageMapper mapper      = session.getMapper( StageMapper.class);
            UserMapper userMapper   = session.getMapper( UserMapper.class);
            
            userMapper.getPermissions( up.getId(), user.getId(), userPermission);
            
            if( !permissionScheme.isAllowed(PermissionScheme.Action.DELETE_UNIQUE, userPermission.getLevel())){
                throw new HTTPErrorException( Status.FORBIDDEN, "You are not allowed here");
            }
            
            Stage stage = mapper.getStage( user, id);
            
            if( stage == null) {
                throw new HTTPErrorException( Status.NOT_FOUND, "Stage " + id + " not found");
            }
            
            int result = mapper.deleteStage( user, stage.getId());
            
            if( result == 0) {
                throw new HTTPErrorException( Status.INTERNAL_SERVER_ERROR, "Error deleting stage");
            }
            
            session.commit();
         }catch( HTTPErrorException e) {
            throw e;
        }catch( Exception e) {
            logger.info( e);
            throw new HTTPErrorException( Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }finally {
            session.close();
        }
    }
    
}

