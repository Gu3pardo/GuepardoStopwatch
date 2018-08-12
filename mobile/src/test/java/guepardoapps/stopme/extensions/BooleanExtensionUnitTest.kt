package guepardoapps.stopme.extensions

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

import org.junit.Assert.*
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith

@RunWith(JUnitPlatform::class)
class BooleanExtensionUnitTest : Spek({

    describe("Unit tests for BooleanExtensionUnitTest") {

        beforeEachTest { }

        afterEachTest { }

        it("toInteger should return 1") {
            // Arrange
            val expected = 1

            // Act
            val actual = true.toInteger()

            // Assert
            assertEquals(expected, actual)
        }

        it("toInteger should return 0") {
            // Arrange
            val expected = 0

            // Act
            val actual = false.toInteger()

            // Assert
            assertEquals(expected, actual)
        }
    }
})
