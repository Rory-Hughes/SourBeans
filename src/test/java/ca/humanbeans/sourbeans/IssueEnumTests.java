package ca.humanbeans.sourbeans;

import static org.junit.jupiter.api.Assertions.*;

import ca.humanbeans.sourbeans.entity.IssuePriority;
import ca.humanbeans.sourbeans.entity.IssueStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Plain unit tests (no Spring, no database) for the enums. The constant names
 * are stored in the database, so ADR-0002 Decision 6 forbids casual renames;
 * these tests make a rename fail loudly.
 */
class IssueEnumTests {

    // ---------- the stored values (ADR-0002, Decision 9) ----------

    @Test
    void statusConstantsMatchTheAdr() {
        assertArrayEquals(new IssueStatus[] {IssueStatus.OPEN,
                                             IssueStatus.CLOSED,
                                             },
                          IssueStatus.values());
    }

    @Test
    void priorityConstantsMatchTheAdr() {
        assertArrayEquals(
            new IssuePriority[] {IssuePriority.LOW,
                                 IssuePriority.MEDIUM, IssuePriority.HIGH},
            IssuePriority.values());
    }

    // ---------- IssueStatus lookups ----------

    @ParameterizedTest
    @CsvSource({"OPEN,OPEN", "open,OPEN", "Open,OPEN",
                "closed,CLOSED", "CLOSED,CLOSED"})
    void statusFromStringIgnoresCase(String text, IssueStatus expected) {
        assertEquals(expected, IssueStatus.fromString(text));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"opun", "status-closed", "nonsense", " TODO",
                            "IN PROGRESS"})
    void statusFromStringOfAnythingElseIsNull(String text) {
        assertNull(IssueStatus.fromString(text));
    }

    @ParameterizedTest
    @CsvSource({"1,OPEN", "2,CLOSED"})
    void statusFromInteger(int n, IssueStatus expected) {
        assertEquals(expected, IssueStatus.fromInteger(n));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 4, 99})
    void statusFromIntegerOutOfRangeIsNull(int n) {
        assertNull(IssueStatus.fromInteger(n));
    }

    // ---------- IssuePriority lookups ----------

    @ParameterizedTest
    @CsvSource({"low,LOW", "Medium,MEDIUM", "HIGH,HIGH"})
    void priorityFromStringIgnoresCase(String text, IssuePriority expected) {
        assertEquals(expected, IssuePriority.fromString(text));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"Unknown", "URGENT", "nonsense", " LOW"})
    void priorityFromStringOfAnythingElseIsNull(String text) {
        assertNull(IssuePriority.fromString(text));
    }

    @ParameterizedTest
    @CsvSource({"1,LOW", "2,MEDIUM", "3,HIGH"})
    void priorityFromInteger(int n, IssuePriority expected) {
        assertEquals(expected, IssuePriority.fromInteger(n));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 4, 99})
    void priorityFromIntegerOutOfRangeIsNull(int n) {
        assertNull(IssuePriority.fromInteger(n));
    }
}
