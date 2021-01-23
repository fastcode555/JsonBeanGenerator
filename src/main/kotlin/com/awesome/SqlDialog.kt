package com.awesome

import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*

class SqlDialog : JDialog() {
    var contentPane: JPanel? = null
    var buttonOK: JButton? = null
    var buttonCancel: JButton? = null
    var tvCreateTable: JTextField? = null
    var tvSelect: JTextField? = null
    var tvDelete: JTextField? = null
    var tvUpdate: JTextField? = null
    var textArea1: JTextArea? = null
    var tvInsert: JTextField? = null
    var sqliteRadioButton: JRadioButton? = null
    var mysqlRadioButton: JRadioButton? = null
    var btnGenerate: JButton? = null
    var tvTable: JTextField? = null
    var pythonRadioButton: JRadioButton? = null
    private fun onOK() {
        dispose()
    }

    fun showDialog(): SqlDialog {
        pack()
        setLocationRelativeTo(null)
        isVisible = true
        return this
    }

    init {
        setContentPane(contentPane)
        isModal = true
        getRootPane().defaultButton = buttonOK
        buttonOK!!.addActionListener { e: ActionEvent? -> onOK() }
        buttonCancel!!.addActionListener { e: ActionEvent? -> dispose() }

        // call onCancel() when cross is clicked
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                dispose()
            }
        })

        // call onCancel() on ESCAPE
        contentPane!!.registerKeyboardAction(
            { e: ActionEvent? -> dispose() },
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        )
    }
}