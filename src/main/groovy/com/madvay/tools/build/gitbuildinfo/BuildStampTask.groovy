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

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecResult

import java.text.SimpleDateFormat

/**
 * Generates a BuildInfo.java file with information about the project
 * and state of the Git repo at build time.
 */
class BuildStampTask extends DefaultTask {

    @Input
    String getVersion() { return project.version }

    @Input
    String getGitCommit() {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream()
        ExecResult r = project.exec {
            executable 'git'
            // Only output is the commit SHA.
            args 'rev-parse', 'HEAD'

            setStandardOutput stdout
        }
        r.assertNormalExitValue()
        return stdout.toString().trim()
    }

    @Input
    boolean getGitIsClean() {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream()
        ExecResult r = project.exec {
            executable 'git'
            args 'status'

            setStandardOutput stdout
        }
        r.assertNormalExitValue()
        return stdout.toString().contains('nothing to commit, working directory clean')
    }

    @Input
    String getTimestamp() {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd'T'HHmmssZ")
        fmt.setTimeZone(TimeZone.getTimeZone('UTC'))
        return fmt.format(new Date())
    }

    /** Like "https://github.com/user/repo/tree/" */
    @Input
    String repoBaseUrl = null

    @Input
    String packageName = null

    static final String SRC_GEN_DIR = 'build/buildStampSrcGenJava'

    @OutputDirectory
    File getSrcGenDir() {
        return project.file(SRC_GEN_DIR)
    }

    File getBuildStampPackageDir() {
        return new File(srcGenDir, "${packageName.replace(".", "/")}")
    }

    File getBuildStampFile() {
        return new File(buildStampPackageDir, "BuildInfo.java")
    }

    @TaskAction
    void genFile() {
        srcGenDir.deleteDir()
        srcGenDir.mkdirs()
        buildStampPackageDir.mkdirs()

        // TODO: Real java escaping.  Errors will be caught by compileJava.
        String javaVersion = version.replace('"', '\\"')
        String javaGitCommit = gitCommit.replace('"', '\\"')
        String javaUrl = (repoBaseUrl + gitCommit).replace('"', '\\"')
        String javaTimestamp = timestamp.replace('"', '\\"')
        String javaGitIsClean = gitIsClean ? 'true' : 'false'

        String out = """
// This is an auto-generated file.  Do not edit.
// Generated using: https://github.com/madvay/git-build-info

package $packageName;

public class BuildInfo {
    public static final String VERSION = "$javaVersion";
    public static final String GIT_COMMIT = "$javaGitCommit";
    public static final String URL = "$javaUrl";
    public static final String TIMESTAMP = "$javaTimestamp";
    public static final boolean GIT_IS_CLEAN = $javaGitIsClean;
}
"""
        buildStampFile.write(out, "UTF-8")
    }
}
