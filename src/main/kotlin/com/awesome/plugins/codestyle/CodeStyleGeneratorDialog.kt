package com.awesome.plugins.codestyle

import com.awesome.plugins.codestyle.interceptor.TailWindProcessor
import com.awesome.utils.runWriteCmd
import com.intellij.openapi.editor.Editor
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*

/**
 * 将复制的Css转换成TailWindCss
 **/
class CodeStyleGeneratorDialog(private val editor: Editor) : JDialog() {
    var contentPane: JPanel? = null
    var buttonOK: JButton? = null
    var buttonCancel: JButton? = null
    var btnConvert: JButton? = null
    var tvFrom: JTextArea? = null
    var tvTo: JTextArea? = null

    init {
        setContentPane(contentPane)
        setModal(true)
        getRootPane().setDefaultButton(buttonOK)
        buttonOK!!.addActionListener { onGenerate() }
        buttonCancel!!.addActionListener { dispose() }
        btnConvert!!.addActionListener { onConvert() }

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

    private fun onGenerate() {
        editor.runWriteCmd {
            val selectionModel = editor.selectionModel
            var content = ""
            if (tvTo!!.text.isNotEmpty()) {
                content = tvTo!!.text
            } else {
                content = TailWindProcessor(editor).process(tvFrom!!.text!!)
            }
            editor.document.replaceString(selectionModel.selectionStart, selectionModel.selectionEnd, content)
            dispose()
        }
    }

    private fun onConvert() {
        editor.runWriteCmd {
            val content = TailWindProcessor(editor).process(tvFrom!!.text!!)
            tvTo!!.text = content
        }
    }

    fun showDialog(): CodeStyleGeneratorDialog {
        pack()
        setLocationRelativeTo(null)
        isVisible = true
        return this
    }

}
