package com.awesome.plugins.language

import com.awesome.utils.regex
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import okhttp3.internal.toHexString
import toUpperCamel
import java.awt.event.ActionEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.event.KeyEvent
import javax.swing.*

/**
 * 对Key开启批量混淆的功能
 **/
class LanguageObfuscateDialog(val editor: Editor?) : JDialog() {
    var contentPane: JPanel? = null
    var buttonOK: JButton? = null
    var buttonCancel: JButton? = null
    var tvSuffix: JTextField? = null
    var count = 0

    //开始陪陪数据进行混淆
    private fun obfuscateResource() {
        try {
            WriteCommandAction.runWriteCommandAction(editor!!.project) {
                val content = editor.document.text
                //匹配所有的双引号或者单引号内的key
                content.regex("[\\n ]*[\\'\"]{1}.*?[\\'\"]{1}") {
                    try {
                        val index = editor.document.text.indexOf(it)
                        val hex = count.toHexString().uppercase()
                        val key = if (tvSuffix!!.text!!.isEmpty()) "$hex" else "${tvSuffix!!.text}#$hex"
                        editor.document.replaceString(index, index + it.length, " '$key'")
                        count++
                    } catch (e: Exception) {
                        count++
                    }
                }
            }
        } catch (e: Exception) {
        }

    }

    private fun onOK() {
        obfuscateResource()
        dispose()
    }

    private fun onCancel() {
        dispose()
    }

    init {
        setContentPane(contentPane)
        isModal = true
        getRootPane().defaultButton = buttonOK
        buttonOK!!.addActionListener { e: ActionEvent? -> onOK() }
        buttonCancel!!.addActionListener { e: ActionEvent? -> onCancel() }

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
    }

    fun showDialog(): LanguageObfuscateDialog {
        pack()
        setLocationRelativeTo(null)
        isVisible = true
        return this
    }
}