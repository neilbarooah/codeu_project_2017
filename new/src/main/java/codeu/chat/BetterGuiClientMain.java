// Copyright 2017 Google Inc.
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package codeu.chat;

import java.io.IOException;

import codeu.chat.client.Controller;
import codeu.chat.client.bettergui.ChatBetterGui;
import codeu.chat.client.View;
import codeu.chat.util.Logger;
import codeu.chat.util.RemoteAddress;
import codeu.chat.util.connections.ClientConnectionSource;
import codeu.chat.util.connections.ConnectionSource;

import javafx.application.Application;

final class BetterGuiClientMain {

  private static final Logger.Log LOG = Logger.newLog(BetterGuiClientMain.class);

  public static void main(String [] args) {

/*    try {
      Logger.enableFileOutput("chat_better_gui_client_log.log");
    } catch (IOException ex) {
      LOG.error(ex, "Failed to set logger to write to file");
    }

    LOG.info("============================= START OF LOG =============================");

    LOG.info("Starting chat client...");

    // Start up server connection/interface.

    final RemoteAddress address = RemoteAddress.parse(args[0]);

    try (
      final ConnectionSource source = new ClientConnectionSource(address.host, address.port)
    ) {
      final Controller controller = new Controller(source);
      final View view = new View(source);

      LOG.info("Creating client...");

      runClient(controller, view);

    } catch (Exception ex) {
      System.out.println("ERROR: Exception setting up client. Check log for details.");
      LOG.error(ex, "Exception setting up client.");
    }*/
      final String host = System.getProperty("host");
      final int myPort = Integer.parseInt(System.getProperty("port"));
      final String myAddress = host+"@"+myPort;

      Application.launch(ChatBetterGui.class, myAddress);
  }

  private static void runClient(Controller controller, View view) {

    //final ChatSimpleGui chatSimpleGui = new ChatSimpleGui(controller, view);
    //final ChatBetterGui chatSimpleGui = new ChatBetterGui(controller, view);



    LOG.info("Created client");

    //chatSimpleGui.run();

    LOG.info("chat client is running.");
  }
}