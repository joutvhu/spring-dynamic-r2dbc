# Spring Dynamic R2DBC

The Spring Dynamic R2DBC will make it easy to implement dynamic queries with R2DBC.

## How to use?

- Add dependency

```groovy
implementation 'com.github.joutvhu:spring-dynamic-r2dbc:1.5.0'
```

```xml
<dependency>
    <groupId>com.github.joutvhu</groupId>
    <artifactId>spring-dynamic-r2dbc</artifactId>
    <version>1.5.0</version>
</dependency>
```

- Please choose the _spring-dynamic-r2dbc_ version appropriate with your spring version.

| spring-boot version | spring-dynamic-r2dbc version |
|:----------:|:-------------:|
| 2.6.x | 1.4.0 |
| 2.7.x | 1.5.0 |


- To use the dynamic query, you need to set the r2dbc repository's `repositoryFactoryBeanClass` property to `DynamicR2dbcRepositoryFactoryBean.class`.

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

- You need to configure a `DynamicQueryTemplates` bean to be loadable external query templates.

```java
@Bean
public DynamicQueryTemplates dynamicQueryTemplates() {
    DynamicQueryTemplates queryTemplates = new DynamicQueryTemplates();
    queryTemplates.setTemplateLocation("classpath:/query");
    queryTemplates.setSuffix(".dsql");
    return queryTemplates;
}
```

- Each template will start with a template name definition line. The template name definition line must be start with two dash characters (`--`). The template name will have the following syntax.

  ```
  entityName:methodName
  ```

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
```

- If you don't specify the query template inside the `@DynamicQuery` annotation, `DynamicR2dbcRepositoryQuery` will find it from the external query files.

```java
public interface UserRepository extends ReactiveCrudRepository<User, Long> {
    @DynamicQuery
    Flux<User> findUserByNames(Long firstName, String lastName);

    @Query(value = "select * from USER where FIRST_NAME = :firstName")
    Flux<User> findByFirstName(String firstName);

    @DynamicQuery
    Flux<Long> searchIdsByName(String name);
}
```
