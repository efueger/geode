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
package org.apache.geode.client.protocol;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.geode.protocol.OpsProcessor;
import org.apache.geode.protocol.operations.OperationHandler;
import org.apache.geode.protocol.operations.ProtobufRequestOperationParser;
import org.apache.geode.protocol.operations.registry.OperationsHandlerRegistry;
import org.apache.geode.protocol.operations.registry.exception.OperationHandlerNotRegisteredException;
import org.apache.geode.protocol.protobuf.ClientProtocol;
import org.apache.geode.protocol.protobuf.RegionAPI;
import org.apache.geode.serialization.SerializationService;
import org.apache.geode.serialization.registry.exception.CodecNotRegisteredForTypeException;
import org.junit.Assert;
import org.junit.Test;

public class OpsProcessorTest {
  @Test
  public void testOpsProcessor() throws CodecNotRegisteredForTypeException, OperationHandlerNotRegisteredException {
    OperationsHandlerRegistry opsHandlerRegistryStub = mock(OperationsHandlerRegistry.class);
    OperationHandler operationHandlerStub = mock(OperationHandler.class);
    SerializationService serializationServiceStub = mock(SerializationService.class);
    int operationID = ClientProtocol.Request.RequestAPICase.GETREQUEST.getNumber();

    ClientProtocol.Request messageRequest = ClientProtocol.Request.newBuilder()
        .setGetRequest(RegionAPI.GetRequest.newBuilder()).build();

    RegionAPI.GetResponse expectedResponse = RegionAPI.GetResponse.newBuilder().build();

    when(opsHandlerRegistryStub.getOperationHandlerForOperationId(operationID))
        .thenReturn(operationHandlerStub);
    when(operationHandlerStub.process(serializationServiceStub, ProtobufRequestOperationParser.getRequestForOperationTypeID(messageRequest)))
        .thenReturn(expectedResponse);

    OpsProcessor processor = new OpsProcessor(opsHandlerRegistryStub, serializationServiceStub);
    ClientProtocol.Response response = processor.process(messageRequest);
    Assert.assertEquals(expectedResponse, response.getGetResponse());
  }
}
