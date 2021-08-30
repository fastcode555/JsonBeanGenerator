package com.awesome.utils

import com.intellij.openapi.module.ModuleUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope

/**
 * Created by JarvisLau on 2018/3/14.
 * Description:
 */
object PsiFileUtils {
    fun getFileByName(psiElement: PsiElement, fileName: String?): PsiFile? {
        val moduleForPsiElement = ModuleUtil.findModuleForPsiElement(psiElement)
        if (moduleForPsiElement != null) {
            var searchScope = GlobalSearchScope.moduleScope(moduleForPsiElement)
            var project = psiElement.project
            //
            var psiFiles = FilenameIndex.getFilesByName(
                project, fileName!!, searchScope
            )
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
}