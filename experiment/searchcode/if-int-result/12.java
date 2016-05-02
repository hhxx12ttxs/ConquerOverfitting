package pl.zgora.uz.wmie.fe.sps.business.abstracts;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;

import pl.zgora.uz.wmie.fe.sps.business.dto.DataResult;
import pl.zgora.uz.wmie.fe.sps.business.util.HibernateUtil;

/**
 * Klasa ktora zawiera wszystkie podstawowe metody na bazie danych. Sa one
 * sparametryzowane ze wzgledu na typ tabeli i rodzaj klucza glownego.
 * 
 * @author Miro007
 * 
 * @param <T>
 *            - klasa odwzorowujaca tabele bazy danych
 * @param <Id>
 *            - klucz glowny tej tabeli
 */
@SuppressWarnings("unchecked")
public abstract class GenericDAO<T, Id extends Serializable> extends
		AbstractFactory {

	private Type thisType = getClass().getGenericSuperclass();
	private Class<? extends T> domainClass;

	public GenericDAO() {
		// Metoda do wyciagania typu generycznego...
		if (thisType instanceof ParameterizedType) {
			domainClass = (Class<? extends T>) ((ParameterizedType) thisType)
					.getActualTypeArguments()[0];
		} else if (thisType instanceof Class) {
			domainClass = (Class<? extends T>) ((ParameterizedType) ((Class) thisType)
					.getGenericSuperclass()).getActualTypeArguments()[0];
		} else {
			throw new IllegalArgumentException(
					"Problem with getting generic type " + getClass());
		}

	}

	protected T load(Id id) throws Exception {
		Object obj = null;
		Session session = null;
		try {
			session = getSession();
			obj = session.load(domainClass, id);
		} catch (HibernateException e) {
			handleException(e);
		}
		return (T) obj;
	}

	protected Id save(T obj) throws Exception {
		Id id = null;
		Session session = null;
		try {
			session = getSession();
			id = (Id) session.save(obj);
		} catch (HibernateException e) {
			handleException(e);
		}
		return id;
	}

	protected void update(T obj) throws Exception {
		Session session = null;
		try {
			session = getSession();
			session.update(obj);
		} catch (HibernateException e) {
			handleException(e);
		}
	}

	protected void delete(T obj) throws Exception {
		Session session = null;
		try {
			session = getSession();
			session.delete(obj);
		} catch (HibernateException e) {
			handleException(e);
		}
	}

	// TODO zoptymalizowac, przyklad transakcji
	protected void deleteById(Id id) throws Exception {
		try {
			T obj = load(id);
			delete(obj);
		} catch (HibernateException e) {
			handleException(e);
		}
	}

	protected List<Object[]> findByParams(ProjectionList projections,
			List<Criterion> criterions, List<Order> orders) throws Exception {
		Criteria criteria = null;
		List<Object[]> result = new ArrayList<Object[]>();
		Session session = null;
		try {
			session = getSession();
			criteria = session.createCriteria(domainClass);

			criteria.setProjection(HibernateUtil.projectionSupport(criteria,
					projections));
			HibernateUtil.criterionSupport(criteria, criterions);

			for (Criterion criterion : criterions) {
				criteria.add(criterion);
			}

			HibernateUtil.orderSupport(criteria, orders);
			for (Order order : orders) {
				criteria.addOrder(order);
			}
			result.addAll((List<Object[]>) criteria.list());
		} catch (HibernateException e) {
			handleException(e);
		}

		return result;
	}

	/*
	 * TODO jesli polaczenia na tabelce sa nastepujace <FE_Table:column
	 * sort="yes" label="Identyfikator"
	 * databaseColumn="<%=Stanowisko.PROPERTY_ID %>" /> <FE_Table:column
	 * sort="yes" label="dzial"databaseColumn=
	 * "<%=Stanowisko.PROPERTY_OBJECT_PPRACOWNIKS+'.'+Pracownik.PROPERTY_OBJECT_DZIALS+'.'+Dzial.PROPERTY_NAZWA_DZIALU %>"
	 * />
	 */
	protected DataResult findByParams(ProjectionList projections,
			List<Criterion> criterions, List<Order> orders, int startIndex,
			int rowAmount) throws Exception {
		Criteria criteria = null;
		List<Object[]> result = new ArrayList<Object[]>();
		Session session = null;
		int rowCount = 0;
		try {
			session = getSession();
			criteria = session.createCriteria(domainClass);

			HibernateUtil.criterionSupport(criteria, criterions);
			for (Criterion criterion : criterions) {
				criteria.add(criterion);
			}

			projections = HibernateUtil
					.projectionSupport(criteria, projections);
			criteria.setProjection(projections);

			rowCount = getRowCount(criteria, projections);

			HibernateUtil.orderSupport(criteria, orders);
			for (Order order : orders) {
				criteria.addOrder(order);
			}

			criteria.setFirstResult(startIndex);
			criteria.setMaxResults(rowAmount);

			result.addAll((List<Object[]>) criteria.list());
		} catch (HibernateException e) {
			handleException(e);
		}

		return new DataResult(result, rowCount, rowAmount);
	}

	protected List<Object[]> findByParams(ProjectionList projections,
			List<Criterion> criterions) throws Exception {
		Criteria criteria = null;
		List<Object[]> result = new ArrayList<Object[]>();
		Session session = null;
		try {
			session = getSession();
			criteria = session.createCriteria(domainClass);
			criteria.setProjection(HibernateUtil.projectionSupport(criteria,
					projections));

			HibernateUtil.criterionSupport(criteria, criterions);
			for (Criterion criterion : criterions) {
				criteria.add(criterion);
			}
			result.addAll((List<Object[]>) criteria.list());
		} catch (HibernateException e) {
			handleException(e);
		}
		return result;
	}

	protected DataResult findByParams(ProjectionList projections,
			List<Criterion> criterions, int startIndex, int rowAmount)
			throws Exception {
		Criteria criteria = null;
		List<Object[]> result = new ArrayList<Object[]>();
		Session session = null;
		int rowCount = 0;
		try {
			session = getSession();
			criteria = session.createCriteria(domainClass);
			criteria.setProjection(HibernateUtil.projectionSupport(criteria,
					projections));

			HibernateUtil.criterionSupport(criteria, criterions);
			for (Criterion criterion : criterions) {
				criteria.add(criterion);
			}
			criteria.setFirstResult(startIndex);
			criteria.setMaxResults(rowAmount);
			result.addAll((List<Object[]>) criteria.list());
		} catch (HibernateException e) {
			handleException(e);
		}

		return new DataResult(result, rowCount, rowAmount);
	}

	@Deprecated
	protected List<Object[]> findByParams(ProjectionList projections,
			int startIndex, int rowAmount) throws Exception {
		Criteria criteria = null;
		List<Object[]> result = new ArrayList<Object[]>();
		Session session = null;
		try {
			session = getSession();
			criteria = session.createCriteria(domainClass);
			criteria.setProjection(HibernateUtil.projectionSupport(criteria,
					projections));

			criteria.setFirstResult(startIndex);
			criteria.setMaxResults(rowAmount);
			result.addAll((List<Object[]>) criteria.list());
		} catch (HibernateException e) {
			handleException(e);
		}

		return result;
	}

	protected List<Object[]> findByParams(ProjectionList projections)
			throws Exception {
		Criteria criteria = null;
		List<Object[]> result = new ArrayList<Object[]>();
		Session session = null;
		try {
			session = getSession();
			criteria = session.createCriteria(domainClass);
			criteria.setProjection(HibernateUtil.projectionSupport(criteria,
					projections));
			result.addAll((List<Object[]>) criteria.list());
		} catch (HibernateException e) {
			handleException(e);
		}

		return result;
	}

	protected List<T> findAll() throws Exception {
		List<T> result = new ArrayList<T>();
		Session session = null;
		try {
			session = getSession();
			result = (List<T>) session.createCriteria(domainClass).list();
		} catch (HibernateException e) {
			handleException(e);
		}
		return result;
	}

	protected List<T> findByParams(List<Criterion> criterions) throws Exception {
		List<T> result = new ArrayList<T>();
		Session session = null;
		try {
			session = getSession();
			Criteria criteria = session.createCriteria(domainClass);
			HibernateUtil.criterionSupport(criteria, criterions);
			for (Criterion criterion : criterions) {
				criteria.add(criterion);
			}
			result = (List<T>) criteria.list();
		} catch (HibernateException e) {
			handleException(e);
		}
		return result;
	}

	protected List<T> findByParam(Criterion criterion) throws Exception {
		List<Criterion> listCList = new ArrayList<Criterion>();
		listCList.add(criterion);
		return findByParams(listCList);
	}

	// TODO optymalizacja
	protected int getRowCount(List<Criterion> criterions) throws Exception {
		Session session = null;
		int result = 0;
		try {
			session = getSession();

			Criteria criteria = session.createCriteria(domainClass);
			criteria.setProjection(Projections.rowCount());
			HibernateUtil.criterionSupport(criteria, criterions);
			for (Criterion criterion : criterions) {
				criteria.add(criterion);
			}
			result = criteria.list().size();
		} catch (HibernateException e) {
			handleException(e);
		}
		return result;
	}

	// TODO optymalizacja
	protected int getRowCount(ProjectionList projections,
			List<Criterion> criterions) throws Exception {
		Session session = null;
		int result = 0;
		try {
			session = getSession();
			Criteria criteria = session.createCriteria(domainClass);

			criteria.setProjection(HibernateUtil.projectionSupport(criteria,
					projections));

			HibernateUtil.criterionSupport(criteria, criterions);
			for (Criterion criterion : criterions) {
				criteria.add(criterion);
			}
			result = criteria.list().size();
		} catch (HibernateException e) {
			handleException(e);
		}
		return result;
	}

	private int getRowCount(Criteria criteria, ProjectionList projectionList) {
		int result = 0;
		String stringProjection = projectionList.getProjection(0).toString();
		if (stringProjection.contains("distinct")) {
			criteria.setProjection(Projections.countDistinct(stringProjection
					.split(" ")[1]));
		} else {
			criteria.setProjection(Projections.count(stringProjection));

		}
		result = (Integer) criteria.list().get(0);
		criteria.setProjection(projectionList);
		return result;
	}

}
