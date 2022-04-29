package com.awesome.plugins.json2bean.database

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.awesome.plugins.json2bean.generators.BaseGenerator
import com.awesome.utils.regex
import com.intellij.psi.PsiDirectory
import toCamel
import toUpperCamel
import java.io.File

class DartDataBaseGenerator(
    content: String,
    val fileName: String,
    val dir: PsiDirectory,
) : BaseGenerator(content) {

    private lateinit var daoName: String

    override fun toString(): String {
        val projectName = dir.project.name
        val modelFile = File(dir.virtualFile.path, "$fileName.dart")
        val packagePath = modelFile.path.replace("${dir.project.basePath}/lib", "")

        val tableName = fileName.toUpperCamel()
        daoName = "${tableName}Dao"
        val classBuilder = StringBuilder()

        classBuilder.insert(0, "import 'package:$projectName$packagePath';\n")
        classBuilder.insert(0, "import 'package:json2dart_safe/json2dart.dart';\n")
        val tableSqlBuilder = tableSqlMethod(json, tableName)
        classBuilder.append("\nclass $daoName extends BaseDao<$tableName> {\n")
        classBuilder.append("$tableSqlBuilder")
        classBuilder.append("  @override\n  $tableName fromJson(Map json) => $tableName.fromJson(json);\n")
        classBuilder.append("}")
        return classBuilder.toString()
    }

    /***
     * 拼接生成Sql的语句代码
     * */
    private fun tableSqlMethod(
        obj: Any?,
        tableName: String
    ): java.lang.StringBuilder {
        val builder = StringBuilder("  static String tableSql([String? tableName]) {\n")
        builder.append("    StringBuffer _buffer = StringBuffer(\"CREATE TABLE IF NOT EXISTS `\${tableName ?? '$tableName'}` (\");\n")
        builder.append("    _buffer\n")
        var parseObj: JSONObject? = null
        if (obj is JSONObject) {
            parseObj = obj
        } else if (obj is JSONArray) {
            parseObj = obj[0] as JSONObject
        }
        for ((key, element) in parseObj!!.innerMap) {
            if (element is JSONObject || element is JSONArray) {
                builder.append("      ..write(\"`$key` TEXT,\")\n")
            } else {
                builder.append("      ..write(\"`$key` ${getType(element)},\")\n")
            }
        }

        var result = builder.toString()
        var index = result.lastIndexOf(",")
        result = result.replaceRange(index, index + 1, ")")
        index = result.lastIndexOf(")")
        result = result.replaceRange(index, index + 1, ");")

        builder.clear()
        builder.append(result)
        builder.append("    return _buffer.toString();\n").append("  }\n\n")
        return builder
    }


    private fun getType(element: Any): String {
        if (element is String) {
            return "TEXT"
        } else if (element is Int) {
            return "INTEGER"
        } else if (element is Double) {
            return "DOUBLE"
        } else if (element is Float) {
            return "FLOAT"
        } else if (element is Boolean) {
            return "BOOLEAN"
        } else {
            return "TEXT"
        }
    }

    /***
     * 开始写入文件到工程中
     **/
    fun startWrite() {
        val dbDir = File("${dir.project.basePath}/lib/database")
        if (!dbDir.exists()) {
            dbDir.mkdirs()
        }

        writeIntoTheDbManagerFile(dbDir)
        writeIntoTheDaoFile(dbDir)
    }

    /***
     * 根据json生成的dao 类，写入到代码中
     * */
    private fun writeIntoTheDaoFile(dbDir: File) {
        //写入dao类
        val daoFile = File(dbDir.path, "${fileName}_dao.dart")
        if (!daoFile.exists()) {
            daoFile.writeText(toString())
        }
    }

    /***
     * 将生成的类型写入到DBManager中
     * */
    private fun writeIntoTheDbManagerFile(dbDir: File) {
        //写入DBManager的类
        val dbManagerFile = File(dbDir.path, "db_manager.dart")
        val dbManagerBuilder = StringBuilder()
        if (dbManagerFile.exists()) {
            dbManagerBuilder.append(dbManagerFile.readText())
        } else {
            dbManagerBuilder.append("import 'dart:async';\n\nimport 'package:json2dart_safe/json2dart.dart';\nimport 'package:sqflite/sqflite.dart';\n\nclass DbManager extends BaseDbManager {\n  static DbManager? _instance;\n\n  factory DbManager() => _getInstance();\n\n  static DbManager get instance => _getInstance();\n\n  static DbManager _getInstance() {\n    _instance ??= DbManager._internal();\n    return _instance!;\n  }\n\n  DbManager._internal();\n\n  @override\n  FutureOr<void> onCreate(Database db, int version) async {\n  }\n}\n\n")
        }
        dbManagerBuilder.insert(0, "import '${fileName}_dao.dart';\n")
        val index = dbManagerBuilder.lastIndexOf("}")

        val fieldName = "_${fileName.toCamel()}Dao"
        dbManagerBuilder.insert(index, "  $daoName get ${fileName.toCamel()}Dao => $fieldName ??= $daoName();\n")
        dbManagerBuilder.insert(index, "\n  $daoName? $fieldName;\n\n")

        if (!dbManagerBuilder.contains("$daoName.tableSql")) {
            val mark = "FutureOr<void> onCreate(Database db, int version) async {"
            val markIndex = dbManagerBuilder.indexOf(mark) + mark.length
            dbManagerBuilder.insert(markIndex, "\n    await db.execute($daoName.tableSql());")
        }

        dbManagerFile.writeText(dbManagerBuilder.toString())
    }
}