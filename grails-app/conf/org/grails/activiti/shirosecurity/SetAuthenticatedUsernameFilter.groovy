/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.grails.activiti.shirosecurity

import org.apache.shiro.SecurityUtils

/**
 *
 * @author nickman
 */
class SetAuthenticatedUsernameFilter {
	def identityService
	
	def filters = {
		all(controller:'*', action:'*') {
			before = {
				if (SecurityUtils.subject && identityService) {
					identityService.authenticatedUserId = SecurityUtils.subject.principal
				}
			}
			after = { 
				identityService?.authenticatedUserId = null 
			}
			afterView = {
			}
		}
	}
}

