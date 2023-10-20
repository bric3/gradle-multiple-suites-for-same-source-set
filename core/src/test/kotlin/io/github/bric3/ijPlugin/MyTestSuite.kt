package io.github.bric3.ijPlugin

import com.intellij.testFramework.ApplicationRule
import com.intellij.testFramework.junit5.TestApplication
import org.junit.platform.suite.api.SelectClasses
import org.junit.platform.suite.api.Suite

@Suite
@SelectClasses(JUnit5Test::class, JUnit4Test::class)
class MyTestSuite {
}

class JUnit4Test {
    @get:org.junit.Rule
    val application = ApplicationRule()

    @org.junit.Test
    fun dumb() {}
}

@TestApplication
class JUnit5Test {
    @org.junit.jupiter.api.Test
    fun dumb() {}
}