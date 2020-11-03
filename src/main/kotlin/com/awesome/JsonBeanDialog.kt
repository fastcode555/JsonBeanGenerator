package com.awesome

import com.awesome.generators.DartJsonGenerator
import com.awesome.utils.NotifyUtils
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiDirectory
import formatJson
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import javax.swing.*

class JsonBeanDialog(val mDirectory: PsiDirectory) : JDialog() {
    var contentPane: JPanel? = null
    var formatBtn: JButton? = null
    var confirmBtn: JButton? = null
    var previewBtn: JButton? = null
    var tvField: JTextArea? = null
    var tvClassField: JTextField? = null
    var tvExtends: JTextField? = null
    var tvImplements: JTextField? = null


    private fun isEmpty(text: String): Boolean {
        return text == null || text.isEmpty()
    }

    private fun onGenerate() {
        if (isEmpty(tvClassField!!.text)) {
            tvClassField!!.text = "auto_generated_name"
        }
        val file = File(mDirectory.virtualFile.path, tvClassField?.text + ".dart")
        if (!file.exists()) {
            try {
                WriteCommandAction.runWriteCommandAction(mDirectory.project) {
                    file.writeText(
                        DartJsonGenerator(
                            tvField!!.text,
                            tvClassField!!.text,
                            tvExtends!!.text,
                            tvImplements!!.text
                        ).toJson()
                    )
                }
            } catch (e: Exception) {
                NotifyUtils.showError(mDirectory.project, e.toString())
            }
        }
        dispose()

    }

    private fun onPreView() {
        if (isEmpty(tvClassField!!.text)) {
            tvClassField!!.text = "auto_generated_name"
        }
        try {
            val content = DartJsonGenerator(
                tvField!!.text,
                tvClassField!!.text,
                tvExtends!!.text,
                tvImplements!!.text
            ).toJson();
            val previewDialog = PreViewDialog(content)
            previewDialog.showDialog()
        } catch (e: Exception) {
            NotifyUtils.showError(mDirectory.project, e.toString())
        }
    }

    fun showDialog(): JsonBeanDialog {
        pack()
        setLocationRelativeTo(null)
        isVisible = true
        return this
    }

    init {
        setContentPane(contentPane)
        isModal = true
        getRootPane().defaultButton = formatBtn
        formatBtn!!.addActionListener { e: ActionEvent? ->
            try {
                tvField?.text = tvField?.text?.formatJson()
            } catch (e: Exception) {
                NotifyUtils.showError(mDirectory.project, e.toString())
            }
        }
        confirmBtn!!.addActionListener { e: ActionEvent? -> onGenerate() }
        previewBtn!!.addActionListener { e: ActionEvent? -> onPreView() }

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