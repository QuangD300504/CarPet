import React, { useState } from 'react';
import { signInWithEmailAndPassword } from 'firebase/auth';
import { auth } from '../firebase/config';
import { useAuth } from '../contexts/AuthContext';
import { Navigate, useLocation } from 'react-router-dom';

export default function Login() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState<string | null>(null);
    const [isLoggingIn, setIsLoggingIn] = useState(false);
    
    const { user, loading } = useAuth();
    const location = useLocation();

    if (loading) {
        return <div className="min-h-screen flex items-center justify-center">Loading...</div>;
    }

    if (user && user.isAdmin) {
        const from = location.state?.from?.pathname || '/';
        return <Navigate to={from} replace />;
    }

    const handleLogin = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        setIsLoggingIn(true);
        
        try {
            await signInWithEmailAndPassword(auth, email, password);
            // Redirection is handled by the AuthContext state update triggering a re-render
        } catch (err: any) {
            console.error(err);
            setError("Invalid credentials or you don't have access.");
            setIsLoggingIn(false);
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-slate-50 py-12 px-4 sm:px-6 lg:px-8">
            <div className="max-w-md w-full space-y-8 bg-white p-10 rounded-xl shadow-lg border border-slate-100">
                <div className="flex flex-col items-center">
                    <img src="/logo.png" alt="VetBook Logo" className="h-20 w-auto mb-4" />
                    <h2 className="text-center text-3xl font-extrabold text-slate-900">
                        VetBook Admin Log In
                    </h2>
                    <div className="h-1 w-16 bg-primary-500 rounded-full mt-4"></div>
                </div>
                
                {error && (
                    <div className="bg-red-50 border-l-4 border-red-500 p-4 mb-4">
                        <p className="text-sm text-red-700">{error}</p>
                    </div>
                )}
                
                {user && !user.isAdmin && (
                    <div className="bg-amber-50 border-l-4 border-amber-500 p-4 mb-4">
                        <p className="text-sm text-amber-700">You do not have Administrator privileges. Please contact Support.</p>
                    </div>
                )}

                <form className="mt-8 space-y-6" onSubmit={handleLogin}>
                    <div className="rounded-md shadow-sm space-y-4">
                        <div>
                            <label className="block text-sm font-medium text-slate-700 mb-1" htmlFor="email-address">Email address</label>
                            <input
                                id="email-address"
                                name="email"
                                type="email"
                                required
                                className="appearance-none relative block w-full px-3 py-2 border border-slate-300 placeholder-slate-500 text-slate-900 rounded-md focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                                placeholder="admin@example.com"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-slate-700 mb-1" htmlFor="password">Password</label>
                            <input
                                id="password"
                                name="password"
                                type="password"
                                required
                                className="appearance-none relative block w-full px-3 py-2 border border-slate-300 placeholder-slate-500 text-slate-900 rounded-md focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm"
                                placeholder="••••••••"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                            />
                        </div>
                    </div>

                    <div>
                        <button
                            type="submit"
                            disabled={isLoggingIn}
                            className={`group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white ${isLoggingIn ? 'bg-primary-400' : 'bg-primary-600 hover:bg-primary-700'} focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 transition-colors`}
                        >
                            {isLoggingIn ? 'Logging in...' : 'Sign In'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}
