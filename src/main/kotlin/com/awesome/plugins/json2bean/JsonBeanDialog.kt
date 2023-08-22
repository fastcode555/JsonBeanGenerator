package com.awesome

import com.awesome.plugins.json2bean.database.DartDataBaseGenerator
import com.awesome.plugins.json2bean.utils.GeneratorHelper
import com.awesome.utils.JTextFieldHintListener
import com.awesome.utils.PropertiesHelper
import com.intellij.psi.PsiDirectory
import formatJson
import org.apache.http.util.TextUtils
import toUpperCamel
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import javax.swing.*

class JsonBeanDialog(val mDirectory: PsiDirectory) : JDialog() {
    var contentPane: JPanel? = null

    //格式化按钮
    var formatBtn: JButton? = null

    //确认按钮
    var confirmBtn: JButton? = null

    //预览按钮
    var previewBtn: JButton? = null

    //输入的json文本
    var tvField: JTextArea? = null

    //用户输入的class的名字
    var tvClassField: JTextField? = null

    //需要继承的对象
    var tvExtends: JTextField? = null

    //需要实现的对象
    var tvImplements: JTextField? = null

    //报错提示
    var tvError: JLabel? = null

    //选中json转TypeScript的按钮
    var rbTs: JRadioButton? = null

    //选中json转Python的按钮
    var rbPy: JRadioButton? = null

    //选中json转Dart的按钮
    var rbDart: JRadioButton? = null

    //选中json转Kotlin的按钮
    var rbKt: JRadioButton? = null

    //数据库的支持
    var cbSqlite: JCheckBox? = null

    //支持数据的primary key
    var tvPrimaryKey: JTextField? = null

    //文件的类型
    var fileType = ".dart"

    //选择生成方式的面板
    var mKtPanel: JPanel? = null

    //生成java或者kotlin
    var rbNone: JRadioButton? = null

    //使用Gson的方式生成Bean
    var rbGson: JRadioButton? = null

    //使用FastJson的方式生成Bean
    var rbFastJson: JRadioButton? = null

    //属性帮助类
    private var properties: PropertiesHelper? = null

    //根据提示，判断显不显示提示
    private var tvPrimaryKeyListener: JTextFieldHintListener

    //所有可选类型的按钮
    private var radioBtns: List<JRadioButton?>? = null

    //选择需要依赖的方式
    private var radioDeps: List<JRadioButton?>? = null

    //Kotlin或者java生成所依赖的库
    private var depType: String = "gson"

    private fun isEmpty(text: String?): Boolean {
        return text.isNullOrEmpty()
    }

    private fun onGenerateJavaOrKt() {
        tvError?.text = ""
        if (isEmpty(tvClassField?.text)) {
            tvClassField!!.text = "auto_root"
        }
        val file = File(mDirectory.virtualFile.path, tvClassField?.text.toUpperCamel() + fileType)
        if (!file.exists()) {
            try {
                mDirectory.run {
                    GeneratorHelper.json2KtOrJava(
                        fileType,
                        tvClassField!!.text,
                        tvField!!.text,
                        tvExtends!!.text,
                        tvImplements!!.text,
                        depType,
                        mDirectory,
                    )
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

    private fun onGenerate() {
        tvError?.text = ""
        if (isEmpty(tvClassField?.text)) {
            tvClassField!!.text = "auto_root"
        }
        val file = File(mDirectory.virtualFile.path, tvClassField?.text + fileType)
        if (!file.exists()) {
            try {
                mDirectory.run {
                    file.writeText(json2Bean())
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

    /**
     * sqlite 是否可以使用
     **/
    private fun isSqliteEnable(): Boolean {
        return cbSqlite!!.isSelected && !TextUtils.isEmpty(tvPrimaryKeyListener.getText());
    }

    /**
     *  将Json转成Bean对象
     **/
    private fun json2Bean(): String {
        return GeneratorHelper.json2Bean(
            fileType, tvField!!.text,
            tvClassField!!.text,
            tvExtends!!.text,
            tvImplements!!.text,
            isSqliteEnable(),
            tvPrimaryKeyListener.getText(),
            depType ?: "gson",
            mDirectory,
        )
    }


    /**
     * 直接生成预览的值
     **/
    private fun onPreView() {
        tvError?.text = ""
        if (isEmpty(tvClassField!!.text)) {
            tvClassField!!.text = "auto_root"
        }
        try {
            val previewDialog = PreViewDialog(json2Bean())
            previewDialog.showDialog()
        } catch (e: Exception) {
            tvError?.text = "JSON Error!!"
            println(e)
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
        radioBtns = listOf(rbPy, rbDart, rbTs, rbKt)
        radioDeps = listOf(rbNone, rbGson, rbFastJson)

        confirmBtn!!.addActionListener {
            if (fileType == ".kt") {
                onGenerateJavaOrKt()
            } else {
                onGenerate()
            }

        }
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
        tvPrimaryKey!!.isEnabled = false
        tvPrimaryKeyListener = JTextFieldHintListener(tvPrimaryKey!!, "please input the database primary key")
        cbSqlite!!.addActionListener {
            tvPrimaryKey!!.isEnabled = cbSqlite!!.isSelected
        }
    }


    /**
     * 初始化JRadioButton的状态
     **/
    private fun initRadioButtons() {
        try {
            properties = PropertiesHelper(mDirectory)
        } catch (e: Exception) {
            print(e)
        }
        fileType = properties?.getProperty("plugin.modelType") ?: ".dart"
        cbSqlite!!.isVisible = fileType == ".dart"
        tvPrimaryKey!!.isVisible = cbSqlite!!.isVisible
        mKtPanel!!.isVisible = fileType == ".kt"
        if (fileType == ".py") {
            rbPy!!.isSelected = true
        } else if (fileType == ".ts") {
            rbTs!!.isSelected = true
        } else if (fileType == ".kt") {
            rbKt!!.isSelected = true
        } else {
            rbDart!!.isSelected = true
        }
        rbDart!!.statusChanged(".dart")
        rbPy!!.statusChanged(".py")
        rbTs!!.statusChanged(".ts")
        rbKt!!.statusChanged(".kt")

        //设置生成Kotlin或者Java 所需要的方式
        depType = properties?.getProperty("plugin.depType") ?: "gson"
        if (depType == "none") {
            rbNone!!.isSelected = true
        } else if (depType == "gson") {
            rbGson!!.isSelected = true
        } else {
            rbFastJson!!.isSelected = true
        }
        rbNone!!.depStatusChanged("none")
        rbGson!!.depStatusChanged("gson")
        rbFastJson!!.depStatusChanged("fastjson")
    }

    private fun JRadioButton.depStatusChanged(type: String) {
        this.addActionListener {
            if (this.isSelected) {
                depType = type
            }
            radioDeps?.forEach {
                if (it != this) {
                    it!!.isSelected = false
                }
            }
            properties?.setProperty("plugin.depType", depType)
        }
    }

    /**
     * 添加状态变更的函数
     **/
    private fun JRadioButton.statusChanged(type: String/*, callBack: (ActionEvent) -> Unit*/) {
        this.addActionListener {
            if (this.isSelected) {
                fileType = type
                properties?.setProperty("plugin.modelType", fileType)
                cbSqlite!!.isVisible = type == ".dart"
                tvPrimaryKey!!.isVisible = cbSqlite!!.isVisible
                radioBtns?.forEach {
                    if (it != this) {
                        it!!.isSelected = false
                    }
                }
                mKtPanel!!.isVisible = fileType == ".kt"
                //callBack(it)
            }
        }
    }

}
