/*
 * Copyright 2017 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onehippo.cm.backend;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.jcr.Session;

import org.apache.commons.lang3.time.StopWatch;
import org.onehippo.cm.api.ConfigurationService;
import org.onehippo.cm.api.model.ConfigurationModel;
import org.onehippo.cm.api.model.DefinitionType;
import org.onehippo.cm.engine.ClasspathConfigurationModelReader;
import org.onehippo.repository.bootstrap.PostStartupTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationServiceImpl implements ConfigurationService {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationServiceImpl.class);

    private final Session session;
    private final ConfigBaselineService baselineService;

    /* TODO refactor after HCM-55
     * For now, storing the read result and merged model in an instance variable. This should be refactored for a
     * couple of reasons .
     *
     * The code in #initializeRepositoryConfiguration should really be in #contentBootstrap, but that is not possible
     * now, as then then the code trips on deleting the property lock that is set in
     * org.hippoecm.repository.LocalHippoRepository#initialize; line 283: initializationProcessor.lock(lockSession);
     * HCM-55 will likely introduce a mechanism so that locked property can be ignored.
     *
     * See also https://issues.onehippo.com/browse/REPO-1236
     *
     * Once the code is moved, the model will likely be loaded in #contentBootstrap and the need for these instance
     * variables will be gone.
     */
    private ConfigurationModel configurationModel;

    public ConfigurationServiceImpl(final Session session) {
        this.session = session;
        baselineService = new ConfigBaselineService(session);
    }

    @Override
    public void configureRepository() {
        try {
            // TODO when merging this code into LocalHippoRepository, use verifyOnly=false parameter
            configurationModel = new ClasspathConfigurationModelReader().read(Thread.currentThread().getContextClassLoader(), true);
            final EnumSet allExceptWebFileBundles = EnumSet.allOf(DefinitionType.class);
            allExceptWebFileBundles.remove(DefinitionType.WEBFILEBUNDLE);
            allExceptWebFileBundles.remove(DefinitionType.CONTENT);

            // TODO this should probably happen in contentBootstrap() instead of here, so that it is protected by the repo lock
            apply(configurationModel, allExceptWebFileBundles);

            if (Boolean.getBoolean("repo.yaml.verify")) {
                log.info("starting YAML verification");
                apply(configurationModel, allExceptWebFileBundles);
                log.info("YAML verification complete");
            }

        } catch (RuntimeException e) {
            // TODO: ensure proper logging is done upstream
            log.debug("Bootstrap failed!", e);
            throw e;
        } catch (Exception e) {
            // TODO: ensure proper logging is done upstream
            log.debug("Bootstrap failed!", e);
            throw new RuntimeException(e);
        }
    }

    public List<PostStartupTask> contentBootstrap() {
        final List<PostStartupTask> tasks = new ArrayList(1);
        tasks.add(() -> {
            try {
                apply(configurationModel, EnumSet.of(DefinitionType.WEBFILEBUNDLE));

                final ContentService contentService = new ContentService();
                contentService.apply(configurationModel, session);

                // update the stored baseline after fully applying the configurationModel
                // this could result in the baseline becoming out of sync if the second phase of the apply fails
                // NOTE: We may prefer to use a two-phase commit style process, where the new baseline is stored in a
                //       separate node, the apply() is completed, and then the old baseline is swapped for new.
                baselineService.storeBaseline(configurationModel);
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.error("Error initializing webfiles", e);
                } else {
                    log.error("Error initializing webfiles", e.getMessage());
                }
            }
            try {
                // We're completely done with the configurationModel at this point, so clean up its resources
                configurationModel.close();
            }
            catch (Exception e) {
                log.error("Error closing configuration ConfigurationModel", e);
            }
        });
        return tasks;
    }

    @Override
    public void apply(final ConfigurationModel model, final EnumSet<DefinitionType> includeDefinitionTypes)
            throws Exception {
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            final ConfigService service =
                    new ConfigService(session);
            service.apply(model, includeDefinitionTypes);
            session.save();

            stopWatch.stop();
            log.info("ConfigurationModel applied in {} for definitionTypes: {}", stopWatch.toString(), includeDefinitionTypes);
        }
        catch (Exception e) {
            log.warn("Failed to apply configuration", e);
            throw e;
        }
    }

    @Override
    public void storeBaseline(final ConfigurationModel model) throws Exception {
        baselineService.storeBaseline(model);
    }

    @Override
    public ConfigurationModel loadBaseline() throws Exception {
        return baselineService.loadBaseline();
    }

    @Override
    public boolean matchesBaselineManifest(final ConfigurationModel model) throws Exception {
        return baselineService.matchesBaselineManifest(model);
    }

}
