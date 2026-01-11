package cn.jzl.ecs.serialization.core

data class Version(
    val major: Int,
    val minor: Int,
    val patch: Int
) {
    override fun toString(): String = "$major.$minor.$patch"

    companion object {
        fun parse(versionString: String): Version {
            val parts = versionString.split(".")
            require(parts.size == 3) { "Invalid version format: $versionString" }
            return Version(
                major = parts[0].toInt(),
                minor = parts[1].toInt(),
                patch = parts[2].toInt()
            )
        }

        val CURRENT = Version(1, 0, 0)
    }

    fun isCompatible(other: Version): Boolean {
        return major == other.major && minor >= other.minor
    }

    fun isGreaterThan(other: Version): Boolean {
        if (major != other.major) return major > other.major
        if (minor != other.minor) return minor > other.minor
        return patch > other.patch
    }
}

class VersionManager(private val currentVersion: Version = Version.CURRENT) {
    private val migrationHandlers = mutableMapOf<Pair<Version, Version>, (Any) -> Any>()

    fun getCurrentVersion(): String = currentVersion.toString()

    fun isCompatible(version: String): Boolean {
        val targetVersion = Version.parse(version)
        return currentVersion.isCompatible(targetVersion)
    }

    fun migrate(data: Any, fromVersion: String, toVersion: String = currentVersion.toString()): Any {
        val from = Version.parse(fromVersion)
        val to = Version.parse(toVersion)

        if (from == to) return data

        var result = data
        var current = from

        while (current != to) {
            val nextVersion = findNextVersion(current, to)
            val handler = migrationHandlers[Pair(current, nextVersion)]
                ?: throw IllegalStateException("No migration handler found from $current to $nextVersion")

            result = handler(result)
            current = nextVersion
        }

        return result
    }

    fun registerMigration(
        from: Version,
        to: Version,
        handler: (Any) -> Any
    ) {
        migrationHandlers[Pair(from, to)] = handler
    }

    private fun findNextVersion(current: Version, target: Version): Version {
        val allVersions = migrationHandlers.keys.map { it.first }.distinct().sortedBy { it.major * 10000 + it.minor * 100 + it.patch }
        val currentIndex = allVersions.indexOf(current)
        require(currentIndex != -1) { "Unknown version: $current" }

        val targetIndex = allVersions.indexOf(target)
        require(targetIndex != -1) { "Unknown target version: $target" }

        return allVersions[currentIndex + 1]
    }
}