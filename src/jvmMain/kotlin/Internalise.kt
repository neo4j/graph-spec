import model.Type
import java.io.File

object Internalise {
    @JvmStatic
    fun main(args: Array<String>) {
        val string = File("/home/greg/IdeaProjects/graph-spec/src/jvmTest/resources/migrate/migration/dataModel/prod-like/adventureworks-sales.yaml").readText()
        val model = GraphSpec.Yaml.decodeFromString(string, Type.GRAPH_SPEC)
        model.internalise()
        println(model)
        val encoded = GraphSpec.Yaml.encodeToString(model)
        println(encoded)
    }
}
