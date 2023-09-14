package com.awesome.utils

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.module.ModuleUtil
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.annotations.NotNull
import java.io.File

/**
 * Created by JarvisLau on 2018/3/14.
 * Description:
 */
fun PsiElement.reload() {
    if (this is PsiDirectory) {
        this.files.forEach {
            println(it.name)
            it.manager.reloadFromDisk(it)
        }
    } else if (this is PsiFile) {
        this.manager.reloadFromDisk(this)
    }
}

fun PsiElement.runWriteCmd(@NotNull runnable: Runnable) {
    WriteCommandAction.runWriteCommandAction(this.project, runnable)
}

fun AnActionEvent.runWriteCmd(@NotNull runnable: Runnable) {
    WriteCommandAction.runWriteCommandAction(this.project, runnable)
}

fun Editor.runWriteCmd(@NotNull runnable: Runnable) {
    WriteCommandAction.runWriteCommandAction(this.project, runnable)
}

/**
 * 获取主工程或者子工程的项目文件夹
 **/
fun PsiElement.basePath(): String {
    if (this is PsiFile) {
        var path = this.virtualFile.path.split("/lib/").first()
        val file = File(path, "pubspec.yaml")
        if (file.exists()) {
            return path
        }
    } else if (this is PsiDirectory) {
        val file = File(this.parent!!.virtualFile.path, "pubspec.yaml")
        if (file.exists()) {
            return this.parent!!.virtualFile.path
        }
    }
    return this.project.basePath!!
}

///读取工程的 ModuleName
fun PsiElement.moduleName(): String {
    val pubspecFile = File("${this.basePath()}/pubspec.yaml").readText()
    val moduleName = pubspecFile.regexOne("(?<=name\\:).*?(?=\\n)")?.trim() ?: ""
    return moduleName;
}

/**
 * 遍历文件夹
 **/
fun PsiDirectory.travse(callback: (File) -> Unit) {
    this.subdirectories.forEach {
        it.travse(callback)
    }
    this.files.forEach {
        callback(File(it.virtualFile.path))
    }
}

fun PsiDirectory.traveseDirectory(callback: (PsiDirectory) -> Unit) {
    this.subdirectories.forEach {
        if (it is PsiDirectory) {
            it.traveseDirectory(callback)
            callback(it)
        }
    }
}

fun PsiDirectory.searchFile(path: String): File? {
    var file = this.findFile(path)?.virtualFile
    if (file != null && file.exists()) return File(file.path)

    var targetFile: File? = File(this.virtualFile.path, path)
    if (targetFile!!.exists()) return targetFile
    targetFile = null
    this.traveseDirectory {
        if (targetFile != null && targetFile!!.exists()) return@traveseDirectory
        val newFile = File(it.virtualFile.path, path)
        if (newFile.exists()) {
            targetFile = newFile
        }
    }
    if (targetFile != null && targetFile!!.exists())
        return targetFile!!

    this.findRootDirectory().traveseDirectory {
        if (targetFile != null && targetFile!!.exists()) return@traveseDirectory
        val newFile = File(it.virtualFile.path, path)
        if (newFile.exists()) {
            targetFile = newFile
        }
    }
    if (targetFile != null && targetFile!!.exists())
        return targetFile!!

    return null
}

/**
 * 找寻根目录下的路径
 **/
fun PsiFileSystemItem.findRootDirectory(): PsiDirectory {
    if (this.basePath() == this.virtualFile.path) {
        return this as PsiDirectory
    }
    return this.parent!!.findRootDirectory()
}

object PsiFileUtils {

    fun getPsiElementByEditor(
        editor: Editor?,
        psiFile: PsiFile?
    ): PsiElement? {
        if (editor == null || psiFile == null) {
            return null
        }
        val caret = editor.caretModel
        return psiFile.findElementAt(caret.offset)
        //NotifyUtils.showError(psiFile.getProject(), "No Layout Found");
    }


    fun getFileByName(psiElement: PsiElement, fileName: String?): PsiFile? {
        val moduleForPsiElement = ModuleUtil.findModuleForPsiElement(psiElement)
        if (moduleForPsiElement != null) {
            var searchScope = GlobalSearchScope.moduleScope(moduleForPsiElement)
            var project = psiElement.project
            //
            var psiFiles = FilenameIndex.getFilesByName(project, fileName!!, searchScope)
            if (psiFiles.size != 0) {
                return psiFiles[0]
            }
            searchScope = GlobalSearchScope.projectScope(project)
            project = psiElement.project
            psiFiles = FilenameIndex.getFilesByName(project, fileName, searchScope)
            if (psiFiles.size != 0) {
                return psiFiles[0]
            }
        }
        return null
    }

    fun getModuleName(psiElement: PsiElement): String {
        return psiElement.project.name
    }
}
