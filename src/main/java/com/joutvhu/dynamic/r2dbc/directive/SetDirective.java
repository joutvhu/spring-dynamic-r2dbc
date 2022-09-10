package com.joutvhu.dynamic.r2dbc.directive;

import freemarker.core.Environment;
import freemarker.template.*;

import java.io.IOException;
import java.util.Map;

/**
 * The set directive knows to only insert "SET" if there is any content returned by the containing tags,
 * If that content begins or ends with ",", it knows to strip it off.
 * They are used in templates like {@code <@set>...</@set>}
 *
 * @author Giao Ho
 * @since 2.x.2
 */
public class SetDirective implements TemplateDirectiveModel {
    private static final TrimDirective.TrimSymbol symbols = new TrimDirective.TrimSymbol("set", null, ",");

    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
        if (!params.isEmpty())
            throw new TemplateModelException("This directive doesn't allow parameters.");

        if (body != null)
            TrimDirective.TrimWriter.of(env.getOut(), symbols).render(body);
    }
}
