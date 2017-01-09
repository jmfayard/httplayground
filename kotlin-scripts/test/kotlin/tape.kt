package tape

import TapeJsonConverter
import com.squareup.moshi.Moshi
import com.squareup.tape.FileObjectQueue
import com.squareup.tape.Task
import io.kotlintest.specs.StringSpec
import onElementAdded
import java.io.File
import java.util.*

data class DummyData(val int: Int, val long: Long)
class DummyTask : Task<DummyData> {
    override fun execute(callback: DummyData?) {
        println(callback)
    }
}


class TapeTest : StringSpec() { init {

    val file = File("test/resources/persisted_tasks")
    println("Persisting in file: ${file.absolutePath}")
    val random = Random()

    val moshi = Moshi.Builder().build()
    val objectQueue = FileObjectQueue<DummyData>(file, TapeJsonConverter(moshi))

    val initialSize = objectQueue.size()
    var nbElementsAdded = 0
    objectQueue.onElementAdded { entry: DummyData, queue ->
        nbElementsAdded++
    }

    val invocations = 2000
    "Initial state : $initialSize elements" {

    }
    "Adding $invocations elements" {
        val entry = DummyData(random.nextInt(), random.nextLong())
        objectQueue.add(entry)
    }.config(invocations = invocations, threads = 1)

    "Final size: ${initialSize+invocations}" {
        objectQueue.size() shouldBe initialSize+invocations
    }


}
}

