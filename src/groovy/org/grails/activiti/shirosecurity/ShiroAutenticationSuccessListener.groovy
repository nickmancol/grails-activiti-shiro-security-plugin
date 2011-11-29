/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.grails.activiti.shirosecurity

import org.apache.shiro.session.SessionListenerAdapter
import org.apache.shiro.session.Session
import org.apache.shiro.SecurityUtils

/**
 *
 * @author nickman
 */
class ShiroAutenticationSuccessListener extends SessionListenerAdapter {
	
    void onStart(Session session) {
        if(SecurityUtils.subject) {
            def sessionUsernameKey = CH.config.activiti.sessionUsernameKey?:ActivitiConstants.DEFAULT_SESSION_USERNAME_KEY
            session.setAttribute(sessionUsernameKey , SecurityUtils.subject?.principal)
        }
    }
}

