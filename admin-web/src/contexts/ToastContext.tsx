import React, { createContext, useContext, useState, useCallback, useRef } from 'react';
import { CheckCircle, XCircle, Info, X } from 'lucide-react';

type ToastType = 'success' | 'error' | 'info';

interface Toast {
    id: string;
    message: string;
    type: ToastType;
}

interface ToastContextValue {
    toast: (message: string, type?: ToastType) => void;
}

const ToastContext = createContext<ToastContextValue>({ toast: () => {} });

export function useToast() {
    return useContext(ToastContext);
}

export function ToastProvider({ children }: { children: React.ReactNode }) {
    const [toasts, setToasts] = useState<Toast[]>([]);
    const counterRef = useRef(0);

    const toast = useCallback((message: string, type: ToastType = 'info') => {
        const id = `toast-${++counterRef.current}`;
        setToasts(prev => [...prev, { id, message, type }]);
        setTimeout(() => {
            setToasts(prev => prev.filter(t => t.id !== id));
        }, 4000);
    }, []);

    const dismiss = (id: string) => setToasts(prev => prev.filter(t => t.id !== id));

    const icons: Record<ToastType, React.ReactNode> = {
        success: <CheckCircle className="h-5 w-5 text-emerald-500 shrink-0" />,
        error:   <XCircle    className="h-5 w-5 text-red-500    shrink-0" />,
        info:    <Info       className="h-5 w-5 text-blue-500   shrink-0" />,
    };

    const bg: Record<ToastType, string> = {
        success: 'bg-emerald-50 border-emerald-200',
        error:   'bg-red-50    border-red-200',
        info:    'bg-blue-50   border-blue-200',
    };

    return (
        <ToastContext.Provider value={{ toast }}>
            {children}
            <div className="fixed bottom-4 right-4 z-50 flex flex-col gap-2 pointer-events-none">
                {toasts.map(t => (
                    <div
                        key={t.id}
                        className={`flex items-center gap-3 px-4 py-3 rounded-xl border shadow-lg max-w-sm pointer-events-auto ${bg[t.type]} animate-slide-up`}
                    >
                        {icons[t.type]}
                        <p className="text-sm font-medium text-slate-800 flex-1">{t.message}</p>
                        <button
                            type="button"
                            onClick={() => dismiss(t.id)}
                            className="text-slate-400 hover:text-slate-600 shrink-0"
                        >
                            <X className="h-4 w-4" />
                        </button>
                    </div>
                ))}
            </div>
        </ToastContext.Provider>
    );
}
