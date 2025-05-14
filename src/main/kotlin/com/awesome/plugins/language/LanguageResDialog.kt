package com.awesome

import clearSymbol
import com.alibaba.fastjson.JSONObject
import com.awesome.common.PluginProps
import com.awesome.plugins.language.LanguageDartWriter
import com.awesome.utils.HttpApi
import com.awesome.utils.PropertiesHelper
import com.awesome.utils.basePath
import com.awesome.utils.runWriteCmd
import com.intellij.openapi.editor.SelectionModel
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import firstUpperCamel
import org.apache.http.util.TextUtils
import toCamel
import toJSON
import java.awt.Label
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.event.KeyEvent
import java.io.File
import java.lang.StringBuilder
import javax.swing.*

class LanguageResDialog(
    private val psiElement: PsiElement,
    private val textValue: String,
    private val selectionModel: SelectionModel?
) :
    JDialog() {
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

    //如果是arb（Applicatio Resource Bundle）需要支持设置描述词
    var tvDescription: JTextField? = null

    var textDescription: JLabel? = null

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

    val descriptionValues = HashMap<String, String?>()

    //通过读取properties读取初始化语种和需要翻译的语种
    private var properties: PropertiesHelper? = null

    // 最后的语种
    var languages: List<String>? = null

    //输入需要翻译语言的初始化语种
    var rawLanguage = "zh-Hans"

    var needTranslate = true

    var dirPath = ""

    var translationSuffix = "tr"

    //点击翻译了，就会将翻译的写入文件中
    private fun onTranslate() {
        builder.clear()
        mapValues.clear()
        descriptionValues.clear()
        psiElement.runWriteCmd(::runTranslate)
    }

    private fun runTranslate() {
        if (rawLanguage.equals("en") && !needTranslate) {
            mapValues.put("en", tvChinese!!.text!!.trim())
            tvKey?.text = tvChinese!!.text?.replace(" ", "_")?.trim().clearSymbol().toCamel()
            builder.append("en").append("\n").append(tvChinese!!.text).append("\n\n")
        } else {
            for (languageCode in languages!!) {
                val value = HttpApi.translate(tvChinese!!.text, languageCode)
                var description = tvDescription?.text
                if (!TextUtils.isEmpty(description)) {
                    description = HttpApi.translate(description!!, languageCode)?.trim()
                }

                if (languageCode == "en") {
                    mapValues.put(languageCode, value?.firstUpperCamel())
                    if (!TextUtils.isEmpty(description)) {
                        descriptionValues.put(languageCode, description?.firstUpperCamel())
                    }
                } else {
                    mapValues.put(languageCode, value)
                    if (!TextUtils.isEmpty(description)) {
                        descriptionValues.put(languageCode, description)
                    }
                }
                builder.append(languageCode)
                    .append("\n")
                    .append(mapValues[languageCode])
                    .append("\n")
                    .append(description)
                    .append("\n")
                if (languageCode == "en") {
                    tvKey?.text = value?.replace(" ", "_")?.trim().clearSymbol().toCamel()
                }
            }
        }
        tvArea?.text = builder.toString()
    }

    //直接点击生成就不会进行翻译
    private fun onOK() {
        psiElement.runWriteCmd {
            if (mapValues.isEmpty()) {
                runTranslate()
            }
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

            if (isJsonOrArb()) {
                handleWrite2JsonFile()
            } else {
                LanguageDartWriter(
                    mapValues,
                    tvKey!!.text,
                    dirPath,
                    psiElement,
                    textValue,
                    selectionModel,
                    translationSuffix
                ).startWrite()
            }
            //psiElement.reload()
            dispose()
        }
    }

    /**
     * 将翻译写入到Json文件中
     **/
    private fun handleWrite2JsonFile() {
        for (psiElement in psiElement.children) {
            if (psiElement is PsiFile) {
                val file = psiElement.virtualFile

                try {
                    if (file.name.endsWith(".json") || file.name.endsWith(".arb")) {
                        val name = file.nameWithoutExtension.replace("app_", "")
                        val jsonFile = File(file.path)
                        val json = jsonFile.readText().toJSON() as JSONObject
                        val value = mapValues[name]
                        json.put(tvKey!!.text, value)

                        val description = descriptionValues[name]
                        if (!TextUtils.isEmpty(description)) {
                            json.put("@${tvKey!!.text}", JSONObject(mapOf(Pair("description", description))))
                        }
                        jsonFile.writeText(json.toJSONString())
                    }
                } catch (e: Exception) {
                    print("the file have problem ${file.name}")
                    print(e.message)
                }
            }
        }
    }

    /**
     * 是否包含json文件
     **/
    private fun isJsonOrArb(): Boolean {
        for (psiElement in psiElement.children) {
            if (psiElement is PsiFile) {
                val name = psiElement.virtualFile.name
                return name.endsWith(".json") || name.endsWith(".arb")
            }
        }
        return false
    }

    private fun isArb(): Boolean {
        for (psiElement in psiElement.children) {
            if (psiElement is PsiFile) {
                return psiElement.virtualFile.name.endsWith(".arb")
            }
        }
        return false
    }

    init {

        try {
            properties = PropertiesHelper(psiElement)
        } catch (e: Exception) {
            print(e)
        }
        translationSuffix = properties!!.getProperty(PluginProps.translationKey) ?: "tr"
        if (translationSuffix.isEmpty()) {
            translationSuffix = "tr"
        }
        if (psiElement is PsiDirectory) {
            dirPath = psiElement.virtualFile.path
        } else {
            val dir = properties!!.getProperty(PluginProps.languageDir)
            dirPath = "${psiElement.basePath()}$dir"
        }
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
        tvChinese?.text = textValue
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
        val defaultLanguage = "zh-Hans,zh-Hant,en,ja,km,th,vi"
        val languageString = properties?.getProperty(PluginProps.languages) ?: defaultLanguage
        rawLanguage = properties?.getProperty(PluginProps.rawLanguage) ?: rawLanguage
        needTranslate = "true" == properties?.getProperty(PluginProps.needTranslate)

        val isArb = isArb();
        tvDescription!!.isVisible = isArb
        textDescription!!.isVisible = isArb

        if (isJsonOrArb()) {
            languages = arrayListOf()
            val arrays = languages as ArrayList<String>
            for (psi in psiElement.children) {
                if (psi is PsiFile) {
                    arrays.add(psi.virtualFile.nameWithoutExtension.replace("app_", ""))
                }
            }
            tvLanguages?.text = arrays.joinToString(separator = ",")
        } else {
            languages = languageString.split(',')
            tvLanguages?.text = languageString
        }
    }

    fun showDialog(): LanguageResDialog {
        pack()
        setLocationRelativeTo(null)
        isVisible = true
        return this
    }
}
