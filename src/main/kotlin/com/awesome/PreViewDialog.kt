package com.awesome

import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*

class PreViewDialog(content: String?) : JDialog() {
    var contentPane: JPanel? = null
    var buttonCancel: JButton? = null
    var tvPreView: JTextArea? = null
    fun showDialog(): PreViewDialog {
        pack()
        setLocationRelativeTo(null)
        isVisible = true
        return this
    }

    private fun onCancel() {
        dispose()
    }

    init {
        setContentPane(contentPane)
        isModal = true
        tvPreView!!.text = content
        buttonCancel!!.addActionListener { e: ActionEvent? -> onCancel() }
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                onCancel()
            }
        })
        contentPane!!.registerKeyboardAction(
            { e: ActionEvent? -> onCancel() },
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        )
    }
}