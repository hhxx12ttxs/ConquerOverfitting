package com.truyen.persistence.dao.base;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.jpa.support.JpaDaoSupport;
import org.springframework.transaction.annotation.Transactional;

import com.truyen.utils.PagingInfo;

/**
 * @author quanhm
 * 
 */
@SuppressWarnings("all")
public class GenericJpaBaseDaoImpl<T, ID extends Serializable> extends
		JpaDaoSupport implements GenericJpaBaseDao<T, ID> {

	private final Class<T> persistentClass;
	protected String singleClassName = "";

	@PersistenceContext(unitName = "truyenOnline")
	protected EntityManager entityManager;

	public GenericJpaBaseDaoImpl() {
		this.persistentClass = (Class<T>) ((ParameterizedType) getClass()
				.getGenericSuperclass()).getActualTypeArguments()[0];
		singleClassName = persistentClass.getSimpleName();
	}

	public GenericJpaBaseDaoImpl(final Class<T> persistentClass) {
		this.persistentClass = persistentClass;
		singleClassName = persistentClass.getSimpleName();
	}

	@Override
	public Class<T> getEntityClass() {
		return persistentClass;
	}

	@Override
	@Transactional(readOnly = true)
	public T findById(ID id) {
		return (T) getJpaTemplate().find(persistentClass, id);
	}

	@Override
	@Transactional(readOnly = true)
	public List<T> findAll() {
		return (List<T>) getJpaTemplate().find(
				"Select t from " + singleClassName + " t");
	}

	@Override
	@Transactional(readOnly = true)
	public List<T> findAll(PagingInfo pInfo) {
		long numOfItems = countAll();
		pInfo.setTotalPage((int) Math.ceil((double) numOfItems
				/ pInfo.getSizePerPage()));
		Query q = getJpaTemplate().getEntityManager().createQuery(
				"Select t from " + singleClassName + " t");
		q.setFirstResult(pInfo.getStartResult());
		q.setMaxResults(pInfo.getSizePerPage());
		return (List<T>) q.getResultList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<T> findByNamedQuery(String queryName, Object... params) {
		return (List<T>) getJpaTemplate().findByNamedQuery(
				singleClassName + "." + queryName, params);
	}

	@Override
	@Transactional(readOnly = true)
	public List<T> findByNamedQueryAndNamedParams(String queryName,
			Map<String, ? extends Object> params) {
		return (List<T>) getJpaTemplate().findByNamedQueryAndNamedParams(
				singleClassName + "." + queryName, params);
	}

	@Override
	@Transactional(readOnly = true)
	public List<T> findByQuery(String query, Object... values) {
		return (List<T>) getJpaTemplate().find(query, values);
	}

	@Override
	@Transactional(readOnly = true)
	public List<T> findByProperty(String property, Object value) {
		CriteriaBuilder critBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery crit = critBuilder.createQuery();
		Root<T> entity = crit.from(persistentClass);
		crit.where(critBuilder.equal(entity.get(property), value));
		return entityManager.createQuery(crit).getResultList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<T> findByPropertyLimit(String property, Object value,
			int maxLimit) {
		CriteriaBuilder critBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery crit = critBuilder.createQuery();
		Root<T> entity = crit.from(persistentClass);

		crit.where(critBuilder.equal(entity.get(property), value));
		TypedQuery<T> query = entityManager.createQuery(crit);
		query.setFirstResult(0);
		query.setMaxResults(maxLimit);
		return query.getResultList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<T> findByProperty(String property, Object value,
			PagingInfo pInfo) {
		CriteriaBuilder critBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery crit = critBuilder.createQuery();
		Root<T> entity = crit.from(persistentClass);

		crit.where(critBuilder.equal(entity.get(property), value));
		TypedQuery<T> query = entityManager.createQuery(crit);
		query.setFirstResult(pInfo.getStartResult());
		query.setMaxResults(pInfo.getSizePerPage());

		int numOfItems = countByProperty(property, value);
		pInfo.setTotalPage((int) Math.ceil((double) numOfItems
				/ pInfo.getSizePerPage()));

		return query.getResultList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<T> findByProperties(Map<String, Object> propertiesMap) {
		CriteriaBuilder critBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> crit = critBuilder.createQuery(persistentClass);
		Root<T> entity = crit.from(persistentClass);
		Predicate p = critBuilder.conjunction();
		for (String propKey : propertiesMap.keySet()) {
			p = critBuilder.and(
					p,
					critBuilder.equal(entity.get(propKey),
							propertiesMap.get(propKey)));
		}
		return entityManager.createQuery(crit.where(p)).getResultList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<T> findByProperties(Map<String, Object> propertiesMap,
			PagingInfo pInfo) {
		CriteriaBuilder critBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> crit = critBuilder.createQuery(persistentClass);
		Root<T> entity = crit.from(persistentClass);

		Predicate p = critBuilder.conjunction();
		for (String propKey : propertiesMap.keySet()) {
			p = critBuilder.and(
					p,
					critBuilder.equal(entity.get(propKey),
							propertiesMap.get(propKey)));
		}

		int numOfItems = countByProperties(propertiesMap, pInfo);
		pInfo.setTotalPage((int) Math.ceil((double) numOfItems
				/ pInfo.getSizePerPage()));

		TypedQuery<T> query = entityManager.createQuery(crit);
		query.setFirstResult(pInfo.getStartResult());
		query.setMaxResults(pInfo.getSizePerPage());

		return query.getResultList();
	}

	@Override
	@Transactional(readOnly = true)
	public long countAll() {
		List<Long> lst = getJpaTemplate().find(
				"Select count(t) from " + singleClassName + " t");
		return lst.get(0);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void save(T entity) {
		if (!getJpaTemplate().contains(entity)) {
			getJpaTemplate().persist(entity);
		} else {
			getJpaTemplate().merge(entity);
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void merge(T entity) {
		getJpaTemplate().merge(entity);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void delete(T entity) {
		getJpaTemplate().remove(entity);
	}

	/**
	 * Set the JPA EntityManagerFactory.
	 * 
	 * @param fooEntityManagerFactory
	 */
	@Autowired(required = true)
	public void setJpaEntityManagerFactory(
			@Qualifier("truyenOnlineEmf") EntityManagerFactory entityManagerFactory) {
		super.setEntityManagerFactory(entityManagerFactory);
		super.setEntityManager(entityManager);
	}

	@Override
	@Transactional(readOnly = true)
	public List<T> findWithPaging(String q, PagingInfo pInfo) {
		Query query = getJpaTemplate().getEntityManager().createQuery(q);
		query.setFirstResult(pInfo.getStartResult());
		query.setMaxResults(pInfo.getSizePerPage());
		return query.getResultList();
	}

	@Override
	public long countByWhere(String whereClauses) {
		Query q = getJpaTemplate().getEntityManager().createQuery(
				"SELECT COUNT(t) from " + singleClassName + " t"
						+ (whereClauses.isEmpty() ? "" : whereClauses));
		return Long.parseLong(q.getSingleResult().toString());
	}

	@Override
	public int countTotalPage(String whereClauses, int sizeInPage) {
		long numOfItems = countByWhere(whereClauses);
		return (int) Math.ceil((double) numOfItems / sizeInPage);
	}

	@Override
	@Transactional(readOnly = true)
	public int countByProperty(String property, Object value) {
		CriteriaBuilder critBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> crit = critBuilder.createQuery(Long.class);
		Root<T> entity = crit.from(persistentClass);

		crit.where(critBuilder.equal(entity.get(property), value));
		crit.select(critBuilder.count(entity));

		return entityManager.createQuery(crit).getSingleResult().intValue();
	}

	@Override
	@Transactional(readOnly = true)
	public int countByProperties(Map<String, Object> propertiesMap,
			PagingInfo pInfo) {
		CriteriaBuilder critBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> crit = critBuilder.createQuery(Long.class);
		Root<T> entity = crit.from(persistentClass);

		Predicate p = critBuilder.conjunction();
		for (String propKey : propertiesMap.keySet()) {
			p = critBuilder.and(
					p,
					critBuilder.equal(entity.get(propKey),
							propertiesMap.get(propKey)));
		}

		crit.where(p);
		crit.select(critBuilder.count(entity));
		return entityManager.createQuery(crit).getSingleResult().intValue();
	}

	public long countByQuery(CriteriaQuery<T> criteriaQuery, Root<T> entity) {
		CriteriaBuilder critBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> countQuery = critBuilder.createQuery(Long.class);
		countQuery.select(critBuilder.count(entity));

		if (criteriaQuery.getRestriction() != null) {
			countQuery.where(criteriaQuery.getRestriction());
		}

		return entityManager.createQuery(countQuery).getSingleResult();
	}

	@Override
	@Transactional(readOnly = true)
	public List<T> findByPropertyNotNull(String property) {
		CriteriaBuilder critBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery crit = critBuilder.createQuery();
		Root<T> entity = crit.from(persistentClass);
		crit.where(critBuilder.isNotNull(entity.get(property)));
		return entityManager.createQuery(crit).getResultList();
	}
}

