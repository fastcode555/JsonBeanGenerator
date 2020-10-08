package com.awesome

import com.awesome.generators.DartJsonGenerator
import com.intellij.psi.PsiDirectory
import formatJson
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import javax.swing.*

class Json2DartDialog(val mDirectory:  PsiDirectory) : JDialog() {
    var contentPane: JPanel? = null
    var formatBtn: JButton? = null
    var confirmBtn: JButton? = null
    var tvField: JTextArea? = null
    var tvClassField: JTextField? = null

    private fun onGenerate() {
        if (tvClassField!!.text != null && tvClassField?.text != null && tvClassField!!.text!!.isNotEmpty()) {
            val file = File(mDirectory.virtualFile.path, tvClassField?.text + ".dart")
            if (!file.exists()) {
                file.writeText(DartJsonGenerator(tvField!!.text, tvClassField!!.text).toJson())
            }
        }
        dispose()
    }

    fun showDialog(): Json2DartDialog {
        pack()
        setLocationRelativeTo(null)
        isVisible = true
        return this
    }

    init {
        setContentPane(contentPane)
        isModal = true
        getRootPane().defaultButton = formatBtn
        formatBtn!!.addActionListener { e: ActionEvent? -> tvField?.text = tvField?.text.formatJson() }
        confirmBtn!!.addActionListener { e: ActionEvent? -> onGenerate() }
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
    }
}