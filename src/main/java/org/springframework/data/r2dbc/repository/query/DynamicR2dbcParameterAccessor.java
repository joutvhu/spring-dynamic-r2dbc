package org.springframework.data.r2dbc.repository.query;

import org.springframework.data.relational.repository.query.RelationalParameterAccessor;
import org.springframework.data.relational.repository.query.RelationalParametersParameterAccessor;
import org.springframework.data.repository.query.Parameters;

import java.util.HashMap;
import java.util.Map;

public class DynamicR2dbcParameterAccessor extends R2dbcParameterAccessor {
    private RelationalParametersParameterAccessor accessor;

    private DynamicR2dbcParameterAccessor(R2dbcQueryMethod method, RelationalParametersParameterAccessor accessor) {
        super(method, accessor.getValues());
        this.accessor = accessor;
    }

    public static DynamicR2dbcParameterAccessor of(R2dbcQueryMethod method, RelationalParameterAccessor accessor) {
        assert (accessor instanceof RelationalParametersParameterAccessor);
        return new DynamicR2dbcParameterAccessor(method, (RelationalParametersParameterAccessor) accessor);
    }

    /**
     * Get map param with value
     *
     * @return a map
     */
    public Map<String, Object> getParamModel() {
        if (this.accessor != null)
            return getParamModel(this.accessor);
        return getParamModel(this);
    }

    private Map<String, Object> getParamModel(RelationalParametersParameterAccessor accessor) {
        Map<String, Object> result = new HashMap<>();
        Parameters<?, ?> parameters = accessor.getParameters();
        Object[] values = accessor.getValues();
        parameters.forEach(parameter -> {
            Object value = values[parameter.getIndex()];
            if (value != null && parameter.isBindable()) {
                String key = parameter.getName().orElse(String.valueOf(parameter.getIndex()));
                result.put(key, value);
            }
        });
        return result;
    }
}
