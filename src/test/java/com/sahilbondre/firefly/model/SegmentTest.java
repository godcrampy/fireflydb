package com.sahilbondre.firefly.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SegmentTest {

    @Test
    void givenByteArray_whenCreatingSegment_thenAccessorsReturnCorrectValues() {
        // Given
        byte[] testData = new byte[]{
            (byte) -83, (byte) 64,
            0x00, 0x05,         // Key Size
            0x00, 0x00, 0x00, 0x05, // Value Size
            0x48, 0x65, 0x6C, 0x6C, 0x6F, // Key: "Hello"
            0x57, 0x6F, 0x72, 0x6C, 0x64  // Value: "World"
        };

        // When
        Segment segment = Segment.fromByteArray(testData);

        // Then
        assertArrayEquals(testData, segment.getBytes());
        assertArrayEquals("Hello".getBytes(), segment.getKey());
        assertArrayEquals("World".getBytes(), segment.getValue());
        assertEquals(5, segment.getKeySize());
        assertEquals(5, segment.getValueSize());
        assertEquals(-83, segment.getCrc()[0]);
        assertEquals(64, segment.getCrc()[1]);
        assertTrue(segment.isSegmentValid());
        assertTrue(segment.isChecksumValid());
    }

    @Test
    void givenCorruptedKeySizeSegment_whenCheckingChecksum_thenIsChecksumValidReturnsFalse() {
        // Given
        byte[] testData = new byte[]{
            (byte) -83, (byte) 64,
            0x01, 0x45,         // Key Size (Bit Flipped)
            0x00, 0x00, 0x00, 0x05, // Value Size
            0x48, 0x65, 0x6C, 0x6C, 0x6F, // Key: "Hello"
            0x57, 0x6F, 0x72, 0x6C, 0x64  // Value: "World"
        };

        // When
        Segment corruptedSegment = Segment.fromByteArray(testData);

        // Then
        assertFalse(corruptedSegment.isChecksumValid());
        assertFalse(corruptedSegment.isSegmentValid());
    }

    @Test
    void givenCorruptedValueSizeSegment_whenCheckingChecksum_thenIsChecksumValidReturnsFalse() {
        // Given
        byte[] testData = new byte[]{
            (byte) -83, (byte) 64,
            0x00, 0x05,         // Key Size
            0x00, 0x00, 0x01, 0x05, // Value Size (Bit Flipped)
            0x48, 0x65, 0x6C, 0x6C, 0x6F, // Key: "Hello"
            0x57, 0x6F, 0x72, 0x6C, 0x64  // Value: "World"
        };

        // When
        Segment corruptedSegment = Segment.fromByteArray(testData);

        // Then
        assertFalse(corruptedSegment.isChecksumValid());
        assertFalse(corruptedSegment.isSegmentValid());
    }

    @Test
    void givenCorruptedKeySegment_whenCheckingChecksum_thenIsChecksumValidReturnsFalse() {
        // Given
        byte[] testData = new byte[]{
            (byte) -83, (byte) 64,
            0x00, 0x05,         // Key Size
            0x00, 0x00, 0x00, 0x05, // Value Size
            0x48, 0x65, 0x6C, 0x6C, 0x6E, // Key: "Hello" (Bit Flipped)
            0x57, 0x6F, 0x72, 0x6C, 0x64  // Value: "World"
        };

        // When
        Segment corruptedSegment = Segment.fromByteArray(testData);

        // Then
        assertFalse(corruptedSegment.isChecksumValid());
        assertFalse(corruptedSegment.isSegmentValid());
    }

    @Test
    void givenCorruptedValueSegment_whenCheckingChecksum_thenIsChecksumValidReturnsFalse() {
        // Given
        byte[] testData = new byte[]{
            (byte) -83, (byte) 64,
            0x00, 0x05,         // Key Size
            0x00, 0x00, 0x00, 0x05, // Value Size
            0x48, 0x65, 0x6C, 0x6C, 0x6F, // Key: "Hello"
            0x57, 0x6F, 0x62, 0x6C, 0x65  // Value: "World" (Bit Flipped)
        };

        // When
        Segment corruptedSegment = Segment.fromByteArray(testData);

        // Then
        assertFalse(corruptedSegment.isChecksumValid());
        assertFalse(corruptedSegment.isSegmentValid());
    }

    @Test
    void givenIncorrectValueLengthSegment_whenCheckingSegmentValid_thenIsSegmentValidReturnsFalse() {
        // Given
        byte[] testData = new byte[]{
            (byte) -43, (byte) -70,
            0x00, 0x05,         // Key Size
            0x00, 0x00, 0x00, 0x06, // Value Size (Incorrect)
            0x48, 0x65, 0x6C, 0x6C, 0x6F, // Key: "Hello"
            0x57, 0x6F, 0x72, 0x6C, 0x64  // Value: "World"
        };

        // When
        Segment corruptedSegment = Segment.fromByteArray(testData);

        // Then
        assertTrue(corruptedSegment.isChecksumValid());
        assertFalse(corruptedSegment.isSegmentValid());
    }

    @Test
    void givenKeyValuePair_whenCreatingSegment_thenAccessorsReturnCorrectValues() {
        // Given
        byte[] key = "Hello".getBytes();
        byte[] value = "World".getBytes();
        byte[] expectedSegment = new byte[]{
            (byte) -83, (byte) 64,
            0x00, 0x05,         // Key Size
            0x00, 0x00, 0x00, 0x05, // Value Size
            0x48, 0x65, 0x6C, 0x6C, 0x6F, // Key: "Hello"
            0x57, 0x6F, 0x72, 0x6C, 0x64  // Value: "World"
        };

        // When
        Segment segment = Segment.fromKeyValuePair(key, value);

        // Then
        assertArrayEquals("Hello".getBytes(), segment.getKey());
        assertArrayEquals("World".getBytes(), segment.getValue());
        assertEquals(5, segment.getKeySize());
        assertEquals(5, segment.getValueSize());
        assertEquals(-83, segment.getCrc()[0]);
        assertEquals(64, segment.getCrc()[1]);
        assertTrue(segment.isSegmentValid());
        assertTrue(segment.isChecksumValid());
        assertArrayEquals(expectedSegment, segment.getBytes());
    }

    @Test
    void givenKeyAndValue_whenCreatingSegment_thenSegmentIsCreatedWithCorrectSizes() {
        // Given
        byte[] key = "Hello".getBytes();
        byte[] value = "World".getBytes();

        // When
        Segment segment = Segment.fromKeyValuePair(key, value);

        // Then
        assertArrayEquals(key, segment.getKey());
        assertArrayEquals(value, segment.getValue());
        assertEquals(key.length, segment.getKeySize());
        assertEquals(value.length, segment.getValueSize());
    }
}
