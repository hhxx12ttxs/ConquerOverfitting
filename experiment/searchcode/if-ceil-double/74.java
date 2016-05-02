package com.truyen.persistence.dao.impl;

import java.util.ArrayList;
import java.util.Random;

import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.truyen.persistence.dao.TruyenKiemHiepDao;
import com.truyen.persistence.dao.base.GenericJpaBaseDaoImpl;
import com.truyen.persistence.entity.TacGia;
import com.truyen.persistence.entity.TruyenKiemHiep;
import com.truyen.utils.PagingInfo;

@Repository
public class TruyenKiemHiepDaoImpl extends GenericJpaBaseDaoImpl<TruyenKiemHiep, Long>
		implements TruyenKiemHiepDao {

	public TruyenKiemHiepDaoImpl() {
		super(TruyenKiemHiep.class);
	}

	@Override
	public List<TruyenKiemHiep> getLstComicLinkImageNullBlogTruyen(int sourceBy) {
		CriteriaBuilder critBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<TruyenKiemHiep> query = critBuilder
				.createQuery(TruyenKiemHiep.class);
		Root<TruyenKiemHiep> entity = query.from(TruyenKiemHiep.class);
		Predicate p = critBuilder.conjunction();
		p = critBuilder.and(p,
				critBuilder.equal(entity.<String> get("sourceBy"),sourceBy),
				critBuilder.isNull(entity.<String> get("linkImage")));
		query = query.where(p);
		TypedQuery<TruyenKiemHiep> q = entityManager.createQuery(query);
		long count = countByQuery(query, entity);
		q.setFirstResult(0);
		q.setMaxResults((int) count);
		List<TruyenKiemHiep> lst = q.getResultList();
		return (lst.size() > 0) ? lst : null;
	}

	@Override
	public List<TruyenKiemHiep> getAllLst(String kwSearch, String sort, String sortBy,
			 PagingInfo pInfo) {
		CriteriaBuilder critBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<TruyenKiemHiep> query = critBuilder
				.createQuery(TruyenKiemHiep.class);
		Root<TruyenKiemHiep> entity = query.from(TruyenKiemHiep.class);
		Predicate p = critBuilder.conjunction();
		if(kwSearch!=null && !kwSearch.equals("")){
			kwSearch = kwSearch + "%";
			p = critBuilder.and(p,
					critBuilder.like(entity.<String> get("alias"), kwSearch));
			p = critBuilder.or(p,
					critBuilder.like(entity.<String> get("name"), kwSearch),
					critBuilder.like(entity.<String> get("alias"), "%" + kwSearch),
					critBuilder.like(entity.<String> get("name"), "%" + kwSearch));
		}
		query = query.where(p);
		if(sort.equals("desc")){
			query.orderBy(critBuilder.desc(entity.get(sortBy)));
		}else{
			query.orderBy(critBuilder.asc(entity.get(sortBy)));
		}
		TypedQuery<TruyenKiemHiep> q = entityManager.createQuery(query);
		long count = countByQuery(query, entity);
		pInfo.setCount(count);
		pInfo.setTotalPage((int)Math.ceil((double)count/pInfo.getSizePerPage()));		
		q.setFirstResult(pInfo.getStartResult());
		q.setMaxResults(pInfo.getSizePerPage());
		List<TruyenKiemHiep> lst = q.getResultList();
		return (lst.size() > 0) ? lst : null;
	}

	@Override
	public List<TruyenKiemHiep> getListTruyenBy(
			String orderByColumn, int begin , int max) {
		CriteriaBuilder critBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<TruyenKiemHiep> query = critBuilder
				.createQuery(TruyenKiemHiep.class);
		Root<TruyenKiemHiep> entity = query.from(TruyenKiemHiep.class);
		Predicate p = critBuilder.conjunction();
		p = critBuilder.and(p,
				critBuilder.isNotNull(entity.<String> get("linkImage")));
		query = query.where(p);
		query.orderBy(critBuilder.desc(entity.get(orderByColumn)));
		TypedQuery<TruyenKiemHiep> q = entityManager.createQuery(query);
		q.setFirstResult(begin);
		q.setMaxResults(max);
		List<TruyenKiemHiep> lst = q.getResultList();
		return (lst.size() > 0) ? lst : null;
	}

	@Override
	public List<TruyenKiemHiep> getListTruyenRandomBy(int maxLimit) {
		Query maxq = entityManager
				.createNativeQuery("SELECT MAX(id)-"+maxLimit+" FROM truyen_kiem_hiep");
		long max = (Long) maxq.getSingleResult();
		Random random = new Random();		
		String randIn12 = "";
		int ifMaxLimit = maxLimit - 1 ;
		for(int i=0; i < maxLimit;i++){
			int rand1 = random.nextInt((int) max);
			if(i!=ifMaxLimit){
				randIn12 += rand1 + ",";
			}else{
				randIn12 += rand1;
			}
		}
		List<TruyenKiemHiep> returnList = new ArrayList<TruyenKiemHiep>();
		Query query = entityManager
				.createNativeQuery("SELECT id FROM truyen_kiem_hiep WHERE id IN ("+randIn12+")");
		List<Object> result = query.getResultList();
		for (Object longObj : result) {
			long id = (Long)longObj;
			returnList.add(findById(id));
		}
		return returnList;
	}

	@Override
	public List<TruyenKiemHiep> getLstTruyenBySourceBy(int sourceBy) {
		CriteriaBuilder critBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<TruyenKiemHiep> query = critBuilder
				.createQuery(TruyenKiemHiep.class);
		Root<TruyenKiemHiep> entity = query.from(TruyenKiemHiep.class);
		Predicate p = critBuilder.conjunction();
		p = critBuilder.and(p,
				critBuilder.equal(entity.<String> get("sourceBy"),sourceBy));
		query = query.where(p);
		TypedQuery<TruyenKiemHiep> q = entityManager.createQuery(query);
		long count = countByQuery(query, entity);
		q.setFirstResult(0);
		q.setMaxResults((int) count);
		List<TruyenKiemHiep> lst = q.getResultList();
		return (lst.size() > 0) ? lst : null;
	}

	@Override
	public List<TruyenKiemHiep> getLstByProperty( String column, Object value, int begin, int max) {
		CriteriaBuilder critBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<TruyenKiemHiep> query = critBuilder
				.createQuery(TruyenKiemHiep.class);
		Root<TruyenKiemHiep> entity = query.from(TruyenKiemHiep.class);
		Predicate p = critBuilder.conjunction();
		p = critBuilder.and(p,
				critBuilder.equal(entity.<String> get(column),value));
		query = query.where(p);
		TypedQuery<TruyenKiemHiep> q = entityManager.createQuery(query);
		q.setFirstResult(begin);
		q.setMaxResults(max);
		List<TruyenKiemHiep> lst = q.getResultList();
		return (lst.size() > 0) ? lst : null;
	}

	@Override
	public List<Object> getObjectByNameUnsigned(
			String kwSearch, int maxLimit, String typeSearch) {
		String keywordTemp = kwSearch.replaceAll("'", "''");
		Query query = null;
		if(typeSearch.equals("tTenTruyen")){
			query = entityManager
					.createNativeQuery("SELECT DISTINCT Convert(s.id using utf8) AS id,name,alias,linkImage,nameImage FROM truyen_kiem_hiep s " +
							"WHERE s.name LIKE '" + keywordTemp + "%' OR s.alias LIKE '" + keywordTemp + "%'" +
							"ORDER BY name ASC");
		}else if(typeSearch.equals("tTacGia")){
			query = entityManager
					.createNativeQuery("SELECT DISTINCT Convert(s.id using utf8) AS id,name,alias,linkImage,nameImage FROM truyen_kiem_hiep s "+
							"WHERE s.tacGiaItem=(SELECT id FROM tac_gia a WHERE a.name LIKE '%" + keywordTemp + "%' OR a.alias LIKE '%" + keywordTemp + "%' LIMIT 0,1) "+
							"ORDER BY s.name ASC");
		}else{
			query = entityManager
					.createNativeQuery("SELECT DISTINCT Convert(s.id using utf8) AS id,name,alias,linkImage,nameImage FROM truyen_kiem_hiep s " +
							"WHERE s.categories like '%" + keywordTemp + "%'" +
							"ORDER BY name ASC");
		}
		query.setFirstResult(0);
		query.setMaxResults(maxLimit);
		List<Object> arrRs = query.getResultList();
		return arrRs;
	}

	@Override
	public List<TruyenKiemHiep> getLstByPropertyWithPaging(String column, String value, String sortBy,PagingInfo pInfo) {
		CriteriaBuilder critBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<TruyenKiemHiep> query = critBuilder.createQuery(TruyenKiemHiep.class);
		Root<TruyenKiemHiep> entity = query.from(TruyenKiemHiep.class);
		Predicate p = critBuilder.conjunction();
		p = critBuilder.and(p,
				critBuilder.isNotNull(entity.<String> get("linkImage")));
		if(!value.equals("a-z")){
			if(!column.equals("tacGia")){
				if(column.equals("alias")){
					p = critBuilder.and(p,
							critBuilder.like(entity.<String> get(column), value + "%"));
				}else{
					p = critBuilder.and(p,
							critBuilder.like(entity.<String> get(column), "%" + value + "%"));
				}
			}else{
				p = critBuilder.and(p,
						critBuilder.like(entity.<TacGia> get("tacGia").<String>get("name"), "%" + value + "%"));
				p = critBuilder.or(p,
						critBuilder.like(entity.<TacGia> get("tacGia").<String>get("alias"), "%" + value + "%"));
			}
		}
		query = query.where(p);
		query.orderBy(critBuilder.desc(entity.get(sortBy)));
		TypedQuery<TruyenKiemHiep> q = entityManager.createQuery(query);
		long count = countByQuery(query, entity);
		pInfo.setCount(count);
		pInfo.setTotalPage((int)Math.ceil((double)count/pInfo.getSizePerPage()));		
		q.setFirstResult(pInfo.getStartResult());
		q.setMaxResults(pInfo.getSizePerPage());
		List<TruyenKiemHiep> lst = q.getResultList();
		return (lst.size() > 0) ? lst : null;
	}

	@Override
	public List<TruyenKiemHiep> getLstByPropertyWithOrderByPaging(String orderBy, PagingInfo pInfo) {
		CriteriaBuilder critBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<TruyenKiemHiep> query = critBuilder.createQuery(TruyenKiemHiep.class);
		Root<TruyenKiemHiep> entity = query.from(TruyenKiemHiep.class);
		Predicate p = critBuilder.conjunction();
		p = critBuilder.and(p,
				critBuilder.isNotNull(entity.<String> get("linkImage")));
		query = query.where(p);
		query.orderBy(critBuilder.desc(entity.get(orderBy)));
		TypedQuery<TruyenKiemHiep> q = entityManager.createQuery(query);
		long count = countByQuery(query, entity);
		pInfo.setCount(count);
		pInfo.setTotalPage((int)Math.ceil((double)count/pInfo.getSizePerPage()));		
		q.setFirstResult(pInfo.getStartResult());
		q.setMaxResults(pInfo.getSizePerPage());
		List<TruyenKiemHiep> lst = q.getResultList();
		return (lst.size() > 0) ? lst : null;
	}

	@Override
	public List<TruyenKiemHiep> getLstByTacGiaWithPaging(TacGia tacGia, String orderBy, PagingInfo pInfo) {
		CriteriaBuilder critBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<TruyenKiemHiep> query = critBuilder.createQuery(TruyenKiemHiep.class);
		Root<TruyenKiemHiep> entity = query.from(TruyenKiemHiep.class);
		Predicate p = critBuilder.conjunction();
		p = critBuilder.and(p,
				critBuilder.equal(entity.<TacGia> get("tacGia"), tacGia),
				critBuilder.isNotNull(entity.<String> get("linkImage")));
		query = query.where(p);
		query.orderBy(critBuilder.desc(entity.get(orderBy)));
		TypedQuery<TruyenKiemHiep> q = entityManager.createQuery(query);
		long count = countByQuery(query, entity);
		pInfo.setCount(count);
		pInfo.setTotalPage((int)Math.ceil((double)count/pInfo.getSizePerPage()));		
		q.setFirstResult(pInfo.getStartResult());
		q.setMaxResults(pInfo.getSizePerPage());
		List<TruyenKiemHiep> lst = q.getResultList();
		return (lst.size() > 0) ? lst : null;
	}

	@Override
	public List<TruyenKiemHiep> getLstByTacGiaLimit(TacGia tacGia, int begin, int maxLimit) {
		CriteriaBuilder critBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<TruyenKiemHiep> query = critBuilder
				.createQuery(TruyenKiemHiep.class);
		Root<TruyenKiemHiep> entity = query.from(TruyenKiemHiep.class);
		Predicate p = critBuilder.conjunction();
		p = critBuilder.and(p,
				critBuilder.equal(entity.<TacGia> get("tacGia"), tacGia));
		query = query.where(p);
		TypedQuery<TruyenKiemHiep> q = entityManager.createQuery(query);
		q.setFirstResult(begin);
		q.setMaxResults(maxLimit);
		List<TruyenKiemHiep> lst = q.getResultList();
		return (lst.size() > 0) ? lst : null;
	}

}

