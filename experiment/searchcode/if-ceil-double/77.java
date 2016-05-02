package com.truyen.persistence.dao.impl;

import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;

import com.truyen.persistence.dao.ChapterTmpTruyenKiemHiepDao;
import com.truyen.persistence.dao.base.GenericJpaBaseDaoImpl;
import com.truyen.persistence.entity.ChapterTmpTruyenKiemHiep;
import com.truyen.persistence.entity.TruyenKiemHiep;
import com.truyen.utils.PagingInfo;

@Repository
public class ChapterTmpTruyenKiemHiepDaoImpl extends GenericJpaBaseDaoImpl<ChapterTmpTruyenKiemHiep, Long>
		implements ChapterTmpTruyenKiemHiepDao {

	public ChapterTmpTruyenKiemHiepDaoImpl() {
		super(ChapterTmpTruyenKiemHiep.class);
	}

	@Override
	public List<ChapterTmpTruyenKiemHiep> getLstChapterByTruyen(TruyenKiemHiep truyen, String kwSearch, String sort, String sortBy,	PagingInfo pInfo) {
		CriteriaBuilder critBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ChapterTmpTruyenKiemHiep> query = critBuilder
				.createQuery(ChapterTmpTruyenKiemHiep.class);
		Root<ChapterTmpTruyenKiemHiep> entity = query.from(ChapterTmpTruyenKiemHiep.class);
		Predicate p = critBuilder.conjunction();
		p = critBuilder.and(p,
				critBuilder.equal(entity.<TruyenKiemHiep> get("truyenKiemHiep"), truyen));
		if(kwSearch!=null && !kwSearch.equals("")){
			kwSearch = kwSearch + "%";
			p = critBuilder.and(p,
					critBuilder.like(entity.<String> get("alias"), kwSearch));
			p = critBuilder.or(p,
					critBuilder.like(entity.<String> get("name"), kwSearch));
		}
		query = query.where(p);
		if(sort.equals("desc")){
			query.orderBy(critBuilder.desc(entity.get(sortBy)));
		}else{
			query.orderBy(critBuilder.asc(entity.get(sortBy)));
		}
		TypedQuery<ChapterTmpTruyenKiemHiep> q = entityManager.createQuery(query);
		long count = countByQuery(query, entity);
		pInfo.setCount(count);
		pInfo.setTotalPage((int)Math.ceil((double)count/pInfo.getSizePerPage()));		
		q.setFirstResult(pInfo.getStartResult());
		q.setMaxResults(pInfo.getSizePerPage());
		List<ChapterTmpTruyenKiemHiep> lst = q.getResultList();
		return (lst.size() > 0) ? lst : null;
	}

	@Override
	public ChapterTmpTruyenKiemHiep findAliasvTruyen(TruyenKiemHiep truyen,
			String chapNameUsigned) {
		CriteriaBuilder critBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ChapterTmpTruyenKiemHiep> query = critBuilder
				.createQuery(ChapterTmpTruyenKiemHiep.class);
		Root<ChapterTmpTruyenKiemHiep> entity = query.from(ChapterTmpTruyenKiemHiep.class);
		Predicate p = critBuilder.conjunction();
		p = critBuilder.and(p,
				critBuilder.equal(entity.<TruyenKiemHiep> get("truyenKiemHiep"), truyen),
				critBuilder.equal(entity.<String> get("alias"), chapNameUsigned));
		query = query.where(p);
		TypedQuery<ChapterTmpTruyenKiemHiep> q = entityManager.createQuery(query);
//		long count = countByQuery(query, entity);
		q.setFirstResult(0);
		q.setMaxResults(1);
		List<ChapterTmpTruyenKiemHiep> lst = q.getResultList();
		return (lst.size() > 0) ? lst.get(0) : null;
	}

	@Override
	public List<ChapterTmpTruyenKiemHiep> getLstAllChapterTmpTruyenKiemHiep(String kwSearch, String sort, String sortBy, PagingInfo pInfo) {
		CriteriaBuilder critBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ChapterTmpTruyenKiemHiep> query = critBuilder
				.createQuery(ChapterTmpTruyenKiemHiep.class);
		Root<ChapterTmpTruyenKiemHiep> entity = query.from(ChapterTmpTruyenKiemHiep.class);
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
		TypedQuery<ChapterTmpTruyenKiemHiep> q = entityManager.createQuery(query);
		long count = countByQuery(query, entity);
		pInfo.setCount(count);
		pInfo.setTotalPage((int)Math.ceil((double)count/pInfo.getSizePerPage()));		
		q.setFirstResult(pInfo.getStartResult());
		q.setMaxResults(pInfo.getSizePerPage());
		List<ChapterTmpTruyenKiemHiep> lst = q.getResultList();
		return (lst.size() > 0) ? lst : null;
	}
	
	
	
	@Override
	public List<ChapterTmpTruyenKiemHiep> getByTruyen(TruyenKiemHiep truyen) {
		CriteriaBuilder critBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ChapterTmpTruyenKiemHiep> query = critBuilder
				.createQuery(ChapterTmpTruyenKiemHiep.class);
		Root<ChapterTmpTruyenKiemHiep> entity = query.from(ChapterTmpTruyenKiemHiep.class);
		Predicate p = critBuilder.conjunction();
		p = critBuilder.and(p,
				critBuilder.equal(entity.<TruyenKiemHiep> get("truyenKiemHiep"), truyen));
		query = query.where(p);
		query.orderBy(critBuilder.desc(entity.get("chapterNumber")));
		TypedQuery<ChapterTmpTruyenKiemHiep> q = entityManager.createQuery(query);
//		long count = countByQuery(query, entity);
//		q.setFirstResult(0);
//		q.setMaxResults((int) count);
		List<ChapterTmpTruyenKiemHiep> lst = q.getResultList();
		return (lst.size() > 0) ? lst : null;
	}
	

	@Override
	public ChapterTmpTruyenKiemHiep getChapterByTruyenChapnumber(TruyenKiemHiep truyen, float chapterNumber) {
		CriteriaBuilder critBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ChapterTmpTruyenKiemHiep> query = critBuilder
				.createQuery(ChapterTmpTruyenKiemHiep.class);
		Root<ChapterTmpTruyenKiemHiep> entity = query.from(ChapterTmpTruyenKiemHiep.class);
		Predicate p = critBuilder.conjunction();
		p = critBuilder.and(p,
				critBuilder.equal(entity.<TruyenKiemHiep> get("truyenKiemHiep"), truyen),
				critBuilder.equal(entity.<String> get("chapterNumber"), chapterNumber));
		query = query.where(p);
		TypedQuery<ChapterTmpTruyenKiemHiep> q = entityManager.createQuery(query);
		q.setFirstResult(0);
		q.setMaxResults(1);
		List<ChapterTmpTruyenKiemHiep> lst = q.getResultList();
		return (lst.size() > 0) ? lst.get(0) : null;
	}

	@Override
	public List<ChapterTmpTruyenKiemHiep> getLstByOrderBy(String columnOrderBy, PagingInfo pInfo) {
		CriteriaBuilder critBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ChapterTmpTruyenKiemHiep> query = critBuilder
				.createQuery(ChapterTmpTruyenKiemHiep.class);
		Root<ChapterTmpTruyenKiemHiep> entity = query.from(ChapterTmpTruyenKiemHiep.class);
		query.orderBy(critBuilder.desc(entity.get(columnOrderBy)));
		TypedQuery<ChapterTmpTruyenKiemHiep> q = entityManager.createQuery(query);
		long count = countByQuery(query, entity);
		pInfo.setCount(count);
		pInfo.setTotalPage((int)Math.ceil((double)count/pInfo.getSizePerPage()));		
		q.setFirstResult(pInfo.getStartResult());
		q.setMaxResults(pInfo.getSizePerPage());
		List<ChapterTmpTruyenKiemHiep> lst = q.getResultList();
		return (lst.size() > 0) ? lst : null;
	}

	@Override
	public List<ChapterTmpTruyenKiemHiep> getByTruyenPaging(TruyenKiemHiep truyen,
			PagingInfo pInfo) {
		CriteriaBuilder critBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ChapterTmpTruyenKiemHiep> query = critBuilder
				.createQuery(ChapterTmpTruyenKiemHiep.class);
		Root<ChapterTmpTruyenKiemHiep> entity = query.from(ChapterTmpTruyenKiemHiep.class);
		Predicate p = critBuilder.conjunction();
		p = critBuilder.and(p,
				critBuilder.equal(entity.<TruyenKiemHiep> get("truyenKiemHiep"), truyen));
		query = query.where(p);
		query.orderBy(critBuilder.asc(entity.get("chapterNumber")));
		TypedQuery<ChapterTmpTruyenKiemHiep> q = entityManager.createQuery(query);
		long count = countByQuery(query, entity);
		pInfo.setCount(count);
		pInfo.setTotalPage((int)Math.ceil((double)count/pInfo.getSizePerPage()));		
		q.setFirstResult(pInfo.getStartResult());
		q.setMaxResults(pInfo.getSizePerPage());
		List<ChapterTmpTruyenKiemHiep> lst = q.getResultList();
		return (lst.size() > 0) ? lst : null;
	}

	@Override
	public ChapterTmpTruyenKiemHiep findByTruyenChapterNumber(TruyenKiemHiep truyen,
			float chapterNumber) {
		CriteriaBuilder critBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ChapterTmpTruyenKiemHiep> query = critBuilder
				.createQuery(ChapterTmpTruyenKiemHiep.class);
		Root<ChapterTmpTruyenKiemHiep> entity = query.from(ChapterTmpTruyenKiemHiep.class);
		Predicate p = critBuilder.conjunction();
		p = critBuilder.and(p,
				critBuilder.equal(entity.<TruyenKiemHiep> get("truyenKiemHiep"), truyen),
				critBuilder.equal(entity.<String> get("chapterNumber"), chapterNumber));
		query = query.where(p);
		TypedQuery<ChapterTmpTruyenKiemHiep> q = entityManager.createQuery(query);
		q.setFirstResult(0);
		q.setMaxResults(1);
		List<ChapterTmpTruyenKiemHiep> lst = q.getResultList();
		return (lst.size() > 0) ? lst.get(0) : null;
	}

	@Override
	public List<ChapterTmpTruyenKiemHiep> getListNewChapter(int maxLimit) {
		CriteriaBuilder critBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ChapterTmpTruyenKiemHiep> query = critBuilder
				.createQuery(ChapterTmpTruyenKiemHiep.class);
		Root<ChapterTmpTruyenKiemHiep> entity = query.from(ChapterTmpTruyenKiemHiep.class);
		query.orderBy(critBuilder.desc(entity.get("id")));
		TypedQuery<ChapterTmpTruyenKiemHiep> q = entityManager.createQuery(query);
		q.setFirstResult(0);
		q.setMaxResults(maxLimit);
		List<ChapterTmpTruyenKiemHiep> lst = q.getResultList();
		return (lst.size() > 0) ? lst : null;
	}

	@Override
	public List<Object> getByIdTruyen(Long idTruyen) {
		Query query = entityManager
				.createNativeQuery("SELECT id, chapterNumber FROM chapter_tmp_truyen_kiem_hiep WHERE truyenKiemHiepItem = "+idTruyen+" ORDER BY chapterNumber DESC");
		List<Object> result = query.getResultList();
		return result;
	}

}

