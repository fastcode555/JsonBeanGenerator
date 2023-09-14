package com.awesome.plugins.ts.tomapfield

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.awesome.utils.runWriteCmd
import com.intellij.openapi.editor.Editor
import toJSON
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.math.BigDecimal
import javax.swing.*

class TopMapFieldDialog(private val editor: Editor?) : JDialog() {
    var contentPane: JPanel? = null
    var buttonOK: JButton? = null
    var buttonCancel: JButton? = null
    var tvArea: JTextArea? = null

    init {
        setContentPane(contentPane)
        setModal(true)
        getRootPane().setDefaultButton(buttonOK)
        buttonOK!!.addActionListener { onOK() }
        buttonCancel!!.addActionListener { dispose() }

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE)
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                dispose()
            }
        })
        contentPane!!.registerKeyboardAction(
            { dispose() },
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        )
    }

    private fun onOK() {
        val text = tvArea!!.text
        val json = text.toJSON()
        val builder = parseJson(json)
        val selectionModel = editor!!.selectionModel
        val index = selectionModel.selectionStart
        print(builder)
        editor.runWriteCmd {
            editor.document.insertString(index, "${builder}\n")
            dispose()
        }
    }

    private fun parseJson(json: JSON?, deep: Int = 0, separator: String = "  ", key: String = ""): StringBuilder {
        val builder = StringBuilder()
        val start = separators(separator, deep)
        val lineStart = "$start$separator"
        if (json is JSONObject) {
            if (key.isEmpty()) {
                builder.append("$lineStart{\n")
            } else {
                builder.append("$ {\n")
            }
            for ((key, element) in json.innerMap) {
                if (element is String) {
                    builder.append("$lineStart$key: '$element',\n")
                } else if (element is Double || element is Int || element is BigDecimal || element is Float || element is Boolean) {
                    builder.append("$lineStart$key: $element,\n")
                } else if (element is JSON) {
                    builder.append("$lineStart$key:${parseJson(element, deep + 1, separator, key)}")
                    builder.append(",\n")
                }
            }
            builder.append("$start}")
        } else if (json is JSONArray) {
            if (key.isEmpty()) {
                builder.append("$lineStart[\n")
            } else {
                builder.append("$ [\n")
            }
            for (element in json) {
                if (element is JSONObject) {
                    builder.append("${parseJson(element, deep + 1, separator, key)}")
                    builder.append(",\n")
                } else if (element is String) {
                    builder.append("$lineStart'$element',\n")
                } else if (element is Double || element is Int || element is BigDecimal || element is Float || element is Boolean) {
                    builder.append("$lineStart$element,\n")
                }
            }
            builder.append("$start]")
        }
        return builder
    }

    /**
     * 生成分割线
     **/
    private fun separators(separator: String, deep: Int): String {
        var builder = ""
        for (i in 0 until deep) {
            builder += separator
        }
        return builder
    }

    fun showDialog(): TopMapFieldDialog {
        pack()
        setLocationRelativeTo(null)
        isVisible = true
        return this
    }
}
