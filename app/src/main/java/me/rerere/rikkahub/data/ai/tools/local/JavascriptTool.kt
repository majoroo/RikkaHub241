package me.rerere.rikkahub.data.ai.tools.local

import com.whl.quickjs.wrapper.QuickJSContext
import com.whl.quickjs.wrapper.QuickJSObject
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import me.rerere.ai.core.InputSchema
import me.rerere.ai.core.Tool
import me.rerere.ai.ui.UIMessagePart

internal fun buildJavascriptTool(): Tool = Tool(
    name = "eval_javascript",
    description = """
        使用 QuickJS 引擎执行 JavaScript 代码（ES2020 标准）。
        返回值为代码中最后一个表达式的执行结果。
        涉及小数计算时，请使用 toFixed() 控制精度。
        Console 输出（log/info/warn/error）会被捕获并返回在 'logs' 字段中。
        不支持 DOM 和 Node.js API。
        示例：'1 + 2' 返回 3；'const x = 5; x * 2' 返回 10。
        角色扮演草稿中如果涉及到需要精准的计算的场景，应当调用此工具加以验证。
    """.trimIndent().replace("\n", " "),
    parameters = {
        InputSchema.Obj(
            properties = buildJsonObject {
                put("code", buildJsonObject {
                    put("type", "string")
                    put("description", "The JavaScript code to execute")
                })
            },
            required = listOf("code")
        )
    },
    execute = {
        val logs = arrayListOf<String>()
        val context = QuickJSContext.create()
        context.setConsole(object : QuickJSContext.Console {
            override fun log(info: String?) {
                logs.add("[LOG] $info")
            }

            override fun info(info: String?) {
                logs.add("[INFO] $info")
            }

            override fun warn(info: String?) {
                logs.add("[WARN] $info")
            }

            override fun error(info: String?) {
                logs.add("[ERROR] $info")
            }
        })
        val code = it.jsonObject["code"]?.jsonPrimitive?.contentOrNull
        val result = context.evaluate(code)
        val payload = buildJsonObject {
            if (logs.isNotEmpty()) {
                put("logs", JsonPrimitive(logs.joinToString("\n")))
            }
            put(
                key = "result",
                element = when (result) {
                    null -> JsonNull
                    is QuickJSObject -> JsonPrimitive(result.stringify())
                    else -> JsonPrimitive(result.toString())
                }
            )
        }
        listOf(UIMessagePart.Text(payload.toString()))
    }
)
