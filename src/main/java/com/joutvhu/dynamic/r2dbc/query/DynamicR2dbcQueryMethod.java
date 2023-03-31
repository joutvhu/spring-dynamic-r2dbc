package com.joutvhu.dynamic.r2dbc.query;

import com.joutvhu.dynamic.commons.DynamicQueryTemplate;
import com.joutvhu.dynamic.commons.DynamicQueryTemplateProvider;
import com.joutvhu.dynamic.commons.util.ApplicationContextHolder;
import com.joutvhu.dynamic.r2dbc.DynamicQuery;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.r2dbc.repository.query.R2dbcQueryMethod;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Reactive specific implementation of {@link R2dbcQueryMethod}.
 *
 * @author Giao Ho
 * @since 1.5.0
 */
public class DynamicR2dbcQueryMethod extends R2dbcQueryMethod {
    private static final Map<String, String> templateMap = new HashMap<>();

    private final Method method;
    private final DynamicQuery query;

    private DynamicQueryTemplateProvider queryTemplateProvider;
    private DynamicQueryTemplate queryTemplate;

    static {
        templateMap.put("value", "");
        templateMap.put("countQuery", "count");
        templateMap.put("countProjection", "projection");
    }

    protected DynamicR2dbcQueryMethod(
            Method method,
            RepositoryMetadata metadata,
            ProjectionFactory projectionFactory,
            MappingContext<? extends RelationalPersistentEntity<?>, ? extends RelationalPersistentProperty> mappingContext
    ) {
        super(method, metadata, projectionFactory, mappingContext);
        this.method = method;
        this.query = AnnotatedElementUtils.findMergedAnnotation(method, DynamicQuery.class);
    }

    public DynamicQueryTemplateProvider getTemplateProvider() {
        if (queryTemplateProvider == null)
            queryTemplateProvider = ApplicationContextHolder.getBean(DynamicQueryTemplateProvider.class);
        return queryTemplateProvider;
    }

    protected DynamicQueryTemplate findTemplate(String name) {
        DynamicQueryTemplateProvider provider = getTemplateProvider();
        return provider != null ? provider.findTemplate(name) : null;
    }

    protected DynamicQueryTemplate createTemplate(String name, String content) {
        DynamicQueryTemplateProvider provider = getTemplateProvider();
        return provider != null ? provider.createTemplate(name, content) : null;
    }

    protected DynamicQueryTemplate getTemplate(String name) {
        String templateName = templateMap.get(name);
        if (StringUtils.hasText(templateName)) templateName = "." + templateName;
        String templateMethodName = getMergedOrDefaultAnnotationValue("name", DynamicQuery.class, String.class);
        if (!StringUtils.hasText(templateMethodName)) templateMethodName = getTemplateKey();
        templateName = templateMethodName + templateName;
        String query = getMergedOrDefaultAnnotationValue(name, DynamicQuery.class, String.class);
        return StringUtils.hasText(query) ? createTemplate(templateName, query) : findTemplate(templateName);
    }

    @Nullable
    public DynamicQueryTemplate getQueryTemplate() {
        if (queryTemplate == null)
            queryTemplate = getTemplate("value");
        return queryTemplate;
    }

    private String getEntityName() {
        return getEntityInformation().getJavaType().getSimpleName();
    }

    private String getTemplateKey() {
        return getEntityName() + ":" + getName();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private <T> T getMergedOrDefaultAnnotationValue(String attribute, Class annotationType, Class<T> targetType) {
        if (this.query == null)
            return targetType.cast(AnnotationUtils.getDefaultValue(annotationType, attribute));
        return targetType.cast(AnnotationUtils.getValue(this.query, attribute));
    }
}
