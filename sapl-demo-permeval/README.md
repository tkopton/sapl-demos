# Submodule  sapl-demo-permeval

This submodule  makes extensive use of the [PermissionEvaluator Interface](https://docs.spring.io/spring-security/site/docs/5.0.2.BUILD-SNAPSHOT/reference/htmlsingle/#el-permission-evaluator) from [Spring Security](https://projects.spring.io/spring-security/).
The most important features of 'sapl-demo-permeval' are given below.

## sapl-spring-boot-starter

Obtaining a decision from SAPL Policies we need a `PolicyDecisionPoint`(`PDP`). A `PDP` as a `bean`  is  available as dependency for
a Spring Boot Starter Project, configured in the submodule [sapl-spring-boot-starter](https://github.com/heutelbeck/sapl-policy-engine/tree/master/sapl-spring-boot-starter)
from project <https://github.com/heutelbeck/sapl-policy-engine> .
Remote or embedded `PDP` can be integrated into a Spring Boot Project with:

```java
<dependency>
        <groupId>io.sapl</groupId>
        <artifactId>sapl-spring-boot-starter</artifactId>
        <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## sapl-spring


In conjunction with SAPL requests we need a [StandardSAPLAuthorizator](https://github.com/heutelbeck/sapl-policy-engine/blob/master/sapl-spring/src/main/java/io/sapl/spring/StandardSAPLAuthorizator.java), information about an authenticated user, objects of the domain model,
the system environment, HttpServletRequest parameters, the requested URI, et cetera,  and last but not least we need a customized
PermissionEvaluator, the [SAPLPermissionEvaluator](#link).
The submodule [sapl-spring](https://github.com/heutelbeck/sapl-policy-engine/tree/master/sapl-spring) from <https://github.com/heutelbeck/sapl-policy-engine> provides these interfaces and classes,
which  can be integrated into a Spring Boot Project with:

```java
<dependency>
        <groupId>io.sapl</groupId>
        <artifactId>sapl-spring</artifactId>
        <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Spring Features

General spring features in this submodule are:

* [Spring Boot](https://projects.spring.io/spring-boot/)
* Standard SQL database: [H2](http://www.h2database.com) (In-Memory), programmable via JPA
* [Hibernate](http://hibernate.org/)
* web interfaces (Rest, UI) with Spring MVC
* model classes (Patient, User, Relation), CrudRepositories in JPA
* [Spring Security](https://projects.spring.io/spring-security/)
* Thymeleaf

## Spring Security Features


We refer to the [Spring Security](https://projects.spring.io/spring-security/) webpage and
its [reference manual](https://docs.spring.io/spring-security/site/docs/5.0.1.BUILD-SNAPSHOT/reference/htmlsingle/).

Successfully implemented features are presented below:

### Http Security

A loginPage, logoutPage is implemented. There is a secured  REST Service. Each request needs authentication.

Example from a class [SecurityConfig.java](https://github.com/heutelbeck/sapl-demos/blob/master/sapl-demo-permeval/src/main/java/io/sapl/peembedded/config/SecurityConfig.java):


``` java
@Override
protected void configure(HttpSecurity http) throws Exception {
    LOGGER.debug("start configuring...");
    http
            .authorizeRequests()
            .anyRequest().authenticated()
            .and()
            .formLogin()
            .loginPage("/login").permitAll()
            .and()
            .logout().logoutUrl("/logout").logoutSuccessUrl("/login").permitAll()
            .and()
            .httpBasic()
            .and()
            .csrf().disable();
    http.headers().frameOptions().disable();

}
```



### @Pre and @Post Annotations 

`@Pre` and `@Post` annotations are enabled with `@EnableGlobalMethodSecurity(prePostEnabled=true)`  at an instance of   `WebSecurityConfigurerAdapter` .

``` java
@Slf4j
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled=true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
....
   }
```




### Permission Evaluator

We refer to the documentation of the  [PermissionEvaluator Interface](https://docs.spring.io/spring-security/site/docs/5.0.2.BUILD-SNAPSHOT/reference/htmlsingle/#el-permission-evaluator). 
`hasPermission()` expressions are delegated to an instance of `PermissionEvaluator` :

``` java
@PreAuthorize("hasPermission(#request, #request)")
```


The `PermissionEvaluator` interface is implemented in the [SAPLPermissionEvaluator](https://github.com/heutelbeck/sapl-policy-engine/blob/master/sapl-spring/src/main/java/io/sapl/spring/SAPLPermissionEvaluator.java), which again is enabled  as bean
in the class [PDPAutoConfiguration](https://github.com/heutelbeck/sapl-policy-engine/blob/master/sapl-spring-boot-starter/src/main/java/io/sapl/springboot/starter/PDPAutoConfiguration.java)
from submodule [sapl-spring-boot-starter](https://github.com/heutelbeck/sapl-policy-engine/tree/master/sapl-spring-boot-starter).




## SAPLPermissionEvaluator <a id="link"></a>

 
Here is an excerpt of the [SAPLPermissionEvaluator](https://github.com/heutelbeck/sapl-policy-engine/blob/master/sapl-spring/src/main/java/io/sapl/spring/SAPLPermissionEvaluator.java):

```java
public class SAPLPermissionEvaluator implements PermissionEvaluator {

    private StandardSAPLAuthorizator saplAuthorizer;

    @Autowired
    public SAPLPermissionEvaluator(StandardSAPLAuthorizator saplAuthorizer) {
        this.saplAuthorizer = saplAuthorizer;
    }

    @Override //(1.) (2.)
    public boolean hasPermission(Authentication authentication, Object targetDomainObject,
                                  Object permission) {
        return authorize(authentication, permission, targetDomainObject); 
   }

    @Override //(1.)
    public boolean hasPermission(Authentication authentication, Serializable targetId,
                                  String targetType, Object permissionText) {
        return false;
    }

    public boolean authorize(Object subject, Object action, Object resource) { 
        LOGGER.trace(
                "Entering hasPermission (Object subject, Object action,
                 Object resource) \n subject: {} \n action {} \n resource: {}",
                subject.getClass(), action.getClass(), resource.getClass());
        if (Authentication.class.isInstance(subject)
              && HttpServletRequest.class.isInstance(action)) {
            Authentication auth = (Authentication) subject;
            Subject authSubject = new AuthenticationSubject(auth);
            HttpServletRequest request = (HttpServletRequest) action;
            Action httpAction = new HttpAction(RequestMethod.valueOf(request.getMethod()));
            Resource httpResource = new HttpResource(request);
            return saplAuthorizer.authorize(authSubject, httpAction, httpResource);//(2.)
        }
        Response response = saplAuthorizer.runPolicyCheck(subject, action, resource); 
        return response.getDecision() == Decision.PERMIT; //(3.)
    }

}
```


1. In a customized PermissionEvaluator always two `hasPermission` methods have to be implemented.

2. `SAPLPermissionEvaluator` basically accepts only following _soft-wired_ expression: 
   `hasPermission(#request, #request)`.
   
3. If you want to use other _hard-wired_   `hasPermission` expressions you have to customize them to match the method  of `runPolicyCheck` from
    [StandardSAPLAuthorizator](https://github.com/heutelbeck/sapl-policy-engine/blob/master/sapl-spring/src/main/java/io/sapl/spring/StandardSAPLAuthorizator.java)
    (which is  enabled as Bean in [PDPAutoConfiguration](https://github.com/heutelbeck/sapl-policy-engine/blob/master/sapl-spring-boot-starter/src/main/java/io/sapl/springboot/starter/PDPAutoConfiguration.java)).


An example for securing the `DELETE` method from
the [RestService](https://github.com/heutelbeck/sapl-demos/blob/master/sapl-demo-permeval/src/main/java/io/sapl/peembedded/controller/RestService.java) is listed below:

```java
	@DeleteMapping("{id}")
	@PreAuthorize("hasPermission(#request, #request)") // using SaplPolicies = DOCTOR
	public void delete(@PathVariable int id, HttpServletRequest request) {
		patientenRepo.deleteById(id);
	}
```

The corresponding SAPL policy can be found in <https://github.com/heutelbeck/sapl-demos/blob/master/sapl-demo-permeval/src/main/resources/policies/httpPolicy.sapl>  and is implemented as follows:

```
policy "permit_doctor_delete_person"
permit
  action.method == "DELETE"
where
  "DOCTOR" in subject..authority;
  resource.uri =~ "/person/[0-9]+";
```