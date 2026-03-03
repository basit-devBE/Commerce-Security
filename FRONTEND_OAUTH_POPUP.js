/**
 * Frontend OAuth2 Popup Implementation
 * 
 * Add this to your React/Vue/Angular frontend
 */

// React Example:
const GoogleLoginButton = () => {
  const handleGoogleLogin = async () => {
    try {
      // Get auth URL from backend
      const response = await fetch('http://localhost:8080/api/oauth2/google/url');
      const { authUrl } = await response.json();

      // Open popup
      const width = 500;
      const height = 600;
      const left = window.screen.width / 2 - width / 2;
      const top = window.screen.height / 2 - height / 2;

      const popup = window.open(
        authUrl,
        'Google Login',
        `width=${width},height=${height},left=${left},top=${top}`
      );

      // Listen for OAuth callback
      window.addEventListener('message', (event) => {
        if (event.origin !== window.location.origin) return;
        
        if (event.data.accessToken && event.data.refreshToken) {
          // Store tokens
          localStorage.setItem('accessToken', event.data.accessToken);
          localStorage.setItem('refreshToken', event.data.refreshToken);
          
          // Close popup and redirect
          popup?.close();
          window.location.href = '/dashboard';
        }
      });
    } catch (error) {
      console.error('OAuth login failed:', error);
    }
  };

  return (
    <button onClick={handleGoogleLogin}>
      Sign in with Google
    </button>
  );
};

// Vanilla JavaScript Example:
function handleGoogleLogin() {
  fetch('http://localhost:8080/api/oauth2/google/url')
    .then(res => res.json())
    .then(data => {
      const width = 500;
      const height = 600;
      const left = window.screen.width / 2 - width / 2;
      const top = window.screen.height / 2 - height / 2;

      const popup = window.open(
        data.authUrl,
        'Google Login',
        `width=${width},height=${height},left=${left},top=${top}`
      );

      window.addEventListener('message', (event) => {
        if (event.origin !== window.location.origin) return;
        
        if (event.data.accessToken && event.data.refreshToken) {
          localStorage.setItem('accessToken', event.data.accessToken);
          localStorage.setItem('refreshToken', event.data.refreshToken);
          popup?.close();
          window.location.href = '/dashboard';
        }
      });
    });
}
