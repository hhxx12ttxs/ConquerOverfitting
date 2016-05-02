package com.truyen.persistence.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.truyen.persistence.dao.AnhDepDao;
import com.truyen.persistence.dao.base.GenericJpaBaseDaoImpl;
import com.truyen.persistence.entity.AnhDep;
import com.truyen.persistence.entity.LoaiAnhDep;
import com.truyen.persistence.entity.TruyenMa;
import com.truyen.utils.PagingInfo;

@Repository
public class AnhDepDaoImpl extends GenericJpaBaseDaoImpl<AnhDep, Long>
		implements AnhDepDao {

	public AnhDepDaoImpl() {
		super(AnhDep.class);
	}

	@Override
	public List<AnhDep> getAllLst(String kwSearch, String sort, String sortBy,
			PagingInfo pInfo) {
		CriteriaBuilder critBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<AnhDep> query = critBuilder
				.createQuery(AnhDep.class);
		Root<AnhDep> entity = query.from(AnhDep.class);
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
		TypedQuery<AnhDep> q = entityManager.createQuery(query);
		long count = countByQuery(query, entity);
		pInfo.setCount(count);
		pInfo.setTotalPage((int)Math.ceil((double)count/pInfo.getSizePerPage()));		
		q.setFirstResult(pInfo.getStartResult());
		q.setMaxResults(pInfo.getSizePerPage());
		List<AnhDep> lst = q.getResultList();
		return (lst.size() > 0) ? lst : null;
	}

	@Override
	public List<AnhDep> getAllWithIsGot(boolean isGot) {
		CriteriaBuilder critBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<AnhDep> query = critBuilder
				.createQuery(AnhDep.class);
		Root<AnhDep> entity = query.from(AnhDep.class);
		Predicate p = critBuilder.conjunction();
		p = critBuilder.and(p,
				critBuilder.equal(entity.<String> get("isGot"), isGot));
		query = query.where(p);
		TypedQuery<AnhDep> q = entityManager.createQuery(query);
		List<AnhDep> lst = q.getResultList();
		return (lst.size() > 0) ? lst : null;
	}

	@Override
	public List<AnhDep> getLstAnhDep(PagingInfo pInfo, boolean isGot,
			String orderBy) {
		CriteriaBuilder critBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<AnhDep> query = critBuilder
				.createQuery(AnhDep.class);
		Root<AnhDep> entity = query.from(AnhDep.class);
		Predicate p = critBuilder.conjunction();
		p = critBuilder.and(p,
				critBuilder.equal(entity.<String> get("isGot"), isGot));
		query = query.where(p);
		query.orderBy(critBuilder.desc(entity.get(orderBy)));
		TypedQuery<AnhDep> q = entityManager.createQuery(query);
		long count = countByQuery(query, entity);
		pInfo.setCount(count);
		pInfo.setTotalPage((int)Math.ceil((double)count/pInfo.getSizePerPage()));		
		q.setFirstResult(pInfo.getStartResult());
		q.setMaxResults(pInfo.getSizePerPage());
		List<AnhDep> lst = q.getResultList();
		return (lst.size() > 0) ? lst : null;
	}

	@Override
	public List<AnhDep> getLstByType(LoaiAnhDep loaiAnhDep, int maxLimit,
			String orderBy, boolean isGot) {
		CriteriaBuilder critBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<AnhDep> query = critBuilder
				.createQuery(AnhDep.class);
		Root<AnhDep> entity = query.from(AnhDep.class);
		Predicate p = critBuilder.conjunction();
		p = critBuilder.and(p,
				critBuilder.equal(entity.<String> get("isGot"), isGot),
				critBuilder.equal(entity.<LoaiAnhDep> get("loaiAnhDep"), loaiAnhDep));
		query = query.where(p);
		query.orderBy(critBuilder.desc(entity.get(orderBy)));
		TypedQuery<AnhDep> q = entityManager.createQuery(query);
		q.setFirstResult(0);
		q.setMaxResults(maxLimit);
		List<AnhDep> lst = q.getResultList();
		return (lst.size() > 0) ? lst : null;
	}

	@Override
	public List<AnhDep> getLstByType(LoaiAnhDep loaiAnhDep, PagingInfo pInfo,
			String orderBy, boolean isGot) {
		CriteriaBuilder critBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<AnhDep> query = critBuilder
				.createQuery(AnhDep.class);
		Root<AnhDep> entity = query.from(AnhDep.class);
		Predicate p = critBuilder.conjunction();
		p = critBuilder.and(p,
				critBuilder.equal(entity.<String> get("isGot"), isGot),
				critBuilder.equal(entity.<LoaiAnhDep> get("loaiAnhDep"), loaiAnhDep));
		query = query.where(p);
		query.orderBy(critBuilder.desc(entity.get(orderBy)));
		TypedQuery<AnhDep> q = entityManager.createQuery(query);
		long count = countByQuery(query, entity);
		pInfo.setCount(count);
		pInfo.setTotalPage((int)Math.ceil((double)count/pInfo.getSizePerPage()));		
		q.setFirstResult(pInfo.getStartResult());
		q.setMaxResults(pInfo.getSizePerPage());
		List<AnhDep> lst = q.getResultList();
		return (lst.size() > 0) ? lst : null;
	}

	@Override
	public List<AnhDep> getListRandom(LoaiAnhDep loaiAnhDep, int maxLimit,
			boolean isGot) {
		Query maxq = entityManager
				.createNativeQuery("SELECT id FROM anh_dep WHERE loaiAnhDepItem="+loaiAnhDep.getId()+" AND isGot=1");
		List<Object> result = maxq.getResultList();
		int rsSize = result.size();
		Random random = new Random();		
		String randIn12 = "";
		int ifMaxLimit = maxLimit - 1 ;
		for(int i=0; i < maxLimit;i++){
			int rand1 = random.nextInt(rsSize);
			if(i!=ifMaxLimit){
				randIn12 += rand1 + ",";
			}else{
				randIn12 += rand1;
			}
		}
		List<AnhDep> returnList = new ArrayList<AnhDep>();
		Query query = entityManager
				.createNativeQuery("SELECT id FROM anh_dep WHERE id IN ("+randIn12+")");
		result = query.getResultList();
		for (Object longObj : result) {
			long id = (Long)longObj;
			returnList.add(findById(id));
		}
		return returnList;
	}

	@Override
	public List<Object> getObjectByNameUnsigned(String kwSearch, int maxLimit) {
		String keywordTemp = kwSearch.replaceAll("'", "''");
		Query query = entityManager
				.createNativeQuery("SELECT DISTINCT Convert(s.id using utf8) AS id,name,alias,linkImage,nameImage,loaiAnhDepItem FROM anh_dep s " +
						"WHERE s.name LIKE '" + keywordTemp + "%' OR s.alias LIKE '" + keywordTemp + "%'" +
						"ORDER BY name ASC");
		query.setFirstResult(0);
		query.setMaxResults(maxLimit);
		List<Object> arrRs = query.getResultList();
		return arrRs;
	}

	@Override
	public List<AnhDep> getLstByPropertyWithPaging(String column,
			String kwSearch, String orderBy, PagingInfo pInfo, boolean isGot) {
		CriteriaBuilder critBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<AnhDep> query = critBuilder
				.createQuery(AnhDep.class);
		Root<AnhDep> entity = query.from(AnhDep.class);
		Predicate p = critBuilder.conjunction();
		p = critBuilder.and(p,
				critBuilder.equal(entity.<String> get("isGot"), isGot),
				critBuilder.like(entity.<String> get(column), kwSearch+"%"));
		query = query.where(p);
		query.orderBy(critBuilder.desc(entity.get(orderBy)));
		TypedQuery<AnhDep> q = entityManager.createQuery(query);
		long count = countByQuery(query, entity);
		pInfo.setCount(count);
		pInfo.setTotalPage((int)Math.ceil((double)count/pInfo.getSizePerPage()));		
		q.setFirstResult(pInfo.getStartResult());
		q.setMaxResults(pInfo.getSizePerPage());
		List<AnhDep> lst = q.getResultList();
		return (lst.size() > 0) ? lst : null;
	}

}

