package guepardoapps.stopme.extensions

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

import org.junit.Assert.*
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith

@RunWith(JUnitPlatform::class)
class ArrayListExtensionUnitTest : Spek({

    describe("Unit tests for ArrayListExtensionUnitTest") {

        beforeEachTest { }

        afterEachTest { }

        it("should replace correct for String") {
            // Arrange
            val toTest = arrayListOf("First", "Second", "OldLastValue")

            // Act
            toTest.replaceLast("NewLastValue")

            // Assert
            assertEquals(3, toTest.size)
            assertEquals("NewLastValue", toTest.last())
        }

        it("should replace correct for Integer") {
            // Arrange
            val toTest = arrayListOf(420)

            // Act
            toTest.replaceLast(69)

            // Assert
            assertEquals(1, toTest.size)
            assertEquals(69, toTest.last())
        }

        it("should replace correct for Long") {
            // Arrange
            val toTest = arrayListOf(1234L, 3210L)

            // Act
            toTest.replaceLast(4545L)

            // Assert
            assertEquals(2, toTest.size)
            assertEquals(4545L, toTest.last())
        }

        it("should do nothing if size is 0") {
            // Arrange
            val toTest = arrayListOf<String>()

            // Act
            toTest.replaceLast("Test")

            // Assert
            assertEquals(0, toTest.size)
        }
    }
})
