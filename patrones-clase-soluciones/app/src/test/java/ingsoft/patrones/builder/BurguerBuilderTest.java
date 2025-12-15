package ingsoft.patrones.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class BurguerBuilderTest {
    @Test
    public void testBurguerWithBreadMeetAndCheese() {
        BurguerPart burguer = new BurguerBuilder()
            .addBread()
            .addCheese()
            .addMeet()
            .addBread()
            .build();

        assertEquals("Bread+Cheese+Meet+Bread", burguer.getDescription());
        assertEquals(9, burguer.getCost());
    }
}
