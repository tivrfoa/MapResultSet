package com.github.tivrfoa.mapresultset;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class MappingProcessorTest {
    @Test
    void testGetDefaultValueForType() {

    }

    @Test
    void testUppercaseFirstLetter() {
        assertEquals("Oi", MappingProcessor.uppercaseFirstLetter("oi"));
    }

    void testQueryNotFinal() {

    }
}
