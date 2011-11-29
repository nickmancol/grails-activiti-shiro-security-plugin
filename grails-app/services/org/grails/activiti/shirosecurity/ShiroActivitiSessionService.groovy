package org.grails.activiti.shirosecurity

import org.apache.shiro.SecurityUtils
import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH

class ShiroActivitiSessionService {

    /**
     * Attaches the username of the logged user into the session
     **/
    def attachUsername2Session() {
        def sessionUsernameKey = CH.config.activiti.sessionUsernameKey?:org.grails.activiti.ActivitiConstants.DEFAULT_SESSION_USERNAME_KEY
         SecurityUtils.subject.session.setAttribute(sessionUsernameKey , SecurityUtils.subject?.principal)
    }
}
