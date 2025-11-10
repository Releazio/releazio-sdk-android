package com.releazio.sdk.utils

/**
 * Utility for comparing version strings
 */
object VersionComparator {

    /**
     * Check if version1 is newer than version2
     * @param version1 First version to compare
     * @param version2 Second version to compare
     * @return True if version1 is newer than version2
     */
    fun isNewerVersion(version1: String, version2: String): Boolean {
        return try {
            val v1 = parseVersion(version1)
            val v2 = parseVersion(version2)
            compareVersions(v1, v2) > 0
        } catch (e: Exception) {
            // Fallback to string comparison
            version2.compareTo(version1, ignoreCase = true) > 0
        }
    }

    /**
     * Compare two versions
     * @param version1 First version
     * @param version2 Second version
     * @return Positive if version1 > version2, negative if version1 < version2, 0 if equal
     */
    fun compareVersions(version1: String, version2: String): Int {
        return try {
            val v1 = parseVersion(version1)
            val v2 = parseVersion(version2)
            compareVersions(v1, v2)
        } catch (e: Exception) {
            // Fallback to string comparison
            version1.compareTo(version2, ignoreCase = true)
        }
    }

    /**
     * Parse version string into comparable parts
     * @param version Version string (e.g., "1.2.3", "2.0.0-beta", "1.0.0+build123")
     * @return Version parts
     */
    private fun parseVersion(version: String): VersionParts {
        // Remove build metadata (+build123)
        val cleanVersion = version.split('+')[0]
        
        // Split by dots and handle pre-release identifiers
        val parts = cleanVersion.split('.')
        val major = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val minor = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val patch = parts.getOrNull(2)?.split('-')[0]?.toIntOrNull() ?: 0
        val preRelease = if (parts.getOrNull(2)?.contains('-') == true) {
            parts[2].substringAfter('-')
        } else null

        return VersionParts(major, minor, patch, preRelease)
    }

    /**
     * Compare two parsed version parts
     */
    private fun compareVersions(v1: VersionParts, v2: VersionParts): Int {
        // Compare major version
        val majorComparison = v1.major.compareTo(v2.major)
        if (majorComparison != 0) return majorComparison

        // Compare minor version
        val minorComparison = v1.minor.compareTo(v2.minor)
        if (minorComparison != 0) return minorComparison

        // Compare patch version
        val patchComparison = v1.patch.compareTo(v2.patch)
        if (patchComparison != 0) return patchComparison

        // Compare pre-release identifiers
        return when {
            v1.preRelease == null && v2.preRelease == null -> 0
            v1.preRelease == null -> 1 // Stable version is newer than pre-release
            v2.preRelease == null -> -1 // Pre-release is older than stable
            else -> comparePreRelease(v1.preRelease, v2.preRelease)
        }
    }

    /**
     * Compare pre-release identifiers
     */
    private fun comparePreRelease(pre1: String, pre2: String): Int {
        val parts1 = pre1.split('.')
        val parts2 = pre2.split('.')

        val maxLength = maxOf(parts1.size, parts2.size)
        for (i in 0 until maxLength) {
            val part1 = parts1.getOrNull(i) ?: ""
            val part2 = parts2.getOrNull(i) ?: ""

            val comparison = comparePreReleasePart(part1, part2)
            if (comparison != 0) return comparison
        }

        return 0
    }

    /**
     * Compare individual pre-release parts
     */
    private fun comparePreReleasePart(part1: String, part2: String): Int {
        val isNumeric1 = part1.all { it.isDigit() }
        val isNumeric2 = part2.all { it.isDigit() }

        return when {
            isNumeric1 && isNumeric2 -> {
                val num1 = part1.toIntOrNull() ?: 0
                val num2 = part2.toIntOrNull() ?: 0
                num1.compareTo(num2)
            }
            isNumeric1 -> -1 // Numeric identifiers have lower precedence
            isNumeric2 -> 1
            else -> part1.compareTo(part2, ignoreCase = true)
        }
    }

    /**
     * Version parts for comparison
     */
    private data class VersionParts(
        val major: Int,
        val minor: Int,
        val patch: Int,
        val preRelease: String?
    )
}
