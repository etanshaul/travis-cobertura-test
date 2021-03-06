package com.google.gct.idea.debugger;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.services.clouddebugger.Clouddebugger.Debugger;
import com.google.api.services.clouddebugger.Clouddebugger.Debugger.Debuggees;
import com.google.api.services.clouddebugger.Clouddebugger.Debugger.Debuggees.Breakpoints;
import com.google.gct.idea.testing.BasePluginTestCase;

import com.intellij.notification.Notification;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;

import org.hamcrest.beans.HasPropertyWithValue;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class CloudDebugGlobalPollerTest extends BasePluginTestCase {

  private static final String FAKE_USER_EMAIL = "foo@example.com";
  private static final String FAKE_DEBUGGEE_ID = "debuggee-id-123456789";

  private CloudDebugProcessState cloudDebugProcessState;
  private CloudDebugGlobalPoller cloudDebugGlobalPoller;
  private Notifications notificationsHandler;

  @Before
  public void setUp() throws Exception {
    cloudDebugProcessState = new CloudDebugProcessState();
    cloudDebugGlobalPoller = new CloudDebugGlobalPoller();
    notificationsHandler = setupNotificationHandlerForVerification();
  }

  @Test
  public void testPollForChanges_firesNotificationIfNoDebugClientObtained() {
    cloudDebugProcessState.setListenInBackground(true);
    cloudDebugProcessState.setProject(getProject());

    cloudDebugGlobalPoller.pollForChanges(cloudDebugProcessState);

    assertFalse(cloudDebugProcessState.isListenInBackground());
    verifyNotificationFired();
  }

  @Test
  public void testPollForChanges_firesNotificationOnIOException() throws IOException {
    cloudDebugProcessState.setListenInBackground(true);
    cloudDebugProcessState.setUserEmail(FAKE_USER_EMAIL);
    cloudDebugProcessState.setDebuggeeId(FAKE_DEBUGGEE_ID);
    cloudDebugProcessState.setProject(getProject());

    setupCloudDebuggerBackendMockWithException(FAKE_USER_EMAIL, new IOException());

    cloudDebugGlobalPoller.pollForChanges(cloudDebugProcessState);

    assertFalse(cloudDebugProcessState.isListenInBackground());
    verifyNotificationFired();
  }

  @NotNull
  private Notifications setupNotificationHandlerForVerification() {
    Notifications handler = mock(Notifications.class);
    // sending out notificationsHandler relies on several static method calls in
    // com.intellij.notification.Notifications, let's subscribe to them and do verification this way
    Application application = ApplicationManager.getApplication();
    getProject().getMessageBus().connect(application).subscribe(Notifications.TOPIC,
                                                               handler);
    return handler;
  }

  private void setupCloudDebuggerBackendMockWithException(String userEmail, IOException e) throws IOException {
    Breakpoints breakpoints = mock(Breakpoints.class);
    when(breakpoints.list(anyString())).thenThrow(e);
    Debuggees debuggees = mock(Debuggees.class);
    when(debuggees.breakpoints()).thenReturn(breakpoints);
    Debugger debugger = mock(Debugger.class);
    when(debugger.debuggees()).thenReturn(debuggees);
    CloudDebuggerClient
        .setClient(userEmail + CloudDebuggerClient.SHORT_CONNECTION_TIMEOUT_MS, debugger);
  }

  private void verifyNotificationFired() {
    verify(notificationsHandler)
        .notify(argThat(HasPropertyWithValue.<Notification>hasProperty("title",
                                                                       is("Error while connecting to Cloud Debugger backend"))));
  }
}
