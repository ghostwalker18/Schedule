// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("io.appmetrica.analytics") version "1.0.1" apply false
    id("io.github.gmazzo.test.aggregation.coverage") version "2.2.1"
    id("io.github.gmazzo.test.aggregation.results") version "2.2.1"
}

testAggregation{
    coverage{
        exclude("**/*_Impl*")
    }
}

buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.0")
    }
}