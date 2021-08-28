package com.awesome.utils;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;

/**
 * Created by JarvisLau on 2018/3/14.
 * Description:
 */
public class PsiFileUtils {


    public static PsiFile getFileByName(PsiElement psiElement, String fileName) {
        Module moduleForPsiElement = ModuleUtil.findModuleForPsiElement(psiElement);
        if (moduleForPsiElement != null) {
            GlobalSearchScope searchScope = GlobalSearchScope.moduleScope(moduleForPsiElement);
            Project project = psiElement.getProject();
            //
            PsiFile[] psiFiles = FilenameIndex.getFilesByName(project, fileName, searchScope);
            if (psiFiles.length != 0) {
                return psiFiles[0];
            }
            searchScope = GlobalSearchScope.projectScope(project);
            project = psiElement.getProject();
            psiFiles = FilenameIndex.getFilesByName(project, fileName, searchScope);
            if (psiFiles.length != 0) {
                return psiFiles[0];
            }
        }
        return null;
    }


}