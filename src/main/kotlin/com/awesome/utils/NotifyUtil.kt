package com.awesome.utils

import java.awt.Frame
import javax.swing.JOptionPane

object NotifyUtil {
    fun showTipDialog(msg: String, title: String) {
        JOptionPane.showMessageDialog(Frame(), msg, title, JOptionPane.INFORMATION_MESSAGE)
    }
}