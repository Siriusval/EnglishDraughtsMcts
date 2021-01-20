package test;

import fr.istic.ia.tp1.EnglishDraughts;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;
import java.util.logging.Logger;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for EnglishDraughts
 *
 * @see fr.istic.ia.tp1.EnglishDraughts
 * @author  Valentin Hulot
 */
class EnglishDraughtsTest {

    /**
     * Logger to print in console
     */
    public static Logger log ;
    public static EnglishDraughts englishDraughts;

    /**
     * Setup method executed before all test
     */
    @BeforeAll
    public static void setup() {
        log = Logger.getLogger(EnglishDraughtsTest.class.getName());
        log.info("@BeforeAll");
        englishDraughts = new EnglishDraughts();
    }

    /**
     * Test if the possibleMovesTest method return a correct move object
     */
    @Test
    @DisplayName("possibleMovesTest")
    void possibleMovesTest() {
        //TODO

    }

}
