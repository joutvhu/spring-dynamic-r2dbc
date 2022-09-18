package org.springframework.data.r2dbc.repository.support;

import org.springframework.expression.ExpressionParser;

public class DynamicCachingExpressionParser extends CachingExpressionParser {
    public DynamicCachingExpressionParser(ExpressionParser delegate) {
        super(delegate);
    }
}
