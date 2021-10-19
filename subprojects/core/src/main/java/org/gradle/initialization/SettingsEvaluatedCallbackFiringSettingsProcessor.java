/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.initialization;

import org.gradle.StartParameter;
import org.gradle.api.internal.GradleInternal;
import org.gradle.api.internal.SettingsInternal;
import org.gradle.api.internal.initialization.ClassLoaderScope;
import org.gradle.internal.deprecation.DeprecationLogger;

public class SettingsEvaluatedCallbackFiringSettingsProcessor implements SettingsProcessor {

    private final SettingsProcessor delegate;

    public SettingsEvaluatedCallbackFiringSettingsProcessor(SettingsProcessor delegate) {
        this.delegate = delegate;
    }

    @Override
    public SettingsInternal process(GradleInternal gradle, SettingsLocation settingsLocation, ClassLoaderScope buildRootClassLoaderScope, StartParameter startParameter) {
        SettingsInternal settings = delegate.process(gradle, settingsLocation, buildRootClassLoaderScope, startParameter);
        gradle.getBuildListenerBroadcaster().settingsEvaluated(settings);
        settings.preventFromFurtherMutation();
        if (!((DefaultProjectDescriptor) settings.getRootProject()).isNameChanged()) {
            DeprecationLogger.deprecate("Implicit rootProject.name")
                .withAdvice("Set the 'rootProject.name' in the settings file.")
                .willBecomeAnErrorInGradle8()
                .withUserManual("multi_project_builds", "naming_recommendations")
                .nagUser();
        }
        return settings;
    }
}
