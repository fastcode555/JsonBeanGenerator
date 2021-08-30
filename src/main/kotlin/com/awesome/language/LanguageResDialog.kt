package com.awesome

import clearSymbol
import com.awesome.utils.HttpApi
import com.awesome.utils.PropertiesHelper
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiDirectory
import toCamel
import toUpperCamel
import java.awt.Label
import java.awt.TextField
import kotlin.jvm.JvmStatic
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.event.KeyEvent
import java.lang.StringBuilder
import javax.swing.*

class LanguageResDialog(val mDirectory: PsiDirectory) : JDialog() {
    var contentPane: JPanel? = null
    var buttonOK: JButton? = null
    var buttonCancel: JButton? = null
    var btnTranslate: JButton? = null
    var tvKey: JTextField? = null
    var tvChinese: JTextField? = null
    var tvLanguages: JTextField? = null
    var tvArea: JTextArea? = null
    val builder: StringBuilder by lazy { StringBuilder() }
    val mapValues = HashMap<String, String?>()
    private var properties: PropertiesHelper? = null

    var languages: List<String>? = null
    var rawLanguage = "zh-Hans"
    private fun onOK() {
        dispose()
    }

    init {
        setContentPane(contentPane)
        isModal = true
        getRootPane().defaultButton = buttonOK
        buttonOK!!.addActionListener { onOK() }
        buttonCancel!!.addActionListener { dispose() }


        btnTranslate!!.addActionListener {
            builder.clear()
            mapValues.clear()
            WriteCommandAction.runWriteCommandAction(mDirectory.project) {
                for (languageCode in languages!!) {
                    val value = HttpApi.translate(tvChinese!!.text, languageCode)
                    mapValues.put(languageCode, value)
                    builder.append(languageCode).append("\n").append(value).append("\n\n")
                    if (languageCode == "en") {
                        tvKey?.text = value?.replace(" ", "_")?.trim().clearSymbol().toCamel()
                    }
                }
                tvArea?.text = builder.toString()
            }

        }
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
        initList();
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
