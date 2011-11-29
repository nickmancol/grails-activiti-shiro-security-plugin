/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.grails.activiti.shirosecurity

import org.activiti.engine.identity.Group
import org.activiti.engine.identity.User
import org.activiti.engine.identity.UserQuery
import org.activiti.engine.impl.Page
import org.activiti.engine.impl.UserQueryImpl
import org.activiti.engine.impl.context.Context
import org.activiti.engine.impl.persistence.entity.UserEntity
import org.activiti.engine.impl.persistence.entity.IdentityInfoEntity

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.commons.ApplicationHolder as AH
import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH
import org.codehaus.groovy.grails.web.pages.FastStringWriter

/**
 *
 * @author nickman
 */
class UserManager extends org.activiti.engine.impl.persistence.entity.UserManager {
	static final Log LOG = LogFactory.getLog(UserManager.class)
	
	User createNewUser(String userId) {
		throw new UnsupportedOperationException("Please use ${getUserDomainClassName()}.save() to create User.")
	}
		
	void insertUser(User user) {
		throw new UnsupportedOperationException("Please use ${getUserDomainClassName()}.save() to insert User.")
	}
	
	void updateUser(User updatedUser) {
		throw new UnsupportedOperationException("Please use ${getUserDomainClassName()}.save() to update User.")
	}
	
	void deleteUser(String userId) {
		throw new UnsupportedOperationException("Please use ${getUserDomainClassName()}.delete() to delete User.")
	}
	
	UserEntity findUserById(String userId) {
		LOG.debug "findUserById ($userId)"
		User user = getUserDomainClass()."findBy${getUsernameClassName()}"(userId)
		return user
	}
	
	List<User> findUserByQueryCriteria(Object query, Page page) {
		LOG.debug "findUserByQueryCriteria (${query.class.name}, $page)"
		List<User> users
		String queryString = createUserQueryString(query)
		LOG.debug "queryString = $queryString"
		if (page) { // listPage()
			users = getUserDomainClass().findAll(queryString, [offset:page.firstResult, max:page.maxResults])
		} else { // list()
			users = getUserDomainClass().findAll(queryString, Collections.emptyMap())
		}
		LOG.debug "query.groupId = ${query.groupId}"
		if (users && query.groupId) {
			users = users.findAll { it.authorities*.id.contains(query.groupId) }
		}
		return users
	}
	
	long findUserCountByQueryCriteria(Object query) {
		LOG.debug "findUserCountByQueryCriteria (${query.class.name})"
		String queryString = createUserQueryString(query)
		LOG.debug "queryString = $queryString"
		return getUserDomainClass().executeQuery("select count(u) ${queryString}")[0]
	}
	
	List<Group> findGroupsByUser(String userId) {
		LOG.debug "findGroupsByUser (${userId})"
		def user = getUserDomainClass()."findBy${getUsernameClassName()}"(userId)
		def groups = user?.authorities.toList()
		return groups
	}
	
	UserQuery createNewUserQuery() {
		return new UserQueryImpl(Context.getProcessEngineConfiguration().getCommandExecutorTxRequired())
	}
	
	IdentityInfoEntity findUserInfoByUserIdAndKey(String userId, String key) {
		throw new UnsupportedOperationException()
	}
	
	List<String> findUserInfoKeysByUserIdAndType(String userId, String type) {
		throw new UnsupportedOperationException()
	}
	
	private String createUserQueryString(Object query) {
		FastStringWriter queryString = new FastStringWriter()
		queryString << "from ${getUserDomainClassName()} as u where 1=1"
		if (query.id)
			queryString << " and u.${getUsernamePropertyName()}='${query.id}'"
		
		if (query.firstName) {
			queryString << " and u.firstName = '${query.firstName}'"
		}
		
		if (query.firstNameLike) {
			queryString << " and u.firstName like '${query.firstNameLike}'"
		}
		
		if (query.lastName) {
			queryString << " and u.lastName = '${query.lastName}'"
		}
		
		if (query.lastNameLike) {
			queryString << " and u.lastName like '${query.lastNameLike}'"
		}
		
		if (query.email) {
			queryString << " and u.email = '${query.email}'"
		}
		
		if (query.emailLike) {
			queryString << " and u.email like '${query.emailLike}'"
		}
		
		if (query.orderBy) {
			String orderBy = query.orderBy.toLowerCase().replace('_', '')
			orderBy = orderBy.replace("last", "lastName")
			orderBy = orderBy.replace("first", "firstName")
			queryString << " order by ${orderBy}"
		}
		return queryString.toString()
	}
	
	private getUsernamePropertyName() {
		return CH.config.securityConfig.userLookup.usernamePropertyName
	}
		
	private getUserDomainClassName() {
		return CH.config.securityConfig.userLookup.userDomainClassName
	}
	
	private getUserDomainClass() {
		return AH.application.getDomainClass(getUserDomainClassName()).clazz
	}
}

