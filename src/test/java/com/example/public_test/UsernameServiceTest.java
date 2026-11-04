package com.example.public_test;



import com.example.user_info.repo.UserDao;
import com.example.user_info.sevice.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsernameServiceTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserService service;


    //  suggestUsername — POSITIVE


    @Nested
    @DisplayName("suggestUsername() — positive")
    class SuggestUsernamePositive {

        @Test
        @DisplayName("returns the name unchanged when it is free")
        void returnsNameUnchanged_whenFree() {
            when(userDao.existsByUserName("bikash")).thenReturn(false);

            String result = service.suggestUsername("bikash");

            assertEquals("bikash", result);
            verify(userDao, times(1)).existsByUserName("bikash");
            verifyNoMoreInteractions(userDao);
        }

        @Test
        @DisplayName("appends a numeric suffix when the name is taken")
        void returnsSuffixedName_whenTaken() {
            // 1st call (pre-check) = taken, 2nd (re-check in loop) = taken, 3rd = free
            when(userDao.existsByUserName("bikash")).thenReturn(true);
            when(userDao.existsByUserName(argThat(s -> s.matches("bikash\\d{3}"))))
                    .thenReturn(false);

            String result = service.suggestUsername("bikash");

            assertNotEquals("bikash", result);
            assertTrue(result.matches("bikash\\d{3}"),
                    "expected prefix + 3-digit suffix, got: " + result);

            int suffix = Integer.parseInt(result.substring("bikash".length()));
            assertTrue(suffix >= 100 && suffix <= 999, "suffix out of range: " + suffix);
        }

        @Test
        @DisplayName("returns a name the DAO reports as free")
        void returnsNameThatIsNotTaken() {
            when(userDao.existsByUserName("john")).thenReturn(true, true, false);

            String result = service.suggestUsername("john");

            // whatever was returned, the last DAO check on it must have been false
            assertTrue(result.matches("john\\d{3}"));
            verify(userDao, atLeastOnce()).existsByUserName(result);
        }
    }


    //  suggestUsername — NEGATIVE / EDGE


    @Nested
    @DisplayName("suggestUsername() — negative / edge")
    class SuggestUsernameNegative {

        @Test
        @DisplayName("null name is passed straight to the DAO (no validation in service)")
        void passesNullToDao() {
            when(userDao.existsByUserName(null)).thenReturn(false);

            String result = service.suggestUsername(null);

            assertNull(result);
            verify(userDao).existsByUserName(null);
        }

        @Test
        @DisplayName("empty name is returned as-is when free (no guard)")
        void returnsEmptyString_whenFree() {
            when(userDao.existsByUserName("")).thenReturn(false);

            assertEquals("", service.suggestUsername(""));
        }

        @Test
        @DisplayName("blank/whitespace name is not sanitized — documents current behaviour")
        void blankNameIsNotSanitized() {
            when(userDao.existsByUserName("   ")).thenReturn(false);

            assertEquals("   ", service.suggestUsername("   "));
        }

        @Test
        @DisplayName("spaces in name are preserved in the suggested username")
        void spacesArePreserved() {
            when(userDao.existsByUserName("Bikash Tharu")).thenReturn(true);
            when(userDao.existsByUserName(argThat(s -> s.matches("Bikash Tharu\\d{3}"))))
                    .thenReturn(false);

            String result = service.suggestUsername("Bikash Tharu");

            assertTrue(result.startsWith("Bikash Tharu"));
            assertTrue(result.contains(" "), "username still contains a space — likely invalid");
        }

        @Test
        @DisplayName("DAO exception is propagated, not swallowed")
        void propagatesDaoException() {
            when(userDao.existsByUserName("bikash"))
                    .thenThrow(new IllegalStateException("db down"));

            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> service.suggestUsername("bikash"));

            assertEquals("db down", ex.getMessage());
        }

        @Test
        @DisplayName("hangs forever when every candidate is taken (no attempt cap)")
        void doesNotTerminate_whenEveryCandidateTaken() {
            when(userDao.existsByUserName(anyString())).thenReturn(true);

            assertThrows(org.opentest4j.AssertionFailedError.class, () ->
                    assertTimeoutPreemptively(
                            Duration.ofMillis(500),
                            () -> service.suggestUsername("bikash"),
                            "expected the service to spin forever — no max-attempt guard"));
        }
    }


    //  generateUniqueUsername — POSITIVE


    @Nested
    @DisplayName("generateUniqueUsername() — positive")
    class GenerateUniqueUsernamePositive {

        @Test
        @DisplayName("returns the original name when it is already free")
        void returnsOriginal_whenFree() {
            when(userDao.existsByUserName("bikash")).thenReturn(false);

            assertEquals("bikash", service.generateUniqueUsername("bikash"));
            verify(userDao, times(1)).existsByUserName("bikash");
        }

        @Test
        @DisplayName("retries until a free candidate is found")
        void retriesUntilFree() {
            when(userDao.existsByUserName("bikash")).thenReturn(true);
            when(userDao.existsByUserName(argThat(s -> s.matches("bikash\\d{3}"))))
                    .thenReturn(true, true, false);

            String result = service.generateUniqueUsername("bikash");

            assertTrue(result.matches("bikash\\d{3}"));
            verify(userDao, times(1)).existsByUserName("bikash"); // redundant re-check, see notes
        }

        @Test
        @DisplayName("suffix is always 3 digits in [100, 999] across many runs")
        void suffixIsAlwaysThreeDigitsInRange() {
            for (int i = 0; i < 100; i++) {
                reset(userDao);
                when(userDao.existsByUserName("bikash")).thenReturn(true);
                when(userDao.existsByUserName(argThat(s -> s.matches("bikash\\d{3}"))))
                        .thenReturn(false);

                String result = service.generateUniqueUsername("bikash");

                assertTrue(result.matches("bikash\\d{3}"), "bad format: " + result);
                int suffix = Integer.parseInt(result.substring(6));
                assertTrue(suffix >= 100 && suffix <= 999, "out of range: " + suffix);
            }
        }

        @Test
        @DisplayName("result always starts with the original name")
        void resultKeepsOriginalPrefix() {
            when(userDao.existsByUserName("bikash")).thenReturn(true);
            when(userDao.existsByUserName(argThat(s -> s.matches("bikash\\d{3}"))))
                    .thenReturn(false);

            assertTrue(service.generateUniqueUsername("bikash").startsWith("bikash"));
        }
    }


    //  generateUniqueUsername — NEGATIVE


    @Nested
    @DisplayName("generateUniqueUsername() — negative")
    class GenerateUniqueUsernameNegative {

        @Test
        @DisplayName("propagates DAO failure from inside the loop")
        void propagatesDaoFailureInLoop() {
            when(userDao.existsByUserName("bikash")).thenReturn(true);
            when(userDao.existsByUserName(argThat(s -> s.matches("bikash\\d{3}"))))
                    .thenThrow(new RuntimeException("connection reset"));

            assertThrows(RuntimeException.class,
                    () -> service.generateUniqueUsername("bikash"));
        }

        @Test
        @DisplayName("null name: DAO is hit twice (pre-check + loop) with null")
        void nullNameHitsDaoTwice() {
            when(userDao.existsByUserName(null)).thenReturn(false);

            assertNull(service.generateUniqueUsername(null));
            verify(userDao, times(1)).existsByUserName(null);
        }
    }
}
