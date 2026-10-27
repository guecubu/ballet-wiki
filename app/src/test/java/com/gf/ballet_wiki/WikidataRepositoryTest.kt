package com.gf.ballet_wiki

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WikidataRepositoryTest {
    @Test
    fun parsesAvailableWorkFields() {
        val json = """
            {"results":{"bindings":[{"workLabel":{"value":"Swan Lake"},"workDescription":{"value":"Ballet by Tchaikovsky"},"composerLabel":{"value":"Pyotr Tchaikovsky"}}]}}
        """.trimIndent()

        assertEquals(
                WorkFields("Swan Lake", "Ballet by Tchaikovsky", "Pyotr Tchaikovsky"),
                parseWorkResponse(json)
                    )
    }

    @Test
    fun returnsNullWhenRequiredDataIsMissing() {
        val json = """{"results":{"bindings":[{"workLabel":{"value":"Swan Lake"}}]}}"""

        assertNull(parseWorkResponse(json))
    }

    @Test
    fun returnsNullWhenThereAreNoResults() {
        assertNull(parseWorkResponse("""{"results":{"bindings":[]}}"""))
    }

    @Test
    fun fallbackContainsVerifiedWorkAndSource() {
        val fallback = swanLakeFallback()

        assertEquals("Swan Lake", fallback.title)
        assertEquals("Pyotr Ilyich Tchaikovsky", fallback.composer)
        assertEquals("https://www.wikidata.org/wiki/Q199786", fallback.sourceUrl)
        assertEquals(3, fallback.titles.size)
    }
}