package com.example.logisticsmiddleware.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostcodeStateMapperTest {

    private final PostcodeStateMapper postcodeStateMapper = new PostcodeStateMapper();

    @Test
    void getStateFromPostcodeShouldMapKnownPrefixes() {
        assertThat(postcodeStateMapper.getStateFromPostcode("13200")).isEqualTo("Pulau Pinang");
        assertThat(postcodeStateMapper.getStateFromPostcode("50000")).isEqualTo("Kuala Lumpur");
        assertThat(postcodeStateMapper.getStateFromPostcode("88000")).isEqualTo("Sarawak");
    }

    @Test
    void getStateFromPostcodeShouldReturnNullForInvalidInputs() {
        assertThat(postcodeStateMapper.getStateFromPostcode(null)).isNull();
        assertThat(postcodeStateMapper.getStateFromPostcode("1234")).isNull();
        assertThat(postcodeStateMapper.getStateFromPostcode("ab123")).isNull();
        assertThat(postcodeStateMapper.getStateFromPostcode("99000")).isNull();
    }
}
