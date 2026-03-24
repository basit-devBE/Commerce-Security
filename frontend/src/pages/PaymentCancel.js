import React from 'react';
import { useNavigate } from 'react-router-dom';
import { XCircleIcon } from '@heroicons/react/24/outline';

const PaymentCancel = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full text-center bg-white p-8 rounded-xl shadow-sm">
        <XCircleIcon className="mx-auto h-24 w-24 text-red-500 mb-4" />
        <h2 className="text-3xl font-bold text-gray-900 mb-4">Payment Cancelled</h2>
        <p className="text-gray-600 mb-8">
          Your payment was cancelled and your order was not processed. Your cart items are still waiting for you!
        </p>
        <div className="flex flex-col space-y-3">
          <button
            onClick={() => navigate('/checkout')}
            className="w-full py-3 px-4 bg-primary-600 border border-transparent rounded-lg text-white hover:bg-primary-700 transition-colors focus:outline-none"
          >
            Try Payment Again
          </button>
          <button
            onClick={() => navigate('/cart')}
            className="w-full py-3 px-4 bg-white border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors focus:outline-none"
          >
            Return to Cart
          </button>
        </div>
      </div>
    </div>
  );
};

export default PaymentCancel;
