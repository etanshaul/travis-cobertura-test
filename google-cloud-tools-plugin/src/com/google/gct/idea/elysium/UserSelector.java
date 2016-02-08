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

import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.gct.idea.ui.CustomizableComboBox;
import com.google.gct.idea.ui.CustomizableComboBoxPopup;
import com.google.gct.idea.util.GctBundle;
import com.google.gct.login.CredentialedUser;
import com.google.gct.login.GoogleLogin;
import com.google.gct.login.IGoogleLoginCompletedCallback;
import com.google.gct.login.ui.GoogleLoginEmptyPanel;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

/**
 * A custom combobox that allows the user to select a GoogleLogin and also signin/add-account all within a single control.
 */
public class UserSelector extends CustomizableComboBox implements CustomizableComboBoxPopup {
  private static final int PREFERRED_HEIGHT = 240;
  private static final int POPUP_HEIGHTFRAMESIZE = 50;
  private static final int MIN_WIDTH = 450;

  private JBPopup myPopup;

  public UserSelector() {
    getTextField().setCursor(Cursor.getDefaultCursor());
    getTextField().getEmptyText().setText(GctBundle.message("select.user.emptytext"));
  }

  /**
   * Returns the selected credentialed user for the project id represented by getText().
   * Note that if the ProjectSelector is created with queryOnExpand, this value could be {@code null} even
   * if {@link #getText()} represents a valid project because the user has not expanded the owning {@link com.google.gct.login.GoogleLogin}.
   */
  @Nullable
  public CredentialedUser getSelectedUser() {
    if (Strings.isNullOrEmpty(getText())) {
      return null;
    }

    for(CredentialedUser user : GoogleLogin.getInstance().getAllUsers().values()) {
      if (user.getEmail() != null && user.getEmail().equalsIgnoreCase(getText())) {
        return user;
      }
    }

    return null;
  }

  @Override
  protected int getPreferredPopupHeight() {
    return !needsToSignIn() ? PREFERRED_HEIGHT : BaseGoogleLoginUI.PREFERRED_HEIGHT + POPUP_HEIGHTFRAMESIZE;
  }

  @Override
  protected CustomizableComboBoxPopup getPopup() {
    return this;
  }

  private static boolean needsToSignIn() {
    Map<String, CredentialedUser> users = GoogleLogin.getInstance().getAllUsers();

    return users.isEmpty();
  }

  @Override
  public void showPopup(RelativePoint showTarget) {
    if (myPopup == null || myPopup.isDisposed()) {
      PopupPanel popupPanel = new PopupPanel();

      popupPanel.initializeContent(getText());
      ComponentPopupBuilder popup = JBPopupFactory.getInstance().
        createComponentPopupBuilder(popupPanel, popupPanel.getInitialFocus());
      myPopup = popup.createPopup();
    }
    if (!myPopup.isVisible()) {
      myPopup.show(showTarget);
    }
  }

  /**
   * The custom popup panel for user selection hosts a listbox of users surrounded with an option
   * to add an account.
   */
  private class PopupPanel extends GoogleLoginEmptyPanel implements ListCellRenderer {
    private JBList myJList;
    private ProjectSelectorCredentialedUser myProjectSelectorCredentialedUser;
    private UserSelectorGoogleLogin myUserSelectorGoogleLogin;
    private int myHoverIndex = -1;

    public PopupPanel() {
      myProjectSelectorCredentialedUser = new ProjectSelectorCredentialedUser();
      myProjectSelectorCredentialedUser.setOpaque(true);
      myUserSelectorGoogleLogin = new UserSelectorGoogleLogin();
    }

    public JComponent getInitialFocus() {
      return myJList;
    }

    public void initializeContent(@Nullable String selectedItem) {
      DefaultListModel model = new DefaultListModel();
      myJList = new JBList(model);

      myJList.setOpaque(false);
      myJList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      myJList.setCellRenderer(this);
      for(CredentialedUser user : GoogleLogin.getInstance().getAllUsers().values()) {
        model.addElement(user);
        if (user.getEmail() != null && user.getEmail().equalsIgnoreCase(selectedItem)) {
          myJList.setSelectedValue(user, true);
        }
      }

      if (model.getSize() == 0) {
        model.addElement(new EmptyMarker());
      }

      getContentPane().setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      getContentPane().setViewportView(myJList);
      myJList.addListSelectionListener(new ListSelectionListener() {
        @Override
        public void valueChanged(ListSelectionEvent e) {
          Object user = myJList.getSelectedValue();
          if (user != null && user instanceof CredentialedUser) {
              UserSelector.this.setText(((CredentialedUser)user).getEmail());
              SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                  UserSelector.this.hidePopup();
                }
              });
          }
        }
      });

      myJList.addMouseMotionListener(new MouseAdapter() {
        @Override
        public void mouseMoved(MouseEvent me) {
          Point p = new Point(me.getX(),me.getY());
          int index = myJList.locationToIndex(p);
          if (index != myHoverIndex) {
            int oldIndex = myHoverIndex;
            myHoverIndex = index;
            if (oldIndex >= 0) {
              myJList.repaint(myJList.getUI().getCellBounds(myJList, oldIndex, oldIndex));
            }
            if (myHoverIndex >= 0) {
              if (myJList.getSelectedIndex() >= 0) {
                myJList.clearSelection();
              }
              myJList.repaint(myJList.getUI().getCellBounds(myJList, myHoverIndex, myHoverIndex));
            }
          }
        }
      });

      myJList.requestFocusInWindow();
      int preferredWidth =  UserSelector.this.getWidth();
      setPreferredSize(new Dimension(Math.max(MIN_WIDTH, preferredWidth), getPreferredPopupHeight()));
    }

    @Override
    protected void doLogin() {
      GoogleLogin.getInstance().logIn(null, new IGoogleLoginCompletedCallback() {
        @Override
        public void onLoginCompleted() {
          SwingUtilities.invokeLater(new Runnable() {
            @SuppressWarnings("ConstantConditions") // This suppresses a nullref warning for GoogleLogin.getInstance().getActiveUser().
            @Override
            public void run() {
              if (GoogleLogin.getInstance().getActiveUser() != null) {
                UserSelector.this.setText(GoogleLogin.getInstance().getActiveUser().getEmail());
              }
            }
          });
        }
      });
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      if (value instanceof EmptyMarker) {
        return myUserSelectorGoogleLogin;
      }

      CredentialedUser targetUser = (CredentialedUser)value;
      if (targetUser != null) {
        myProjectSelectorCredentialedUser.initialize(targetUser.getPicture(), targetUser.getName(), targetUser.getEmail());
      }
      else {
        myProjectSelectorCredentialedUser.initialize(null, "", null);
      }

      if (isSelected || cellHasFocus || index == myHoverIndex) {
        myProjectSelectorCredentialedUser.setBackground(list.getSelectionBackground());
        myProjectSelectorCredentialedUser.setForeground(list.getSelectionForeground());
      }
      else {
        myProjectSelectorCredentialedUser.setBackground(list.getBackground());
        myProjectSelectorCredentialedUser.setForeground(list.getForeground());
      }

      return myProjectSelectorCredentialedUser;
    }

    /**
     * This class marks an empty credential list, giving us an indication to show the signin UI.
     */
    class EmptyMarker {
    }
  }

  @Override
  public void hidePopup() {
    if (isPopupVisible()) {
      myPopup.closeOk(null);
    }
  }

  @Override
  public boolean isPopupVisible() {
    return myPopup != null && !myPopup.isDisposed() && myPopup.isVisible();
  }
}
