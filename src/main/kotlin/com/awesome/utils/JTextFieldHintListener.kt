package com.awesome.utils

import java.awt.Color
import javax.swing.JTextField
import java.awt.event.FocusListener
import java.awt.event.FocusEvent

class JTextFieldHintListener(private val textField: JTextField, private val hintText: String) : FocusListener {
    override fun focusGained(e: FocusEvent) {
        //获取焦点时，清空提示内容
        val temp = textField.text
        if (temp == hintText) {
            textField.text = ""
            textField.foreground = Color.BLACK
        }
    }

    override fun focusLost(e: FocusEvent) {
        //失去焦点时，没有输入内容，显示提示内容
        val temp = textField.text
        if (temp == "") {
            textField.foreground = Color.GRAY
            textField.text = hintText
        }
    }

    fun getText(): String {
        if (textField.text == hintText) return ""
        return textField.text
    }

    init {
        textField.text = hintText //默认直接显示
        textField.foreground = Color.GRAY
        textField.addFocusListener(this)
    }
}