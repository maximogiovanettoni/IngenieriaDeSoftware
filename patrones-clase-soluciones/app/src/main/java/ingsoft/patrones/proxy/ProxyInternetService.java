package ingsoft.patrones.proxy;

public class ProxyInternetService implements InternetService {
  private RealInternetService realInternetService;
  private static final String[] blockedSites = {
    "instagram.com",
    "facebook.com"
  };
  
  public ProxyInternetService() {
    this.realInternetService = new RealInternetService();

  }
  
  @Override
  public String connectTo(String url) {
    if (isBlocked(url)) {
      return "Access to " + url + " is blocked.";
    }
    return realInternetService.connectTo(url);
  }
  
  private boolean isBlocked(String url) {
    // Simple blocking logic for demonstration
    for (String blockedSite : blockedSites) {
      if (url.contains(blockedSite)) {
        return true;
      }
    }
    return false;
  }
    
}
