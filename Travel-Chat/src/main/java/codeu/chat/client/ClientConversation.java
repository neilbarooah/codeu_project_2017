// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package codeu.chat.client;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.List;
import java.util.regex.Matcher;

import codeu.chat.common.Conversation;
import codeu.chat.common.ConversationSummary;
import codeu.chat.common.Group;
import codeu.chat.common.GroupSummary;
import codeu.chat.util.Logger;
import codeu.chat.util.Method;
import codeu.chat.util.Uuid;
import codeu.chat.util.store.Store;

public final class ClientConversation {

  private final static Logger.Log LOG = Logger.newLog(ClientConversation.class);

  private final Controller controller;
  private final View view;

  private ConversationSummary currentSummary = null;
  private Conversation currentConversation = null;

  private final ClientUser userContext;
  private final ClientGroup groupContext;
  private ClientMessage messageContext = null;

  // This is the set of conversations known to the server.
  private final Map<Uuid, ConversationSummary> summariesByUuid = new HashMap<>();

  // This is the set of conversations known to the server, sorted by title.
  private Store<String, ConversationSummary> summariesSortedByTitle =
      new Store<>(String.CASE_INSENSITIVE_ORDER);

  public ClientConversation(Controller controller, View view, ClientUser userContext, ClientGroup groupContext) {
    this.controller = controller;
    this.view = view;
    this.userContext = userContext;
    this.groupContext = groupContext;
    this.groupContext.setConversationContext(this);
  }

  public void setMessageContext(ClientMessage messageContext) {
    this.messageContext = messageContext;
  }

  // Validate the title of the conversation
  static public boolean isValidTitle(String title) {
    boolean clean = true;
    if ((title.length() <= 0) || (title.length() > 64)) {
      clean = false;
    } else {

      // only accepts conversation titles that contain alphabets and spaces.
      Pattern validPattern = Pattern.compile("^[ A-z]+$");
      Matcher match = validPattern.matcher(title);
      clean = match.matches();
    }
    return clean;
  }

  // Check if this has already existed
  public boolean ifExistedTitle(String titleName) {
    final ConversationSummary title = summariesSortedByTitle.first(titleName);
    return (title != null);
  }

  public boolean hasCurrent() {
    return (currentSummary != null);
  }

  public ConversationSummary getCurrent() {
    return currentSummary;
  }

  public Uuid getCurrentId() { return (currentSummary != null) ? currentSummary.id : null; }

  public int currentMessageCount() {
    return messageContext.currentMessageCount();
  }

  public void showCurrent() {
    printConversation(currentSummary, userContext, groupContext);
  }

  public void resetCurrent(boolean replaceAll) {
    updateConversations(replaceAll);
  }

  // Update the list of conversations for the current group.
  public void updateConversations(boolean replaceAll) {
    updateConversations(groupContext.getCurrent(), replaceAll);
  }

  // Update the list of conversations for the given group.
  public void updateConversations(GroupSummary group, boolean replaceAll) {

  }

  public void startConversation(String title, Uuid owner, Uuid group) {
    final boolean validInputs = isValidTitle(title);
    final boolean existed = ifExistedTitle(title);

    final Conversation conv = (validInputs && !existed) ? controller.newConversation(title, owner, group) : null;
    if (conv == null) {
      System.out.format("Error: conversation not created - %s.\n",
          (!validInputs) ? "bad input value" : ((existed) ? "existed title" : "server failure"));
    } else {
      LOG.info("New conversation: Title= \"%s\" UUID= %s", conv.title, conv.id);
      currentSummary = conv.summary;
      updateAllConversations(currentSummary != null);
    }
  }

  public void deleteConversation(String title) {
    final Conversation c = controller.deleteConversation(title);
    if (c == null) {
      System.out.format("Error: conversation not deleted. Server failure");
    } else {
      LOG.info("Deleted conversation: Title= \"%s\" UUID= %s", c.title, c.id);
      Boolean currentDeleted = getCurrentId().equals(c.id);
      if (currentDeleted) {
        currentSummary = null;
        updateCurrentConversation();
      }
      updateAllConversations(currentSummary != null);
    }
  }

  public void setCurrent(ConversationSummary conv) { currentSummary = conv; }

  public void showAllConversations(Uuid group_id) {
    updateAllConversations(false);

    for (final ConversationSummary c : summariesByUuid.values()) {
        printConversation(c, userContext, groupContext);
    }
  }

  // Get a single conversation from the server.
  public Conversation getConversation(Uuid conversationId) {
    for (final Conversation c : view.getConversations(Arrays.asList(conversationId))) {
      return c;
    }
    return null;
  }

  private void joinConversation(String match) {
    Method.notImplemented();
  }

  private void leaveCurrentConversation() {
    Method.notImplemented();
  }

  private void updateCurrentConversation() {
    if (currentSummary == null) {
      currentConversation = null;
    } else {
      currentConversation = getConversation(currentSummary.id);
      if (currentConversation == null) {
        LOG.info("GetConversation: current=%s, current.id=%s, but currentConversation == null",
            currentSummary, currentSummary.id);
      } else {
        LOG.info("Get Conversation: Title=\"%s\" UUID=%s first=%s last=%s\n",
            currentConversation.title, currentConversation.id, currentConversation.firstMessage,
            currentConversation.lastMessage);
      }
    }
  }

  public int conversationsCount() {
   return summariesByUuid.size();
  }

  public Iterable<ConversationSummary> getConversationSummaries() {
    return summariesSortedByTitle.all();
  }

  // Update the list of known Conversations.
  // If the input currentChanged is true, then re-establish the state of
  // the current Conversation, including its messages.
  public void updateAllConversations(boolean currentChanged) {

    summariesByUuid.clear();
    summariesSortedByTitle = new Store<>(String.CASE_INSENSITIVE_ORDER);

    for (final ConversationSummary cs : view.getAllConversations()) {
      summariesByUuid.put(cs.id, cs);
      summariesSortedByTitle.insert(cs.title, cs);
    }

    if (currentChanged) {
      updateCurrentConversation();
      messageContext.resetCurrent(true);
    }
  }

  // Print Conversation.  User context is used to map from owner UUID to name.
  public static void printConversation(ConversationSummary c, ClientUser userContext, ClientGroup groupContext) {
    if (c == null) {
      System.out.println("Null conversation");
    } else {
      final String name = (userContext == null) ? null : userContext.getName(c.owner);
      final String ownerName = (name == null) ? "" : String.format(" (%s)", name);
      final String groupName = (groupContext == null) ? null : groupContext.getGroup(c.group).title;
      final String groupNameFormatted = (groupName == null) ? "" : String.format(" (%s)", groupName);
      System.out.format(" Title: %s\n", c.title);
      System.out.format("    Id: %s owner: %s%s group: %s%s created %s\n", c.id, c.owner, ownerName, c.group, groupNameFormatted, c.creation);
    }
  }

  // Print Conversation outside of User context.
  public static void printConversation(ConversationSummary c) {
    printConversation(c, null, null);
  }
}
