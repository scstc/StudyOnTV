package com.studyontv.tv

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class UpdaterTest {
    private val flavor = if (BuildConfig.FLAVOR == "tv") "TV" else "Mobile"
    private val otherFlavor = if (flavor == "TV") "Mobile" else "TV"

    private fun asset(device: String, repository: String = Updater.OWNER_REPO): JSONObject = JSONObject()
        .put("name", "StudyOnTV-$device-v26.4.1.2.apk")
        .put("browser_download_url", "https://github.com/$repository/releases/download/v26.4.1.2/StudyOnTV-$device-v26.4.1.2.apk")
        .put("digest", "sha256:" + "ab".repeat(32))

    private fun release(): JSONObject = JSONObject()
        .put("tag_name", "v26.4.1.2")
        .put("body", "修复更新检查")
        .put("assets", JSONArray().put(asset(otherFlavor)).put(asset(flavor)))

    @Test fun updatesFollowThisRepositoryAndSelectTheInstalledDevice() {
        assertEquals("scstc/StudyOnTV", Updater.OWNER_REPO)
        assertEquals("https://github.com/scstc/StudyOnTV/releases/latest", Updater.RELEASE_PAGE)
        val info = Updater.parseRelease(release(), "26.4.1.1", "GitHub")!!
        assertEquals("26.4.1.2", info.latest)
        assertEquals("26.4.1.1", info.current)
        assertEquals("GitHub", info.source)
        assertTrue(info.url.endsWith("/StudyOnTV-$flavor-v26.4.1.2.apk"))
        assertEquals("ab".repeat(32), info.sha256)
    }

    @Test fun anotherDeviceApkCannotBeInstalledAsAnUpdate() {
        val data = release().put("assets", JSONArray().put(asset(otherFlavor)))
        assertNull(Updater.parseRelease(data, "26.4.1.1", "GitHub"))
    }

    @Test fun upstreamDownloadLinksAreRejected() {
        val data = release().put("assets", JSONArray().put(asset(flavor, "chadyi/StudyOnTV")))
        assertNull(Updater.parseRelease(data, "26.4.1.1", "GitHub"))
    }

    @Test fun draftsAndPrereleasesDoNotReachInstalledClients() {
        assertNull(Updater.parseRelease(release().put("draft", true), "26.4.1.1", "GitHub"))
        assertNull(Updater.parseRelease(release().put("prerelease", true), "26.4.1.1", "GitHub"))
        assertNull(Updater.parseRelease(release().put("tag_name", "vnext"), "26.4.1.1", "GitHub"))
    }

    @Test fun forceUpdatesOnlyBlockOlderClients() {
        val data = release().put("body", "[force]\n必须更新")
        assertTrue(Updater.parseRelease(data, "26.4.1.1", "GitHub")!!.force)
        assertFalse(Updater.parseRelease(data, "26.4.1.2", "GitHub")!!.force)
        assertFalse(Updater.parseRelease(data, "26.4.1.3", "GitHub")!!.force)
    }

    @Test fun branchAndReleaseNumbersCompareNumericallyAcrossRollovers() {
        assertTrue(Updater.isNewer("26.4.1.1", "0.3.21"))
        assertTrue(Updater.isNewer("26.4.1.10", "26.4.1.9"))
        assertTrue(Updater.isNewer("26.4.2.1", "26.4.1.10"))
        assertTrue(Updater.isNewer("27.1.1.1", "26.4.2.9"))
        assertFalse(Updater.isNewer("26.4.1.1", "26.4.1.1"))
        assertFalse(Updater.isNewer("26.4.1.9", "26.4.1.10"))
    }
}
