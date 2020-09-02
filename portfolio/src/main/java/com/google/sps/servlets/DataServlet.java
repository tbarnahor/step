// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.sps.data.Comment;

/** Servlet responsible for creating new comments and listing the comments. */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Comment");
        PreparedQuery results =  datastore.prepare(query);
        List<Key> commentsKeys = new ArrayList<>();
        for (Entity entity : results.asIterable()) {
        commentsKeys.add(entity.getKey());
    }
    response.setContentType("application/json");
    response.getWriter().println(new Gson().toJson(commentsKeys));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the input from the form
    String text = request.getParameter("comment");
    String maxComments = request.getParameter("numOfCom");
    long timestamp = System.currentTimeMillis();

    //Create entity for Datastore
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("text", text);
    commentEntity.setProperty("timestamp", timestamp);
    datastore.put(commentEntity);

    //If no maxComments was selected pass 1 as default
    if (!maxComments.equals("5") && !maxComments.equals("10")) {
        maxComments = "1";
    }

    // Redirect back to the HTML page
    response.sendRedirect("/index.html?maxComments=" + maxComments);
  }
}
