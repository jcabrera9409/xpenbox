(function (window) {
  window.__env = window.__env || {};
  
  window.__env.production = false;
  window.__env.apiUrl = 'http://localhost:8080/api/rest/v1';
  window.__env.domains = ['localhost:8080'];
  window.__env.googleAnalyticsId = '';

  window.__env.apiUrlMobile = 'https://xenomorphic-prayerfully-keegan.ngrok-free.dev/api/rest/v1';
  window.__env.domainsMobile = ['xenomorphic-prayerfully-keegan.ngrok-free.dev'];
})(this);