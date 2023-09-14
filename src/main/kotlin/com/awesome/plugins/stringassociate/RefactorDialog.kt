package com.awesome.plugins.stringassociate

import com.awesome.utils.runWriteCmd
import com.intellij.psi.PsiDirectory
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*

val regex = Regex(" src=\".*?\"")

class RefactoryDialog(private val directory: PsiDirectory) : JDialog() {
    var contentPane: JPanel? = null
    var buttonCancel: JButton? = null
    var btnRefactor: JButton? = null

    init {
        setContentPane(contentPane)
        setModal(true)
        getRootPane().setDefaultButton(buttonCancel)
        buttonCancel!!.addActionListener { dispose() }

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE)
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                dispose()
            }
        })

        contentPane!!.registerKeyboardAction(
            { dispose() },
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        )
        btnRefactor!!.addActionListener { directory.runWriteCmd(::handleRefactorResource) }
    }

    /**
     * 重构资源文件
     **/
    private fun handleRefactorResource() {

    }

    fun showDialog(): RefactoryDialog {
        pack()
        setLocationRelativeTo(null)
        isVisible = true
        return this
    }


}
