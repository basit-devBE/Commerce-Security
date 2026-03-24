import React, { useEffect, useRef } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { CheckCircleIcon } from '@heroicons/react/24/outline';
import { useCart } from '../context/CartContext';

const PaymentSuccess = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const sessionId = searchParams.get('session_id');
  const { clearCart } = useCart();
  const hasCleared = useRef(false);

  useEffect(() => {
    if (hasCleared.current) return;
    
    // We can clear the cart locally just to be sure, though the backend webhook also does it
    clearCart();
    hasCleared.current = true;
    
    // In a real application, you might also want to verify the session_id with your backend
    const timer = setTimeout(() => {
      navigate('/orders');
    }, 4000);

    return () => clearTimeout(timer);
  }, [clearCart, navigate]);

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full text-center bg-white p-8 rounded-xl shadow-sm">
        <CheckCircleIcon className="mx-auto h-24 w-24 text-green-500 mb-4" />
        <h2 className="text-3xl font-bold text-gray-900 mb-4">Payment Successful!</h2>
        <p className="text-gray-600 mb-2">Thank you for your purchase.</p>
        {sessionId && (
          <p className="text-xs text-gray-400 mb-6 break-all">
            Session REF: {sessionId}
          </p>
        )}
        <p className="text-sm font-medium text-primary-600">
          Redirecting to your orders...
        </p>
        <button
          onClick={() => navigate('/orders')}
          className="mt-6 w-full py-3 px-4 bg-primary-600 border border-transparent rounded-lg text-white hover:bg-primary-700 transition-colors focus:outline-none"
        >
          View Orders Now
        </button>
      </div>
    </div>
  );
};

export default PaymentSuccess;
