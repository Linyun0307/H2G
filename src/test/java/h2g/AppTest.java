package h2g;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        //ThreadManager.main(null);
        
        try {
            BarBasicSkin.main(null);
        } catch (Exception e) {
            System.out.print(e);
        }
        assertTrue( true );
    }
}
