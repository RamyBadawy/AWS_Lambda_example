package com.gaggle.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Service;

import com.gaggle.config.HibernateUtil;
import com.gaggle.model.User;

@Service
public class UserServiceImpl {

	public List<User> getUserByNameContains(String search) {
		try (SessionFactory sessionFactory = HibernateUtil.getSessionFactory()) {
			EntityManager manager = sessionFactory.createEntityManager();
			CriteriaBuilder criteriaBuilder = manager.getCriteriaBuilder();
			CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);
			Root<User> root = criteriaQuery.from(User.class);

			Predicate[] restrictions = new Predicate[2];
			restrictions[0] = criteriaBuilder.like(root.<String>get("firstName"), search);
			restrictions[1] = (criteriaBuilder.like(root.<String>get("lastName"), "%" + search + "%"));

			criteriaQuery
				.select(root)
				.where(restrictions)
				.orderBy(criteriaBuilder.asc(root.get("firstName")));

			List<User> result = manager.createQuery(criteriaQuery).getResultList();
			manager.close();

			return result;
		} catch (HibernateException he) {
			throw he;
		} catch (PersistenceException pe) {
			throw pe;
		} catch (IllegalArgumentException ie) {
			throw ie;
		}
	}

}
