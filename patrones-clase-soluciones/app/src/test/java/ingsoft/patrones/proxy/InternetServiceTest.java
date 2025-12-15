package ingsoft.patrones.proxy;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class InternetServiceTest {
  @Test
  public void testConnectTo() {
    InternetService internetService = new RealInternetService();
    String website = "example.com";
    assertEquals("Connecting to " + website, internetService.connectTo(website));
  }

  @Test
  public void testProxyConnectsToBannedSite() {
    InternetService internetService = new ProxyInternetService();
    String bannedWebsite = "instagram.com";
    assertEquals("Access to " + bannedWebsite + " is blocked.", internetService.connectTo(bannedWebsite));
  }

  @Test
  public void testProxyConnectsToAllowedSite() {
    InternetService internetService = new ProxyInternetService();
    String allowedWebsite = "example.com";
    assertEquals("Connecting to " + allowedWebsite, internetService.connectTo(allowedWebsite));
  }
}
