package com.awesome

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.awesome.utils.DataBaseUtils
import com.awesome.utils.NotifyUtils
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import org.apache.http.util.TextUtils
import toJSON
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.lang.StringBuilder
import javax.swing.*

class SqlDialog(val project: Project, val editor: Editor) : JDialog() {
    val TYPE_SQLITE: Int = 0
    val TYPE_MYSQL: Int = 1

    var tvErrorTip: JLabel? = null

    var contentPane: JPanel? = null
    var buttonOK: JButton? = null
    var buttonCancel: JButton? = null
    var tvCreateTable: JTextField? = null
    var tvSelect: JTextField? = null
    var tvDelete: JTextField? = null
    var tvUpdate: JTextField? = null
    var tvArea: JTextArea? = null
    var tvInsert: JTextField? = null
    var btnGenerate: JButton? = null
    var tvTable: JTextField? = null
    var pythonRadioButton: JRadioButton? = null

    var sqliteRadioButton: JRadioButton? = null
    var mysqlRadioButton: JRadioButton? = null


    var sqlType: Int = TYPE_SQLITE
    private fun onOK() {
        dispose()
    }

    fun showDialog(): SqlDialog {
        pack()
        setLocationRelativeTo(null)
        isVisible = true
        return this
    }

    init {
        setContentPane(contentPane)
        isModal = true
        getRootPane().defaultButton = buttonOK
        buttonOK!!.addActionListener { e: ActionEvent? -> onOK() }
        buttonCancel!!.addActionListener { e: ActionEvent? -> dispose() }
        btnGenerate!!.addActionListener { e: ActionEvent? -> onGenerate() }

        // call onCancel() when cross is clicked
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                dispose()
            }
        })

        // call onCancel() on ESCAPE
        contentPane!!.registerKeyboardAction(
            { e: ActionEvent? -> dispose() },
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        )

        //其它控件初始化
        sqliteRadioButton?.isSelected = true
    }

    //根据填写信息以及json数据数据生成对应数据库所需要的东西
    private fun onGenerate() {
        onSelectedType()//确定哪一种类型
        val content = tvArea?.text
        val result: JSON? = content?.toJSON()
        var json: JSONObject? = null
        if (result is JSONObject) {
            json = result
        } else if (result is JSONArray) {
            json = result.getJSONObject(0)
        }
        val tableBuilder =
            StringBuilder("CREATE TABLE IF NOT EXISTS ${tvTable!!.text}(id INTEGER PRIMARY KEY NOT NULL,")
        val selectBuilder = StringBuilder("SELECT * FROM ${tvTable!!.text} WHERE id = ?")
        val deleteBuilder = StringBuilder("DELETE FROM ${tvTable!!.text} WHERE id = ?")
        val insertBuilder = StringBuilder("INSERT INTO ${tvTable!!.text} (")
        val listKeys: MutableList<String> = mutableListOf()
        if (TextUtils.isEmpty(tvTable!!.text)) {
            tvErrorTip!!.text = "请输入表格TableName字段"
            return
        }
        tvErrorTip!!.text = ""
        for ((key, element) in json!!.innerMap) {
            if (DataBaseUtils.SENSORKEYS.contains(key)) {
                tvErrorTip!!.text = "含有数据库敏感字段${key}"
                return
            }
            tvErrorTip!!.text = ""
            listKeys.add(key)
            if (element is String) {
                tableBuilder.append("$key TEXT,")
            } else if (element is Int) {
                tableBuilder.append("$key INTEGER,")
            } else if (element is Boolean) {
                tableBuilder.append("$key INTEGER,")
            } else if (element is Double || element is Float) {
                tableBuilder.append("$key REAL,")
            } else {
                tableBuilder.append("$key TEXT,")
            }
        }
        //减去后面逗号
        tableBuilder.deleteAt(tableBuilder.length - 1)
        tableBuilder.append(");")
        for (i in listKeys.indices) {
            val key: String = listKeys[i]
            insertBuilder.append("$key ${if (i != listKeys.size - 1) "," else ""}")
        }
        insertBuilder.append(") VALUES (")
        for (i in listKeys.indices) {
            insertBuilder.append("? ${if (i != listKeys.size - 1) "," else ""}")
        }
        insertBuilder.append(")")

        tvCreateTable!!.text = tableBuilder.toString()
        tvSelect!!.text = selectBuilder.toString()
        tvDelete!!.text = deleteBuilder.toString()
        tvInsert!!.text = insertBuilder.toString()
    }

    private fun onSelectedType() {
        if (sqliteRadioButton!!.isSelected) {
            sqlType = TYPE_SQLITE
        }
        if (mysqlRadioButton!!.isSelected) {
            sqlType = TYPE_MYSQL
        }
    }

}

