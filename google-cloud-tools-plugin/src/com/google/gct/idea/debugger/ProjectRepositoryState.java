/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.gct.idea.debugger;

import com.intellij.openapi.util.Key;
import com.intellij.util.xmlb.annotations.Transient;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Holds state and performs operations related to source control contexts.
 */
public class ProjectRepositoryState {
  private static final Key<ProjectRepositoryState> REPO_KEY =
    Key.create("com.google.gct.idea.debugger.ProjectRepositoryState");
  private String myOriginalBranchName;
  private GitRepository mySourceRepository;
  private String myStashMessage;

  private ProjectRepositoryState() {
  }

  @NotNull
  public static ProjectRepositoryState fromProcessState(@NotNull CloudDebugProcessState processState) {
    ProjectRepositoryState repoState = processState.getUserData(REPO_KEY);
    if (repoState == null) {
      repoState = new ProjectRepositoryState();
      processState.putUserData(REPO_KEY, repoState);
    }
    return repoState;
  }

  public void clearForNextSession() {
    setStashMessage(null);
  }

  /**
   * This is the branch the user was on before they started a debug session and we moved them to the target SHA.
   */
  @Transient
  @Nullable
  protected String getOriginalBranchName() {
    return myOriginalBranchName;
  }

  @Transient
  public void setOriginalBranchName(@Nullable String originalBranchName) {
    myOriginalBranchName = originalBranchName;
  }

  /**
   * The source repository is used during stash/unstash and sync to perform Git operations. Right now we only support
   * Git.  If we added citc or other clients, this would need to be factored out.
   */
  @Transient
  @Nullable
  protected GitRepository getSourceRepository() {
    return mySourceRepository;
  }

  @Transient
  public void setSourceRepository(@Nullable GitRepository sourceRepository) {
    mySourceRepository = sourceRepository;
  }

  /**
   * The stash message is how we identify which item to unstash when the session ends. Stashes are ordered and may not
   * necessarily have the same ordinal value because new stashes are inserted at the top.
   */
  @Transient
  @Nullable
  protected String getStashMessage() {
    return myStashMessage;
  }

  @Transient
  public void setStashMessage(@Nullable String stashMessage) {
    myStashMessage = stashMessage;
  }

  /**
   * @return True if we have a valid git repo
   */
  protected boolean hasSourceRepository() {
    return getSourceRepository() != null;
  }

}
