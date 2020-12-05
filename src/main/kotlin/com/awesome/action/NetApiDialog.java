package com.awesome.action;

import com.intellij.psi.PsiDirectory;

import javax.swing.*;
import java.awt.event.*;

public class NetApiDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField tvAction;
    private JComboBox serviceCombox;

    private PsiDirectory mDirectory;

    public NetApiDialog(PsiDirectory mDirectory) {
        this.mDirectory = mDirectory;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    NetApiDialog showDialog() {
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        return this;
    }
}
