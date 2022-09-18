package com.joutvhu.dynamic.r2dbc.query;

import com.joutvhu.dynamic.r2dbc.DynamicQuery;
import com.joutvhu.dynamic.r2dbc.DynamicQueryTemplates;
import com.joutvhu.dynamic.r2dbc.util.ApplicationContextHolder;
import com.joutvhu.dynamic.r2dbc.util.TemplateConfiguration;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.jpa.provider.QueryExtractor;
import org.springframework.data.jpa.repository.query.JpaQueryMethod;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.r2dbc.repository.query.R2dbcQueryMethod;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.util.Lazy;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * JPA specific extension of {@link JpaQueryMethod}.
 *
 * @author Giao Ho
 * @since 2.x.1
 */
public class DynamicR2dbcQueryMethod extends R2dbcQueryMethod {
    private static final Map<String, String> templateMap = new HashMap<>();
    private static Configuration cfg = TemplateConfiguration.instanceWithDefault().configuration();

    private final Method method;
    private final Lazy<Boolean> isNativeQuery;

    private Template queryTemplate;
    private Template countQueryTemplate;
    private Template countProjectionTemplate;

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
        this.isNativeQuery = Lazy
                .of(() -> getMergedOrDefaultAnnotationValue("nativeQuery", DynamicQuery.class, Boolean.class));
    }

    protected Template findTemplate(String name) {
        DynamicQueryTemplates queryTemplates = ApplicationContextHolder.getBean(DynamicQueryTemplates.class);
        return queryTemplates != null ? queryTemplates.findTemplate(name) : null;
    }

    protected Template createTemplate(String name, String content) {
        try {
            return new Template(name, content, cfg);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected Template getTemplate(String name) {
        String templateName = templateMap.get(name);
        if (StringUtils.hasText(templateName)) templateName = "." + templateName;
        templateName = getTemplateKey() + templateName;
        String query = getMergedOrDefaultAnnotationValue(name, DynamicQuery.class, String.class);
        queryTemplate = StringUtils.hasText(query) ? createTemplate(templateName, query) : findTemplate(templateName);
        return queryTemplate;
    }

    public boolean isNativeQuery() {
        return this.isNativeQuery.get();
    }

    @Nullable
    public Template getQueryTemplate() {
        if (queryTemplate == null)
            queryTemplate = getTemplate("value");
        return queryTemplate;
    }

    @Nullable
    public Template getCountQueryTemplate() {
        if (countQueryTemplate == null)
            countQueryTemplate = getTemplate("countQuery");
        return countQueryTemplate;
    }

    @Nullable
    public Template getCountProjectionTemplate() {
        if (countProjectionTemplate == null)
            countProjectionTemplate = getTemplate("countProjection");
        return countProjectionTemplate;
    }

    private String getEntityName() {
        return getEntityInformation().getJavaType().getSimpleName();
    }

    private String getTemplateKey() {
        return getEntityName() + ":" + getName();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private <T> T getMergedOrDefaultAnnotationValue(String attribute, Class annotationType, Class<T> targetType) {
        Annotation annotation = AnnotatedElementUtils.findMergedAnnotation(method, annotationType);
        if (annotation == null)
            return targetType.cast(AnnotationUtils.getDefaultValue(annotationType, attribute));
        return targetType.cast(AnnotationUtils.getValue(annotation, attribute));
    }
}
