package br.com.arizona.gestaoestoque.dao;

import java.util.List;

import javax.faces.bean.ManagedProperty;
import javax.persistence.Query;

import org.hibernate.Filter;
import org.hibernate.Session;

import br.com.arizona.gestaoestoque.annotation.ConviteDaoQualifier;
import br.com.arizona.gestaoestoque.bean.GlobalBean;
import br.com.arizona.gestaoestoque.controller.LoginController;
import br.com.arizona.gestaoestoque.domain.AreaNegocio;
import br.com.arizona.gestaoestoque.domain.Convidado;
import br.com.arizona.gestaoestoque.domain.Convite;
import br.com.arizona.gestaoestoque.domain.ConviteIngresso;
import br.com.arizona.gestaoestoque.domain.Evento;
import br.com.arizona.gestaoestoque.domain.Usuario;
import br.com.arizona.gestaoestoque.exception.BeanException;
import br.com.arizona.gestaoestoque.exception.DAOException;

@ConviteDaoQualifier
public class ConviteDao extends GenericDao<Convite> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1047219447151692203L;
	
	
	public void updateStatus(Convite convite, List<String> ingressos, Usuario usuario) throws DAOException {
		String numeros = new String("");

		if (!ingressos.isEmpty()) {
			StringBuilder sb = new StringBuilder();
		    
			for (String each : ingressos) {
		    	sb.append(",").append(each);
		    }
		    numeros = sb.substring(1);			
		}
	    
		em.getTransaction().begin();
		
		try {
			Query q;
			
			if(usuario != null) {
				q = em.createNativeQuery("CALL SPR_ALTERA_CONVITE(?, ?, ?, ?);");
			} else {
				q = em.createNativeQuery("CALL SPR_ALTERA_CONVITE(?, ?, ?, NULL);");
			}
			q.setParameter(1, convite.getId());
			q.setParameter(2, convite.getStatus());
			q.setParameter(3, numeros);
			
			if(usuario != null) {
				q.setParameter(4, usuario.getId());
			}
			
			q.executeUpdate();
			
			em.getTransaction().commit();			
		} catch (Exception ex) {
			em.getTransaction().rollback();
			if (ex.getCause().getCause() != null)
				throw new DAOException(ex.getCause().getCause().getMessage());
			else
				throw new DAOException(ex.getMessage());
		}
	}
	
	public void updateStatusBrindes(Convite convite) throws DAOException {
		em.getTransaction().begin();
		
		try {
			Query q = em.createNativeQuery("UPDATE SGE_CONVITE SET STATUS_BRINDES = ? WHERE CONVITE_ID = ?;");

			q.setParameter(1, convite.getStatusBrindes());
			q.setParameter(2, convite.getId());
			
			q.executeUpdate();
			
			Query q2 = em.createNativeQuery("UPDATE SGE_ESTOQUE_BRINDE SET QUANTIDADE = QUANTIDADE - ? WHERE EMPRESA_ID = ? AND EVENTO_ID = ? AND QUANTIDADE >= ?;");

			q2.setParameter(1, convite.getIngressos().size());
			q2.setParameter(2, convite.getEmpresa().getId());
			q2.setParameter(3, convite.getEventoAreaNegocio().getEvento().getId());
			q2.setParameter(4, convite.getIngressos().size());
			
			q2.executeUpdate();
			
			em.getTransaction().commit();			
		} catch (Exception ex) {
			em.getTransaction().rollback();
			if (ex.getCause().getCause() != null)
				throw new DAOException(ex.getCause().getCause().getMessage());
			else
				throw new DAOException(ex.getMessage());
		}
	}
	
	public void replaceTickets(Convite convite) throws DAOException {
		em.getTransaction().begin();
		
		try {
			if (em.getTransaction().isActive()) {
				Query qDel = em.createNativeQuery("DELETE FROM SGE_INGRESSO_CONVITE WHERE CONVITE_ID = ?;");
				qDel.setParameter(1, convite.getId());
				qDel.executeUpdate();
			}

			Query qIns = em.createNativeQuery("INSERT INTO SGE_INGRESSO_CONVITE(CONVITE_ID,INGRESSO_ID,EMPRESA_ID,STATUS"
											+ "  ,USU_CRIACAO,DATA_CRIACAO,USU_ALTERACAO,DATA_ALTERACAO)"
											+ " VALUES (?, ?, ?, ?, ?, SYSDATE(), ?, SYSDATE());");

			for (ConviteIngresso ingresso : convite.getIngressos()) {
				if (em.getTransaction().isActive()) {
					qIns.setParameter(1, ingresso.getConvite().getId());
					qIns.setParameter(2, ingresso.getIngresso().getId());
					qIns.setParameter(3, ingresso.getEmpresa().getId());
					qIns.setParameter(4, ingresso.getStatus());
					qIns.setParameter(5, ingresso.getUsuCriacao().getId());
					qIns.setParameter(6, ingresso.getUsuAlteracao().getId());
					
					qIns.executeUpdate();
				}
			}
			
			if (em.getTransaction().isActive()) {
				em.getTransaction().commit();
			}
		} catch (Exception ex) {
			em.getTransaction().rollback();
			if (ex.getCause().getCause() != null)
				throw new DAOException(ex.getCause().getCause().getMessage());
			else
				throw new DAOException(ex.getMessage());
		}
	}

	public Long countConviteByConvidado(Convidado convidado, Evento evento, AreaNegocio area, String status, Usuario usuario) throws DAOException {
		Long counter = 0L;
		
		try {
			String query = "SELECT COUNT(*) ";
			query += "  FROM SGE_CONVITE c";
			query += "  JOIN SGE_EVENTO_AN ean ON ean.EVENTO_AN_ID = c.EVENTO_AN_ID";
			query += " WHERE c.empresa_id = " + usuario.getId().toString();
			query += "   AND c.convidado_id = " + convidado.getId().toString();
			query += "   AND ean.evento_id = " + evento.getId().toString();
			query += "   AND ean.area_negocio_id <> " + area.getId().toString();
			query += "   AND c.status <> '" + status + "'";
			
			Query queryNamed = em.createNativeQuery(query);
			
			counter = Long.parseLong(queryNamed.getSingleResult().toString());
			
			
		} catch (Exception ex) {
			if (ex.getCause().getCause() != null)
				throw new DAOException(ex.getCause().getCause().getMessage());
			else
				throw new DAOException(ex.getMessage());
		}
		return counter;
	}
	
	public List<Convite> listConvitePromoter(List<Parameter> params, String statusAceito, String statusEntregue) throws DAOException {
		try {
			Session session = em.unwrap(Session.class);
			
			Filter filter = session.enableFilter("filtroIngressoValido");
	        filter.setParameter("parametroIngressoAceito", statusAceito);
	        filter.setParameter("parametroIngressoEntregue", statusEntregue);
	        
	        List<Convite> localResult = listNamedQuery("listConviteByConvidadoPromoter", params);
	        
	        session.disableFilter("filtroIngressoValido");
	        
	        return localResult;
		} catch (Exception e) {
			throw new DAOException(e.getMessage());
		} 
	}
	
	public Convite getConviteByID(List<Parameter> params, String statusAceito, String statusEntregue) throws DAOException {
		try {
			Session session = em.unwrap(Session.class);
			
			Filter filter = session.enableFilter("filtroIngressoValido");
	        filter.setParameter("parametroIngressoAceito", statusAceito);
	        filter.setParameter("parametroIngressoEntregue", statusEntregue);
	        
	        Convite localResult = (Convite) getObjectByNamedQuery("findConviteByIdPromoter", params);
	        
	        session.disableFilter("filtroIngressoValido");
	        
	        return localResult;
		} catch (Exception e) {
			throw new DAOException(e.getMessage());
		} 		
	}
	
	public Convite getConviteByID(List<Parameter> params) throws DAOException {
		try {
			
	        Convite localResult = (Convite) getObjectByNamedQuery("findConviteById", params);
	        	        
	        
	        return localResult;
		} catch (Exception e) {
			throw new DAOException(e.getMessage());
		} 		
	}
}

