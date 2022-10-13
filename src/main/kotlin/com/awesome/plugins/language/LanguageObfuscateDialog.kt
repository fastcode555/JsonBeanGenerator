package com.awesome.plugins.language

import com.awesome.utils.matchOneRegex
import com.awesome.utils.matchRegexOne
import com.awesome.utils.regex
import com.awesome.utils.regexOne
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import okhttp3.internal.toHexString
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
    var buttonOK: JButton? = null
    var buttonCancel: JButton? = null
    var tvSuffix: JTextField? = null
    var count = 0

    //开始对数据进行混淆,intellij 这个方法是有效的
    private fun obfuscateResource() {
        WriteCommandAction.runWriteCommandAction(psiFile!!.project) {
            val content = editor!!.document.text
            //匹配所有的双引号或者单引号内的key
            content.regex("[\\n ]*[\\'\"]{1}.*?[\\'\"]{1}") {
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
    }

    private fun onOK() {
        obfuscateResource()
        //androidStudioObfuscateResource()
    }

    private fun onCancel() {
        dispose()
    }

    init {
        setContentPane(contentPane)
        isModal = true
        getRootPane().defaultButton = buttonOK
        buttonOK!!.addActionListener { onOK() }
        buttonCancel!!.addActionListener { onCancel() }

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

    fun showDialog(): LanguageObfuscateDialog {
        pack()
        setLocationRelativeTo(null)
        isVisible = true
        return this
    }
}