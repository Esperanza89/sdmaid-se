package eu.darken.sdmse.common.files.core.local

import com.squareup.moshi.JsonDataException
import eu.darken.sdmse.common.files.core.*
import eu.darken.sdmse.common.serialization.SerializationModule
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import testhelpers.json.toComparableJson
import java.io.File
import java.time.Instant

class LocalPathTest {
    private val testFile = File("./testfile")

    @AfterEach
    fun cleanup() {
        testFile.delete()
    }

    @Test
    fun `test direct serialization`() {
        testFile.tryMkFile()
        val original = LocalPath.build(file = testFile)

        val adapter = SerializationModule().moshi().adapter(LocalPath::class.java)

        val json = adapter.toJson(original)
        json.toComparableJson() shouldBe """
            {
                "file": "$testFile",
                "pathType":"LOCAL"
            }
        """.toComparableJson()

        adapter.fromJson(json) shouldBe original
    }

    @Test
    fun `test polymorph serialization`() {
        testFile.tryMkFile()
        val original = LocalPath.build(file = testFile)

        val adapter = SerializationModule().moshi().adapter(APath::class.java)

        val json = adapter.toJson(original)
        json.toComparableJson() shouldBe """
            {
                "file":"$testFile",
                "pathType":"LOCAL"
            }
        """.toComparableJson()

        adapter.fromJson(json) shouldBe original
    }

    @Test
    fun `test fixed type`() {
        val file = LocalPath(testFile)
        file.pathType shouldBe APath.PathType.LOCAL
        shouldThrow<IllegalArgumentException> {
            file.pathType = APath.PathType.RAW
            Any()
        }
        file.pathType shouldBe APath.PathType.LOCAL
    }

    @Test
    fun `force typing`() {
        val original = RawPath.build("test", "file")

        val moshi = SerializationModule().moshi()

        shouldThrow<JsonDataException> {
            val json = moshi.adapter(RawPath::class.java).toJson(original)
            moshi.adapter(LocalPath::class.java).fromJson(json)
        }
    }

    @Test
    fun `path comparison`() {
        val file1a = LocalPath.build("test", "file1")
        val file1b = LocalPath.build("test", "file1")
        val file2 = LocalPath.build("test", "file2")
        file1a shouldBe file1b
        file1a shouldNotBe file2
    }

    @Test
    fun `lookup comparison`() {
        val lookup1a = LocalPathLookup(
            lookedUp = LocalPath.build("test", "file1"),
            fileType = FileType.FILE,
            size = 16,
            modifiedAt = Instant.EPOCH,
            ownership = null,
            permissions = null,
            target = null,
        )
        val lookup1b = LocalPathLookup(
            lookedUp = LocalPath.build("test", "file1"),
            fileType = FileType.FILE,
            size = 8,
            modifiedAt = Instant.ofEpochMilli(123),
            ownership = Ownership(1, 1),
            permissions = Permissions(444),
            target = null,
        )
        val lookup1c = LocalPathLookup(
            LocalPath.build("test", "file1"),
            fileType = FileType.DIRECTORY,
            size = 16,
            modifiedAt = Instant.EPOCH,
            ownership = null,
            permissions = null,
            target = null,
        )
        val lookup2 = LocalPathLookup(
            lookedUp = LocalPath.build("test", "file2"),
            fileType = FileType.FILE,
            size = 16,
            modifiedAt = Instant.EPOCH,
            ownership = null,
            permissions = null,
            target = null,
        )
        lookup1a shouldBe lookup1b
        lookup1a shouldNotBe lookup1c
        lookup1a shouldNotBe lookup2
    }
}