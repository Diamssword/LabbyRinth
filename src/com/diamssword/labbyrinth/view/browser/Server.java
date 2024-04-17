package com.diamssword.labbyrinth.view.browser;/*
 * Copyright 2020 Jens "Nigjo" Hofschröer.
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

import com.diamssword.labbyrinth.LauncherVariables;

import java.io.File;

/**
 * Runs a simple Debug Server in a local folder.
 * This file can be directly started with Java 11+.
 */
class Server
{
  static final java.util.Map<String, String> types =
      new java.util.HashMap<>(java.util.Map.of(
          "html", "text/html",
          "js", "text/javascript",
          "css", "text/css",
          "jpg", "image/jpeg",
          "png", "image/png",
              "svg", "image/svg+xml",
          "ico", "image/x-icon",
          "json", "application/json"
      ));

  private static int port = 51973;
  private static String prefix = "/";

  public static void args(String[] a)
  {
    for(String arg : a)
    {
      try
      {
        port = Integer.parseInt(arg);
      }
      catch(NumberFormatException ex)
      {
        if(arg.indexOf('=') > 0)
        {
          String pair[] = arg.split("=", 2);
          types.putIfAbsent(pair[0], pair[1]);
        }
        else
        {
          prefix = arg;
          if(prefix.charAt(0) != '/')
          {
            prefix = '/' + prefix;
          }
          if(prefix.charAt(prefix.length() - 1) != '/')
          {
            prefix += '/';
          }
        }
      }
    }
  }

  public static void start(String[] a) throws java.io.IOException
  {
    args(a);
    java.net.InetSocketAddress host =
        new java.net.InetSocketAddress("localhost", port);
    com.sun.net.httpserver.HttpServer server =
        com.sun.net.httpserver.HttpServer.create(host, 0
        );
    server.createContext("/", Server::handleRequest);
    server.start();
    System.out.println("Server is running at http://"
        + host.getHostName() + ":" + host.getPort() + prefix);
  }

  private static void handleRequest(com.sun.net.httpserver.HttpExchange t)
      throws java.io.IOException
  {
    java.net.URI uri = t.getRequestURI();
    if(uri.toString().endsWith("/"))
    {
      uri = uri.resolve("index.html");
    }
    String path = uri.getPath();
    java.io.File local = null;
    if(path.startsWith(prefix))
    {
      local = new java.io.File(new File(LauncherVariables.gameDirectory,"view"), path.substring(prefix.length()));
    }
    System.out.print(new java.util.Date().toString());
    System.out.print(" GET " + uri);
    if(local != null && local.exists())
    {
      //String response = "This is the response of "+local.getAbsolutePath();
      String filename = local.getName();
      String ext = filename.substring(filename.lastIndexOf('.') + 1);
      if(types.containsKey(ext))
      {
        System.out.print(" " + types.get(ext));
        t.getResponseHeaders()
            .add("Content-Type", types.get(ext));
      }
      System.out.print(" 200 " + local.length());
      t.sendResponseHeaders(200, local.length());
      try(java.io.OutputStream out = t.getResponseBody())
      {
        java.nio.file.Files.copy(local.toPath(), out);
      }
    }
    else
    {
      System.out.print(" 404");
      String response = "File not found " + uri.toString();
      t.sendResponseHeaders(404, response.length());
      try(java.io.OutputStream os = t.getResponseBody())
      {
        os.write(response.getBytes());
      }
    }
    System.out.println();
  }
}