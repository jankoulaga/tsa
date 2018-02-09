package com.ju.tsa.model;

import com.ju.tsa.model.Transaction;
import com.ju.tsa.model.Window;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class WindowTest {

    @Test
    public void addTransactionToAWindow() {
        Window window = new Window(BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN, 1);
        Window updatedWindow = window.withTransaction(new Transaction(BigDecimal.valueOf(3), 123L));

        Assert.assertEquals("overall sum should be the sum of all amounts", BigDecimal.valueOf(13), updatedWindow.getSum());
        Assert.assertEquals("show the correct count of trx.", 2, updatedWindow.getCount());
        Assert.assertEquals("max trx.amount is chosen correctly", BigDecimal.valueOf(10), updatedWindow.getMaximum());
        Assert.assertEquals("smallest trx is chosen correctly", BigDecimal.valueOf(3), updatedWindow.getMinimum());
        Assert.assertEquals("average should be correct", new BigDecimal("6.50"), updatedWindow.getAverage());
    }

    @Test
    public void flattenWindows() {

        Window windowOne = new Window(BigDecimal.valueOf(120), BigDecimal.valueOf(20), BigDecimal.valueOf(2), BigDecimal.valueOf(6), 20);
        Window windowTwo = new Window(BigDecimal.valueOf(60), BigDecimal.valueOf(20), BigDecimal.valueOf(2), BigDecimal.valueOf(12), 5);
        Window windowThree = new Window(BigDecimal.valueOf(250), BigDecimal.valueOf(250), BigDecimal.valueOf(250), BigDecimal.valueOf(250), 1);
        Window windowFour = new Window(BigDecimal.valueOf(900), BigDecimal.valueOf(300), BigDecimal.valueOf(20), BigDecimal.valueOf(150), 8);

        List<Window> windows = Arrays.asList(windowOne, windowTwo, windowThree, windowFour);
        Window flattened = Window.flatten(windows);

        Assert.assertEquals("overall sum should be the sum of all amounts", BigDecimal.valueOf(1330), flattened.getSum());
        Assert.assertEquals("show the correct count of trx.", 34, flattened.getCount());
        Assert.assertEquals("max trx.amount is chosen correctly", BigDecimal.valueOf(300), flattened.getMaximum());
        Assert.assertEquals("smallest trx is chosen correctly", BigDecimal.valueOf(2), flattened.getMinimum());
        Assert.assertEquals("average should be correct", BigDecimal.valueOf(39.12), flattened.getAverage());
    }
}
