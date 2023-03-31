# Spring Dynamic R2DBC

The Spring Dynamic R2DBC will make it easy to implement dynamic queries with R2DBC.

## How to use?

### Install dependency

- Add dependency

```groovy
implementation 'com.github.joutvhu:spring-dynamic-r2dbc:3.0.2'
```

```xml
<dependency>
    <groupId>com.github.joutvhu</groupId>
    <artifactId>spring-dynamic-r2dbc</artifactId>
    <version>3.0.2</version>
</dependency>
```

- Please choose the _Spring Dynamic R2DBC_ version appropriate with your spring version.

  | Spring Boot version | Spring Dynamic R2DBC version |
  |:----------:|:-------------:|
  | 2.6.x | 1.4.2 |
  | 2.7.x | 1.5.2 |
  | 3.0.x | 3.0.2 |

Also, you have to choose a [Dynamic Query Template Provider](https://github.com/joutvhu/spring-dynamic-commons#dynamic-query-template-provider) to use,
the Dynamic Query Template Provider will decide the style you write dynamic query template.

In this document, I will use [Spring Dynamic Freemarker](https://github.com/joutvhu/spring-dynamic-freemarker).
If you migrated from a lower version, you should use it.

```groovy
implementation 'com.github.joutvhu:spring-dynamic-freemarker:1.0.0'
```

```xml
<dependency>
    <groupId>com.github.joutvhu</groupId>
    <artifactId>spring-dynamic-freemarker</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Configuration

- First you need to create a bean of `DynamicQueryTemplateProvider`, that depending on which the Dynamic Query Template Provider you are using.

```java
@Bean
public DynamicQueryTemplateProvider dynamicQueryTemplateProvider() {
    FreemarkerQueryTemplateProvider provider = new FreemarkerQueryTemplateProvider();
    provider.setTemplateLocation("classpath:/query");
    provider.setSuffix(".dsql");
    return provider;
}
```

- Next, you need to set the r2dbc repository's `repositoryFactoryBeanClass` property to `DynamicR2dbcRepositoryFactoryBean.class`.

```java
@EnableR2dbcRepositories(repositoryFactoryBeanClass = DynamicR2dbcRepositoryFactoryBean.class)
```

### Dynamic query

- Use annotation `@DynamicQuery` to define dynamic queries.

```java
public interface UserRepository extends R2dbcRepository<User, Long> {
    @DynamicQuery(
        value = "select * from USER where FIRST_NAME = :firstName\n" +
            "<#if lastName?has_content>\n" +
            "  and LAST_NAME = :lastName\n" +
            "</#if>"
    )
    Flux<User> findUserByNames(Long firstName, String lastName);

    @Query(value = "select * from USER where FIRST_NAME = :firstName")
    Flux<User> findByFirstName(String firstName);

    @DynamicQuery(
        value = "select USER_ID from USER\n" +
            "<#if name??>\n" +
            "  where concat(FIRST_NAME, ' ', LAST_NAME) like %:name%\n" +
            "</#if>"
    )
    Flux<Long> searchIdsByName(String name);

    @DynamicQuery(
        value = "select * from USER\n" +
            "<#if role??>\n" +
            "  where ROLE = :role\n" +
            "</#if>"
    )
    Flux<User> findByRole(String role);
}
```

### Load query template files

- If you do not specify the query template on the `@DynamicQuery` annotation.
  The `DynamicQueryTemplateProvider` will find them from external template files based on the `TemplateLocation` and `Suffix` that you specify in the provider.

- If you don't want to load the template from external template files you can use the following code `provider.setSuffix(null);`.

- Each template will start with a template name definition line. The template name definition line must be start with two dash characters (`--`). The template name will have the following syntax.

  ```
  queryMethodName
  ```

  - `queryMethodName` can be provided through field `@DynamicQuery.name`. If `@DynamicQuery.name` is not provided, `queryMethodName` will be `entityName:methodName` where `entityName` is entity class name, `methodName` is query method name

- Query templates (Ex: `resoucers/query/user-query.dsql`)

```sql
--User:findUserByNames
select * from USER where FIRST_NAME = :firstName
<#if lastName?has_content>
  and LAST_NAME = :lastName
</#if>

-- User:searchIdsByName
select USER_ID from USER
<#if name??>
  where concat(FIRST_NAME, ' ', LAST_NAME) like %:name%
</#if>

-- get_user_by_username_and_email
select * from USER
<@where>
  <#if username??>
    and USERNAME = :username
  </#if>
  <#if email??>
    and EMAIL = :email
  </#if>
</@where>
```

- Now you don't need to specify the query template on `@DynamicQuery` annotation.

```java
public interface UserRepository extends ReactiveCrudRepository<User, Long> {
    @DynamicQuery
    Flux<User> findUserByNames(Long firstName, String lastName);

    @Query(value = "select * from USER where FIRST_NAME = :firstName")
    Flux<User> findByFirstName(String firstName);

    @DynamicQuery
    Flux<Long> searchIdsByName(String name);

    @DynamicQuery(name = "get_user_by_username_and_email")
    Flux<User> getUserWithUsernameAndEmail(String username, String email);
}
```
