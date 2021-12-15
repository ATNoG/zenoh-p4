 
package org.onosproject.pipelines.zenoh_fabric.impl;

import com.google.common.collect.Lists;
import org.onlab.util.SharedScheduledExecutors;
import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.config.ConfigFactory;
import org.onosproject.net.config.NetworkConfigRegistry;
import org.onosproject.net.config.basics.SubjectFactories;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.group.Group;
import org.onosproject.net.group.GroupService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.onosproject.pipelines.zenoh_fabric.impl.AppConstants.CLEAN_UP_DELAY;
import static org.onosproject.pipelines.zenoh_fabric.impl.AppConstants.DEFAULT_CLEAN_UP_RETRY_TIMES;
import static org.onosproject.pipelines.zenoh_fabric.impl.Utils.sleep;

/**
 * A component which among other things registers the fabricDeviceConfig to the
 * netcfg subsystem.
 */
@Component(immediate = true, service = UtilsComponent.class)
public class UtilsComponent {

    private static final Logger log =
            LoggerFactory.getLogger(UtilsComponent.class.getName());

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public CoreService coreService;
    
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public FlowRuleService flowRuleService;

    
    
    private ApplicationId appId;
    
    @Activate
    protected void activate() {
        appId = coreService.getAppId("org.onosproject.zenoh_app");
        // Wait to remove flow and groups from previous executions.
        waitPreviousCleanup();
        log.info("Started");
    }

    @Deactivate
    protected void deactivate() {
        cleanUp();
        log.info("Stopped");
    }

    /**
     * Returns the application ID.
     *
     * @return application ID
     */
    ApplicationId getAppId() {
        return appId;
    }

    /**
     * Returns the executor service managed by this component.
     *
     * @return executor service
     */
    public ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * Schedules a task for the future using the executor service managed by
     * this component.
     *
     * @param task task runnable
     * @param delaySeconds delay in seconds
     */
    public void scheduleTask(Runnable task, int delaySeconds) {
        SharedScheduledExecutors.newTimeout(
                () -> executorService.execute(task),
                delaySeconds, TimeUnit.SECONDS);
    }

    /**
     * Triggers clean up of flows and groups from this app, returns false if no
     * flows or groups were found, true otherwise.
     *
     * @return false if no flows or groups were found, true otherwise
     */
    private boolean cleanUp() {
        Collection<FlowRule> flows = Lists.newArrayList(
                flowRuleService.getFlowEntriesById(appId).iterator());

        flows.forEach(flowRuleService::removeFlowRules);
        

        return true;
    }

    private void waitPreviousCleanup() {
        int retry = DEFAULT_CLEAN_UP_RETRY_TIMES;
        while (retry != 0) {

            if (!cleanUp()) {
                return;
            }

            log.info("Waiting to remove flows and groups from " +
                             "previous execution of {}...",
                     appId.name());

            sleep(CLEAN_UP_DELAY);

            --retry;
        }
    }
}
 
