(function (window) {
  window.__env = window.__env || {};
  
  window.__env.production = false;
  window.__env.apiUrl = 'http://localhost:8080/api/rest/v1';
  window.__env.domains = ['localhost:8080'];
  window.__env.googleAnalyticsId = '';

  window.__env.apiUrlMobile = 'http://10.0.2.2:8080/api/rest/v1';
  window.__env.domainsMobile = ['10.0.2.2:8080'];
})(this);