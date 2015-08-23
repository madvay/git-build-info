# git-build-info
Gradle plugin to incorporate Git repo info into your build


## Usage
```gradle
apply from: 'git-build-info.gradle'

task buildStamp(type: BuildStamp) {
    repoBaseUrl "https://github.com/USER/REPO/repo/tree/"
    packageName "com.example"
}
task cleanSrcGen << {
    file('src-gen').deleteDir()
}

compileJava.dependsOn 'buildStamp'
clean.dependsOn 'cleanSrcGen'

sourceSets {
    main {
        java.srcDirs += 'src-gen/main/java'
    }
}
```

## License
See [LICENSE](LICENSE) and [NOTICE](NOTICE).
