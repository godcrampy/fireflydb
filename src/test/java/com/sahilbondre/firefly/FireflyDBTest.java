package com.sahilbondre.firefly;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

class FireflyDBTest {

    private static final String FOLDER_A = "/path/to/folderA";
    private static final String FOLDER_B = "/path/to/folderB";

    @Test
    void givenSameFolder_whenGetInstance_thenSameObjectReferenced() {
        // Given
        // Two instances with the same folder should reference the same object

        // When
        FireflyDB dbA1 = FireflyDB.getInstance(FOLDER_A);
        FireflyDB dbA2 = FireflyDB.getInstance(FOLDER_A);

        // Then
        assertSame(dbA1, dbA2);
        assertEquals(FOLDER_A, dbA1.getFolderPath());
        assertEquals(FOLDER_A, dbA1.getFolderPath());
    }

    @Test
    void givenDifferentFolders_whenGetInstance_thenDifferentObjectsReferenced() {
        // Given
        // Two instances with different folders should reference different objects

        // When
        FireflyDB dbA = FireflyDB.getInstance(FOLDER_A);
        FireflyDB dbB = FireflyDB.getInstance(FOLDER_B);

        // Then
        assertNotSame(dbA, dbB);
        assertEquals(FOLDER_A, dbA.getFolderPath());
        assertEquals(FOLDER_B, dbB.getFolderPath());
    }

    @Test
    void givenGetInstanceMethod_whenCheckSynchronizedModifier_thenTrue() throws NoSuchMethodException {
        // Given
        Method getInstanceMethod = FireflyDB.class.getDeclaredMethod("getInstance", String.class);

        // When/Then
        assertTrue(Modifier.isSynchronized(getInstanceMethod.getModifiers()));
    }
}
