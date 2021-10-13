package com.awesome

import com.awesome.generators.DartJsonGenerator
import com.awesome.generators.PythonJsonGenerator
import com.awesome.utils.PropertiesHelper
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiDirectory
import com.intellij.ui.layout.selected
import formatJson
import org.apache.http.util.TextUtils
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
    var tvError: JLabel? = null
    var rbPy: JRadioButton? = null
    var rbDart: JRadioButton? = null
    var fileType = ".dart"
    private var properties: PropertiesHelper? = null


    private fun isEmpty(text: String?): Boolean {
        return text == null || text.isEmpty()
    }

    private fun onGenerate() {
        tvError?.text = ""
        if (isEmpty(tvClassField?.text)) {
            tvClassField!!.text = "auto_generated_name"
        }
        val file = File(mDirectory.virtualFile.path, tvClassField?.text + fileType)
        if (fileType.equals(".dart")) {
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
                    tvError?.text = "JSON Error!!"
                }
            }
        } else if (fileType.equals(".py")) {
            if (!file.exists()) {
                try {
                    WriteCommandAction.runWriteCommandAction(mDirectory.project) {
                        file.writeText(
                            PythonJsonGenerator(
                                tvField!!.text,
                                tvClassField!!.text,
                                tvExtends!!.text,
                                tvImplements!!.text
                            ).toJson()
                        )
                    }
                } catch (e: Exception) {
                    tvError?.text = "JSON Error!!"
                }
            }
        }
        dispose()
    }

    private fun onPreView() {
        tvError?.text = ""
        if (isEmpty(tvClassField!!.text)) {
            tvClassField!!.text = "auto_generated_name"
        }
        if (fileType.equals(".dart")) {
            try {
                val content = DartJsonGenerator(
                    tvField!!.text,
                    tvClassField!!.text,
                    tvExtends!!.text,
                    tvImplements!!.text
                ).toJson()
                val previewDialog = PreViewDialog(content)
                previewDialog.showDialog()
            } catch (e: Exception) {
                tvError?.text = "JSON Error!!"
            }
        } else if (fileType.equals(".py")) {
            try {
                val content = PythonJsonGenerator(
                    tvField!!.text,
                    tvClassField!!.text,
                    tvExtends!!.text,
                    tvImplements!!.text
                ).toJson()
                val previewDialog = PreViewDialog(content)
                previewDialog.showDialog()
            } catch (e: Exception) {
                tvError?.text = "JSON Error!!"
            }
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
        tvError?.text = ""
        formatBtn!!.addActionListener {
            try {
                tvField?.text = tvField?.text?.formatJson()
            } catch (e: Exception) {
                tvError?.text = "JSON Error!!"
            }
        }
        confirmBtn!!.addActionListener { onGenerate() }
        previewBtn!!.addActionListener { onPreView() }

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
        initRadioButtons()
    }

    private fun initRadioButtons() {
        try {
            properties = PropertiesHelper(mDirectory)
        } catch (e: Exception) {
            print(e)
        }
        fileType = properties?.getProperty("plugin.modelType") ?: ".dart"
        if (fileType.equals(".py")) {
            rbPy!!.isSelected = true
        } else {
            rbDart!!.isSelected = true
            fileType = ".dart"
        }
        rbDart!!.addActionListener {
            if (rbDart!!.isSelected) {
                fileType = ".dart"
                rbPy!!.isSelected = false
                properties?.setProperty("plugin.modelType", ".dart")
            }
        }
        rbPy!!.addActionListener {
            if (rbPy!!.isSelected) {
                fileType = ".py"
                rbDart!!.isSelected = false
                properties?.setProperty("plugin.modelType", ".py")
            }
        }
    }

}