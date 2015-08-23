/*
 * Copyright (c) 2015 by Advay Mengle.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.madvay.tools.build.gitbuildinfo

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin

/**
 *
 */
class GitBuildInfoPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.with {
            // If already applied, does nothing.
            pluginManager.apply(JavaPlugin)

            tasks.create('buildStamp', BuildStampTask)
            tasks.create('verifyCleanGit', VerifyCleanGitTask)
            compileJava.dependsOn('buildStamp')
            tasks.all {
                // If you are publishing plugins, you should have a clean git repo.
                if (it.name == 'publishPlugins') {
                    it.dependsOn('verifyCleanGit')
                }
            }

            sourceSets {
                main {
                    java.srcDirs += buildStamp.srcGenDir
                }
            }
        }
    }
}
