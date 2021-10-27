package com.awesome.generators

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiDirectory
import toUpperCamel
import java.awt.Dialog
import java.io.File


class PythonSqlGenerator(val tableName: String, val directory: PsiDirectory) {
    var className: String? = null
    val pythonBuilder by lazy {
        java.lang.StringBuilder("import sqlite3\nimport json\n\nTABLE_NAME='$className'\n\n\nclass ${className}Dao:\n")
    }
    var projectDbName = ""

    init {
        className = tableName.toUpperCamel()
        projectDbName = directory.project.baseDir.name
    }

    //创建初始化代码
    fun initMethod(sql: String): PythonSqlGenerator {
        val builder = StringBuilder()
        builder.append("\tdef __init__(self):\n")
            .append("\t\tself.conn = sqlite3.connect('./${projectDbName}.db')\n")
            .append("\t\tself.cursor = self.conn.cursor()\n")
            .append("\t\tself.cursor.execute(f'${sql.format("{TABLE_NAME}")}')\n\n")
        pythonBuilder.append(builder.toString())
        return this
    }

    //删除数据
    private fun deleteMethod(): PythonSqlGenerator {
        val builder = StringBuilder()
        builder.append("\tdef delete(self, id):\n")
            .append("\t\tself.cursor.execute(f'DELETE FROM {TABLE_NAME} WHERE id= ?', [id])\n\n")
        pythonBuilder.append(builder.toString())
        return this
    }

    //插入数据
    fun insertMethod(sql: String, args: String, insertArgHeader: String): PythonSqlGenerator {
        val builder = StringBuilder()
        builder.append("\tdef insert(self, *args):\n")
            .append("\t\tif len(args) == 1:\n")
            .append("\t\t\tbean = args[0]\n")
            .append(insertArgHeader)
            .append("\t\t\tself.cursor.execute(f'${sql.format("{TABLE_NAME}")}', [bean.id,${args}])\n")
            .append("\t\telse:\n")
            .append("\t\t\tself.cursor.execute(f'${sql.format("{TABLE_NAME}")}', args)\n")
            .append("\t\tself.conn.commit()\n\n")
        pythonBuilder.append(builder.toString())
        return this
    }

    //筛选数据
    private fun selectMethod(): PythonSqlGenerator {
        val builder = StringBuilder()
        builder.append("\tdef select(self, id):\n")
            .append("\t\tresults = self.cursor.execute(f'SELECT * FROM {TABLE_NAME} WHERE id= ?',[id]).fetchall()\n")
            .append("\t\treturn None if results is None or len(results) <= 0 else results[0]\n\n")
        pythonBuilder.append(builder.toString())
        return this
    }

    private fun selectAllMethod(): PythonSqlGenerator {
        val builder = StringBuilder()
        builder.append("\tdef selectAll(self):\n")
            .append("\t\treturn self.cursor.execute(f'SELECT * FROM {TABLE_NAME}').fetchall()\n\n")
        pythonBuilder.append(builder.toString())
        return this
    }

    //插入数据
    private fun closeMethod(): PythonSqlGenerator {
        val builder = StringBuilder()
        builder.append("\tdef close(self):\n")
            .append("\t\tself.cursor.close()\n")
            .append("\t\tself.conn.close()\n\n")
        pythonBuilder.append(builder.toString())
        return this
    }

    fun generateFile(dialog: Dialog, listValues: MutableList<Any>) {
        deleteMethod().selectAllMethod().selectMethod().closeMethod()
        pythonBuilder.append('\n')
        pythonBuilder.append("dao =${className}Dao()\n")
        pythonBuilder.append("dao.insert(1,")
        for (value in listValues) {
            pythonBuilder.append(value)?.append(',')
        }
        pythonBuilder.deleteAt(pythonBuilder.length - 1)
        pythonBuilder.append(")\n").append("dao.close()\n")

        val content = pythonBuilder.toString()
        val file = File(directory.virtualFile.path, "${tableName}_dao.py")
        if (!file.exists()) {
            try {
                WriteCommandAction.runWriteCommandAction(directory.project) {
                    file.writeText(content)
                    dialog.dispose()
                }
            } catch (e: Exception) {
            }
        }
    }
}