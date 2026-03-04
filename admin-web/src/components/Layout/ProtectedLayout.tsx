import { Outlet, Navigate, useLocation, Link } from 'react-router-dom'
import { LayoutDashboard, Store, Stethoscope, Layers, LogOut } from 'lucide-react'

import { useAuth } from '../../contexts/AuthContext'

export const ProtectedLayout = () => {
    const { user, loading, signOut } = useAuth()
    const location = useLocation()

    if (loading) return <div className="min-h-screen flex items-center justify-center">Loading...</div>

    if (!user || !user.isAdmin) {
        return <Navigate to="/login" state={{ from: location }} replace />
    }

    const navItems = [
        { path: '/', label: 'Dashboard', icon: LayoutDashboard },
        { path: '/store', label: 'Store Admin', icon: Store },
        { path: '/vets', label: 'Vet Care Admin', icon: Stethoscope },
        { path: '/settings', label: 'Sponsors & Services', icon: Layers },
    ]

    return (
        <div className="min-h-screen bg-slate-50 flex">
            {/* Sidebar */}
            <aside className="w-64 bg-white border-r border-slate-200 flex flex-col">
                <div className="p-6 border-b border-slate-200">
                    <h1 className="text-xl font-bold text-slate-800">VetBook Admin</h1>
                </div>
                
                <nav className="flex-1 p-4 space-y-1">
                    {navItems.map((item) => {
                        const isHome = item.path === '/'
                        const active = isHome 
                            ? location.pathname === '/' 
                            : location.pathname.startsWith(item.path)
                        
                        return (
                            <Link
                                key={item.path}
                                to={item.path}
                                className={`flex items-center gap-3 px-4 py-3 rounded-lg font-medium transition-colors ${
                                    active 
                                    ? 'bg-blue-50 text-blue-700 shadow-sm' 
                                    : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900'
                                }`}
                            >
                                <item.icon className="h-5 w-5" />
                                {item.label}
                            </Link>
                        )
                    })}
                </nav>

                <div className="p-4 border-t border-slate-200">
                    <button 
                        onClick={() => signOut()}
                        className="flex items-center gap-3 px-4 py-3 w-full rounded-lg font-medium text-red-600 hover:bg-red-50 transition-colors"
                    >
                        <LogOut className="h-5 w-5" />
                        Sign out
                    </button>
                </div>
            </aside>

            {/* Main Content */}
            <main className="flex-1 flex flex-col overflow-hidden">
                <div className="flex-1 overflow-y-auto p-8">
                    <Outlet />
                </div>
            </main>
        </div>
    )
}
