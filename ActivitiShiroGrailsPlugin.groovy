/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH

/**
 *
 * @author nickman
 */
class ActivitiShiroGrailsPlugin {

    def version = "0.1.1"
    def grailsVersion = "1.3.5  > *"
    // the other plugins this plugin depends on
    def dependsOn = [shiro: '1.1.3 > *', activiti: '5.5 > *']
    def author = "Nicolas Bohorquez Gutierrez (@nickmancoi)"
    def authorEmail = "nicolas.bg@gmail.com"
    def title = "This plugin integrates Shiro Security to Activiti."
    def description = '''
        Activiti plugin brings the power of bpmn to grails, Shiro security enables an elegant way to perform 
authentication and authorization in your app, this plugin provides the basic glue necessary between them
'''
    def documentation = "https://bitbucket.org/Nickmancol/grails-activiti-shiro-security-plugin"
    
    def doWithSpring = {
		def disabledActiviti = System.getProperty("disabledActiviti")
		
		if (!disabledActiviti && !CH.config.activiti.disabled) {
			println "Activiti Process Engine with Shiro Security Initialization ..."
			userManagerFactory(org.grails.activiti.shirosecurity.ShiroSecurityUserManagerFactory)
			groupManagerFactory(org.grails.activiti.shirosecurity.ShiroSecurityGroupManagerFactory)
			processEngineConfiguration(org.activiti.spring.SpringProcessEngineConfiguration) {
				processEngineName = CH.config.activiti.processEngineName?:org.grails.activiti.ActivitiConstants.DEFAULT_PROCESS_ENGINE_NAME
				databaseType = CH.config.activiti.databaseType?:org.grails.activiti.ActivitiConstants.DEFAULT_DATABASE_TYPE
				databaseSchemaUpdate = CH.config.activiti.databaseSchemaUpdate ? CH.config.activiti.databaseSchemaUpdate.toString() : org.grails.activiti.ActivitiConstants.DEFAULT_DATABASE_SCHEMA_UPDATE
				deploymentName = CH.config.activiti.deploymentName?:org.grails.activiti.ActivitiConstants.DEFAULT_DEPLOYMENT_NAME
				deploymentResources = CH.config.activiti.deploymentResources?:org.grails.activiti.ActivitiConstants.DEFAULT_DEPLOYMENT_RESOURCES
				jobExecutorActivate = CH.config.activiti.jobExecutorActivate?:org.grails.activiti.ActivitiConstants.DEFAULT_JOB_EXECUTOR_ACTIVATE
				history = CH.config.activiti.history?:org.grails.activiti.ActivitiConstants.DEFAULT_HISTORY
				mailServerHost = CH.config.activiti.mailServerHost?:org.grails.activiti.ActivitiConstants.DEFAULT_MAIL_SERVER_HOST
				mailServerPort = CH.config.activiti.mailServerPort?:org.grails.activiti.ActivitiConstants.DEFAULT_MAIL_SERVER_PORT
				mailServerUsername = CH.config.activiti.mailServerUsername
				mailServerPassword = CH.config.activiti.mailServerPassword
				mailServerDefaultFrom = CH.config.activiti.mailServerDefaultFrom?:org.grails.activiti.ActivitiConstants.DEFAULT_MAIL_SERVER_FROM
				customSessionFactories = [ref("userManagerFactory"), ref("groupManagerFactory")]
				dataSource = ref("dataSource")
				transactionManager = ref("transactionManager")
			}
			
			processEngine(org.activiti.spring.ProcessEngineFactoryBean) { processEngineConfiguration = ref("processEngineConfiguration") }
			
			runtimeService(processEngine:"getRuntimeService")
			repositoryService(processEngine:"getRepositoryService")
			taskService(processEngine:"getTaskService")
			managementService(processEngine:"getManagementService")
			identityService(processEngine:"getIdentityService")
			historyService(processEngine:"getHistoryService")
			formService(processEngine:"getFormService")
			
			activitiService(org.grails.activiti.ActivitiService) {
				runtimeService = ref("runtimeService")
				taskService = ref("taskService")
				identityService = ref("identityService")
				formService = ref("formService")
			}
		}
	}
    
}

