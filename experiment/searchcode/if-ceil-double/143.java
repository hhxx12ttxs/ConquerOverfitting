package com.truyen.persistence.dao.impl;

import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.truyen.persistence.dao.LoaiTruyenChuDao;
import com.truyen.persistence.dao.base.GenericJpaBaseDaoImpl;
import com.truyen.persistence.entity.LoaiTruyenChu;
import com.truyen.utils.PagingInfo;

@Repository
public class LoaiTruyenChuDaoImpl extends GenericJpaBaseDaoImpl<LoaiTruyenChu, Long>
		implements LoaiTruyenChuDao {

	public LoaiTruyenChuDaoImpl() {
		super(LoaiTruyenChu.class);
	}

	@Override
	public List<LoaiTruyenChu> getAllLst(String kwSearch, String sort, String sortBy,
			PagingInfo pInfo) {
		CriteriaBuilder critBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<LoaiTruyenChu> query = critBuilder
				.createQuery(LoaiTruyenChu.class);
		Root<LoaiTruyenChu> entity = query.from(LoaiTruyenChu.class);
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
		TypedQuery<LoaiTruyenChu> q = entityManager.createQuery(query);
		long count = countByQuery(query, entity);
		pInfo.setCount(count);
		pInfo.setTotalPage((int)Math.ceil((double)count/pInfo.getSizePerPage()));		
		q.setFirstResult(pInfo.getStartResult());
		q.setMaxResults(pInfo.getSizePerPage());
		List<LoaiTruyenChu> lst = q.getResultList();
		return (lst.size() > 0) ? lst : null;
	}

	@Override
	public List<LoaiTruyenChu> getLstAllWithPagingInfo(PagingInfo pInfo) {
		CriteriaBuilder critBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<LoaiTruyenChu> query = critBuilder
				.createQuery(LoaiTruyenChu.class);
		Root<LoaiTruyenChu> entity = query.from(LoaiTruyenChu.class);
		TypedQuery<LoaiTruyenChu> q = entityManager.createQuery(query);
		long count = countByQuery(query, entity);
		pInfo.setCount(count);
		pInfo.setTotalPage((int)Math.ceil((double)count/pInfo.getSizePerPage()));	
		q.setFirstResult(pInfo.getStartResult());
		q.setMaxResults(pInfo.getSizePerPage());
		List<LoaiTruyenChu> lst = q.getResultList();
		return (lst.size() > 0) ? lst : null;
	}

	@Override
	public LoaiTruyenChu findByAliasVType(String alias, int type) {
		CriteriaBuilder critBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<LoaiTruyenChu> query = critBuilder
				.createQuery(LoaiTruyenChu.class);
		Root<LoaiTruyenChu> entity = query.from(LoaiTruyenChu.class);
		Predicate p = critBuilder.conjunction();
		p = critBuilder.and(p,
				critBuilder.equal(entity.<String> get("alias"), alias),
				critBuilder.equal(entity.<String> get("type"), type));
		query = query.where(p);
		TypedQuery<LoaiTruyenChu> q = entityManager.createQuery(query);
		List<LoaiTruyenChu> lst = q.getResultList();
		return (lst.size() > 0) ? lst.get(0) : null;
	}

	@Override
	public List<LoaiTruyenChu> getLstAllWithPagingInfo(PagingInfo pInfo,
			int type) {
		CriteriaBuilder critBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<LoaiTruyenChu> query = critBuilder
				.createQuery(LoaiTruyenChu.class);
		Root<LoaiTruyenChu> entity = query.from(LoaiTruyenChu.class);
		Predicate p = critBuilder.conjunction();
		p = critBuilder.and(p,
				critBuilder.equal(entity.<String> get("type"), type));
		query = query.where(p);
		query.orderBy(critBuilder.desc(entity.get("id")));
		TypedQuery<LoaiTruyenChu> q = entityManager.createQuery(query);
		long count = countByQuery(query, entity);
		pInfo.setCount(count);
		pInfo.setTotalPage((int)Math.ceil((double)count/pInfo.getSizePerPage()));		
		q.setFirstResult(pInfo.getStartResult());
		q.setMaxResults(pInfo.getSizePerPage());
		List<LoaiTruyenChu> lst = q.getResultList();
		return (lst.size() > 0) ? lst : null;
	}

}

