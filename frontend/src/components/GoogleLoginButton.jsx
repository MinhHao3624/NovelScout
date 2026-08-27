import React, { useEffect, useState } from 'react';
import { authApi } from '../api/auth.js';

export function GoogleLoginButton({ onSuccess, onError }) {
  const [loading, setLoading] = useState(false);
  const clientId = (import.meta.env.VITE_GOOGLE_CLIENT_ID || '').trim();

  useEffect(() => {
    if (!clientId) return;

    // Load Google Identity Services SDK
    const script = document.createElement('script');
    script.src = 'https://accounts.google.com/gsi/client';
    script.async = true;
    script.defer = true;
    script.onload = () => {
      if (window.google?.accounts?.id) {
        window.google.accounts.id.initialize({
          client_id: clientId,
          callback: handleGoogleResponse,
          auto_select: false,
        });
      }
    };
    document.body.appendChild(script);

    return () => {
      if (document.body.contains(script)) {
        document.body.removeChild(script);
      }
    };
  }, [clientId]);

  const handleGoogleResponse = async (response) => {
    if (!response.credential) return;
    setLoading(true);
    try {
      await authApi.loginWithGoogle(response.credential);
      if (onSuccess) {
        onSuccess();
      } else {
        window.location.href = '/';
      }
    } catch (err) {
      if (onError) {
        onError(err.message || 'Đăng nhập Google thất bại');
      } else {
        alert('Lỗi đăng nhập Google: ' + err.message);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleManualGoogleClick = () => {
    if (!clientId) {
      alert('Vui lòng mở tệp frontend/.env và dán VITE_GOOGLE_CLIENT_ID để hoàn tất đăng nhập bằng Google.');
      return;
    }
    if (window.google?.accounts?.id) {
      window.google.accounts.id.prompt((notification) => {
        if (notification.isNotDisplayed() || notification.isSkippedMoment()) {
          // Fallback to standard prompt if one-tap is dismissed or blocked
          window.google.accounts.id.prompt();
        }
      });
    } else {
      alert('Đang tải thư viện Google Sign-In, vui lòng thử lại sau vài giây.');
    }
  };

  return (
    <div className="google-auth-wrapper" style={{ margin: '16px 0', width: '100%' }}>
      <button
        type="button"
        className="button secondary google-custom-btn"
        onClick={handleManualGoogleClick}
        disabled={loading}
        style={{
          width: '100%',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          gap: '10px',
          padding: '12px',
          borderRadius: '8px',
          border: '1px solid #cbd5e1',
          background: '#ffffff',
          color: '#1e293b',
          fontWeight: 600,
          fontSize: '0.95rem',
          cursor: 'pointer',
          boxShadow: '0 1px 3px rgba(0,0,0,0.05)',
          transition: 'all 0.2s ease'
        }}
      >
        <svg width="18" height="18" viewBox="0 0 24 24">
          <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
          <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
          <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"/>
          <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"/>
        </svg>
        {loading ? 'Đang xác thực với Google...' : 'Đăng nhập nhanh với Google'}
      </button>
    </div>
  );
}

export default GoogleLoginButton;
