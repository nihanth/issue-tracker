/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package org.wso2.carbon.issue.tracker.common.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.issue.tracker.common.IssueTrackerConfiguration;
import org.wso2.carbon.issue.tracker.common.util.IssueTrackerUtil;
import org.wso2.carbon.securevault.SecretCallbackHandlerService;

/**
 * @scr.component name="issue.tracker.common" immediate="true"
 */
public class IssueTrackerCommonServiceComponent {

    private static final Log log = LogFactory.getLog(IssueTrackerCommonServiceComponent.class);
    private static SecretCallbackHandlerService secretCallbackHandlerService;

    protected void activate(ComponentContext context) {
        BundleContext bundleContext = context.getBundleContext();
        IssueTrackerConfiguration configuration;
        try {
            configuration = IssueTrackerUtil.getIssueTrackerConfiguration();
            bundleContext.registerService(IssueTrackerConfiguration.class.getName(), configuration, null);
            if (log.isDebugEnabled()) {
                log.debug("IssueTracker common bundle is activated");
            }
        } catch (Throwable e) {
            log.error("Error in creating IssueTracker configuration", e);
        }
    }

    protected void deactivate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("IssueTracker common bundle is deactivated");
        }
    }
}
