package com.awesome.plugins.language

import clearSymbol
import com.alibaba.fastjson2.JSONObject
import com.awesome.common.MultiBase
import com.awesome.common.PluginProps
import com.awesome.utils.*
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.NlsSafe
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileSystemItem
import toCamel
import toJSON
import toUpperCamel
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import javax.swing.*
import javax.swing.filechooser.FileFilter

/**
 * 对Key开启批量混淆的功能
 **/
class LanguageObfuscateDialog(val editor: Editor?, val psiFile: PsiFile) : JDialog() {
    var contentPane: JPanel? = null

    ///混淆
    var buttonOK: JButton? = null

    ///反混淆
    var antiObfuscate: JButton? = null

    ///导入Json的按钮
    var btnImportJson: JButton? = null

    ///导出Json的按钮
    var btnExportJson: JButton? = null

    var tvSuffix: JTextField? = null
    var count = "0"

    /**
     * 开始对数据进行混淆,intellij 这个方法是有效的
     **/
    private fun obfuscateResource() {
        val content = editor!!.document.text
        val keyLists = hashMapOf<String, String>()
        val allKeys = arrayListOf<String>()
        //匹配所有的双引号或者单引号内的key
        content.regex("[\\n ]*[\\'\\\"]{1}.*?[\\'\\\"]{1}(?=;)") {
            try {
                val index = editor.document.text.indexOf(it)
                val key = if (tvSuffix!!.text!!.isEmpty()) "$count" else "${tvSuffix!!.text}#$count"
                editor.document.replaceString(index, index + it.length, " '$key'")
                var rawKey = it.trim()
                rawKey = rawKey.substring(1, rawKey.length - 1)
                allKeys.add(rawKey)
                if (rawKey != key) {
                    keyLists[rawKey] = key
                }
                count = MultiBase.addOne(count)
            } catch (e: Exception) {
                count = MultiBase.addOne(count)
            }
        }
        val properties = PropertiesHelper(psiFile)
        val value = properties.getProperty(PluginProps.languageAssetsDir)
        var dir = psiFile.findExistsDirectory(value) ?: return
        if (dir.exists()) {
            dir.listFiles().forEach {
                val json = try {
                    it.readText().toJSON() as JSONObject
                } catch (e: Exception) {
                    return@forEach
                }
                val removeKeys = arrayListOf<String>()
                for ((key, _) in json) {
                    if (!allKeys.contains(key)) {
                        removeKeys.add(key)
                    }
                }
                for (key in removeKeys) {
                    println("remove useless $key")
                    json.remove(key)
                }
                val newJson = JSONObject()
                for ((key, newKey) in keyLists) {
                    newJson.put(newKey, json.remove(key))
                }
                for ((key, ele) in json) {
                    newJson.put(key, ele)
                }
                it.writeText(newJson.toJSONString())
                removeKeys.clear()
            }
        }
        dispose()
    }

    /**
     * 找寻项目中存在的语言文件的文件夹
     **/
    private fun PsiFileSystemItem.findExistsDirectory(value: String?): File? {
        val parent = this.parent
        val file = File(parent!!.virtualFile.path, value)
        if (file.exists()) {
            return file
        }
        if (this.basePath() == file.path) return null
        return parent.parent!!.findExistsDirectory(value)
    }

    /**
     * 对页面进行反混淆
     **/
    private fun onAntiObfuscate() {
        val maps = queryKeyAndEnValue() ?: return
        val restoreKeys = hashMapOf<String, String>()
        val allKeys = arrayListOf<String>()
        //根据匹配到的所有内容回复当前文件的所有key
        maps.forEach { (key, value) ->
            val pattern = " $key \\=[ \\n]*[\\'\\\"].*?[\\'\\\"];"
            editor?.apply {
                val matchContent = document.text.regexOne(pattern) ?: return@forEach
                val content = " $key = '$value';"
                val index = document.text.indexOf(matchContent)
                var oldKey = matchContent.split("=").last().trim()
                oldKey = oldKey.substring(1, oldKey.length - 2)
                if (value != null) {
                    restoreKeys[oldKey] = value
                    allKeys.add(oldKey)
                }
                try {
                    document.replaceString(index, index + matchContent.length, content)
                } catch (e: Exception) {
                    print(e)
                }
            }
        }
        val properties = PropertiesHelper(psiFile)
        val value = properties.getProperty(PluginProps.languageAssetsDir)
        var dir = psiFile.findExistsDirectory(value) ?: return
        if (dir.exists()) {
            dir.listFiles().forEach {
                val json = try {
                    it.readText().toJSON() as JSONObject
                } catch (e: Exception) {
                    return@forEach
                }
                val removeKeys = arrayListOf<String>()
                for ((key, _) in json) {
                    if (!allKeys.contains(key)) {
                        removeKeys.add(key)
                    }
                }
                for (key in removeKeys) {
                    println("remove useless $key")
                    json.remove(key)
                }
                val newJson = JSONObject()
                for ((key, ele) in json) {
                    newJson.put(restoreKeys[key], ele)
                }
                it.writeText(newJson.toJSONString().trimJson())
                removeKeys.clear()
            }
        }
        dispose()
    }

    /**
     * 查询驼峰字段对应的英语值
     **/
    private fun queryKeyAndEnValue(): HashMap<String, String?>? {
        val enFilePath = psiFile.virtualFile?.path?.replace("strings.dart", "string_en.dart")
        if (enFilePath != null) {
            val maps = HashMap<String, String?>()
            val enFile = File(enFilePath)
            val content = enFile.readText()
            //正则读取多有字段，并切匹配出所有数据
            content.regex("(?<=Ids\\.)(.*?)\\:[ \\n]*['|\"].*?['|\"](?=[,]*\\n)") {
                val index = it.indexOf(":")
                val key = it.substring(0, index)
                var value = it.substring(index + 1, it.length).trim()
                value = value.substring(1, value.length - 1)
                maps[key] = value
            }
            return maps
        }
        return null
    }

    /**
     * 查询对应的stringKeys
     **/
    private fun queryStringKeys(): HashMap<String, String?>? {
        val idsFilePath = psiFile.virtualFile?.path
        if (idsFilePath != null) {
            val maps = HashMap<String, String?>()
            val enFile = File(idsFilePath)
            val content = enFile.readText()
            content.regex("(?<=String ).*? =[ \\n]*['|\"].*?['|\"](?=;)") {
                val arrays = it.split("=")
                val key = arrays.first().trim()
                var value = arrays.last().trim()
                value = value.substring(1, value.length - 1)
                maps[key] = value
            }
            return maps
        }
        return null
    }

    /**
     * 导出Json文件
     **/
    private fun onExportJson() {
        psiFile.apply {
            val maps = queryStringKeys() ?: return
            val dir = findLanguageDir()
            if (maps.isEmpty()) return
            virtualFile.parent.children.forEach { file ->
                if (file.name.startsWith("string_")) {
                    val realFile = File(file.path)
                    val content = realFile.readText()
                    val jsonObj = JSONObject()
                    val jsonFile = File("${dir.path}/${file.nameWithoutExtension.replace("string_", "")}.json")
                    content.regex("(?<=Ids\\.)(.*?)\\:[ \\n]*['|\"].*?['|\"](?=[,]*\\n)") {
                        val index = it.indexOf(":")
                        val key = it.substring(0, index)
                        var value = it.substring(index + 1, it.length).trim()
                        value = value.substring(1, value.length - 1)
                        jsonObj.put(maps[key], value)
                    }
                    val string = jsonObj.toJSONString().trimJson()
                    jsonFile.writeText(string)
                }
            }
        }
        dispose()
    }

    private fun String.trimJson(): String {
        return this.replace("\\\\n", "\\n").replace("\\'", "'")
    }

    /**
     *  导入Json文件，生成对应的key值
     **/
    private fun onImportJson() {
        psiFile.apply {
            val file = selectFile() ?: return
            editor ?: return
            val json = file.readText().toJSON() as JSONObject
            val editorContent = editor.document.text
            for ((key, element) in json) {
                val idKey = key?.newIdKey(editorContent)
                if (editorContent.contains("\"${element}\"") || editorContent.contains("'${element}'")) {
                    continue
                }
                var index = editor.document.text.lastIndexOf("}")
                if (name == "strings.dart") {
                    if (index < 0) {
                        val header = "class Ids {\n}\n"
                        editor.document.insertString(editor.document.text.length, header)
                    }
                    index = editor.document.text.lastIndexOf("}")
                    editor.document.insertString(index, "  static const String $idKey = '$element';\n")
                } else if (name.startsWith("string_")) {
                    if (index < 0) {
                        val header =
                            "import 'package:eplay/res/strings.dart';\n\nconst Map<String, String> language${file.nameWithoutExtension.toUpperCamel()} = {\n};\n"
                        editor.document.insertString(editor.document.text.length, header)
                    }
                    index = editor.document.text.lastIndexOf("}")
                    editor.document.insertString(index, "  Ids.$idKey: '$element',\n")
                }
            }

        }
        dispose()
    }


    private fun String.newIdKey(content: @NlsSafe String): String {
        val idKey = this.replace(" ", "_").trim().clearSymbol().toCamel()
        val count = content.showCount("static const String $idKey ")
        if (count == 0) {
            return idKey
        }
        return "$idKey${count + 1}"
    }

    /**
     * 创建文件选择器
     **/
    private fun selectFile(): File? {
        val fileChooser = JFileChooser(psiFile.virtualFile?.parent?.path ?: "")
        fileChooser.fileFilter = object : FileFilter() {
            override fun accept(f: File?): Boolean {
                return f?.extension == ".json"
            }

            override fun getDescription(): String {
                return ""
            }
        }
        fileChooser.fileSelectionMode = JFileChooser.FILES_ONLY
        try {
            val option = fileChooser.showSaveDialog(this)
            if (option == JFileChooser.APPROVE_OPTION) {
                return fileChooser.selectedFile
            }
        } catch (e: Exception) {
            println(e.toString())
        }
        return null
    }


    /**
     * 创建languages这个文件夹
     **/
    private fun findLanguageDir(): File {
        val languageDir = "${psiFile.parent?.virtualFile?.path}/languages"
        val dir = File(languageDir)
        if (!dir.exists()) {
            dir.mkdir()
        }
        return dir
    }

    init {
        setContentPane(contentPane)
        isModal = true
        getRootPane().defaultButton = buttonOK
        buttonOK!!.addActionListener { psiFile.runWriteCmd(::obfuscateResource) }
        btnImportJson!!.addActionListener { psiFile.runWriteCmd(::onImportJson) }
        antiObfuscate!!.addActionListener { psiFile.runWriteCmd(::onAntiObfuscate) }
        btnExportJson!!.addActionListener { psiFile.runWriteCmd(::onExportJson) }

        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                dispose()
            }
        })

        contentPane!!.registerKeyboardAction(
            { e: ActionEvent? -> dispose() },
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        )
        val content = editor!!.document.text
        content.regex("(?<=[\\'\"]{1}).*?(?=[\\'\"]{1})") {
            if (tvSuffix!!.text.isEmpty() && it.contains("#")) {
                tvSuffix!!.text = it.split('#').first()
            }
        }
    }


    ///显示弹窗
    fun showDialog(): LanguageObfuscateDialog {
        pack()
        setLocationRelativeTo(null)
        isVisible = true
        return this
    }
}
