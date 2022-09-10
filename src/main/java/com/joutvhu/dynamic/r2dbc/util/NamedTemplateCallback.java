package com.joutvhu.dynamic.r2dbc.util;

/**
 * Callback when found a query template {@link freemarker.template.Template}
 *
 * @author Giao Ho
 * @since 2.x.1
 */
public interface NamedTemplateCallback {
    void process(String templateName, String content);
}
