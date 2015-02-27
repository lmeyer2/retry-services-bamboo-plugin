package com.aboutcoders.atlassian.bamboo;

import com.atlassian.bamboo.builder.BuildState;
import com.atlassian.bamboo.chains.Chain;
import com.atlassian.bamboo.chains.ChainExecution;
import com.atlassian.bamboo.chains.ChainResultsSummary;
import com.atlassian.bamboo.chains.cache.ImmutableChainStage;
import com.atlassian.bamboo.plan.*;
import com.atlassian.bamboo.plan.cache.CachedPlanManager;
import com.atlassian.bamboo.plan.cache.ImmutableJob;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.user.BambooUser;
import com.atlassian.bamboo.user.BambooUserManager;
import com.atlassian.bamboo.util.Narrow;
import com.atlassian.bamboo.v2.build.trigger.ManualBuildTriggerReason;
import com.atlassian.bamboo.v2.build.trigger.TriggerReason;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

/**
 * Created by lme on 27/02/15.
 */
public class PostChainAction implements com.atlassian.bamboo.chains.plugins.PostChainAction {

    private final static Logger LOG = LoggerFactory.getLogger(PostChainAction.class);

    private PlanExecutionManager planExecutionManager;
    private BambooUserManager userManager;
    private CachedPlanManager cachedPlanManager;
    private PlanManager planManager;

    private final static String pluginKey = "com.aboutcoders.atlassian.bamboo.RetryTask";

    @Override
    public void execute(@NotNull Chain chain, @NotNull ChainResultsSummary chainResultsSummary, @NotNull ChainExecution chainExecution) throws Exception {

        if (chainResultsSummary.getBuildState() == BuildState.FAILED) {

            int maxRetries = 3;
            String enabled = "false";
            String defaultUser = "";

            TopLevelPlan plan = (TopLevelPlan) planManager.getPlanByKey(chain.getPlanKey());

            if (plan != null && chainExecution.getCurrentStage() != null)
            {
                for (ImmutableChainStage stage : plan.getAllStages())
                {
                    if (stage.getName().equals(chainExecution.getCurrentStage().getName()))
                    {
                        for (ImmutableJob job : stage.getJobs())
                        {
                            for (TaskDefinition definition : job.getBuildDefinition().getTaskDefinitions())
                            {
                                String currentPluginKey = definition.getPluginKey();
                                if (currentPluginKey.startsWith(pluginKey))
                                {
                                    enabled = definition.getConfiguration().get("checkboxEnabled");
                                    String maximumRetries = definition.getConfiguration().get("numberOfRetries");
                                    defaultUser = definition.getConfiguration().get("defaultUser");


                                    if (!StringUtils.isEmpty(maximumRetries))
                                    {
                                        maxRetries = Integer.parseInt(maximumRetries);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }


            if (enabled.equals("false"))
            {
                LOG.debug("Retry Task is not available or disabled");
                return;
            }

            LOG.debug("Retry Task is enabled");

            int retryCounter = 1;

            String retryCounterStr = chainResultsSummary.getCustomBuildData().get(pluginKey+ ".retrycounter");

            if (retryCounterStr != null)
            {
                retryCounter = Integer.valueOf(retryCounterStr);
            }

            if (retryCounter > maxRetries)
                return;

            retryCounter++;

            chainResultsSummary.getCustomBuildData().put(pluginKey + ".retrycounter", String.valueOf(retryCounter));

            final PlanExecutionConfig config = new PlanExecutionConfigImpl(PlanExecutionConfig.PlanExecutionType.RESTART)
                    .setChainResultSummary(chainResultsSummary)
                    .build();

            TriggerReason triggerReason = chainExecution.getTriggerReason();

            ManualBuildTriggerReason manualBuildTriggerReason = Narrow.to(triggerReason, ManualBuildTriggerReason.class);

            BambooUser user;

            if (manualBuildTriggerReason != null)
            {
                user = userManager.getBambooUser(manualBuildTriggerReason.getUserName());
            }
            else
            {
                user = userManager.getBambooUser(defaultUser);
            }


            ExecutionRequestResult executionRequestResult = planExecutionManager.startManualExecution(chain, config, user.getUser(),
                    Collections.<String, String>emptyMap(),
                    Collections.<String, String>emptyMap());

            LOG.debug("Restarted failed plan {}, {}", chain.getPlanKey().toString(), executionRequestResult.getPlanResultKey().toString());
        }
    }

    public void setPlanExecutionManager(PlanExecutionManager planExecutionManager) {
        this.planExecutionManager = planExecutionManager;
    }

    public BambooUserManager getUserManager() {
        return userManager;
    }

    public void setUserManager(BambooUserManager userManager) {
        this.userManager = userManager;
    }

    public void setCachedPlanManager(CachedPlanManager cachedPlanManager) {
        this.cachedPlanManager = cachedPlanManager;
    }

    public void setPlanManager(PlanManager planManager) {
        this.planManager = planManager;
    }
}
