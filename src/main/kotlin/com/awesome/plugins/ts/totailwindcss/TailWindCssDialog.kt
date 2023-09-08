package com.awesome.plugins.ts.totailwindcss

import com.awesome.plugins.ts.totailwindcss.processor.BaseProcessor
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*

/**
 * 将复制的Css转换成TailWindCss
 **/
class TailWindCssDialog(private val editor: Editor, private val processors: List<BaseProcessor>) : JDialog() {
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
        WriteCommandAction.runWriteCommandAction(editor.project) {
            val selectionModel = editor.selectionModel
            var content = tvTo!!.text
            if (content.isEmpty()) {
                content = tvFrom!!.text
                processors.forEach { content = it.process(content) }
            }
            editor.document.replaceString(selectionModel.selectionStart, selectionModel.selectionEnd, content)
            dispose()
        }
    }

    private fun onConvert() {
        WriteCommandAction.runWriteCommandAction(editor.project)
        {
            var content = tvFrom!!.text
            processors.forEach { content = it.process(content) }
            tvTo!!.text = content
        }

    }

    fun showDialog(): TailWindCssDialog {
        pack()
        setLocationRelativeTo(null)
        isVisible = true
        return this
    }

}
