Este plugin integra http://www.activiti.org con http://shiro.apache.org/|Shiro Security en una 
aplicación http://shiro.apache.org. 
Se encuentra basado en el trabajo de Lim Chee Kin (plugins para http://code.google.com/p/grails-activiti-plugin y 
http://code.google.com/p/grails-activiti-spring-security-plugin/) y 
Peter Ledbrook (plugin para http://grails.org/plugin/shiro). 
Apache Software License v2.0.

## Modelos de dominio
Activiti usa un modelo de seguridad con tres entidades (User, Role y el enlace entre las dos), para ello define dos interfaces que deben ser implementadas por los modelos de dominio [[http://activiti.org/javadocs/org/activiti/engine/identity/User.html|User]] y [[http://activiti.org/javadocs/org/activiti/engine/identity/Group.html|Group]], la tercera es la asociacion m:n entre ellas (UserRole).

El siguiente modelo es una ligera adaptación del modelo de dominio base de Spring Security: 

{{https://docs.google.com/drawings/pub?id=1hd15f_YZQGY__MTgHIvXE_LZQU7RiNfGYqmh_LWNn2g&w=490&h=594|Modelo}}

No todos los atributos de las clases son necesarios (solmente los definidos por las interfaces de Activiti y en ShiroUser //passwordChangeRequiredOnNextLogon//). Éstas clases se pueden crear a partir del plugin de shiro para grails.

El plugin usa convenciones para generar las consultas dinámicas de permisos, por tanto es neceserio que en **UserRole** los nombres de los atributos user y role conincidan con los nombres de las clases User y Role usadas, en este caso ShiroUser y ShiroRole.

Activiti necesita realizar consultas basado en el nombre de usuario y el identificador de grupo por ello los atributos //id// de los modelos son de tipo String y deben ser mapeados de la siguiente manera:

Para User
```groovy

static mapping = {
		password column: '`password`'
		id generator: 'uuid'
	}
```
Para Group
```groovy

static mapping = {
                id generator: 'assigned'
	}
```
Para UserRole
```groovy

static mapping = {
		id composite: ['shiroRole', 'shiroUser']
		version false
	}
```
Lo que implica que los identificadores de los roles deben ser asignados, éstos identificadores deben contener los nombres de los roles usados en las expresiones de selección de psoibles usuarios durante la definición de tareas del proceso, el siguiente fragmento es extraído del proceso VacationRequest:

```xml

<userTask id="initiateVacationRequest" activiti:formKey="/vacationRequest/create"
			name="Initiate Vacation Request">
			<documentation>Vacation request by ${username}</documentation>
			<potentialOwner>
				<resourceAssignmentExpression>
					<formalExpression>user</formalExpression>
				</resourceAssignmentExpression>
			</potentialOwner>
		</userTask>
		<sequenceFlow id="flow1" targetRef="handleVacationRequest"
			sourceRef="initiateVacationRequest" />
		<userTask id="handleVacationRequest" activiti:formKey="/vacationRequest/approval"
			name="Handle Vacation Request">
			<documentation>Vacation request by ${username}</documentation>
			<potentialOwner>
				<resourceAssignmentExpression>
					<formalExpression>management</formalExpression>
				</resourceAssignmentExpression>
			</potentialOwner>
		</userTask>
```

debido a que el Activiti realiza la búsqueda de posibles usuarios basándose en las expresiones formales (//formalExpression//) del xml del proceso.

## Instalación

* Descargar el plugin desde https://bitbucket.org/Nickmancol/grails-activiti-shiro-security-plugin/downloads/grails-activiti-shiro-0.1.1.zip

* Instalar el plugin en la aplicacion
```
grails install-plugin /path/plgin/grails-activiti-shiro-0.1.zip
```

## Inicialización

El plugin implementa las interfaces [[http://www.activiti.org/javadocs/org/activiti/engine/impl/persistence/entity/GroupManager.html| GroupManager]] y [[http://www.activiti.org/javadocs/org/activiti/engine/impl/persistence/entity/UserManager.html|UserManager]] de Activiti y configura el [[http://www.activiti.org/javadocs/org/activiti/engine/IdentityService.html|IdentityService]] para usar las implementaciones, usa las propiedades de //**Config.groovy**// (Ver instalación) para generar las consultas dinámicas.

Es necesario registrar en la sesión de usuario el nombre de usuario para que Activiti busque los grupos y usuarios posibles para las tareas para ello el plugin implementa el método //**attachUsername2Session()**// del servicio **ShiroActivitiSessionService**, este servicio debe ser inyectado en la aplicación cliente y ser llamado una vez se inicie sesión, es posible realizar esto con un listener de Shiro o en el controlador de grails que realiza la autenticación de usuario.

* Configurar los nombres de los modelos y propiedades usadas en //**Config.groovy**// 
```groovy
securityConfig.userLookup.usernamePropertyName = 'username' //username property
securityConfig.userLookup.userDomainClassName = 'ShiroUser' //domain classname without package
securityConfig.userLookup.authorityJoinClassName = 'UserRole' //domain classname without package
securityConfig.userLookup.authority.className = 'ShiroRole' //domain classname without package
```

* Declarar el servicio //**ShiroActivitiSessionService**// y llamar al método //**attachUsername2Session()**// una vez se haya iniciado sesión (probablemente en el //**AuthController**// de Shiro - linea 45 -)

## Lista de Todas las Tareas

El plugin de Activiti para Grails contiene tres vistas de tareas (myTasks, unassignedTasks y allTasks), en esta última vista (en la versión 5.7 del plugin) por defecto no se presentan los usuarios que pueden ejecutar la tareas, para ello es necesario modificar en //**allTaskList.gsp**// el scriptlet que inicia en la linea 66 por el siguiente código:

```java

def users = []
																		def userList=[:]
																		def userIds = ActivitiUtils.activitiService.getCandidateUserIds(taskInstance.id)
																		def groups
																		def groupIds
																		if (!applicationContext.getBean('pluginManager').hasGrailsPlugin('activitiSpringSecurity')) {
																		
																		def User = grailsApplication.getDomainClass(grailsApplication.config.securityConfig.userLookup.userDomainClassName).clazz
																		  users = User."findAllBy${GrailsNameUtils.getClassNameRepresentation(grailsApplication.config.securityConfig.userLookup.usernamePropertyName)}InList"(userIds)
																		
																			for (id in userIds) {
									                        groups = ActivitiUtils.identityService.createGroupQuery().groupMember(id).orderByGroupId().asc().list()
									                        
									                                        }		       
																		} else {
																		  def User = grailsApplication.getDomainClass(grailsApplication.config.grails.plugins.springsecurity.userLookup.userDomainClassName).clazz
																		  users = User."findAllBy${GrailsNameUtils.getClassNameRepresentation(grailsApplication.config.grails.plugins.springsecurity.userLookup.usernamePropertyName)}InList"(userIds)
																			
																		}
																		for (user in users) {
															          groups = ActivitiUtils.identityService.createGroupQuery().groupMember(user.id).orderByGroupId().asc().list()
															          groupIds = groups?" ${groups.collect{it.name}}":""
																				userList[user.username]="${user.username}${groupIds}"
																			}
```
### Version 0.1
29/11/2011 - Versión inicial

* Implementada inicialización de IdentityService con GroupManager y UserManager
* Implementado servicio de enlace de nombre de usuario en sesion
* Prueba de integración con Grails-Activiti VacationRequest
