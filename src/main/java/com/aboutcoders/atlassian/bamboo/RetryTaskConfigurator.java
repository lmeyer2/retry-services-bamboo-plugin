package com.aboutcoders.atlassian.bamboo;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.opensymphony.xwork.TextProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class RetryTaskConfigurator extends AbstractTaskConfigurator
{
    private TextProvider textProvider;

    @NotNull
    @Override
    public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params, @Nullable final TaskDefinition previousTaskDefinition)
    {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);
        config.put("checkboxEnabled", params.getString("checkboxEnabled"));
        config.put("numberOfRetries", params.getString("numberOfRetries"));
        config.put("defaultUser", params.getString("defaultUser"));
        return config;
    }

    @Override
    public void populateContextForCreate(@NotNull final Map<String, Object> context)
    {
        super.populateContextForCreate(context);
        context.put("checkboxEnabled", true);
        context.put("numberOfRetries", 3);
        context.put("defaultUser", "");
    }

    @Override
    public void populateContextForEdit(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition)
    {
        super.populateContextForEdit(context, taskDefinition);
        context.put("checkboxEnabled", taskDefinition.getConfiguration().get("checkboxEnabled"));
        context.put("numberOfRetries", taskDefinition.getConfiguration().get("numberOfRetries"));
        context.put("defaultUser", taskDefinition.getConfiguration().get("defaultUser"));
    }

    @Override
    public void populateContextForView(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition)
    {
        super.populateContextForView(context, taskDefinition);
        context.put("checkboxEnabled", taskDefinition.getConfiguration().get("checkboxEnabled"));
        context.put("numberOfRetries", taskDefinition.getConfiguration().get("numberOfRetries"));
        context.put("defaultUser", taskDefinition.getConfiguration().get("defaultUser"));
    }

    @Override
    public void validate(@NotNull final ActionParametersMap params, @NotNull final ErrorCollection errorCollection)
    {
        super.validate(params, errorCollection);
    }

    public void setTextProvider(final TextProvider textProvider)
    {
        this.textProvider = textProvider;
    }
}
