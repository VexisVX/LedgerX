package io.github.vexisvx.ledgerx.util;

import io.github.vexisvx.ledgerx.config.AppConfig;
import org.junit.jupiter.api.*;

class PinServiceTest {

    @BeforeEach
    void cleanup() { PinService.clearPin(); }

    @AfterEach
    void tearDown() { PinService.clearPin(); }

    @Test void noPinMeansUnlocked() {
        Assertions.assertFalse(PinService.isEnabled());
        Assertions.assertTrue(PinService.verify("0000"));
    }

    @Test void setAndVerify() {
        PinService.setPin("1234");
        Assertions.assertTrue(PinService.isEnabled());
        Assertions.assertTrue(PinService.verify("1234"));
        Assertions.assertFalse(PinService.verify("9999"));
    }

    @Test void clearDisables() {
        PinService.setPin("1234");
        PinService.clearPin();
        Assertions.assertFalse(PinService.isEnabled());
    }

    @Test void invalidPinFormatRejected() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> PinService.setPin("abc"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> PinService.setPin("123"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> PinService.setPin("12345"));
    }
}
