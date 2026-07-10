package com.hello.bravebook

import com.hello.bravebook.utils.FullscreenManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FullscreenManagerTest {

    @Test
    fun startsInactive() {
        val m = FullscreenManager()
        assertFalse(m.isFullscreen)
        assertNull(m.current)
        assertNull(m.callback)
    }

    @Test
    fun enterSetsActiveAndStoresViewAndCallback() {
        val m = FullscreenManager()
        val view = "video-surface"
        val cb = "custom-view-callback"
        m.enter(view, cb)
        assertTrue(m.isFullscreen)
        assertEquals(view, m.current)
        assertEquals(cb, m.callback)
    }

    @Test
    fun exitClearsStateAndReturnsCallback() {
        val m = FullscreenManager()
        val cb = "custom-view-callback"
        m.enter("video-surface", cb)
        val returned = m.exit()
        assertFalse(m.isFullscreen)
        assertNull(m.current)
        assertNull(m.callback)
        assertEquals(cb, returned)
    }

    @Test
    fun exitWhenInactiveReturnsNull() {
        val m = FullscreenManager()
        assertNull(m.exit())
    }
}
