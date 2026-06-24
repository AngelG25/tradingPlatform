// small UX tweaks. Keycloak will load this after the page is ready.
document.addEventListener('DOMContentLoaded', function () {
  // autofocus the username field
  const username = document.getElementById('username');
  if (username) username.focus();
});