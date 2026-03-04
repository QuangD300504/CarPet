import { Link, Outlet, useLocation } from 'react-router-dom';
import { Package, ShoppingCart, Tag } from 'lucide-react';

export default function StoreAdminLayout() {
    const location = useLocation();

    const tabs = [
        { path: '/store/products', label: 'Products', icon: Package },
        { path: '/store/categories', label: 'Categories', icon: Tag },
        { path: '/store/orders', label: 'Orders', icon: ShoppingCart },
    ];

    return (
        <div className="space-y-6">
            <div className="border-b border-slate-200">
                <nav className="-mb-px flex space-x-8">
                    {tabs.map((tab) => {
                        const active = location.pathname.startsWith(tab.path);
                        return (
                            <Link
                                key={tab.path}
                                to={tab.path}
                                className={`
                                    whitespace-nowrap pb-4 px-1 border-b-2 font-medium text-sm flex items-center gap-2 transition-colors
                                    ${active 
                                        ? 'border-blue-500 text-blue-600' 
                                        : 'border-transparent text-slate-500 hover:text-slate-700 hover:border-slate-300'}
                                `}
                            >
                                <tab.icon className="h-5 w-5" />
                                {tab.label}
                            </Link>
                        )
                    })}
                </nav>
            </div>

            <div className="mt-4">
                <Outlet />
            </div>
        </div>
    );
}
