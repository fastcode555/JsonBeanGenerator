package com.awesome.ideatool

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement

/**
 * Notifications.Bus.notify 是 IDE 中发送通知提醒的一个 API，我们熟悉它即可。
 * Notification 中有几个核心字段，该构造方法中：
 *   第一个参数是通知消息的分组
 *   第二个参数是通知框的标题，第三个参数是通知框的内容。
 *   第四个参数是通知信息级别，主要的有 Info，Waring，Error 3个级别。
 *
 * e.getProject() 是一个获取到当前 IDE 所打开项目，在代码中的抽象表示的 API 。
 *  本处表示在我们当前 IDE 中所打开的项目进行提示，若我们同时打开多个 IDE，其他 IDE 项目窗口中不会收到改消息。
 */
/**
 * Notifications.Bus.notify 是 IDE 中发送通知提醒的一个 API，我们熟悉它即可。
 * Notification 中有几个核心字段，该构造方法中：
 * 第一个参数是通知消息的分组
 * 第二个参数是通知框的标题，第三个参数是通知框的内容。
 * 第四个参数是通知信息级别，主要的有 Info，Waring，Error 3个级别。
 *
 * e.getProject() 是一个获取到当前 IDE 所打开项目，在代码中的抽象表示的 API 。
 * 本处表示在我们当前 IDE 中所打开的项目进行提示，若我们同时打开多个 IDE，其他 IDE 项目窗口中不会收到改消息。
 */
fun PsiElement.showNotify(title: String, content: String, type: NotificationType = NotificationType.INFORMATION) {
    Notifications.Bus.notify(Notification("Print", title, content, type), this.project)
}

/**
 * 显示通知的功能
 **/
fun Project.showNotify(title: String, content: String, type: NotificationType = NotificationType.INFORMATION) {
    Notifications.Bus.notify(Notification("Print", title, content, type), this)
}

object IdeaTool {


}