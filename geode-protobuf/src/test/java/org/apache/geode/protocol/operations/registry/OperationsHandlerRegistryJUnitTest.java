package org.apache.geode.protocol.operations.registry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.apache.geode.protocol.operations.OperationHandler;
import org.apache.geode.protocol.operations.registry.exception.OperationHandlerAlreadyRegisteredException;
import org.apache.geode.protocol.operations.registry.exception.OperationHandlerNotRegisteredException;
import org.apache.geode.serialization.SerializationService;
import org.apache.geode.test.junit.categories.UnitTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTest.class)
public class OperationsHandlerRegistryJUnitTest {
  public static final int DUMMY_OPERATION_CODE = 999;
  private OperationsHandlerRegistry operationsHandlerRegistry;

  @Before
  public void setup() throws OperationHandlerAlreadyRegisteredException {
    operationsHandlerRegistry = new OperationsHandlerRegistry();
  }

  @Test
  public void testAddOperationsHandlerForOperationType()
      throws OperationHandlerAlreadyRegisteredException {
    int initialHandlerCount = operationsHandlerRegistry.getRegisteredOperationHandlersCount();
    operationsHandlerRegistry.registerOperationHandlerForOperationId(DUMMY_OPERATION_CODE,
        new DummyOperationHandler());
    assertEquals(initialHandlerCount + 1, operationsHandlerRegistry.getRegisteredOperationHandlersCount());
  }

  @Test
  public void testAddingDuplicateOperationsHandlerForOperationType_ThrowsException()
      throws OperationHandlerAlreadyRegisteredException, OperationHandlerNotRegisteredException {
    DummyOperationHandler expectedOperationHandler = new DummyOperationHandler();
    operationsHandlerRegistry.registerOperationHandlerForOperationId(DUMMY_OPERATION_CODE, expectedOperationHandler);
    int initialHandlerCount = operationsHandlerRegistry.getRegisteredOperationHandlersCount();
    boolean exceptionCaught = false;
    try {
      operationsHandlerRegistry.registerOperationHandlerForOperationId(DUMMY_OPERATION_CODE,
          new DummyOperationHandler());
    } catch (OperationHandlerAlreadyRegisteredException e) {
      exceptionCaught = true;
    }
    assertTrue(exceptionCaught);
    assertEquals(initialHandlerCount, operationsHandlerRegistry.getRegisteredOperationHandlersCount());
    assertSame(expectedOperationHandler,
        operationsHandlerRegistry.getOperationHandlerForOperationId(DUMMY_OPERATION_CODE));
  }

  @Test
  public void testGetOperationsHandlerForOperationType()
      throws OperationHandlerAlreadyRegisteredException, OperationHandlerNotRegisteredException {
    DummyOperationHandler expectedOperationHandler = new DummyOperationHandler();
    operationsHandlerRegistry.registerOperationHandlerForOperationId(DUMMY_OPERATION_CODE, expectedOperationHandler);
    OperationHandler operationHandler =
        operationsHandlerRegistry.getOperationHandlerForOperationId(DUMMY_OPERATION_CODE);
    assertSame(expectedOperationHandler, operationHandler);
  }

  @Test
  public void testGetOperationsHandlerForMissingOperationType_ThrowsException() {
    boolean exceptionCaught = false;
    try {
      operationsHandlerRegistry.getOperationHandlerForOperationId(DUMMY_OPERATION_CODE);
    } catch (OperationHandlerNotRegisteredException e) {
      exceptionCaught = true;
    }
    assertTrue(exceptionCaught);
  }

  private class DummyOperationHandler implements OperationHandler {

    @Override
    public Object process(SerializationService serializationService, Object request) {
      return null;
    }

    @Override
    public int getOperationCode() {
      return 0;
    }
  }


}
