package com.joutvhu.dynamic.r2dbc.query;

import com.joutvhu.dynamic.r2dbc.DynamicQuery;
import freemarker.template.Template;
import org.springframework.data.jpa.repository.query.*;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.ReactiveDataAccessStrategy;
import org.springframework.data.r2dbc.repository.query.AbstractR2dbcQuery;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.query.*;
import org.springframework.data.util.Lazy;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.lang.Nullable;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.Tuple;
import java.util.Map;

/**
 * {@link RepositoryQuery} implementation that inspects a {@link DynamicR2dbcQueryMethod}
 * for the existence of an {@link DynamicQuery} annotation and creates a JPA {@link DynamicQuery} from it.
 *
 * @author Giao Ho
 * @since 2.x.1
 */
public class DynamicR2dbcRepositoryQuery extends AbstractR2dbcQuery {
    private static final SpelExpressionParser PARSER = new SpelExpressionParser();
    private final DynamicR2dbcQueryMethod method;
    private final NamedQueries namedQueries;
    private final R2dbcEntityOperations entityOperations;
    private final R2dbcConverter converter;
    private final ReactiveDataAccessStrategy dataAccessStrategy;
    private final ExpressionParser expressionParser;
    private final QueryMethodEvaluationContextProvider evaluationContextProvider;
    private final DynamicQueryMetadataCache metadataCache = new DynamicQueryMetadataCache();

    private JpaParametersParameterAccessor accessor;
    private DynamicBasedStringQuery query;
    private DynamicBasedStringQuery countQuery;
    private Lazy<ParameterBinder> parameterBinder;

    public DynamicR2dbcRepositoryQuery(
            DynamicR2dbcQueryMethod method,
            NamedQueries namedQueries,
            R2dbcEntityOperations entityOperations,
            R2dbcConverter converter,
            ReactiveDataAccessStrategy dataAccessStrategy,
            ExpressionParser expressionParser,
            ReactiveQueryMethodEvaluationContextProvider evaluationContextProvider
    ) {
        super(method, entityOperations, converter);

        this.method = method;
        this.namedQueries = namedQueries;
        this.entityOperations = entityOperations;
        this.converter = converter;
        this.dataAccessStrategy = dataAccessStrategy;
        this.expressionParser = expressionParser;
        this.evaluationContextProvider = evaluationContextProvider;
    }

    protected String buildQuery(Template template, JpaParametersParameterAccessor accessor) {
        try {
            if (template != null) {
                Map<String, Object> model = DynamicJpaParameterAccessor.of(accessor).getParamModel();
                String queryString = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
                queryString = queryString
                        .replaceAll("\n", " ")
                        .replaceAll("\t", " ")
                        .replaceAll(" +", " ")
                        .trim();
                return queryString.isEmpty() ? null : queryString;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected ParameterBinder createBinder() {
        return DynamicParameterBinderFactory.createQueryAwareBinder(getQueryMethod().getParameters(), query, PARSER,
                evaluationContextProvider);
    }

    protected DynamicBasedStringQuery setAccessor(JpaParametersParameterAccessor accessor) {
        if (query == null || this.accessor != accessor) {
            this.accessor = accessor;
            String queryString = buildQuery(method.getQueryTemplate(), accessor);
            query = new DynamicBasedStringQuery(queryString, method.getEntityInformation(), PARSER, method.isNativeQuery());
            parameterBinder = Lazy.of(createBinder());

            validateQuery();
        }
        return query;
    }

    @Override
    protected Query doCreateQuery(JpaParametersParameterAccessor accessor) {
        setAccessor(accessor);

        String sortedQueryString = QueryUtils
                .applySorting(query.getQueryString(), accessor.getSort(), query.getAlias());
        ResultProcessor processor = getQueryMethod().getResultProcessor().withDynamicProjection(accessor);

        Query query = createJpaQuery(sortedQueryString, processor.getReturnedType());

        return metadataCache.bindAndPrepare(sortedQueryString, query, accessor, parameterBinder);
    }

    @Override
    protected Query doCreateCountQuery(JpaParametersParameterAccessor accessor) {
        setAccessor(accessor);

        String countQueryString = buildQuery(method.getCountQueryTemplate(), accessor);
        String countProjectionString = buildQuery(method.getCountProjectionTemplate(), accessor);

        countQuery = new DynamicBasedStringQuery(
                query.deriveCountQuery(countQueryString, countProjectionString),
                method.getEntityInformation(), PARSER, method.isNativeQuery());

        if (!method.isNativeQuery() && method.isPageQuery())
            validateQuery(countQuery.getQueryString(), "Count query validation failed for method %s!");

        String queryString = countQuery.getQueryString();
        EntityManager em = getEntityManager();

        Query query = method.isNativeQuery() ? em.createNativeQuery(queryString) :
                em.createQuery(queryString, Long.class);

        metadataCache.bind(queryString, query, accessor, parameterBinder);

        return query;
    }

    /**
     * Creates an appropriate JPA query from an {@link EntityManager} according to the current {@link DynamicR2dbcRepositoryQuery}
     * type.
     *
     * @param queryString  is query
     * @param returnedType of method
     * @return a {@link Query}
     */
    protected Query createJpaQuery(String queryString, ReturnedType returnedType) {
        EntityManager em = getEntityManager();
        if (method.isNativeQuery()) {
            Class<?> type = getTypeToQueryFor(returnedType);
            return type == null ? em.createNativeQuery(queryString) : em.createNativeQuery(queryString, type);
        } else {
            if (this.query.hasConstructorExpression() || this.query.isDefaultProjection())
                return em.createQuery(queryString);
            Class<?> typeToRead = getTypeToRead(returnedType);
            return typeToRead == null ? em.createQuery(queryString) : em.createQuery(queryString, typeToRead);
        }
    }

    @Nullable
    private Class<?> getTypeToQueryFor(ReturnedType returnedType) {
        Class<?> result = getQueryMethod().isQueryForEntity() ? returnedType.getDomainType() : null;
        if (this.query.hasConstructorExpression() || this.query.isDefaultProjection()) return result;
        return returnedType.isProjecting() && !getMetamodel()
                .isJpaManaged(returnedType.getReturnedType()) ? Tuple.class : result;
    }

    private void validateQuery() {
        if (method.isNativeQuery()) {
            Parameters<?, ?> parameters = method.getParameters();

            if (parameters.hasSortParameter() && !query.getQueryString().contains("#sort")) {
                throw new InvalidJpaQueryMethodException(String
                        .format("Cannot use native queries with dynamic sorting in method %s", method));
            }
        } else validateQuery(query.getQueryString(), "Validation failed for query for method %s!");
    }

    /**
     * Validates the given query for syntactical correctness.
     */
    private void validateQuery(String query, String errorMessage) {
        EntityManager validatingEm = null;
        if (getQueryMethod().isProcedureQuery()) return;

        try {
            validatingEm = getEntityManager().getEntityManagerFactory().createEntityManager();
            validatingEm.createQuery(query);

        } catch (RuntimeException e) {
            // Needed as there's ambiguities in how an invalid query string shall be expressed by the persistence provider
            // https://java.net/projects/jpa-spec/lists/jsr338-experts/archive/2012-07/message/17
            throw new IllegalArgumentException(String.format(errorMessage, method), e);
        } finally {
            if (validatingEm != null) {
                validatingEm.close();
            }
        }
    }
}
