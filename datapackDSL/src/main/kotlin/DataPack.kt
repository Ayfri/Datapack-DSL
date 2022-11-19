
import annotations.FunctionsHolder
import functions.Function
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.benwoodworth.knbt.NbtString
import net.benwoodworth.knbt.NbtTag
import serializers.NbtAsJsonTextComponentSerializer
import tags.Tags
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.Path

@Serializable
data class FilteredBlock(
	var namespace: String? = null,
	var path: String? = null,
)

@Serializable
class Filter {
	@SerialName("block")
	internal val blocks = mutableListOf<FilteredBlock>()
	
	fun block(namespace: String? = null, path: String? = null) {
		blocks += FilteredBlock(namespace, path)
	}
	
	fun block(block: FilteredBlock) {
		blocks += block
	}
	
	fun block(block: FilteredBlock.() -> Unit) {
		blocks += FilteredBlock().apply(block)
	}
	
	fun blocks(vararg blocks: FilteredBlock) {
		this.blocks += blocks
	}
	
	fun blocks(blocks: Collection<FilteredBlock>) {
		this.blocks += blocks
	}
}

@Serializable
data class Pack(
	@SerialName("pack_format")
	var format: Int,
	@Serializable(NbtAsJsonTextComponentSerializer::class)
	var description: NbtTag,
)

@Serializable
data class SerializedDataPack(
	val pack: Pack,
	val filter: Filter,
)

@FunctionsHolder
class DataPack(val name: String) {
	val path: Path = Path("out")
	var iconPath: Path? = null
	
	private val filter = Filter()
	private val pack = Pack(10, NbtString("Generated by DataPackDSL"))
	private val functions = mutableListOf<Function>()
	val tags = mutableListOf<Tags>()
	
	fun addFunction(function: Function) {
		functions += function
	}
	
	fun pack(block: Pack.() -> Unit) = pack.run(block)
	fun filter(block: Filter.() -> Unit) = filter.run(block)
	
	fun generate() {
		val root = File("$path/$name")
		root.mkdirs()
		
		val serialized = SerializedDataPack(pack, filter)
		
		File(root, "pack.mcmeta").writeText(jsonEncoder.encodeToString(serialized))
		iconPath?.let { File(root, "pack.png").writeBytes(it.toFile().readBytes()) }
		
		val data = File(root, "data")
		data.mkdirs()
		
		functions.groupBy { it.namespace }.forEach { (namespace, functions) ->
			val namespaceDir = File(data, namespace)
			namespaceDir.mkdirs()
			
			val functionsDir = File(namespaceDir, "functions")
			functionsDir.mkdirs()
			
			functions.forEach { it.generate(functionsDir) }
		}
		
		tags.groupBy { it.namespace }.forEach { (namespace, tags) ->
			val namespaceDir = File(data, namespace)
			namespaceDir.mkdirs()
			
			val tagsDir = File(namespaceDir, "tags")
			tagsDir.mkdirs()
			
			tags.forEach { it.generate(tagsDir) }
		}
	}
	
	fun generateZip() {
		generate()
		
		ZipOutputStream(BufferedOutputStream(FileOutputStream("$path/$name.zip"))).use { zip ->
			val root = File("$path/$name")
			root.walk().forEach { file ->
				if (file.isFile) {
					zip.putNextEntry(ZipEntry(file.relativeTo(root).path))
					zip.write(file.readBytes())
					zip.closeEntry()
				}
			}
		}
	}
	
	companion object {
		@OptIn(ExperimentalSerializationApi::class)
		val jsonEncoder = Json {
			prettyPrint = true
			encodeDefaults = true
			ignoreUnknownKeys = true
			explicitNulls = false
		}
	}
}

fun dataPack(name: String, block: DataPack.() -> Unit) = DataPack(name).apply(block)
