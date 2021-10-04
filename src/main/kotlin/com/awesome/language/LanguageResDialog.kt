package com.awesome

import clearSymbol
import com.awesome.language.LanguageDartWriter
import com.awesome.utils.HttpApi
import com.awesome.utils.PropertiesHelper
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiDirectory
import org.apache.http.util.TextUtils
import toCamel
import java.awt.Label
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.event.KeyEvent
import java.lang.StringBuilder
import javax.swing.*

class LanguageResDialog(val mDirectory: PsiDirectory) : JDialog() {
    var contentPane: JPanel? = null

    //确认生成按钮
    var buttonOK: JButton? = null

    //取消按钮
    var buttonCancel: JButton? = null

    //翻译按钮
    var btnTranslate: JButton? = null

    //需要生成的key的内容
    var tvKey: JTextField? = null

    //带翻译的内容
    var tvChinese: JTextField? = null

    //选择的语言内容
    var tvLanguages: JTextField? = null

    //所有翻译的文本的展示区域
    var tvArea: JTextArea? = null

    //错误提示
    var tvTip: JLabel? = null

    //翻译后需要展示的文本
    val builder: StringBuilder by lazy { StringBuilder() }

    //语言码跟翻译后的文本
    val mapValues = HashMap<String, String?>()

    //通过读取properties读取初始化语种和需要翻译的语种
    private var properties: PropertiesHelper? = null

    // 最后的语种
    var languages: List<String>? = null

    //输入需要翻译语言的初始化语种
    var rawLanguage = "zh-Hans"

    var needTranslate = true

    //点击翻译了，就会将翻译的写入文件中
    private fun onTranslate() {
        builder.clear()
        mapValues.clear()
        WriteCommandAction.runWriteCommandAction(mDirectory.project) {

            if (rawLanguage.equals("en")&&!needTranslate) {
                mapValues.put("en", tvChinese!!.text)
                tvKey?.text = tvChinese!!.text?.replace(" ", "_")?.trim().clearSymbol().toCamel()
                builder.append("en").append("\n").append(tvChinese!!.text).append("\n\n")
            } else {
                for (languageCode in languages!!) {
                    val value = HttpApi.translate(tvChinese!!.text, languageCode)
                    mapValues.put(languageCode, value)
                    builder.append(languageCode).append("\n").append(value).append("\n\n")
                    if (languageCode == "en") {
                        tvKey?.text = value?.replace(" ", "_")?.trim().clearSymbol().toCamel()
                    }
                }
            }
            tvArea?.text = builder.toString()
        }
    }

    //直接点击生成就不会进行翻译
    private fun onOK() {
        WriteCommandAction.runWriteCommandAction(mDirectory.project) {
            if (mapValues.isEmpty() || TextUtils.isEmpty(tvKey?.text)) {
                for (languageCode in languages!!) {
                    mapValues.put(languageCode, tvChinese!!.text)
                    builder.append(languageCode).append("\n").append(tvChinese!!.text).append("\n\n")
                    if (languageCode == "en") {
                        val value = HttpApi.translate(tvChinese!!.text, languageCode)
                        tvKey?.text = value?.replace(" ", "_")?.trim().clearSymbol().toCamel()
                    }
                }
            }
            val writer = LanguageDartWriter(mapValues, tvKey!!.text, mDirectory)
            writer.startWrite()
            dispose()
        }
    }

    init {
        setContentPane(contentPane)
        isModal = true
        getRootPane().defaultButton = buttonOK
        buttonOK!!.addActionListener { onOK() }
        buttonCancel!!.addActionListener { dispose() }
        btnTranslate!!.addActionListener { onTranslate() }
        // call onCancel() when cross is clicked
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

        initProperties()
        initList()
    }


    private fun initList() {
        for (i in 0 until languages!!.size) {
            val language = languages!![i]
            val jpanel = JPanel()
            jpanel.add(Label(language))
            jpanel.add(JTextField())
        }
    }

    private fun initProperties() {
        try {
            properties = PropertiesHelper(mDirectory)
        } catch (e: Exception) {
            print(e)
        }
        val defaultLanguage = "zh-Hans,zh-Hant,en,ja,km,th,vi"
        val languageString = properties?.getProperty("plugin.languages") ?: defaultLanguage
        rawLanguage = properties?.getProperty("plugin.rawLanguage") ?: rawLanguage
        needTranslate="true".equals(properties?.getProperty("plugin.needTranslate"))
        languages = languageString.split(',')//languageString.split(',')
        tvLanguages?.text = languageString
    }

    fun showDialog(): LanguageResDialog {
        pack()
        setLocationRelativeTo(null)
        isVisible = true
        return this
    }
}
