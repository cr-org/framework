package com.vaadin.tests.components.grid;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import com.vaadin.testbench.By;
import com.vaadin.testbench.elements.ButtonElement;
import com.vaadin.testbench.elements.GridElement;
import com.vaadin.testbench.elements.GridElement.GridCellElement;
import com.vaadin.testbench.elements.GridElement.GridRowElement;
import com.vaadin.testbench.elements.LabelElement;
import com.vaadin.testbench.elements.NotificationElement;
import com.vaadin.tests.tb3.MultiBrowserTest;

public class GridComponentsTest extends MultiBrowserTest {

    @Test
    public void testReuseTextFieldOnScroll() {
        openTestURL();
        GridElement grid = $(GridElement.class).first();
        editTextFieldInCell(grid, 0, 1);
        // Scroll out of view port
        grid.getRow(900);
        // Scroll back
        grid.getRow(0);

        WebElement textField = grid.getCell(0, 1)
                .findElement(By.tagName("input"));
        Assert.assertEquals("TextField value was reset", "Foo",
                textField.getAttribute("value"));
        Assert.assertTrue("No mention in the log",
                logContainsText("1. Reusing old text field for: Row 0"));
    }

    @Test
    public void testReuseTextFieldOnSelect() {
        openTestURL();
        GridElement grid = $(GridElement.class).first();
        editTextFieldInCell(grid, 1, 1);
        // Select row
        grid.getCell(1, 1).click(1, 1);

        WebElement textField = grid.getCell(1, 1)
                .findElement(By.tagName("input"));
        Assert.assertEquals("TextField value was reset", "Foo",
                textField.getAttribute("value"));
        Assert.assertTrue("No mention in the log",
                logContainsText("1. Reusing old text field for: Row 1"));
    }

    @Test
    public void testReplaceData() {
        openTestURL();
        assertRowExists(5, "Row 5");
        $(ButtonElement.class).caption("Reset data").first().click();
        assertRowExists(5, "Row 1005");
    }

    @Test
    public void testTextFieldSize() {
        openTestURL();
        GridCellElement cell = $(GridElement.class).first().getCell(0, 1);
        int cellWidth = cell.getSize().getWidth();
        int fieldWidth = cell.findElement(By.tagName("input")).getSize()
                .getWidth();
        // padding left and right, +1 to fix sub pixel issues
        int padding = 18 * 2 + 1;

        int extraSpace = Math.abs(fieldWidth - cellWidth);
        Assert.assertTrue("Too much unused space in cell. Expected: " + padding
                + " Actual: " + extraSpace, extraSpace <= padding);
    }

    private void editTextFieldInCell(GridElement grid, int row, int col) {
        WebElement textField = grid.getCell(row, col)
                .findElement(By.tagName("input"));
        textField.clear();
        textField.sendKeys("Foo");
    }

    @Test
    public void testRow5() {
        openTestURL();
        assertRowExists(5, "Row 5");
    }

    @Test
    public void testRow0() {
        openTestURL();
        assertRowExists(0, "Row 0");
        Assert.assertEquals("Grid row height is not what it should be", 40,
                $(GridElement.class).first().getRow(0).getSize().getHeight());
    }

    @Test
    public void testRow999() {
        openTestURL();
        assertRowExists(999, "Row 999");
    }

    @Test
    public void testRow30() {
        openTestURL();
        Stream.of(30, 130, 230, 330).forEach(this::assertNoButton);
        IntStream.range(300, 310).forEach(this::assertNoButton);
    }

    @Test(expected = AssertionError.class)
    public void testRow31() {
        openTestURL();
        // There is a button on row 31. This should fail.
        assertNoButton(31);
    }

    @Test
    public void testHeaders() {
        openTestURL();
        GridElement grid = $(GridElement.class).first();
        GridCellElement headerCell = grid.getHeaderCell(0, 0);
        Assert.assertTrue("First header should contain a Label",
                headerCell.isElementPresent(LabelElement.class));
        Assert.assertEquals("Label",
                headerCell.$(LabelElement.class).first().getText());
        Assert.assertFalse("Second header should not contain a component",
                grid.getHeaderCell(0, 1).isElementPresent(LabelElement.class));
        Assert.assertEquals("Other Components",
                grid.getHeaderCell(0, 1).getText());
    }

    private void assertRowExists(int i, String string) {
        GridRowElement row = $(GridElement.class).first().getRow(i);
        Assert.assertEquals("Label text did not match", string,
                row.getCell(0).getText());
        row.findElement(By.id(string.replace(' ', '_').toLowerCase())).click();
        // IE 11 is slow, need to wait for the notification.
        waitUntil(driver -> isElementPresent(NotificationElement.class), 10);
        Assert.assertTrue("Notification should contain given text",
                $(NotificationElement.class).first().getText()
                        .contains(string));
        waitUntil(driver -> !isElementPresent(NotificationElement.class), 10);
    }

    private void assertNoButton(int i) {
        GridRowElement row = $(GridElement.class).first().getRow(i);
        Assert.assertFalse("Row " + i + " should not have a button",
                row.getCell(2).isElementPresent(ButtonElement.class));
    }
}
