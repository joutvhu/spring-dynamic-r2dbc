package org.springframework.data.r2dbc.repository.query;

import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.ReactiveDataAccessStrategy;
import org.springframework.data.r2dbc.dialect.BindTargetBinder;
import org.springframework.data.relational.repository.query.RelationalParameterAccessor;
import org.springframework.data.repository.query.ReactiveQueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.ResultProcessor;
import org.springframework.data.spel.ExpressionDependencies;
import org.springframework.expression.ExpressionParser;
import org.springframework.r2dbc.core.Parameter;
import org.springframework.r2dbc.core.PreparedOperation;
import org.springframework.r2dbc.core.binding.BindTarget;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class DynamicStringBasedR2dbcQuery extends AbstractR2dbcQuery {
    protected final ReactiveDataAccessStrategy dataAccessStrategy;
    protected final ExpressionParser expressionParser;
    protected final ReactiveQueryMethodEvaluationContextProvider evaluationContextProvider;

    /**
     * Creates a new {@link AbstractR2dbcQuery} from the given {@link R2dbcQueryMethod} and {@link R2dbcEntityOperations}.
     *
     * @param method           must not be {@literal null}.
     * @param entityOperations must not be {@literal null}.
     * @param converter        must not be {@literal null}.
     * @since 1.4
     */
    public DynamicStringBasedR2dbcQuery(
            R2dbcQueryMethod method,
            R2dbcEntityOperations entityOperations,
            R2dbcConverter converter,
            ReactiveDataAccessStrategy dataAccessStrategy,
            ExpressionParser expressionParser,
            ReactiveQueryMethodEvaluationContextProvider evaluationContextProvider
    ) {
        super(method, entityOperations, converter);
        this.dataAccessStrategy = dataAccessStrategy;
        this.expressionParser = expressionParser;
        this.evaluationContextProvider = evaluationContextProvider;
    }

    protected Mono<PreparedOperation<?>> createQuery(String queryString, RelationalParameterAccessor accessor) {
        ExpressionQuery expressionQuery = ExpressionQuery.create(queryString);
        ExpressionDependencies expressionDependencies = createExpressionDependencies(expressionQuery);
        ExpressionEvaluatingParameterBinder binder = new ExpressionEvaluatingParameterBinder(expressionQuery, dataAccessStrategy);
        return getSpelEvaluator(accessor, expressionDependencies)
                .map(evaluator -> new ExpandedQuery(expressionQuery, binder, accessor, evaluator));
    }

    public ExpressionDependencies createExpressionDependencies(ExpressionQuery expressionQuery) {
        if (expressionQuery.getBindings().isEmpty()) {
            return ExpressionDependencies.none();
        }
        List<ExpressionDependencies> dependencies = new ArrayList<>();
        for (ExpressionQuery.ParameterBinding binding : expressionQuery.getBindings()) {
            dependencies.add(ExpressionDependencies.discover(expressionParser.parseExpression(binding.getExpression())));
        }
        return ExpressionDependencies.merged(dependencies);
    }

    protected Mono<R2dbcSpELExpressionEvaluator> getSpelEvaluator(
            RelationalParameterAccessor accessor,
            ExpressionDependencies expressionDependencies
    ) {
        return evaluationContextProvider
                .getEvaluationContextLater(getQueryMethod().getParameters(), accessor.getValues(), expressionDependencies)
                .<R2dbcSpELExpressionEvaluator>map(context -> new DefaultR2dbcSpELExpressionEvaluator(expressionParser, context))
                .defaultIfEmpty(DefaultR2dbcSpELExpressionEvaluator.unsupported());
    }

    @Override
    Class<?> resolveResultType(ResultProcessor resultProcessor) {
        Class<?> returnedType = resultProcessor.getReturnedType().getReturnedType();
        return !returnedType.isInterface() ? returnedType : super.resolveResultType(resultProcessor);
    }

    protected class ExpandedQuery implements PreparedOperation<String> {
        private final ExpressionQuery expressionQuery;
        private final BindTargetRecorder recordedBindings;
        private final PreparedOperation<?> expanded;
        private final Map<String, Parameter> remainderByName;
        private final Map<Integer, Parameter> remainderByIndex;

        public ExpandedQuery(
                ExpressionQuery expressionQuery,
                ExpressionEvaluatingParameterBinder binder,
                RelationalParameterAccessor accessor,
                R2dbcSpELExpressionEvaluator evaluator
        ) {
            this.expressionQuery = expressionQuery;
            this.recordedBindings = new BindTargetRecorder();
            binder.bind(recordedBindings, accessor, evaluator);

            remainderByName = new LinkedHashMap<>(recordedBindings.byName);
            remainderByIndex = new LinkedHashMap<>(recordedBindings.byIndex);
            expanded = dataAccessStrategy.processNamedParameters(expressionQuery.getQuery(), (index, name) -> {
                if (recordedBindings.byName.containsKey(name)) {
                    remainderByName.remove(name);
                    return recordedBindings.byName.get(name);
                }

                if (recordedBindings.byIndex.containsKey(index)) {
                    remainderByIndex.remove(index);
                    return recordedBindings.byIndex.get(index);
                }

                return null;
            });
        }

        @Override
        public String getSource() {
            return expressionQuery.getQuery();
        }

        @Override
        public void bindTo(BindTarget target) {
            BindTargetBinder binder = new BindTargetBinder(target);
            expanded.bindTo(target);
            remainderByName.forEach(binder::bind);
        }

        @Override
        public String toQuery() {
            return expanded.toQuery();
        }

        @Override
        public String toString() {
            return String.format("Original: [%s], Expanded: [%s]", expressionQuery.getQuery(), expanded.toQuery());
        }
    }

    protected static class BindTargetRecorder implements BindTarget {
        final Map<Integer, Parameter> byIndex = new LinkedHashMap<>();
        final Map<String, Parameter> byName = new LinkedHashMap<>();

        @Override
        public void bind(String identifier, Object value) {
            byName.put(identifier, toParameter(value));
        }

        private Parameter toParameter(Object value) {
            return value instanceof Parameter ? (Parameter) value : Parameter.from(value);
        }

        @Override
        public void bind(int index, Object value) {
            byIndex.put(index, toParameter(value));
        }

        @Override
        public void bindNull(String identifier, Class<?> type) {
            byName.put(identifier, Parameter.empty(type));
        }

        @Override
        public void bindNull(int index, Class<?> type) {
            byIndex.put(index, Parameter.empty(type));
        }
    }
}
