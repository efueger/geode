/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geode.serialization;

import org.apache.geode.protocol.protobuf.BasicTypes;
import org.apache.geode.serialization.exception.SerializationServiceException;
import org.apache.geode.serialization.protobuf.translation.EncodingTypeTranslator;
import org.apache.geode.serialization.protobuf.translation.exception.UnsupportedEncodingTypeException;
import org.apache.geode.serialization.registry.SerializationCodecRegistry;
import org.apache.geode.serialization.registry.exception.CodecAlreadyRegisteredForTypeException;
import org.apache.geode.serialization.registry.exception.CodecNotRegisteredForTypeException;

public class ProtobufSerializationServiceImpl implements SerializationService<BasicTypes.EncodingType> {
  private SerializationCodecRegistry serializationCodecRegistry = new SerializationCodecRegistry();
  private EncodingTypeTranslator translator = new EncodingTypeTranslator();

  public ProtobufSerializationServiceImpl() throws CodecAlreadyRegisteredForTypeException {
  }

  @Override
  public byte[] encode(BasicTypes.EncodingType encodingTypeValue, Object value) throws SerializationServiceException {
    try {
      TypeCodec codecForType = getTypeCodecForProtobufType(encodingTypeValue);
      return codecForType.encode(value);
    } catch (UnsupportedEncodingTypeException | CodecNotRegisteredForTypeException e) {
      e.printStackTrace();
      throw new SerializationServiceException("Failed to encode object: " + value + " with encoding type: " + encodingTypeValue,e);
    }
  }

  @Override
  public Object decode(BasicTypes.EncodingType encodingTypeValue, byte[] value)
      throws SerializationServiceException {
    try {
      TypeCodec codecForType = getTypeCodecForProtobufType(encodingTypeValue);
      return codecForType.decode(value);
    } catch (UnsupportedEncodingTypeException | CodecNotRegisteredForTypeException e) {
      e.printStackTrace();
      throw new SerializationServiceException("Failed to handle data of type:" + encodingTypeValue +" data: " + value,e);
    }
  }

  private TypeCodec getTypeCodecForProtobufType(BasicTypes.EncodingType encodingTypeValue) throws UnsupportedEncodingTypeException, CodecNotRegisteredForTypeException {
    SerializationType
        serializationTypeForEncodingType =
        translator.getSerializationTypeForEncodingType(encodingTypeValue);

    return serializationCodecRegistry.getCodecForType(serializationTypeForEncodingType);
  }
}
