package com.awesome.plugins.language

import com.alibaba.fastjson.JSONObject
import com.awesome.utils.regex
import com.awesome.utils.regexOne
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import java.awt.event.ActionEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.event.KeyEvent
import java.io.File
import javax.swing.*

/**
 * 对Key开启批量混淆的功能
 **/
class LanguageObfuscateDialog(val editor: Editor?, val psiFile: PsiFile?) : JDialog() {
    var contentPane: JPanel? = null

    ///混淆
    var buttonOK: JButton? = null

    ///反混淆
    var antiObfuscate: JButton? = null

    ///取消按钮
    var buttonCancel: JButton? = null

    ///导出Json的按钮
    var btnExportJson: JButton? = null

    var tvSuffix: JTextField? = null
    var count = 0

    private fun runBackGround(runnable: Runnable) {
        WriteCommandAction.runWriteCommandAction(psiFile!!.project, runnable)
    }

    /**
     * 开始对数据进行混淆,intellij 这个方法是有效的
     **/
    private fun obfuscateResource() {
        val content = editor!!.document.text
        //匹配所有的双引号或者单引号内的key
        content.regex("[\\n ]*[\\'\\\"]{1}.*?[\\'\\\"]{1}(?=;)") {
            try {
                val index = editor.document.text.indexOf(it)
                val hex = Integer.toHexString(count).uppercase()
                val key = if (tvSuffix!!.text!!.isEmpty()) "$hex" else "${tvSuffix!!.text}#$hex"
                editor.document.replaceString(index, index + it.length, " '$key'")
                count++
            } catch (e: Exception) {
                count++
            }
        }
        dispose()
    }

    /**
     * 对页面进行反混淆
     **/
    private fun onAntiObfuscate() {
        val maps = queryKeyAndEnValue() ?: return
        //根据匹配到的所有内容回复当前文件的所有key
        maps.forEach { (key, value) ->
            val pattern = " $key \\=[ \\n]*[\\'\\\"].*?[\\'\\\"];"
            editor?.apply {
                val matchContent = document.text.regexOne(pattern) ?: return@forEach
                val content = " $key = '$value';"
                val index = document.text.indexOf(matchContent)
                try {
                    document.replaceString(index, index + matchContent.length, content)
                } catch (e: Exception) {
                    print(e)
                }
            }
        }
        dispose()
    }

    /**
     * 查询驼峰字段对应的英语值
     **/
    private fun queryKeyAndEnValue(): HashMap<String, String?>? {
        val enFilePath = psiFile?.virtualFile?.path?.replace("strings.dart", "string_en.dart")
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
        val idsFilePath = psiFile?.virtualFile?.path
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
        psiFile?.apply {
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
                    jsonFile.writeText(jsonObj.toString())
                }
            }
        }
        dispose()
    }

    private fun findLanguageDir(): File {
        val languageDir = "${psiFile?.parent?.virtualFile?.path}/languages"
        val dir = File(languageDir)
        if (!dir.exists()) {
            dir.mkdir()
        }
        return dir
    }

    private fun onCancel() {
        dispose()
    }

    init {
        setContentPane(contentPane)
        isModal = true
        getRootPane().defaultButton = buttonOK
        buttonOK!!.addActionListener { runBackGround { obfuscateResource() } }
        buttonCancel!!.addActionListener { onCancel() }
        antiObfuscate!!.addActionListener { runBackGround { onAntiObfuscate() } }
        btnExportJson!!.addActionListener { runBackGround { onExportJson() } }

        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                onCancel()
            }
        })

        contentPane!!.registerKeyboardAction(
            { e: ActionEvent? -> onCancel() },
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
