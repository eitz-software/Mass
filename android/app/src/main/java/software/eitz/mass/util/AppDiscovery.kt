package software.eitz.mass.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import software.eitz.mass.ui.screens.LaunchableApp

object AppDiscovery {

    private val MESSAGING_PACKAGES = listOf(
        "com.whatsapp",
        "org.telegram.messenger",
        "org.thoughtcrime.securesms",
        "com.facebook.orca",
        "com.viber.voip",
        "com.google.android.apps.messaging",
        "com.whatsapp.w4b"
    )

    fun getFilteredApps(context: Context, tileId: String): List<LaunchableApp> {
        val pm = context.packageManager
        val allLaunchable = getAllLaunchableApps(context)

        val filteredPackages = when (tileId) {
            "messages" -> getMessagingPackages(pm)
            "email" -> getEmailPackages(pm)
            "calls" -> getCallPackages(pm)
            "camera" -> getCameraPackages(pm)
            "weather" -> getWeatherPackages(pm)
            else -> emptySet()
        }

        if (filteredPackages.isEmpty()) return emptyList()

        return allLaunchable.filter { it.packageName in filteredPackages }
    }

    fun getAllLaunchableApps(context: Context): List<LaunchableApp> {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
        return resolveInfos.map { info ->
            LaunchableApp(
                name = info.loadLabel(pm).toString(),
                packageName = info.activityInfo.packageName,
                icon = info.loadIcon(pm)
            )
        }.sortedBy { it.name }
    }

    private fun getMessagingPackages(pm: PackageManager): Set<String> {
        val packages = mutableSetOf<String>()
        
        // 1. CATEGORY_APP_MESSAGING (API 24+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val intent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MESSAGING)
            pm.queryIntentActivities(intent, 0).forEach { packages.add(it.activityInfo.packageName) }
        }

        // 2. ACTION_SEND with text/plain
        val sendIntent = Intent(Intent.ACTION_SEND).apply { type = "text/plain" }
        pm.queryIntentActivities(sendIntent, 0).forEach { packages.add(it.activityInfo.packageName) }

        // 3. Known Messaging Packages
        MESSAGING_PACKAGES.forEach { pkg ->
            try {
                pm.getPackageInfo(pkg, 0)
                packages.add(pkg)
            } catch (e: Exception) { }
        }

        return packages
    }

    private fun getEmailPackages(pm: PackageManager): Set<String> {
        val packages = mutableSetOf<String>()
        val intent = Intent(Intent.ACTION_SENDTO).apply { data = Uri.parse("mailto:") }
        pm.queryIntentActivities(intent, 0).forEach { packages.add(it.activityInfo.packageName) }
        return packages
    }

    private fun getCallPackages(pm: PackageManager): Set<String> {
        val packages = mutableSetOf<String>()
        val intent = Intent(Intent.ACTION_DIAL)
        pm.queryIntentActivities(intent, 0).forEach { packages.add(it.activityInfo.packageName) }
        return packages
    }

    private fun getCameraPackages(pm: PackageManager): Set<String> {
        val packages = mutableSetOf<String>()
        val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
        pm.queryIntentActivities(intent, 0).forEach { packages.add(it.activityInfo.packageName) }
        
        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        pm.queryIntentActivities(captureIntent, 0).forEach { packages.add(it.activityInfo.packageName) }
        
        return packages
    }

    private fun getWeatherPackages(pm: PackageManager): Set<String> {
        val packages = mutableSetOf<String>()
        
        // 1. CATEGORY_APP_WEATHER (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val intent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_WEATHER)
            pm.queryIntentActivities(intent, 0).forEach { packages.add(it.activityInfo.packageName) }
        }

        // 2. Keyword fallback
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        pm.queryIntentActivities(mainIntent, 0).forEach {
            val pkg = it.activityInfo.packageName.lowercase()
            if (pkg.contains("weather")) {
                packages.add(it.activityInfo.packageName)
            }
        }

        return packages
    }
}
