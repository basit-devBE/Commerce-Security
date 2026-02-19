import React, { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useCart } from '../context/CartContext';

const OAuth2RedirectHandler = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { syncCartToServer, fetchCart } = useCart();

  useEffect(() => {
    const accessToken = searchParams.get('accessToken');
    const refreshToken = searchParams.get('refreshToken');

    if (accessToken && refreshToken) {
      // Store tokens
      localStorage.setItem('authToken', accessToken);
      
      // Decode JWT to get user info (simple base64 decode of payload)
      const payload = JSON.parse(atob(accessToken.split('.')[1]));
      const user = {
        id: payload.userId,
        email: payload.sub,
        role: payload.role
      };
      localStorage.setItem('user', JSON.stringify(user));

      // Sync cart and redirect
      syncCartToServer().then(() => {
        fetchCart().then(() => {
          navigate('/');
        });
      });
    } else {
      navigate('/login');
    }
  }, [searchParams, navigate, syncCartToServer, fetchCart]);

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center">
      <div className="text-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto mb-4"></div>
        <p className="text-gray-600">Completing sign in...</p>
      </div>
    </div>
  );
};

export default OAuth2RedirectHandler;
