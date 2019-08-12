// This is a generated file. Not intended for manual editing.
package com.modusbox.portx.jsonnet.language.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.modusbox.portx.jsonnet.language.psi.JsonnetTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.modusbox.portx.jsonnet.language.psi.*;

public class JsonnetObjextendImpl extends ASTWrapperPsiElement implements JsonnetObjextend {

  public JsonnetObjextendImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull JsonnetVisitor visitor) {
    visitor.visitObjextend(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof JsonnetVisitor) accept((JsonnetVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public JsonnetObjinside getObjinside() {
    return findNotNullChildByClass(JsonnetObjinside.class);
  }

}
