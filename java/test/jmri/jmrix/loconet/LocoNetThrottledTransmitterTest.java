package jmri.jmrix.loconet;

import java.util.concurrent.TimeUnit;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.loconet.LocoNetThrottledTransmitter class.
 *
 * @author Bob Jacobsen Copyright 2001, 2002, 2009, 2015
 */
public class LocoNetThrottledTransmitterTest {

    @Test
    public void testCtorAndDispose() {
        LocoNetThrottledTransmitter q = new LocoNetThrottledTransmitter(new LocoNetInterfaceScaffold(memo), false);
        q.dispose();
        JUnitUtil.waitFor(()->{return !q.running;}, "stopped");
    }

    @Test
    public void testMemoCtor() {
        LocoNetThrottledTransmitter q = new LocoNetThrottledTransmitter(new LocoNetInterfaceScaffold(memo), false);
        Assertions.assertNotNull(
            new LocoNetThrottledTransmitter.Memo(null, 100, TimeUnit.MILLISECONDS));

        q.dispose();
        JUnitUtil.waitFor(()->{return !q.running;}, "stopped");
    }

    @Test
    public void testMemoComparable() {
        LocoNetThrottledTransmitter.Memo m50   = new LocoNetThrottledTransmitter.Memo(null, 50, TimeUnit.MILLISECONDS);
        LocoNetThrottledTransmitter.Memo m100a = new LocoNetThrottledTransmitter.Memo(null, 100, TimeUnit.MILLISECONDS);
        LocoNetThrottledTransmitter.Memo m100b = new LocoNetThrottledTransmitter.Memo(null, 100, TimeUnit.MILLISECONDS);
        LocoNetThrottledTransmitter.Memo m200a = new LocoNetThrottledTransmitter.Memo(null, 200, TimeUnit.MILLISECONDS);
        LocoNetThrottledTransmitter.Memo m200b = new LocoNetThrottledTransmitter.Memo(null, 200, TimeUnit.MILLISECONDS);

        Assert.assertNotNull("exists", m100b);
        Assert.assertNotNull("exists", m200b);
        Assert.assertEquals("same object", 0, m100a.compareTo(m100a));
        Assert.assertEquals("same object", 0, m100a.compareTo(m100a));

        Assert.assertEquals("less than 1", -1, m100a.compareTo(m200a));
        Assert.assertEquals("less than 2", -1, m50.compareTo(m100a));
        Assert.assertEquals("less than 3", -1, m50.compareTo(m200a));

        Assert.assertEquals("greater than 1", 1, m200a.compareTo(m100a));
        Assert.assertEquals("greater than 2", 1, m100a.compareTo(m50));
        Assert.assertEquals("greater than 3", 1, m200a.compareTo(m50));
    }

    @Test
    public void testThreadStartStop() {
        LocoNetThrottledTransmitter q = new LocoNetThrottledTransmitter(new LocoNetInterfaceScaffold(memo), false);
        JUnitUtil.waitFor(()->{return q.running;}, "started");

        Assert.assertTrue("started", q.running);

        q.dispose();
        JUnitUtil.waitFor(()->{return !q.running;}, "stopped");
    }

    @Test
    public void testSendOneImmediate() {
        LocoNetInterfaceScaffold s = new LocoNetInterfaceScaffold(memo);
        LocoNetThrottledTransmitter q = new LocoNetThrottledTransmitter(s, false);

        LocoNetMessage m1;

        m1 = new LocoNetMessage(2);
        m1.setElement(0, 0x01);  // dummy value

        q.minInterval = 0;
        q.sendLocoNetMessage(m1);

        JUnitUtil.waitFor(()->{return s.outbound.size() == 1;}, "one sent");

        Assert.assertEquals("one sent", 1, s.outbound.size());
        Assert.assertEquals("right one", m1, s.outbound.elementAt(s.outbound.size() - 1));

        q.dispose();
        JUnitUtil.waitFor(()->{return !q.running;}, "stopped");
    }

    @Test
    public void testSendOneNowOneLater() {
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));
        LocoNetInterfaceScaffold s = new LocoNetInterfaceScaffold(memo);
        LocoNetThrottledTransmitter q = new LocoNetThrottledTransmitter(s, false);

        LocoNetMessage m1 = new LocoNetMessage(2);
        m1.setElement(0, 0x01);  // dummy value
        LocoNetMessage m2 = new LocoNetMessage(2);
        m2.setElement(0, 0x02);  // dummy value

        q.minInterval = 1;
        q.sendLocoNetMessage(m1);
        q.minInterval = 1000;
        q.sendLocoNetMessage(m2);

        JUnitUtil.waitFor(()->{return s.outbound.size() >= 1;}, "at least one sent failed with s.outbound.size() "+s.outbound.size());

        Assert.assertEquals("right one", m1, s.outbound.elementAt(0));

        JUnitUtil.waitFor(()->{return s.outbound.size() == 2;}, "only two sent failed with s.outbound.size() "+s.outbound.size());

        Assert.assertEquals("two sent", 2, s.outbound.size());
        Assert.assertEquals("right 2nd", m2, s.outbound.elementAt(1));

        q.dispose();
        JUnitUtil.waitFor(()->{return !q.running;}, "stopped");
    }

    @Test
    public void testAfterTimeNewMessageSentImmediately() {
        LocoNetInterfaceScaffold s = new LocoNetInterfaceScaffold(memo);
        LocoNetThrottledTransmitter q = new LocoNetThrottledTransmitter(s, false);

        LocoNetMessage m1 = new LocoNetMessage(2);
        m1.setElement(0, 0x01);  // dummy value
        LocoNetMessage m2 = new LocoNetMessage(2);
        m2.setElement(0, 0x02);  // dummy value

        q.minInterval = 100;
        q.sendLocoNetMessage(m1);

        JUnitUtil.waitFor(()->{return s.outbound.size() == 1;}, "only one sent");

        Assert.assertEquals("only one sent", 1, s.outbound.size());
        Assert.assertEquals("right one", m1, s.outbound.elementAt(0));

        q.sendLocoNetMessage(m2);
        JUnitUtil.waitFor(()->{return s.outbound.size() == 2;}, "two sent");

        Assert.assertEquals("two sent", 2, s.outbound.size());
        Assert.assertEquals("right 2nd", m2, s.outbound.elementAt(1));

        q.dispose();
        JUnitUtil.waitFor(()->{return !q.running;}, "stopped");
    }

    private LocoNetSystemConnectionMemo memo;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new LocoNetSystemConnectionMemo();
    }

    @AfterEach
    public void tearDown() {
        Assertions.assertNotNull(memo);
        memo.dispose();
        JUnitUtil.waitThreadTerminated(memo.getUserName() + LocoNetThrottledTransmitter.SERVICE_THREAD_NAME);
        memo = null;
        JUnitUtil.tearDown();
    }

}
