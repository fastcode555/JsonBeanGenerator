package com.awesome

import com.awesome.plugins.json2bean.database.DartDataBaseGenerator
import com.awesome.plugins.json2bean.generators.DartJsonGenerator
import com.awesome.plugins.json2bean.generators.PythonJsonGenerator
import com.awesome.plugins.json2bean.generators.TsJsonGenerator
import com.awesome.utils.JTextFieldHintListener
import com.awesome.utils.PropertiesHelper
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiDirectory
import formatJson
import org.apache.http.util.TextUtils
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
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
    var rbTs: JRadioButton? = null
    var rbPy: JRadioButton? = null
    var rbDart: JRadioButton? = null
    var cbSqlite: JCheckBox? = null
    var tvPrimaryKey: JTextField? = null
    var fileType = ".dart"
    private var properties: PropertiesHelper? = null
    private var tvPrimaryKeyListener: JTextFieldHintListener
    private var radioBtns: List<JRadioButton?>? = null


    private fun isEmpty(text: String?): Boolean {
        return text == null || text.isEmpty()
    }

    private fun onGenerate() {
        tvError?.text = ""
        if (isEmpty(tvClassField?.text)) {
            tvClassField!!.text = "auto_generated_name"
        }
        val file = File(mDirectory.virtualFile.path, tvClassField?.text + fileType)
        if (!file.exists()) {
            try {
                WriteCommandAction.runWriteCommandAction(mDirectory.project) {
                    file.writeText(getParseTargetResult(fileType))
                    //生成数据库基类跟dao类
                    println("isSelected:${cbSqlite!!.isSelected}  $fileType")
                    if (cbSqlite!!.isSelected && fileType == ".dart" && !TextUtils.isEmpty(tvPrimaryKeyListener.getText())) {
                        println("进入DartDataBaseGenerator")
                        DartDataBaseGenerator(
                            tvField!!.text,
                            tvClassField!!.text,
                            mDirectory,
                            tvPrimaryKeyListener.getText(),
                        ).startWrite()
                    }
                    dispose()
                }
            } catch (e: Exception) {
                tvError?.text = "JSON Error!!"
                println(e)
            }
        } else {
            dispose()
        }
    }

    private fun getParseTargetResult(fileType: String): String {
        if (fileType.equals(".dart")) {
            return DartJsonGenerator(
                tvField!!.text,
                tvClassField!!.text,
                tvExtends!!.text,
                tvImplements!!.text,
                isSqliteEnable(),
                tvPrimaryKeyListener.getText(),
            ).toString()
        } else if (fileType.equals(".py")) {
            return PythonJsonGenerator(
                tvField!!.text,
                tvClassField!!.text,
                tvExtends!!.text,
                tvImplements!!.text
            ).toString()
        } else if (fileType.equals(".ts")) {
            return TsJsonGenerator(
                tvField!!.text,
                tvClassField!!.text,
                tvExtends!!.text,
                tvImplements!!.text,
            ).toString()
        }
        return DartJsonGenerator(
            tvField!!.text,
            tvClassField!!.text,
            tvExtends!!.text,
            tvImplements!!.text,
            isSqliteEnable(),
            tvPrimaryKeyListener.getText(),
        ).toString()
    }

    private fun isSqliteEnable(): Boolean {
        return cbSqlite!!.isSelected && !TextUtils.isEmpty(tvPrimaryKeyListener.getText());
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
                    tvImplements!!.text,
                    isSqliteEnable(),
                    tvPrimaryKeyListener.getText(),
                ).toString()
                val previewDialog = PreViewDialog(content)
                previewDialog.showDialog()
            } catch (e: Exception) {
                tvError?.text = "JSON Error!!"
                println(e)
            }
        } else if (fileType.equals(".py")) {
            try {
                val content = PythonJsonGenerator(
                    tvField!!.text,
                    tvClassField!!.text,
                    tvExtends!!.text,
                    tvImplements!!.text
                ).toString()
                val previewDialog = PreViewDialog(content)
                previewDialog.showDialog()
            } catch (e: Exception) {
                tvError?.text = "JSON Error!!"
                println(e)
            }
        } else if (fileType.equals(".ts")) {
            try {
                val content = TsJsonGenerator(
                    tvField!!.text,
                    tvClassField!!.text,
                    tvExtends!!.text,
                    tvImplements!!.text
                ).toString()
                val previewDialog = PreViewDialog(content)
                previewDialog.showDialog()
            } catch (e: Exception) {
                tvError?.text = "JSON Error!!"
                println(e)
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
                println(e)
            }
        }
        radioBtns = listOf(rbPy, rbDart, rbTs)
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
        tvPrimaryKeyListener = JTextFieldHintListener(tvPrimaryKey!!, "please input the database primary key")
        tvPrimaryKey!!.isEnabled = false
        cbSqlite!!.addActionListener {
            tvPrimaryKey!!.isEnabled = cbSqlite!!.isSelected
        }
    }


    private fun initRadioButtons() {
        try {
            properties = PropertiesHelper(mDirectory)
        } catch (e: Exception) {
            print(e)
        }
        fileType = properties?.getProperty("plugin.modelType") ?: ".dart"
        cbSqlite!!.isEnabled = fileType == ".dart"
        if (fileType == ".py") {
            rbPy!!.isSelected = true
        } else if (fileType == ".ts") {
            rbTs!!.isSelected = true
        } else {
            rbDart!!.isSelected = true
        }

        rbDart!!.statusChanged(".dart")
        rbPy!!.statusChanged(".py")
        rbTs!!.statusChanged(".ts")
    }

    /**
     * 添加状态变更的函数
     **/
    private fun JRadioButton.statusChanged(type: String/*, callback: (ActionEvent) -> Unit*/) {
        this.addActionListener {
            if (this.isSelected) {
                fileType = type
                properties?.setProperty("plugin.modelType", fileType)
                cbSqlite!!.isSelected = false
                cbSqlite!!.isEnabled = type == ".dart"
                //callback(it)
                radioBtns?.forEach {
                    if (it != this) {
                        it!!.isSelected = false
                    }
                }
            }
        }
    }

}
