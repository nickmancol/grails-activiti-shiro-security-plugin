/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.grails.activiti.shirosecurity;

import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.interceptor.SessionFactory;

/**
 *
 * @author nickman
 */
public class ShiroSecurityUserManagerFactory  implements SessionFactory {
    public Class<?> getSessionType() {
                return org.activiti.engine.impl.persistence.entity.UserManager.class;
    }

    public Session openSession() {
                return new UserManager();
    }
}
