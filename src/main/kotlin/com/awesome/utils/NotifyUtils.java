package com.awesome.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;

import javax.swing.*;
import java.awt.*;

/**
 * Created by JarvisLau on 2018/3/14.
 * Description:
 */
public class NotifyUtils {
    private static void showNotification(Project project, MessageType type, String text) {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder(text, type, null)
                .setFadeoutTime(7500)
                .createBalloon()
                .show(RelativePoint.getCenterOf(statusBar.getComponent()), Balloon.Position.atRight);
    }

    public static void showError(Project project, String msg) {
        showNotification(project, MessageType.ERROR, msg);
    }

    public static void showInfo(Project project, String msg) {
        showNotification(project, MessageType.INFO, msg);
    }

    /**
     * @param message 设置消息弹窗的内容
     * @param title   设置消息弹窗的标题
     *                设置消息弹窗w
     **/
    public static void showMessageDialog(String title, String message) {
        JOptionPane.showMessageDialog(new Frame(), message, title, JOptionPane.INFORMATION_MESSAGE);

    }

    /***
     * @param message 弹出错误弹窗
     *弹出错误弹窗
     **/
    public static void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(new Frame(), message, "error", JOptionPane.INFORMATION_MESSAGE);
    }

    /***
     * @param message 弹出错误弹窗
     *弹出错误弹窗
     **/
    public static void showErrorDialog(Component parentComponent, String message) {
        JOptionPane.showMessageDialog(new Frame(), message, "error", JOptionPane.INFORMATION_MESSAGE);
    }

}
