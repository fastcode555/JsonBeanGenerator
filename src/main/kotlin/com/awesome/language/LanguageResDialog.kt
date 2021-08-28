package com.awesome

import com.awesome.utils.PropertiesHelper
import com.intellij.psi.PsiDirectory
import java.awt.Label
import java.awt.TextField
import kotlin.jvm.JvmStatic
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.event.KeyEvent
import javax.swing.*

class LanguageResDialog(/*val mDirectory: PsiDirectory*/) : JDialog() {
    var contentPane: JPanel? = null
    var buttonOK: JButton? = null
    var buttonCancel: JButton? = null
    var btnAddLanguage: JButton? = null
    var btnTranslate: JButton? = null
    var tvKey: JTextField? = null
    var tvChinese: JTextField? = null
    var tvLanguages: JTextField? = null
    var tvArea: JTextArea? = null
    /*   private val properties: PropertiesHelper by lazy {
           PropertiesHelper(mDirectory)
       }*/

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
        /*val languageString = properties.getProperty("plugin.languages")
        rawLanguage = properties.getProperty("plugin.rawLanguage")*/
        languages = "zh-Hans,zh-Hant,ja,km,th,vi".split(',')//languageString.split(',')
        tvLanguages?.text = "zh-Hans,zh-Hant,ja,km,th,vi"
    }

    fun showDialog(): LanguageResDialog {
        pack()
        setLocationRelativeTo(null)
        isVisible = true
        return this
    }
}
