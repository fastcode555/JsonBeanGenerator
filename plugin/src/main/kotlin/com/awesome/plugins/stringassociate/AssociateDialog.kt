package com.awesome.plugins.stringassociate

import clearSymbol
import com.awesome.utils.runWriteCmd
import com.intellij.openapi.editor.Editor
import toCamel
import toLowerUnderScore
import toUpperCamel
import toUpperUnderScore
import java.awt.FlowLayout
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*


/**
 * 字符串联想关键词
 **/
class AssociateDialog(private val editor: Editor) : JDialog() {
    var contentPanel: JPanel? = null
    var mainPanel: JPanel? = null
    var buttonCancel: JButton? = null

    init {
        setContentPane(contentPanel)
        setModal(true)
        buttonCancel!!.addActionListener { dispose() }
        getRootPane().defaultButton = buttonCancel
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE)
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                dispose()
            }
        })
        contentPanel!!.registerKeyboardAction(
            { dispose() }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        )
        initRecommendStrings()
    }

    /**
     * 初始化相关推荐的数据
     **/
    private fun initRecommendStrings() {
        val selectionModel = editor.selectionModel
        val content = selectionModel.selectedText.clearSymbol()
        mainPanel!!.setLayout(FlowLayout())
        mainPanel!!.add("${content.toCamel()}".newButton())
        mainPanel!!.add("m${content.toUpperCamel()}".newButton())
        mainPanel!!.add("${content.toUpperCamel()}".newButton())
        mainPanel!!.add("${content.toLowerUnderScore()}".newButton())
        mainPanel!!.add("${content.toUpperUnderScore()}".newButton())
        mainPanel!!.add("${selectionModel.selectedText?.lowercase()}".newButton())
        mainPanel!!.add("${selectionModel.selectedText?.uppercase()}".newButton())
        mainPanel!!.add("${content.toCamel()}: '${selectionModel.selectedText}'".newButton())
    }

    private fun String.newButton(): JButton {
        val string = this
        val jbutton = JButton(this)
        jbutton.addActionListener {
            editor.runWriteCmd {
                editor.selectionModel.apply {
                    editor.document.replaceString(selectionStart, selectionEnd, string)
                }
                dispose()
            }
        }
        return jbutton
    }

    fun showDialog(): AssociateDialog {
        pack()
        setLocationRelativeTo(null)
        isVisible = true
        return this
    }
}
