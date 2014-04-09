package com.github.kl.webintegration.app.test;

import com.github.kl.webintegration.app.PluginControllerCollection;
import com.github.kl.webintegration.app.controllers.PluginController;

import junit.framework.TestCase;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PluginControllerCollectionTest extends TestCase {

    private PluginController mockController1 = makeMockPluginController("mock");
    private PluginController mockController2 = makeMockPluginController("test");

    private PluginControllerCollection sut;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        TestHelper.setDexCache();

        sut = new PluginControllerCollection(mockController1, mockController2);
    }

    private PluginController makeMockPluginController(String type) {
        PluginController controller = mock(PluginController.class);
        when(controller.getType()).thenReturn(type);
        return controller;
    }

    public void testReturnsTheRightControllerForAGivenType() throws Exception {
        assertSame(mockController1, sut.getPluginController("mock"));
        assertSame(mockController2, sut.getPluginController("test"));
    }

    public void testThrowsExceptionWhenGivenAnUnknownType() {
        try {
            sut.getPluginController("coffee");
            fail("Expected PluginControllerNotFoundException to be thrown");
        } catch (PluginControllerCollection.PluginControllerNotFoundException e) {
            assertEquals("coffee", e.getType());
        }
    }
}
