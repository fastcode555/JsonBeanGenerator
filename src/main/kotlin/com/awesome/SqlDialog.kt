package com.awesome

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.awesome.generators.PythonSqlGenerator
import com.awesome.utils.DataBaseUtils
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import org.apache.http.util.TextUtils
import toCamel
import toJSON
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.lang.StringBuilder
import javax.swing.*

class SqlDialog(val project: Project?, val directory: PsiDirectory) : JDialog() {
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
        buttonOK!!.addActionListener { onGenerate(true) }
        buttonCancel!!.addActionListener { dispose() }
        btnGenerate!!.addActionListener { onGenerate(false) }
        pythonRadioButton!!.isSelected = true
        // call onCancel() when cross is clicked
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                dispose()
            }
        })

        // call onCancel() on ESCAPE
        contentPane!!.registerKeyboardAction(
            { dispose() },
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        )

        //其它控件初始化
        sqliteRadioButton?.isSelected = true
    }

    //根据填写信息以及json数据数据生成对应数据库所需要的东西
    private fun onGenerate(generate: Boolean) {
        onSelectedType()//确定哪一种类型
        val content = tvArea?.text
        val result: JSON? = content?.toJSON()
        var json: JSONObject? = null
        if (result is JSONObject) {
            json = result
        } else if (result is JSONArray) {
            json = result.getJSONObject(0)
        }
        val tableName = tvTable!!.text

        if (TextUtils.isEmpty(tableName)) {
            tvErrorTip!!.text = "请输入表格TableName字段"
            return
        }
        val tableBuilder =
            StringBuilder("CREATE TABLE IF NOT EXISTS %s(id INTEGER PRIMARY KEY NOT NULL,")
        val selectBuilder = StringBuilder("SELECT * FROM %s WHERE id = ?")
        val deleteBuilder = StringBuilder("DELETE FROM %s WHERE id = ?")
        val insertBuilder = StringBuilder("INSERT INTO %s (id,")
        val insertArgsBuilder = StringBuilder()
        val updateBuilder = StringBuilder("UPDATE %s SET ")

        val codeGenerator = PythonSqlGenerator(tableName, directory)
        val listKeys: MutableList<String> = mutableListOf()
        val listValues: MutableList<Any> = mutableListOf()

        tvErrorTip!!.text = ""
        for ((key, element) in json!!.innerMap) {
            if (DataBaseUtils.SENSORKEYS.contains(key)) {
                tvErrorTip!!.text = "含有数据库敏感字段${key}"
                return
            }
            tvErrorTip!!.text = ""
            if (key.equals("id")) {
                continue
            }
            listKeys.add(key)
            if (element is String) {
                tableBuilder.append("$key TEXT,")
                listValues.add("'${element.toString()}'")
            } else if (element is Double || element is Float) {
                tableBuilder.append("$key REAL,")
                listValues.add(element)
            } else if (element is Int) {
                tableBuilder.append("$key INTEGER,")
                listValues.add(element)
            } else if (element is Boolean) {
                tableBuilder.append("$key INTEGER,")
                listValues.add(if (element) 1 else 0)
            } else {
                tableBuilder.append("$key TEXT,")
                listValues.add("'${element.toString()}'")
            }
        }
        //减去后面逗号
        tableBuilder.deleteAt(tableBuilder.length - 1)
        tableBuilder.append(");")
        for (i in listKeys.indices) {
            val key: String = listKeys[i]
            insertBuilder.append("$key${if (i != listKeys.size - 1) "," else ""}")
            insertArgsBuilder.append("bean.${key.toCamel()}${if (i != listKeys.size - 1) "," else ""}")
            updateBuilder.append("$key = ? ${if (i != listKeys.size - 1) "," else ""}")
        }
        insertBuilder.append(") VALUES (?,")
        for (i in listKeys.indices) {
            insertBuilder.append("?${if (i != listKeys.size - 1) "," else ""}")
        }
        insertBuilder.append(")")
        updateBuilder.append("WHERE id = ?;")
        tvCreateTable!!.text = tableBuilder.toString().format(tableName)
        tvSelect!!.text = selectBuilder.toString().format(tableName)
        tvDelete!!.text = deleteBuilder.toString().format(tableName)
        tvInsert!!.text = insertBuilder.toString().format(tableName)
        tvUpdate!!.text = updateBuilder.toString().format(tableName)
        if (generate) {
            codeGenerator.initMethod(tableBuilder.toString())
                .insertMethod(insertBuilder.toString(), insertArgsBuilder.toString())
                .generateFile(this, listValues)
        }
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

