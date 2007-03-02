/**
 * Copyright (c) 2000-2007 Liferay, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.liferay.portlet.softwarecatalog.service.persistence;

import com.liferay.portal.SystemException;
import com.liferay.portal.kernel.dao.DynamicQuery;
import com.liferay.portal.kernel.dao.DynamicQueryInitializer;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.StringMaker;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.service.persistence.BasePersistence;
import com.liferay.portal.spring.hibernate.HibernateUtil;

import com.liferay.portlet.softwarecatalog.NoSuchProductEntryException;
import com.liferay.portlet.softwarecatalog.model.SCProductEntry;
import com.liferay.portlet.softwarecatalog.model.impl.SCProductEntryImpl;

import com.liferay.util.dao.hibernate.QueryPos;
import com.liferay.util.dao.hibernate.QueryUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

import org.springframework.dao.DataAccessException;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.jdbc.object.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import java.util.Iterator;
import java.util.List;

/**
 * <a href="SCProductEntryPersistence.java.html"><b><i>View Source</i></b></a>
 *
 * @author Brian Wing Shun Chan
 *
 */
public class SCProductEntryPersistence extends BasePersistence {
	public SCProductEntry create(long productEntryId) {
		SCProductEntry scProductEntry = new SCProductEntryImpl();
		scProductEntry.setNew(true);
		scProductEntry.setPrimaryKey(productEntryId);

		return scProductEntry;
	}

	public SCProductEntry remove(long productEntryId)
		throws NoSuchProductEntryException, SystemException {
		Session session = null;

		try {
			session = openSession();

			SCProductEntry scProductEntry = (SCProductEntry)session.get(SCProductEntryImpl.class,
					new Long(productEntryId));

			if (scProductEntry == null) {
				if (_log.isWarnEnabled()) {
					_log.warn("No SCProductEntry exists with the primary key " +
						productEntryId);
				}

				throw new NoSuchProductEntryException(
					"No SCProductEntry exists with the primary key " +
					productEntryId);
			}

			return remove(scProductEntry);
		}
		catch (HibernateException he) {
			throw new SystemException(he);
		}
		finally {
			closeSession(session);
		}
	}

	public SCProductEntry remove(SCProductEntry scProductEntry)
		throws SystemException {
		Session session = null;

		try {
			session = openSession();
			session.delete(scProductEntry);
			session.flush();
			clearSCLicenses.clear(scProductEntry.getPrimaryKey());

			return scProductEntry;
		}
		catch (HibernateException he) {
			throw new SystemException(he);
		}
		finally {
			closeSession(session);
		}
	}

	public com.liferay.portlet.softwarecatalog.model.SCProductEntry update(
		com.liferay.portlet.softwarecatalog.model.SCProductEntry scProductEntry)
		throws SystemException {
		return update(scProductEntry, false);
	}

	public com.liferay.portlet.softwarecatalog.model.SCProductEntry update(
		com.liferay.portlet.softwarecatalog.model.SCProductEntry scProductEntry,
		boolean saveOrUpdate) throws SystemException {
		Session session = null;

		try {
			session = openSession();

			if (saveOrUpdate) {
				session.saveOrUpdate(scProductEntry);
			}
			else {
				if (scProductEntry.isNew()) {
					session.save(scProductEntry);
				}
			}

			session.flush();
			scProductEntry.setNew(false);

			return scProductEntry;
		}
		catch (HibernateException he) {
			throw new SystemException(he);
		}
		finally {
			closeSession(session);
		}
	}

	public SCProductEntry findByPrimaryKey(long productEntryId)
		throws NoSuchProductEntryException, SystemException {
		SCProductEntry scProductEntry = fetchByPrimaryKey(productEntryId);

		if (scProductEntry == null) {
			if (_log.isWarnEnabled()) {
				_log.warn("No SCProductEntry exists with the primary key " +
					productEntryId);
			}

			throw new NoSuchProductEntryException(
				"No SCProductEntry exists with the primary key " +
				productEntryId);
		}

		return scProductEntry;
	}

	public SCProductEntry fetchByPrimaryKey(long productEntryId)
		throws SystemException {
		Session session = null;

		try {
			session = openSession();

			return (SCProductEntry)session.get(SCProductEntryImpl.class,
				new Long(productEntryId));
		}
		catch (HibernateException he) {
			throw new SystemException(he);
		}
		finally {
			closeSession(session);
		}
	}

	public List findByGroupId(long groupId) throws SystemException {
		Session session = null;

		try {
			session = openSession();

			StringMaker query = new StringMaker();
			query.append(
				"FROM com.liferay.portlet.softwarecatalog.model.SCProductEntry WHERE ");
			query.append("groupId = ?");
			query.append(" ");
			query.append("ORDER BY ");
			query.append("modifiedDate DESC").append(", ");
			query.append("name DESC");

			Query q = session.createQuery(query.toString());
			q.setCacheable(true);

			int queryPos = 0;
			q.setLong(queryPos++, groupId);

			return q.list();
		}
		catch (HibernateException he) {
			throw new SystemException(he);
		}
		finally {
			closeSession(session);
		}
	}

	public List findByGroupId(long groupId, int begin, int end)
		throws SystemException {
		return findByGroupId(groupId, begin, end, null);
	}

	public List findByGroupId(long groupId, int begin, int end,
		OrderByComparator obc) throws SystemException {
		Session session = null;

		try {
			session = openSession();

			StringMaker query = new StringMaker();
			query.append(
				"FROM com.liferay.portlet.softwarecatalog.model.SCProductEntry WHERE ");
			query.append("groupId = ?");
			query.append(" ");

			if (obc != null) {
				query.append("ORDER BY ");
				query.append(obc.getOrderBy());
			}
			else {
				query.append("ORDER BY ");
				query.append("modifiedDate DESC").append(", ");
				query.append("name DESC");
			}

			Query q = session.createQuery(query.toString());
			q.setCacheable(true);

			int queryPos = 0;
			q.setLong(queryPos++, groupId);

			return QueryUtil.list(q, getDialect(), begin, end);
		}
		catch (HibernateException he) {
			throw new SystemException(he);
		}
		finally {
			closeSession(session);
		}
	}

	public SCProductEntry findByGroupId_First(long groupId,
		OrderByComparator obc)
		throws NoSuchProductEntryException, SystemException {
		List list = findByGroupId(groupId, 0, 1, obc);

		if (list.size() == 0) {
			StringMaker msg = new StringMaker();
			msg.append("No SCProductEntry exists with the key ");
			msg.append(StringPool.OPEN_CURLY_BRACE);
			msg.append("groupId=");
			msg.append(groupId);
			msg.append(StringPool.CLOSE_CURLY_BRACE);
			throw new NoSuchProductEntryException(msg.toString());
		}
		else {
			return (SCProductEntry)list.get(0);
		}
	}

	public SCProductEntry findByGroupId_Last(long groupId, OrderByComparator obc)
		throws NoSuchProductEntryException, SystemException {
		int count = countByGroupId(groupId);
		List list = findByGroupId(groupId, count - 1, count, obc);

		if (list.size() == 0) {
			StringMaker msg = new StringMaker();
			msg.append("No SCProductEntry exists with the key ");
			msg.append(StringPool.OPEN_CURLY_BRACE);
			msg.append("groupId=");
			msg.append(groupId);
			msg.append(StringPool.CLOSE_CURLY_BRACE);
			throw new NoSuchProductEntryException(msg.toString());
		}
		else {
			return (SCProductEntry)list.get(0);
		}
	}

	public SCProductEntry[] findByGroupId_PrevAndNext(long productEntryId,
		long groupId, OrderByComparator obc)
		throws NoSuchProductEntryException, SystemException {
		SCProductEntry scProductEntry = findByPrimaryKey(productEntryId);
		int count = countByGroupId(groupId);
		Session session = null;

		try {
			session = openSession();

			StringMaker query = new StringMaker();
			query.append(
				"FROM com.liferay.portlet.softwarecatalog.model.SCProductEntry WHERE ");
			query.append("groupId = ?");
			query.append(" ");

			if (obc != null) {
				query.append("ORDER BY ");
				query.append(obc.getOrderBy());
			}
			else {
				query.append("ORDER BY ");
				query.append("modifiedDate DESC").append(", ");
				query.append("name DESC");
			}

			Query q = session.createQuery(query.toString());
			q.setCacheable(true);

			int queryPos = 0;
			q.setLong(queryPos++, groupId);

			Object[] objArray = QueryUtil.getPrevAndNext(q, count, obc,
					scProductEntry);
			SCProductEntry[] array = new SCProductEntryImpl[3];
			array[0] = (SCProductEntry)objArray[0];
			array[1] = (SCProductEntry)objArray[1];
			array[2] = (SCProductEntry)objArray[2];

			return array;
		}
		catch (HibernateException he) {
			throw new SystemException(he);
		}
		finally {
			closeSession(session);
		}
	}

	public List findByCompanyId(String companyId) throws SystemException {
		Session session = null;

		try {
			session = openSession();

			StringMaker query = new StringMaker();
			query.append(
				"FROM com.liferay.portlet.softwarecatalog.model.SCProductEntry WHERE ");

			if (companyId == null) {
				query.append("companyId IS NULL");
			}
			else {
				query.append("companyId = ?");
			}

			query.append(" ");
			query.append("ORDER BY ");
			query.append("modifiedDate DESC").append(", ");
			query.append("name DESC");

			Query q = session.createQuery(query.toString());
			q.setCacheable(true);

			int queryPos = 0;

			if (companyId != null) {
				q.setString(queryPos++, companyId);
			}

			return q.list();
		}
		catch (HibernateException he) {
			throw new SystemException(he);
		}
		finally {
			closeSession(session);
		}
	}

	public List findByCompanyId(String companyId, int begin, int end)
		throws SystemException {
		return findByCompanyId(companyId, begin, end, null);
	}

	public List findByCompanyId(String companyId, int begin, int end,
		OrderByComparator obc) throws SystemException {
		Session session = null;

		try {
			session = openSession();

			StringMaker query = new StringMaker();
			query.append(
				"FROM com.liferay.portlet.softwarecatalog.model.SCProductEntry WHERE ");

			if (companyId == null) {
				query.append("companyId IS NULL");
			}
			else {
				query.append("companyId = ?");
			}

			query.append(" ");

			if (obc != null) {
				query.append("ORDER BY ");
				query.append(obc.getOrderBy());
			}
			else {
				query.append("ORDER BY ");
				query.append("modifiedDate DESC").append(", ");
				query.append("name DESC");
			}

			Query q = session.createQuery(query.toString());
			q.setCacheable(true);

			int queryPos = 0;

			if (companyId != null) {
				q.setString(queryPos++, companyId);
			}

			return QueryUtil.list(q, getDialect(), begin, end);
		}
		catch (HibernateException he) {
			throw new SystemException(he);
		}
		finally {
			closeSession(session);
		}
	}

	public SCProductEntry findByCompanyId_First(String companyId,
		OrderByComparator obc)
		throws NoSuchProductEntryException, SystemException {
		List list = findByCompanyId(companyId, 0, 1, obc);

		if (list.size() == 0) {
			StringMaker msg = new StringMaker();
			msg.append("No SCProductEntry exists with the key ");
			msg.append(StringPool.OPEN_CURLY_BRACE);
			msg.append("companyId=");
			msg.append(companyId);
			msg.append(StringPool.CLOSE_CURLY_BRACE);
			throw new NoSuchProductEntryException(msg.toString());
		}
		else {
			return (SCProductEntry)list.get(0);
		}
	}

	public SCProductEntry findByCompanyId_Last(String companyId,
		OrderByComparator obc)
		throws NoSuchProductEntryException, SystemException {
		int count = countByCompanyId(companyId);
		List list = findByCompanyId(companyId, count - 1, count, obc);

		if (list.size() == 0) {
			StringMaker msg = new StringMaker();
			msg.append("No SCProductEntry exists with the key ");
			msg.append(StringPool.OPEN_CURLY_BRACE);
			msg.append("companyId=");
			msg.append(companyId);
			msg.append(StringPool.CLOSE_CURLY_BRACE);
			throw new NoSuchProductEntryException(msg.toString());
		}
		else {
			return (SCProductEntry)list.get(0);
		}
	}

	public SCProductEntry[] findByCompanyId_PrevAndNext(long productEntryId,
		String companyId, OrderByComparator obc)
		throws NoSuchProductEntryException, SystemException {
		SCProductEntry scProductEntry = findByPrimaryKey(productEntryId);
		int count = countByCompanyId(companyId);
		Session session = null;

		try {
			session = openSession();

			StringMaker query = new StringMaker();
			query.append(
				"FROM com.liferay.portlet.softwarecatalog.model.SCProductEntry WHERE ");

			if (companyId == null) {
				query.append("companyId IS NULL");
			}
			else {
				query.append("companyId = ?");
			}

			query.append(" ");

			if (obc != null) {
				query.append("ORDER BY ");
				query.append(obc.getOrderBy());
			}
			else {
				query.append("ORDER BY ");
				query.append("modifiedDate DESC").append(", ");
				query.append("name DESC");
			}

			Query q = session.createQuery(query.toString());
			q.setCacheable(true);

			int queryPos = 0;

			if (companyId != null) {
				q.setString(queryPos++, companyId);
			}

			Object[] objArray = QueryUtil.getPrevAndNext(q, count, obc,
					scProductEntry);
			SCProductEntry[] array = new SCProductEntryImpl[3];
			array[0] = (SCProductEntry)objArray[0];
			array[1] = (SCProductEntry)objArray[1];
			array[2] = (SCProductEntry)objArray[2];

			return array;
		}
		catch (HibernateException he) {
			throw new SystemException(he);
		}
		finally {
			closeSession(session);
		}
	}

	public List findByG_U(long groupId, String userId)
		throws SystemException {
		Session session = null;

		try {
			session = openSession();

			StringMaker query = new StringMaker();
			query.append(
				"FROM com.liferay.portlet.softwarecatalog.model.SCProductEntry WHERE ");
			query.append("groupId = ?");
			query.append(" AND ");

			if (userId == null) {
				query.append("userId IS NULL");
			}
			else {
				query.append("userId = ?");
			}

			query.append(" ");
			query.append("ORDER BY ");
			query.append("modifiedDate DESC").append(", ");
			query.append("name DESC");

			Query q = session.createQuery(query.toString());
			q.setCacheable(true);

			int queryPos = 0;
			q.setLong(queryPos++, groupId);

			if (userId != null) {
				q.setString(queryPos++, userId);
			}

			return q.list();
		}
		catch (HibernateException he) {
			throw new SystemException(he);
		}
		finally {
			closeSession(session);
		}
	}

	public List findByG_U(long groupId, String userId, int begin, int end)
		throws SystemException {
		return findByG_U(groupId, userId, begin, end, null);
	}

	public List findByG_U(long groupId, String userId, int begin, int end,
		OrderByComparator obc) throws SystemException {
		Session session = null;

		try {
			session = openSession();

			StringMaker query = new StringMaker();
			query.append(
				"FROM com.liferay.portlet.softwarecatalog.model.SCProductEntry WHERE ");
			query.append("groupId = ?");
			query.append(" AND ");

			if (userId == null) {
				query.append("userId IS NULL");
			}
			else {
				query.append("userId = ?");
			}

			query.append(" ");

			if (obc != null) {
				query.append("ORDER BY ");
				query.append(obc.getOrderBy());
			}
			else {
				query.append("ORDER BY ");
				query.append("modifiedDate DESC").append(", ");
				query.append("name DESC");
			}

			Query q = session.createQuery(query.toString());
			q.setCacheable(true);

			int queryPos = 0;
			q.setLong(queryPos++, groupId);

			if (userId != null) {
				q.setString(queryPos++, userId);
			}

			return QueryUtil.list(q, getDialect(), begin, end);
		}
		catch (HibernateException he) {
			throw new SystemException(he);
		}
		finally {
			closeSession(session);
		}
	}

	public SCProductEntry findByG_U_First(long groupId, String userId,
		OrderByComparator obc)
		throws NoSuchProductEntryException, SystemException {
		List list = findByG_U(groupId, userId, 0, 1, obc);

		if (list.size() == 0) {
			StringMaker msg = new StringMaker();
			msg.append("No SCProductEntry exists with the key ");
			msg.append(StringPool.OPEN_CURLY_BRACE);
			msg.append("groupId=");
			msg.append(groupId);
			msg.append(", ");
			msg.append("userId=");
			msg.append(userId);
			msg.append(StringPool.CLOSE_CURLY_BRACE);
			throw new NoSuchProductEntryException(msg.toString());
		}
		else {
			return (SCProductEntry)list.get(0);
		}
	}

	public SCProductEntry findByG_U_Last(long groupId, String userId,
		OrderByComparator obc)
		throws NoSuchProductEntryException, SystemException {
		int count = countByG_U(groupId, userId);
		List list = findByG_U(groupId, userId, count - 1, count, obc);

		if (list.size() == 0) {
			StringMaker msg = new StringMaker();
			msg.append("No SCProductEntry exists with the key ");
			msg.append(StringPool.OPEN_CURLY_BRACE);
			msg.append("groupId=");
			msg.append(groupId);
			msg.append(", ");
			msg.append("userId=");
			msg.append(userId);
			msg.append(StringPool.CLOSE_CURLY_BRACE);
			throw new NoSuchProductEntryException(msg.toString());
		}
		else {
			return (SCProductEntry)list.get(0);
		}
	}

	public SCProductEntry[] findByG_U_PrevAndNext(long productEntryId,
		long groupId, String userId, OrderByComparator obc)
		throws NoSuchProductEntryException, SystemException {
		SCProductEntry scProductEntry = findByPrimaryKey(productEntryId);
		int count = countByG_U(groupId, userId);
		Session session = null;

		try {
			session = openSession();

			StringMaker query = new StringMaker();
			query.append(
				"FROM com.liferay.portlet.softwarecatalog.model.SCProductEntry WHERE ");
			query.append("groupId = ?");
			query.append(" AND ");

			if (userId == null) {
				query.append("userId IS NULL");
			}
			else {
				query.append("userId = ?");
			}

			query.append(" ");

			if (obc != null) {
				query.append("ORDER BY ");
				query.append(obc.getOrderBy());
			}
			else {
				query.append("ORDER BY ");
				query.append("modifiedDate DESC").append(", ");
				query.append("name DESC");
			}

			Query q = session.createQuery(query.toString());
			q.setCacheable(true);

			int queryPos = 0;
			q.setLong(queryPos++, groupId);

			if (userId != null) {
				q.setString(queryPos++, userId);
			}

			Object[] objArray = QueryUtil.getPrevAndNext(q, count, obc,
					scProductEntry);
			SCProductEntry[] array = new SCProductEntryImpl[3];
			array[0] = (SCProductEntry)objArray[0];
			array[1] = (SCProductEntry)objArray[1];
			array[2] = (SCProductEntry)objArray[2];

			return array;
		}
		catch (HibernateException he) {
			throw new SystemException(he);
		}
		finally {
			closeSession(session);
		}
	}

	public List findWithDynamicQuery(DynamicQueryInitializer queryInitializer)
		throws SystemException {
		Session session = null;

		try {
			session = openSession();

			DynamicQuery query = queryInitializer.initialize(session);

			return query.list();
		}
		catch (HibernateException he) {
			throw new SystemException(he);
		}
		finally {
			closeSession(session);
		}
	}

	public List findWithDynamicQuery(DynamicQueryInitializer queryInitializer,
		int begin, int end) throws SystemException {
		Session session = null;

		try {
			session = openSession();

			DynamicQuery query = queryInitializer.initialize(session);
			query.setLimit(begin, end);

			return query.list();
		}
		catch (HibernateException he) {
			throw new SystemException(he);
		}
		finally {
			closeSession(session);
		}
	}

	public List findAll() throws SystemException {
		return findAll(QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);
	}

	public List findAll(int begin, int end) throws SystemException {
		return findAll(begin, end, null);
	}

	public List findAll(int begin, int end, OrderByComparator obc)
		throws SystemException {
		Session session = null;

		try {
			session = openSession();

			StringMaker query = new StringMaker();
			query.append(
				"FROM com.liferay.portlet.softwarecatalog.model.SCProductEntry ");

			if (obc != null) {
				query.append("ORDER BY ");
				query.append(obc.getOrderBy());
			}
			else {
				query.append("ORDER BY ");
				query.append("modifiedDate DESC").append(", ");
				query.append("name DESC");
			}

			Query q = session.createQuery(query.toString());
			q.setCacheable(true);

			return QueryUtil.list(q, getDialect(), begin, end);
		}
		catch (HibernateException he) {
			throw new SystemException(he);
		}
		finally {
			closeSession(session);
		}
	}

	public void removeByGroupId(long groupId) throws SystemException {
		Iterator itr = findByGroupId(groupId).iterator();

		while (itr.hasNext()) {
			SCProductEntry scProductEntry = (SCProductEntry)itr.next();
			remove(scProductEntry);
		}
	}

	public void removeByCompanyId(String companyId) throws SystemException {
		Iterator itr = findByCompanyId(companyId).iterator();

		while (itr.hasNext()) {
			SCProductEntry scProductEntry = (SCProductEntry)itr.next();
			remove(scProductEntry);
		}
	}

	public void removeByG_U(long groupId, String userId)
		throws SystemException {
		Iterator itr = findByG_U(groupId, userId).iterator();

		while (itr.hasNext()) {
			SCProductEntry scProductEntry = (SCProductEntry)itr.next();
			remove(scProductEntry);
		}
	}

	public void removeAll() throws SystemException {
		Iterator itr = findAll().iterator();

		while (itr.hasNext()) {
			remove((SCProductEntry)itr.next());
		}
	}

	public int countByGroupId(long groupId) throws SystemException {
		Session session = null;

		try {
			session = openSession();

			StringMaker query = new StringMaker();
			query.append("SELECT COUNT(*) ");
			query.append(
				"FROM com.liferay.portlet.softwarecatalog.model.SCProductEntry WHERE ");
			query.append("groupId = ?");
			query.append(" ");

			Query q = session.createQuery(query.toString());
			q.setCacheable(true);

			int queryPos = 0;
			q.setLong(queryPos++, groupId);

			Iterator itr = q.list().iterator();

			if (itr.hasNext()) {
				Long count = (Long)itr.next();

				if (count != null) {
					return count.intValue();
				}
			}

			return 0;
		}
		catch (HibernateException he) {
			throw new SystemException(he);
		}
		finally {
			closeSession(session);
		}
	}

	public int countByCompanyId(String companyId) throws SystemException {
		Session session = null;

		try {
			session = openSession();

			StringMaker query = new StringMaker();
			query.append("SELECT COUNT(*) ");
			query.append(
				"FROM com.liferay.portlet.softwarecatalog.model.SCProductEntry WHERE ");

			if (companyId == null) {
				query.append("companyId IS NULL");
			}
			else {
				query.append("companyId = ?");
			}

			query.append(" ");

			Query q = session.createQuery(query.toString());
			q.setCacheable(true);

			int queryPos = 0;

			if (companyId != null) {
				q.setString(queryPos++, companyId);
			}

			Iterator itr = q.list().iterator();

			if (itr.hasNext()) {
				Long count = (Long)itr.next();

				if (count != null) {
					return count.intValue();
				}
			}

			return 0;
		}
		catch (HibernateException he) {
			throw new SystemException(he);
		}
		finally {
			closeSession(session);
		}
	}

	public int countByG_U(long groupId, String userId)
		throws SystemException {
		Session session = null;

		try {
			session = openSession();

			StringMaker query = new StringMaker();
			query.append("SELECT COUNT(*) ");
			query.append(
				"FROM com.liferay.portlet.softwarecatalog.model.SCProductEntry WHERE ");
			query.append("groupId = ?");
			query.append(" AND ");

			if (userId == null) {
				query.append("userId IS NULL");
			}
			else {
				query.append("userId = ?");
			}

			query.append(" ");

			Query q = session.createQuery(query.toString());
			q.setCacheable(true);

			int queryPos = 0;
			q.setLong(queryPos++, groupId);

			if (userId != null) {
				q.setString(queryPos++, userId);
			}

			Iterator itr = q.list().iterator();

			if (itr.hasNext()) {
				Long count = (Long)itr.next();

				if (count != null) {
					return count.intValue();
				}
			}

			return 0;
		}
		catch (HibernateException he) {
			throw new SystemException(he);
		}
		finally {
			closeSession(session);
		}
	}

	public int countAll() throws SystemException {
		Session session = null;

		try {
			session = openSession();

			StringMaker query = new StringMaker();
			query.append("SELECT COUNT(*) ");
			query.append(
				"FROM com.liferay.portlet.softwarecatalog.model.SCProductEntry");

			Query q = session.createQuery(query.toString());
			q.setCacheable(true);

			Iterator itr = q.list().iterator();

			if (itr.hasNext()) {
				Long count = (Long)itr.next();

				if (count != null) {
					return count.intValue();
				}
			}

			return 0;
		}
		catch (HibernateException he) {
			throw new SystemException(he);
		}
		finally {
			closeSession(session);
		}
	}

	public List getSCLicenses(long pk)
		throws NoSuchProductEntryException, SystemException {
		return getSCLicenses(pk, QueryUtil.ALL_POS, QueryUtil.ALL_POS);
	}

	public List getSCLicenses(long pk, int begin, int end)
		throws NoSuchProductEntryException, SystemException {
		return getSCLicenses(pk, begin, end, null);
	}

	public List getSCLicenses(long pk, int begin, int end, OrderByComparator obc)
		throws NoSuchProductEntryException, SystemException {
		Session session = null;

		try {
			session = HibernateUtil.openSession();

			StringMaker sm = new StringMaker();
			sm.append(_SQL_GETSCLICENSES);

			if (obc != null) {
				sm.append("ORDER BY ");
				sm.append(obc.getOrderBy());
			}
			else {
				sm.append("ORDER BY ");
				sm.append("SCLicense.name ASC");
			}

			String sql = sm.toString();
			SQLQuery q = session.createSQLQuery(sql);
			q.setCacheable(false);
			q.addEntity("SCLicense",
				com.liferay.portlet.softwarecatalog.model.impl.SCLicenseImpl.class);

			QueryPos qPos = QueryPos.getInstance(q);
			qPos.add(pk);

			return QueryUtil.list(q, HibernateUtil.getDialect(), begin, end);
		}
		catch (Exception e) {
			throw new SystemException(e);
		}
		finally {
			HibernateUtil.closeSession(session);
		}
	}

	public int getSCLicensesSize(long pk) throws SystemException {
		Session session = null;

		try {
			session = openSession();

			SQLQuery q = session.createSQLQuery(_SQL_GETSCLICENSESSIZE);
			q.setCacheable(false);
			q.addScalar(HibernateUtil.getCountColumnName(), Hibernate.LONG);

			QueryPos qPos = QueryPos.getInstance(q);
			qPos.add(pk);

			Iterator itr = q.list().iterator();

			if (itr.hasNext()) {
				Long count = (Long)itr.next();

				if (count != null) {
					return count.intValue();
				}
			}

			return 0;
		}
		catch (HibernateException he) {
			throw new SystemException(he);
		}
		finally {
			closeSession(session);
		}
	}

	public boolean containsSCLicense(long pk, long scLicensePK)
		throws SystemException {
		try {
			return containsSCLicense.contains(pk, scLicensePK);
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public boolean containsSCLicenses(long pk) throws SystemException {
		if (getSCLicensesSize(pk) > 0) {
			return true;
		}
		else {
			return false;
		}
	}

	public void addSCLicense(long pk, long scLicensePK)
		throws NoSuchProductEntryException, 
			com.liferay.portlet.softwarecatalog.NoSuchLicenseException, 
			SystemException {
		try {
			addSCLicense.add(pk, scLicensePK);
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public void addSCLicense(long pk,
		com.liferay.portlet.softwarecatalog.model.SCLicense scLicense)
		throws NoSuchProductEntryException, 
			com.liferay.portlet.softwarecatalog.NoSuchLicenseException, 
			SystemException {
		try {
			addSCLicense.add(pk, scLicense.getPrimaryKey());
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public void addSCLicenses(long pk, long[] scLicensePKs)
		throws NoSuchProductEntryException, 
			com.liferay.portlet.softwarecatalog.NoSuchLicenseException, 
			SystemException {
		try {
			for (int i = 0; i < scLicensePKs.length; i++) {
				addSCLicense.add(pk, scLicensePKs[i]);
			}
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public void addSCLicenses(long pk, List scLicenses)
		throws NoSuchProductEntryException, 
			com.liferay.portlet.softwarecatalog.NoSuchLicenseException, 
			SystemException {
		try {
			for (int i = 0; i < scLicenses.size(); i++) {
				com.liferay.portlet.softwarecatalog.model.SCLicense scLicense = (com.liferay.portlet.softwarecatalog.model.SCLicense)scLicenses.get(i);
				addSCLicense.add(pk, scLicense.getPrimaryKey());
			}
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public void clearSCLicenses(long pk)
		throws NoSuchProductEntryException, SystemException {
		try {
			clearSCLicenses.clear(pk);
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public void removeSCLicense(long pk, long scLicensePK)
		throws NoSuchProductEntryException, 
			com.liferay.portlet.softwarecatalog.NoSuchLicenseException, 
			SystemException {
		try {
			removeSCLicense.remove(pk, scLicensePK);
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public void removeSCLicense(long pk,
		com.liferay.portlet.softwarecatalog.model.SCLicense scLicense)
		throws NoSuchProductEntryException, 
			com.liferay.portlet.softwarecatalog.NoSuchLicenseException, 
			SystemException {
		try {
			removeSCLicense.remove(pk, scLicense.getPrimaryKey());
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public void removeSCLicenses(long pk, long[] scLicensePKs)
		throws NoSuchProductEntryException, 
			com.liferay.portlet.softwarecatalog.NoSuchLicenseException, 
			SystemException {
		try {
			for (int i = 0; i < scLicensePKs.length; i++) {
				removeSCLicense.remove(pk, scLicensePKs[i]);
			}
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public void removeSCLicenses(long pk, List scLicenses)
		throws NoSuchProductEntryException, 
			com.liferay.portlet.softwarecatalog.NoSuchLicenseException, 
			SystemException {
		try {
			for (int i = 0; i < scLicenses.size(); i++) {
				com.liferay.portlet.softwarecatalog.model.SCLicense scLicense = (com.liferay.portlet.softwarecatalog.model.SCLicense)scLicenses.get(i);
				removeSCLicense.remove(pk, scLicense.getPrimaryKey());
			}
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public void setSCLicenses(long pk, long[] scLicensePKs)
		throws NoSuchProductEntryException, 
			com.liferay.portlet.softwarecatalog.NoSuchLicenseException, 
			SystemException {
		try {
			clearSCLicenses.clear(pk);

			for (int i = 0; i < scLicensePKs.length; i++) {
				addSCLicense.add(pk, scLicensePKs[i]);
			}
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	public void setSCLicenses(long pk, List scLicenses)
		throws NoSuchProductEntryException, 
			com.liferay.portlet.softwarecatalog.NoSuchLicenseException, 
			SystemException {
		try {
			clearSCLicenses.clear(pk);

			for (int i = 0; i < scLicenses.size(); i++) {
				com.liferay.portlet.softwarecatalog.model.SCLicense scLicense = (com.liferay.portlet.softwarecatalog.model.SCLicense)scLicenses.get(i);
				addSCLicense.add(pk, scLicense.getPrimaryKey());
			}
		}
		catch (DataAccessException dae) {
			throw new SystemException(dae);
		}
	}

	protected void initDao() {
		containsSCLicense = new ContainsSCLicense(this);
		addSCLicense = new AddSCLicense(this);
		clearSCLicenses = new ClearSCLicenses(this);
		removeSCLicense = new RemoveSCLicense(this);
	}

	protected ContainsSCLicense containsSCLicense;
	protected AddSCLicense addSCLicense;
	protected ClearSCLicenses clearSCLicenses;
	protected RemoveSCLicense removeSCLicense;

	protected class ContainsSCLicense extends MappingSqlQuery {
		protected ContainsSCLicense(SCProductEntryPersistence persistence) {
			super(persistence.getDataSource(), _SQL_CONTAINSSCLICENSE);
			declareParameter(new SqlParameter(Types.BIGINT));
			declareParameter(new SqlParameter(Types.BIGINT));
			compile();
		}

		protected Object mapRow(ResultSet rs, int rowNumber)
			throws SQLException {
			return new Integer(rs.getInt("COUNT_VALUE"));
		}

		protected boolean contains(long productEntryId, long licenseId) {
			List results = execute(new Object[] {
						new Long(productEntryId), new Long(licenseId)
					});

			if (results.size() > 0) {
				Integer count = (Integer)results.get(0);

				if (count.intValue() > 0) {
					return true;
				}
			}

			return false;
		}
	}

	protected class AddSCLicense extends SqlUpdate {
		protected AddSCLicense(SCProductEntryPersistence persistence) {
			super(persistence.getDataSource(),
				"INSERT INTO SCLicenses_SCProductEntries (productEntryId, licenseId) VALUES (?, ?)");
			_persistence = persistence;
			declareParameter(new SqlParameter(Types.BIGINT));
			declareParameter(new SqlParameter(Types.BIGINT));
			compile();
		}

		protected void add(long productEntryId, long licenseId) {
			if (!_persistence.containsSCLicense.contains(productEntryId,
						licenseId)) {
				update(new Object[] {
						new Long(productEntryId), new Long(licenseId)
					});
			}
		}

		private SCProductEntryPersistence _persistence;
	}

	protected class ClearSCLicenses extends SqlUpdate {
		protected ClearSCLicenses(SCProductEntryPersistence persistence) {
			super(persistence.getDataSource(),
				"DELETE FROM SCLicenses_SCProductEntries WHERE productEntryId = ?");
			declareParameter(new SqlParameter(Types.BIGINT));
			compile();
		}

		protected void clear(long productEntryId) {
			update(new Object[] { new Long(productEntryId) });
		}
	}

	protected class RemoveSCLicense extends SqlUpdate {
		protected RemoveSCLicense(SCProductEntryPersistence persistence) {
			super(persistence.getDataSource(),
				"DELETE FROM SCLicenses_SCProductEntries WHERE productEntryId = ? AND licenseId = ?");
			declareParameter(new SqlParameter(Types.BIGINT));
			declareParameter(new SqlParameter(Types.BIGINT));
			compile();
		}

		protected void remove(long productEntryId, long licenseId) {
			update(new Object[] { new Long(productEntryId), new Long(licenseId) });
		}
	}

	private static final String _SQL_GETSCLICENSES = "SELECT {SCLicense.*} FROM SCLicense INNER JOIN SCLicenses_SCProductEntries ON (SCLicenses_SCProductEntries.licenseId = SCLicense.licenseId) WHERE (SCLicenses_SCProductEntries.productEntryId = ?)";
	private static final String _SQL_GETSCLICENSESSIZE = "SELECT COUNT(*) AS COUNT_VALUE FROM SCLicenses_SCProductEntries WHERE productEntryId = ?";
	private static final String _SQL_CONTAINSSCLICENSE = "SELECT COUNT(*) AS COUNT_VALUE FROM SCLicenses_SCProductEntries WHERE productEntryId = ? AND licenseId = ?";
	private static Log _log = LogFactory.getLog(SCProductEntryPersistence.class);
}