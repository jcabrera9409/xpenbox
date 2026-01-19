(function (window) {
  window.__env = window.__env || {};

  // Estos valores serán reemplazados en tiempo de deploy
  window.__env.production = false;
  window.__env.apiUrl = 'http://localhost:8080/api/rest/v1';
  window.__env.domains = ['localhost:8080'];
  window.__env.googleAnalyticsId = '';
})(this);