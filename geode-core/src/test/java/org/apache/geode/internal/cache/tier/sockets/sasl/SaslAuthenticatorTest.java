/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.geode.internal.cache.tier.sockets.sasl;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;

import javax.security.sasl.SaslException;
import javax.security.sasl.SaslServer;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.ArgumentCaptor;

import org.apache.geode.internal.security.SecurityService;
import org.apache.geode.test.junit.categories.UnitTest;

@Category(UnitTest.class)
public class SaslAuthenticatorTest {
  @Test
  public void authenticateClientPassesResponsesToSaslServerTillComplete() throws Exception {
    SaslServer saslServerMock = mock(SaslServer.class);
    SaslMessenger saslMessengerStub = mock(SaslMessenger.class);
    SaslAuthenticator
        saslServer = new SaslAuthenticator(saslServerMock, saslMessengerStub);
    ArgumentCaptor<byte[]> saslServerArgumentCaptor = ArgumentCaptor.forClass(byte[].class);
    ArgumentCaptor<byte[]> messengerArgumentCaptor = ArgumentCaptor.forClass(byte[].class);
    byte[][] challengesFromServer = {
        new byte[] {0, 1, 2},
        new byte[0],
    };
    when(saslServerMock.evaluateResponse(isA(byte[].class))).thenReturn(challengesFromServer[0],
        challengesFromServer[1]);
    when(saslServerMock.isComplete()).thenReturn(true);
    when(saslMessengerStub.readMessage()).thenReturn(new byte[] {6, 7, 8});

    boolean authenticateClient = saslServer.authenticateClient();

    verify(saslServerMock, times(1)).evaluateResponse(saslServerArgumentCaptor.capture());
    verify(saslMessengerStub, times(0)).sendMessage(messengerArgumentCaptor.capture());
    assertTrue(authenticateClient);

//    List<byte[]> sentMessages = messengerArgumentCaptor.getAllValues();
//    assertEquals(1, sentMessages.size());
//    assertArrayEquals(challengesFromServer[0], sentMessages.get(0));

    List<byte[]> passedResponses = saslServerArgumentCaptor.getAllValues();
    assertEquals(1, passedResponses.size());
    assertArrayEquals(new byte[] {6, 7, 8}, passedResponses.get(0)); // response from client

    verify(saslMessengerStub, times(1)).readMessage();
  }

  @Test
  public void authenticateClientReturnsFalseIfCredentialsAreWrong() throws IOException {
    SaslServer saslServerMock = mock(SaslServer.class);
    SaslMessenger saslMessengerStub = mock(SaslMessenger.class);
    SaslAuthenticator
        saslAuthenticator = new SaslAuthenticator(saslServerMock, saslMessengerStub);
    ArgumentCaptor<byte[]> saslServerArgumentCaptor = ArgumentCaptor.forClass(byte[].class);
    ArgumentCaptor<byte[]> messengerArgumentCaptor = ArgumentCaptor.forClass(byte[].class);
    byte[][] challengesFromServer = {
        new byte[] {0, 1, 2},
        new byte[0],
    };
    when(saslServerMock.evaluateResponse(isA(byte[].class))).thenThrow(new SaslException("Invalid response"));
    when(saslServerMock.isComplete()).thenReturn(false);
    when(saslMessengerStub.readMessage()).thenReturn(new byte[] {6, 7, 8});

    boolean clientIsAuthenticated = saslAuthenticator.authenticateClient();

    assertFalse(clientIsAuthenticated);

    verify(saslServerMock, times(1)).evaluateResponse(saslServerArgumentCaptor.capture());
//    verify(saslMessengerStub, times(1)).sendMessage(messengerArgumentCaptor.capture());
//    assertFalse(clientIsAuthenticated);
  }

  @Test
  public void authenticateClientReturnsFalseIfClientStopsResponding() throws IOException {
    SaslServer saslServerMock = mock(SaslServer.class);
    SaslMessenger saslMessengerStub = mock(SaslMessenger.class);
    SaslAuthenticator
            saslServer = new SaslAuthenticator(saslServerMock, saslMessengerStub);
    byte[][] challengesFromServer = {
            new byte[] {0, 1, 2},
            new byte[0],
    };
    when(saslServerMock.evaluateResponse(isA(byte[].class))).thenReturn(challengesFromServer[0]).thenReturn(new byte[0]);
    when(saslServerMock.isComplete()).thenReturn(false);
    when(saslMessengerStub.readMessage()).thenThrow(new SocketTimeoutException());

    boolean authenticateClient = saslServer.authenticateClient();

    assertFalse(authenticateClient);
  }
}
