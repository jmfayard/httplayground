#!/usr/bin/env kotlin-script.sh
// Installation:  see kotlin-scripts/README.md
package dataclass

import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import okio.BufferedSource
import okio.Okio
import java.io.File
import java.util.*

fun help() {
    println("""
$ dataclass.kt input.json
$ dataclass.kt <<'JSON'
{
  "address": {
    "streetAddress": "21 2nd Street", "city": "New York"
  },
  "phoneNumber": [
    {
      "location": "home", "code": 44
    }
  ],
}
JSON
    """)
    System.exit(1)
}

fun main(args: Array<String>) {

    val stream = if (args.isNotEmpty() && File(args.first()).canRead()) {
        File(args.first()).inputStream()
    } else if (System.`in`.available() > 3) {
        System.`in`
    } else {
        help()
        return
    }
    val code = generateDataclassCode(jsonSource = Okio.buffer(Okio.source(stream)))
    println(code)

}

fun generateDataclassCode(jsonSource: BufferedSource) : String {
    val reader = JsonReader.of(jsonSource)
    if (!reader.hasNext()) help()

    val adapter = if (reader.peek() == JsonReader.Token.BEGIN_ARRAY) {
        Json.listAdapter
    } else {
        Json.mapAdapter
    }

    val data = adapter.fromJson(reader)
    val schema = schemaOf(data)
    val generator = KotlinPoet(schema)
    val classes = generator.dataClasses()
    val code = classes.map { generator.generateKotlinCode(it) }.joinToString(separator = "")
    return code

}


data class DataClass(val name: String, val properties: List<DataClassProp>)
data class DataClassProp(val name: String, val type: String, val defaultValue: String?, val required: Boolean)

sealed class Schema(var type: String?) {

    class O(val properties: Map<String, Schema>) : Schema(null) {
        override fun toString(): String = "O($type, $properties)"
    }
    class L<out T : Schema>(val list: List<T>) : Schema(null) {
        override fun toString(): String = "L($type, $list)"
    }
    object I : Schema("Int") {
        override fun toString(): String = "I"
    }
    object D : Schema("Double") {
        override fun toString(): String = "D"
    }
    object B : Schema("Boolean") {
        override fun toString(): String = "B"
    }
    object S : Schema("String") {
        override fun toString(): String = "S"
    }
    object N : Schema("Any?") {
        override fun toString(): String = "N"
    }
}


fun schemaOf(value: Any?): Schema = when (value) {
    null -> Schema.N
    is Boolean -> Schema.B
    is Number -> {
        val double = value.toDouble()
        if (double == Math.floor(double))
            Schema.I
        else
            Schema.D
    }
    is String -> Schema.S
    is List<*> -> {
        val children = value.map { e -> schemaOf(e) }
        Schema.L(children)
    }
    is Map<*, *> -> {
        val children = value.mapValues { e ->
            schemaOf(e.value)
        }
        Schema.O(children as Map<String, Schema>)
    }
    else -> TODO("Unexpected value $value")
}



object Json {
    val moshi = Moshi.Builder().build()
    val mapAdapter = moshi.adapter(Map::class.java)
    val listAdapter = moshi.adapter(List::class.java)
}

data class KotlinPoet(val schema: Schema) {

    init {
        var i = 0
        forAllTypes { s: Schema ->
            if (s.type == null) {
                s.type = "T${i++}"
            }
        }
    }

    fun forAllTypes(operation: (Schema) -> Unit) {
        val stack = Stack<Schema>()
        stack.addElement(schema)
        while (stack.isNotEmpty()) {
            val s = stack.pop()
            operation(s)
            if (s is Schema.O) {
                stack.addAll(s.properties.values)
            }
            if (s is Schema.L<*>) {
                stack.addAll(s.list)
            }
        }
    }


    fun defaultValue(schema: Schema): String? =
        when (schema) {
            is Schema.O -> null
            is Schema.L<*> -> "emptyList()"
            is Schema.I -> "0"
            is Schema.D -> "0.0"
            is Schema.B -> "false"
            is Schema.S -> "\"\""
            is Schema.N -> "null"
        }

    fun objectClass(schema: Schema.O): DataClass {
        val properties = schema.properties.entries.map {
            val (name: String, s: Schema) = it
            val type = if (s is Schema.L<*>) {
                if (s.list.isEmpty()) {
                    "List<Any?>"
                } else {
                    val child = s.list.first()
                    "List<${child.type}>"
                }
            } else {
                s.type!!
            }
            DataClassProp(name, type, defaultValue(s), true)
        }
        return DataClass(schema.type!!, properties)
    }

    fun dataClasses(): List<DataClass> {
        val result = mutableListOf<DataClass>()
        forAllTypes { s: Schema ->
            if (s is Schema.O) {
                result += objectClass(s)
            }
        }
        return result
    }

    fun generateKotlinCode(c: DataClass) = buildString {
        append("data class ")
        append(c.name)
        append(" (")
        val props = c.properties
        props.forEachIndexed { i, prop ->
            append("\n    val ")
            append(prop.name)
            append(": ")
            append(prop.type)
            if (prop.defaultValue != null) {
                append(" = ")
                append(prop.defaultValue)
            }
            if (i != props.lastIndex) {
                append(",")
            }

        }
        append("\n")
        append(")")
        append("\n\n")
    }
}
