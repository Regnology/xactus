package info.fingo.xactus.processor.internal.types;

import static org.assertj.core.api.Assertions.assertThat;

import info.fingo.xactus.api.ResultBuffer;
import info.fingo.xactus.processor.DynamicError;
import info.fingo.xactus.processor.internal.function.FnAdjustDateTimeToTimeZone;
import info.fingo.xactus.processor.internal.function.FnAdjustDateToTimeZone;
import info.fingo.xactus.processor.internal.function.FnAdjustTimeToTimeZone;
import java.util.Arrays;
import java.util.Calendar;
import org.junit.jupiter.api.Test;

class CalendarTimeZoneIdentityTest {

    @Test
    void dateMinusDayStaysEqualToParsedDate() throws DynamicError {
        XSDate result = (XSDate) XSDate.parse_date("2024-01-01")
            .minus(ResultBuffer.wrap(oneDay()))
            .first();
        XSDate expected = XSDate.parse_date("2023-12-31");

        assertSameInstant(result, expected);
        assertThat(result.getStringValue()).isEqualTo("2023-12-31");
    }

    @Test
    void datePlusAndMinusKeepTheSameTimezone() throws DynamicError {
        XSDayTimeDuration oneDay = oneDay();
        XSDate plus = (XSDate) XSDate.parse_date("2023-12-31")
            .plus(ResultBuffer.wrap(oneDay))
            .first();
        XSDate minus = (XSDate) XSDate.parse_date("2024-01-01")
            .minus(ResultBuffer.wrap(oneDay))
            .first();

        assertThat(plus.eq(XSDate.parse_date("2024-01-01"), null)).isTrue();
        assertThat(minus.eq(XSDate.parse_date("2023-12-31"), null)).isTrue();
        assertThat(plus.calendar().getTimeZone()).isEqualTo(minus.calendar().getTimeZone());
        assertThat(minus.calendar().getTimeZone().getID()).isNotEqualTo("GMT+00:00");
    }

    @Test
    void dateTimeMinusAndPlusDayStayEqualToParsedValues() throws DynamicError {
        XSDayTimeDuration oneDay = oneDay();
        XSDateTime minus = (XSDateTime) XSDateTime.parseDateTime("2024-01-01T00:00:00Z")
            .minus(ResultBuffer.wrap(oneDay))
            .first();
        XSDateTime plus = (XSDateTime) XSDateTime.parseDateTime("2023-12-31T00:00:00Z")
            .plus(ResultBuffer.wrap(oneDay))
            .first();

        assertSameInstant(minus, XSDateTime.parseDateTime("2023-12-31T00:00:00Z"));
        assertSameInstant(plus, XSDateTime.parseDateTime("2024-01-01T00:00:00Z"));
    }

    @Test
    void dateTimeWithOffsetMinusDayStaysEqualToParsedValue() throws DynamicError {
        XSDateTime result = (XSDateTime) XSDateTime.parseDateTime("2024-01-02T00:00:00+01:00")
            .minus(ResultBuffer.wrap(oneDay()))
            .first();

        assertSameInstant(result, XSDateTime.parseDateTime("2024-01-01T00:00:00+01:00"));
    }

    @Test
    void timeMinusHourStaysEqualToParsedTime() throws DynamicError {
        XSTime result = (XSTime) ((XSTime) XSTime.parse_time("12:00:00Z"))
            .minus(ResultBuffer.wrap(new XSDayTimeDuration(0, 1, 0, 0, false)))
            .first();

        assertSameInstant(result, (XSTime) XSTime.parse_time("11:00:00Z"));
    }

    @Test
    void adjustDateToTimezoneKeepsSourceTimezone() throws DynamicError {
        XSDate source = XSDate.parse_date("2024-01-01Z");
        XSDate adjusted = (XSDate) FnAdjustDateToTimeZone.adjustDate(
            Arrays.asList(source, new XSDayTimeDuration(0, 1, 0, 0, false)),
            null).first();

        assertThat(adjusted.calendar().get(Calendar.HOUR_OF_DAY)).isEqualTo(1);
        assertThat(adjusted.calendar().getTimeZone()).isEqualTo(source.calendar().getTimeZone());
    }

    @Test
    void adjustDateTimeToTimezoneKeepsSourceTimezone() throws DynamicError {
        XSDateTime source = XSDateTime.parseDateTime("2024-01-01T00:00:00Z");
        XSDateTime adjusted = (XSDateTime) FnAdjustDateTimeToTimeZone.adjustdateTime(
            Arrays.asList(source, new XSDayTimeDuration(0, 1, 0, 0, false)),
            null).first();

        assertThat(adjusted.calendar().get(Calendar.HOUR_OF_DAY)).isEqualTo(1);
        assertThat(adjusted.calendar().getTimeZone()).isEqualTo(source.calendar().getTimeZone());
    }

    @Test
    void adjustTimeToTimezoneKeepsSourceTimezone() throws DynamicError {
        XSTime source = (XSTime) XSTime.parse_time("12:00:00Z");
        XSTime adjusted = (XSTime) FnAdjustTimeToTimeZone.adjustTime(
            Arrays.asList(source, new XSDayTimeDuration(0, 1, 0, 0, false)),
            null).first();

        assertThat(adjusted.calendar().get(Calendar.HOUR_OF_DAY)).isEqualTo(13);
        assertThat(adjusted.calendar().getTimeZone()).isEqualTo(source.calendar().getTimeZone());
    }

    private static void assertSameInstant(CalendarType actual, CalendarType expected) throws DynamicError {
        assertThat(actual.calendar().getTimeZone()).isEqualTo(expected.calendar().getTimeZone());
        assertThat(actual.calendar().getTimeZone().getID()).isNotEqualTo("GMT+00:00");
        assertThat(((info.fingo.xactus.processor.internal.function.CmpEq) actual).eq(expected, null)).isTrue();
    }

    private static XSDayTimeDuration oneDay() {
        return new XSDayTimeDuration(1, 0, 0, 0, false);
    }
}
