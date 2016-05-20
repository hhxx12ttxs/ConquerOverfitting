package com.cibertec.proydawi.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.omg.CORBA.portable.ApplicationException;

import com.cibertec.proydawi.bean.UsuarioDTO;
import com.cibertec.proydawi.dao.UsuarioDAO;
import com.cibertec.proydawi.utils.Entidad;
import com.mysql.jdbc.EscapeTokenizer;
import com.opensymphony.xwork2.ActionContext;

public class UsuarioAction extends AbstractAction 
{
	private UsuarioDTO us;
	
	private List<UsuarioDTO> lstusuario;
	private List<UsuarioDTO> listUsuarios;
	
	private String mensaje="";
	
	
	
	public String getMensaje() {
		return mensaje;
	}

	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}

	public List<UsuarioDTO> getListUsuarios() {
		return listUsuarios;
	}

	public void setListUsuarios(List<UsuarioDTO> listUsuarios) {
		this.listUsuarios = listUsuarios;
	}

	public List<UsuarioDTO> getLstusuario() {
		return lstusuario;
	}

	public void setLstusuario(List<UsuarioDTO> lstusuario) {
		this.lstusuario = lstusuario;
	}

	 
	public UsuarioDTO getUs() {
		return us;
	}

	public void setUs(UsuarioDTO us) {
		this.us = us;
	}

	@Override
	public String execute() throws Exception
	{
		// TODO Auto-generated method stub
		//Map session=ActionContext.getContext().getSession();
		//session.put("login", true);
		//session.put("state", null);
		
		return EXITO;
		
		/*switch (us.getTipo()) 
			{
				case 100:
					return "exitodocente";
				case 101:
					return "exitoadm";
				default:
					return "exito"; //verificar esto, ya que ne este caso no habría un tipo de usuario
			}
		}else
		{
			mensaje="Error Verifique su usuario y clave";
			//session= ActionContext.getContext().getSession();
			session.put("msj", mensaje);
			System.out.println("mensaje "+mensaje);
			return "error";
		}*/
		
	}

	
	public String login()
	{
		System.out.println("usuario "+us.getUsuario()+
							" clave :"+us.getClave());
		UsuarioDAO udao=new UsuarioDAO();
		UsuarioDTO aux=udao.validar(us);
		

		if(aux!=null)
		{
			Map<String, Object>	session=ActionContext.getContext().getSession();
			
			session.put("usuario", aux);
			
			System.out.println("login ok "+aux.getCod_tuw());
			return EXITO;
		}
		//
		
		return "error";
		/*if(us!=null)//existe
		{
			//bienvenida, guardar en session datos del usuario.....
			session=	ActionContext.getContext().getSession();
			
		
			switch (us.getCod_tuw()) {
			case 1:
				return "exitoadm";
			case 2:
				return "exitotram";
			default:
				return "exitousu";
			}

		}else
		{
			mensaje="Verifique su clave  y/o contrase?a";
		
			
			System.out.println("mensaje "+mensaje);
			return "error";
		}*/
		
	}

	public String aNuevoU() throws Exception
	{
		Map session=ActionContext.getContext().getSession();
		session.put("state", "nuevo");
		return EXITO;
	}
	//listado de Usuario
	public String listaUsuario() throws Exception
	{
		System.out.println("[UsuarioAction ]listaUsuario");
		Map session=ActionContext.getContext().getSession();
		UsuarioDAO udao=new UsuarioDAO();
		
		lstusuario=udao.listarUsuario("usuario");
		session.put("state", "buscar");
		
		return EXITO;
	}
	public String listadoUsuarios() throws Exception
	{
		System.out.println("[UsuarioAction ]listadoUsuarios");
		UsuarioDAO udao=new UsuarioDAO();
		List<Entidad> lst1=new ArrayList<Entidad>();
		lst1.add(new Entidad("coduw", null, Entidad.EINT));
		lst1.add(new Entidad("usuario", null, Entidad.ESTRING));
		lst1.add(new Entidad("estado", null, Entidad.ESTRING));
		lst1.add(new Entidad("clave", null, Entidad.ESTRING));
		
		List<Entidad> lst2=new ArrayList<Entidad>();
		lst2.add(new Entidad("nombre", null, Entidad.ESTRING));
		lst2.add(new Entidad("apepaterno", null, Entidad.ESTRING));
		lst2.add(new Entidad("apematerno", null, Entidad.ESTRING));
		
		String key="codpersona";

		listUsuarios=udao.listadoUsuarios("tb_usuarioweb",
									"tb_persona", lst1, lst2, key);
		return EXITO;
	}
	
	
	
	//Nuevo de Usuario
	public String nuevoUsuario() throws Exception
	{
		/*System.out.println("[UsuarioAction ]nuevoUsuario "+us.getDni()+" "+us.getUsuario()+" "+
				us.getClave());*/
		
		System.out.println("[UsuarioAction ]nuevoUsuario "+us);
		System.out.println("[UsuarioAction ]nuevoUsuario "+
				us.getNombre()+" "+
				us.getApepaterno()+" "+				
				us.getApematerno()+" "+
				us.getDireccion()+" "+
				us.getTelefono()+" "+
				us.getSexo()+" "+
				us.getFechanac() );
		//[UsuarioAction ]nuevoUsuario null aaa null 0
		
		//entidades
		/*
		

		*/
		UsuarioDAO udao=new UsuarioDAO();
		int codmax=udao.selecMaxCod("tb_persona")+1;
		
		System.out.println("cod "+codmax);
		List<Entidad> lst=new ArrayList<Entidad>();
		lst.add(new Entidad("codpersona", codmax, Entidad.EINT));
		lst.add(new Entidad("nombre", us.getNombre(), Entidad.ESTRING));
		lst.add(new Entidad("apepaterno", us.getApepaterno(), Entidad.ESTRING));
		lst.add(new Entidad("apematerno", us.getApematerno(), Entidad.ESTRING));
		lst.add(new Entidad("direccion", us.getDireccion(), Entidad.ESTRING));
		lst.add(new Entidad("telefono", us.getTelefono(), Entidad.ESTRING));
		lst.add(new Entidad("sexo", us.getSexo(), Entidad.ESTRING));
		lst.add(new Entidad("fechanac", us.getFechanac(), Entidad.ESTRING));
			
		
		//error ---
		
		
		if(udao.nuevoUsuario(lst, "tb_persona",true))
		{
			System.out.println("registro de persona ok");
			
			List<Entidad> lstru=new ArrayList<Entidad>();
			
			lstru.add(new Entidad("usuario", us.getUsuario(), Entidad.ESTRING));
			lstru.add(new Entidad("clave", us.getClave(), Entidad.ESTRING));
			lstru.add(new Entidad("estado", us.getEstado(), Entidad.EINT));
			lstru.add(new Entidad("cod_tuw", us.getCod_tuw(), Entidad.EINT));
			lstru.add(new Entidad("codpersona", (codmax), Entidad.EINT));
			
			System.out.println("Registro exitoso ---");
			udao.nuevoUsuario(lstru, "tb_usuarioweb",false);
			
			System.out.println("registro de usuario ok");
			//registro exitoso ---
		}else
		{
			//error en el registro ---
			System.out.println("error ---");
		}
		
		System.out.println("entro en registrar"+ us.toString());
		
		return EXITO;
	}
	
	/*public String selectMaxCodigo()throws Exception
	{
		
		UsuarioDAO udao=new UsuarioDAO();
		udao.selecMaxCod("tb_persona");
		
		return EXITO;
		
	}*/
	
	public String setUsuarios()throws Exception
	{
		System.out.println("Seteoo -->" +us.getCoduw());
		
		return EXITO;
	}
	//Modificar de Usuario
	public String actUsuario() throws Exception
	{
		
		List<Entidad> lst=new ArrayList<Entidad>();
		lst.add(new Entidad("usuario", us.getUsuario(), Entidad.ESTRING));
		lst.add(new Entidad("nombre", us.getNombre(), Entidad.ESTRING));
		lst.add(new Entidad("apepaterno", us.getApepaterno(),Entidad.ESTRING));
		lst.add(new Entidad("apematerno", us.getApematerno(),Entidad.ESTRING));
		lst.add(new Entidad("clave", us.getClave(),Entidad.ESTRING));
		lst.add(new Entidad("estado", us.getEstado(),Entidad.EINT));
		
		UsuarioDAO udao=new UsuarioDAO();
		
		
		udao.modificaUsuario(lst,"tb_usuarioweb",new Entidad("coduw", us.getCoduw(),Entidad.EINT));
		
		us=null;
		//System.out.println("entro en actualizar"+ us.toString());
		
		return EXITO;
	}
	
	
	public String actUsuarioWeb() throws Exception
	{
		
		List<Entidad> lst=new ArrayList<Entidad>();
		lst.add(new Entidad("usuario", us.getUsuario(),Entidad.ESTRING));
		lst.add(new Entidad("clave", us.getClave(),Entidad.ESTRING));
		lst.add(new Entidad("estado", us.getEstado(),Entidad.EINT));
		
		System.out.println("actualizar datos >>> "+
				us.getUsuario()+" "+
				us.getClave()+" "+
				us.getEstado());
		UsuarioDAO udao=new UsuarioDAO();
		
		
		udao.modificaUsuario(lst,"tb_usuarioweb",new Entidad("coduw", us.getCoduw(),Entidad.EINT));
		
		us=null;
		//System.out.println("entro en actualizar"+ us.toString());
		
		return EXITO;
	}
	public String buscarUsuario() throws Exception
	{
		
		List<Entidad> lst=new ArrayList<Entidad>();
		lst.add(new Entidad("usuario", us.getApepaterno(),Entidad.ESTRING));
		
		System.out.println("buscar usuario >>> "+
				us.getApepaterno()+" "
			);
		
		UsuarioDAO udao=new UsuarioDAO();

		//udao.buscarUsuario(lst,"tb_persona",new Entidad("apepaterno", us.getApepaterno(),Entidad.ESTRING));
		
		us=null;
		//System.out.println("entro en actualizar"+ us.toString());
		
		return EXITO;
	}
	
}

