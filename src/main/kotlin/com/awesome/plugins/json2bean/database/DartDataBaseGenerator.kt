package com.awesome.plugins.json2bean.database

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.awesome.plugins.json2bean.generators.BaseGenerator
import com.intellij.psi.PsiDirectory
import toCamel
import toUpperCamel
import java.io.File

class DartDataBaseGenerator(
    content: String,
    private val fileName: String,
    private val dir: PsiDirectory,
    private val primaryKey: String,
) : BaseGenerator(content) {

    private var daoName: String = "${fileName.toUpperCamel()}Dao"
    private var isAutoIncrease = false

    override fun toString(): String {
        val projectName = dir.project.name
        val modelFile = File(dir.virtualFile.path, "$fileName.dart")
        val packagePath = modelFile.path.replace("${dir.project.basePath}/lib", "")

        val tableName = fileName.toUpperCamel()
        val classBuilder = StringBuilder()

        classBuilder.insert(0, "import 'package:$projectName$packagePath';\n")
        classBuilder.insert(0, "import 'package:json2dart_safe/json2dart.dart';\n")
        val tableSqlBuilder = tableSqlMethod(json, tableName)
        classBuilder.append("\nclass $daoName extends BaseDao<$tableName> {\n")
        classBuilder.append("$tableSqlBuilder")
        classBuilder.append("  @override\n  $tableName fromJson(Map json) => $tableName.fromJson(json);\n")
        classBuilder.append(primaryKeyMethod(tableName))
        classBuilder.append("}")
        return classBuilder.toString()
    }

    /***
     * 为每个dao生成一个根据primary key 进行数据查询的方法
     * */
    private fun primaryKeyMethod(tableName: String): String {
        return "\n  @override\n  String get primaryKey => '$primaryKey';\n"
    }

    /***
     * 拼接生成Sql的语句代码
     * */
    private fun tableSqlMethod(
        obj: Any?,
        tableName: String
    ): String {
        val builder = StringBuilder("  static String tableSql([String? tableName]) => \"\"\n")
        builder.append("      \"CREATE TABLE IF NOT EXISTS `\${tableName ?? '$tableName'}` (\"\n")
        var parseObj: JSONObject? = null
        if (obj is JSONObject) {
            parseObj = obj
        } else if (obj is JSONArray) {
            parseObj = obj[0] as JSONObject
        }
        for ((key, element) in parseObj!!.innerMap) {
            if (element is JSONObject || element is JSONArray) {
                builder.append("      \"`$key` TEXT,\"\n")
            } else {
                builder.append("      \"`$key` ${getType(element)}${addPrimaryKey(key)},\"\n")
            }
        }
        if (!builder.contains("`$primaryKey`")) {
            isAutoIncrease = true
            builder.append("      \"`$primaryKey` INTEGER PRIMARY KEY AUTOINCREMENT,\"\n")
        }

        var result = builder.toString()
        var index = result.lastIndexOf(",")
        if (index > 0) {
            result = result.replaceRange(index, index + 1, ")")
        }
        builder.clear()
        builder.append(result)
        return "${builder.trimEnd()};\n\n"
    }

    private fun addPrimaryKey(key: String): String {
        if (key == primaryKey) {
            return " PRIMARY KEY"
        }
        return ""
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
        println("start to write DartDataBaseGenerator")
        val dbDir = File("${dir.project.basePath}/lib/database")
        if (!dbDir.exists()) {
            dbDir.mkdirs()
        }

        writeIntoTheDaoFile(dbDir)
        writeIntoTheDbManagerFile(dbDir)
    }

    /***
     * 根据json生成的dao 类，写入到代码中
     * */
    private fun writeIntoTheDaoFile(dbDir: File) {
        println("开始写入到${fileName} File 中：writeIntoTheDaoFile")
        //写入dao类
        val daoFile = File(dbDir.path, "${fileName}_dao.dart")
        if (!daoFile.exists()) {
            daoFile.createNewFile()
            val result = toString()
            println("开始写入到${fileName} 的内容:$result")
            daoFile.writeText(result)
        }
    }

    /***
     * 将生成的类型写入到DBManager中
     * */
    private fun writeIntoTheDbManagerFile(dbDir: File) {
        print("开始写入到DbMangaer File 中：writeIntoTheDbManagerFile")
        //写入DBManager的类
        val dbManagerFile = File(dbDir.path, "db_manager.dart")
        val dbManagerBuilder = StringBuilder()
        if (dbManagerFile.exists()) {
            dbManagerBuilder.append(dbManagerFile.readText())
        } else {
            dbManagerFile.createNewFile()
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

        print("开始写入到DbMangaer File 中：writeIntoTheDbManagerFile:$dbManagerBuilder")
        dbManagerFile.writeText(dbManagerBuilder.toString())
        println("开始写入到DbManager 的内容:${dbManagerBuilder.toString()}")
    }
}