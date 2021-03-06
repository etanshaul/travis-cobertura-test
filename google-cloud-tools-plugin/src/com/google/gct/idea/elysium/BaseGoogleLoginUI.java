/*
 * Copyright (C) 2014 The Android Open Source Project
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
package com.google.gct.idea.elysium;

import com.google.gct.login.ui.GoogleLoginIcons;

import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.UIUtil;

import org.jetbrains.annotations.NotNull;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

/**
 * UI for the node that prompts for Google Login.
 */
class BaseGoogleLoginUI extends JPanel {

  public static final int PREFERRED_HEIGHT = 150;
  public static final int MIN_WIDTH = 450;

  public BaseGoogleLoginUI(@NotNull String signinText) {
    setLayout(new GridBagLayout());
    setPreferredSize(new Dimension(MIN_WIDTH, PREFERRED_HEIGHT));
    setOpaque(false);

    JLabel googleIcon = new JBLabel();

    setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
    googleIcon.setHorizontalAlignment(SwingConstants.CENTER);
    googleIcon.setVerticalAlignment(SwingConstants.CENTER);
    googleIcon.setOpaque(false);
    googleIcon.setIcon(GoogleLoginIcons.GOOGLE_LOGO);
    googleIcon.setBorder(BorderFactory.createEmptyBorder(5,0,5,0));
    GridBagConstraints c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 0;
    c.weighty = 0;
    add(googleIcon, c);

    JTextArea signinTextArea = new JTextArea();
    signinTextArea.setFont(UIUtil.getLabelFont());
    signinTextArea.setLineWrap(true);
    signinTextArea.setWrapStyleWord(true);
    signinTextArea.setOpaque(false);
    signinTextArea.setText(signinText);
    c.gridx = 0;
    c.gridy = 1;
    c.weighty = 1;
    c.gridwidth = 2;
    c.weightx = 1;
    c.fill = GridBagConstraints.BOTH;
    c.anchor = GridBagConstraints.CENTER;
    add(signinTextArea, c);
  }
}
