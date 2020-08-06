// Copyright 2020 Google LLC

// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at

//     https://www.apache.org/licenses/LICENSE-2.0

// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import com.google.appengine.api.datastore.*;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.gson.Gson;
import com.google.sps.servlets.UserServlet;
import com.google.sps.user.User;
import com.google.sps.user.repository.impl.DatastoreUserRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public final class UserServletTest {
  private static final String ID = "userid";
  private static final String EMAIL = "user@gmail.com";
  private static final String NAME = "username";
  private static final String SELF_INTRODUCTION = "I am the user";
  private static final String IMG_KEY = "/img.com";

  private final User toSaveUser =
          new User.Builder(ID, EMAIL).setName(NAME).setPublicPortfolio(true).addSelfIntroduction(SELF_INTRODUCTION).addImgKey(IMG_KEY).build();
  private User toGetUser;

  private UserServlet userServlet;
  private Map<String, Object> attributeToValue = new HashMap<>();
  private LocalServiceTestHelper helper;

  private HttpServletRequest request;
  private HttpServletResponse response;

  @Before
  public void setup() {
    // Set the userdata that the Userservice will return.
    attributeToValue.put("com.google.appengine.api.users.UserService.user_id_key", (Object) ID);
    helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig())
            .setEnvIsLoggedIn(true)
            .setEnvAuthDomain("localhost")
            .setEnvEmail(EMAIL)
            .setEnvAttributes(attributeToValue);
    helper.setUp();

    userServlet = new UserServlet();

    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

//   @Test
//   public void doPost() throws IOException, ServletException {
//     when(request.getParameter(UserServlet.NAME_INPUT)).thenReturn(NAME);
//     when(request.getParameter(UserServlet.SELF_INTRODUCTION_INPUT)).thenReturn(SELF_INTRODUCTION);
//     when(request.getParameter(UserServlet.PUBLIC_PORTFOLIO_INPUT)).thenReturn("private");

//     StringWriter sw = new StringWriter();
//     PrintWriter pw = new PrintWriter(sw);

//     when(response.getWriter()).thenReturn(pw);

//     // Save the currenly logged in user's data
//     userServlet.doPost(request, response);

//     // Get the currenly logged in user's previously saved data
//     DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
//     Key userKey = KeyFactory.createKey(DatastoreUserRepository.ENTITY_KIND, ID);
//     try {
//       Entity userEntity = datastore.get(userKey);
//       assertEquals(NAME, userEntity.getProperty(DatastoreUserRepository.NAME_PROPERTY));
//       assertEquals(EMAIL, userEntity.getProperty(DatastoreUserRepository.EMAIL_PROPERTY));
//       assertEquals(SELF_INTRODUCTION, userEntity.getProperty(DatastoreUserRepository.SELF_INTRODUCTION_PROPERTY));
//       assertEquals(false, userEntity.getProperty(DatastoreUserRepository.PUBLIC_PORTFOLIO_PROPERTY));
//     } catch (EntityNotFoundException e) {
//       fail("Entity not found: " + e);
//     }
//   }

  @Test
  public void doGet_existingUser_returnsUser() throws IOException, ServletException {
    // Create entity to save.
    Entity userEntity = new Entity(DatastoreUserRepository.ENTITY_KIND, toSaveUser.getId());
    userEntity.setProperty(DatastoreUserRepository.NAME_PROPERTY, toSaveUser.getName());
    userEntity.setProperty(DatastoreUserRepository.EMAIL_PROPERTY, toSaveUser.getEmail());
    userEntity.setProperty(DatastoreUserRepository.PUBLIC_PORTFOLIO_PROPERTY, toSaveUser.portfolioIsPublic());
    userEntity.setProperty(DatastoreUserRepository.SELF_INTRODUCTION_PROPERTY, toSaveUser.getSelfIntroduction());
    userEntity.setProperty(DatastoreUserRepository.IMG_KEY_PROPERTY, toSaveUser.getImgKey());

    // Save entity to datastore.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(userEntity);

    // Get User corresponding to entity from servlet
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);

    when(response.getWriter()).thenReturn(pw);

    // Get the currenly logged in user's previously saved data
    userServlet.doGet(request, response);

    pw.flush();
    Gson gson = new Gson();
    User resultUser = gson.fromJson(sw.toString(), User.class);
    assertEquals(ID, resultUser.getId());
    assertEquals(EMAIL, resultUser.getEmail());
    assertEquals(NAME, resultUser.getName());
    assertTrue(resultUser.portfolioIsPublic());
    assertEquals(SELF_INTRODUCTION, resultUser.getSelfIntroduction());
  }

  @Test
  public void doGet_inexistentUser_returnsNull() throws IOException, ServletException {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);

    when(response.getWriter()).thenReturn(pw);

    // Try to get the currenly logged in user's data.
    // It is not saved.
    userServlet.doGet(request, response);

    pw.flush();
    Gson gson = new Gson();
    User resultUser = gson.fromJson(sw.toString(), User.class);
    assertEquals(null, resultUser);
  }
}