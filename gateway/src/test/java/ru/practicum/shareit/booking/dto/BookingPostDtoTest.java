package ru.practicum.shareit.booking.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingPostDtoTest {

    @Autowired
    private JacksonTester<BookingPostDto> json;

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void testSerialize() throws Exception {
        BookingPostDto dto = new BookingPostDto();
        dto.setItemId(1L);
        dto.setStart(LocalDateTime.of(2030, 1, 1, 12, 0));
        dto.setEnd(LocalDateTime.of(2030, 1, 2, 12, 0));

        JsonContent<BookingPostDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2030-01-01T12:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2030-01-02T12:00:00");
    }

    @Test
    void testValidation_WhenStartInPast_ShouldFail() {
        BookingPostDto dto = new BookingPostDto();
        dto.setItemId(1L);
        dto.setStart(LocalDateTime.now().minusDays(1)); // Past date
        dto.setEnd(LocalDateTime.now().plusDays(1));    // Future date

        Set<ConstraintViolation<BookingPostDto>> violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("start");
    }

    @Test
    void testValidation_WhenEndBeforeStart_ShouldFail() {
        BookingPostDto dto = new BookingPostDto();
        dto.setItemId(1L);
        dto.setStart(LocalDateTime.now().plusDays(2));
        dto.setEnd(LocalDateTime.now().plusDays(1));

        Set<ConstraintViolation<BookingPostDto>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }
}
