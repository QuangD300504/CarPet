import { useEffect, useState, useMemo } from 'react';
import { collection, getDocs, onSnapshot, doc, updateDoc, orderBy, query } from 'firebase/firestore';
import { db } from '../../firebase/config';
import { PackageSearch, Mail, Search, X } from 'lucide-react';
import Pagination from '../../components/Common/Pagination';
import { usePagination } from '../../hooks/usePagination';
import { formatVND } from '../../utils/format';

export interface Order {
    id: string;
    userId: string;
    userName?: string;
    userEmail?: string;
    totalAmount: number;
    status: 'pending' | 'processing' | 'shipped' | 'delivered' | 'cancelled';
    shippingAddress: string;
    createdAt: any;
    items: Array<{ productId: string; quantity: number; price: number; name: string }>;
}

const STATUS_OPTIONS: Order['status'][] = ['pending', 'processing', 'shipped', 'delivered', 'cancelled'];

export default function OrdersList() {
    const [orders, setOrders] = useState<Order[]>([]);
    const [loading, setLoading] = useState(true);
    const [search, setSearch] = useState('');
    const [statusFilter, setStatusFilter] = useState<Order['status'] | ''>('');
    // Cache userId → display name
    const [userNames, setUserNames] = useState<Record<string, string>>({});

    // Real-time orders listener
    useEffect(() => {
        const q = query(collection(db, 'orders'), orderBy('createdAt', 'desc'));
        const unsub = onSnapshot(q,
            snap => {
                const ords: Order[] = snap.docs.map(d => ({ id: d.id, ...d.data() } as Order));
                setOrders(ords);
                setLoading(false);
            },
            err => {
                console.error('Orders listener error:', err);
                setLoading(false);
            }
        );
        return () => unsub();
    }, []);

    // One-time fetch of user names for display
    useEffect(() => {
        const fetchUsers = async () => {
            try {
                const snap = await getDocs(collection(db, 'users'));
                const map: Record<string, string> = {};
                snap.forEach(d => {
                    const data = d.data();
                    const name = data.displayName || data.name || data.email || d.id;
                    map[d.id] = name;
                    if (data.email) map[`${d.id}:email`] = data.email;
                });
                setUserNames(map);
            } catch (e) {
                console.error('Error fetching users for names:', e);
            }
        };
        fetchUsers();
    }, []);

    const updateOrderStatus = async (orderId: string, newStatus: Order['status']) => {
        try {
            await updateDoc(doc(db, 'orders', orderId), { status: newStatus });
        } catch (error) {
            console.error('Error updating order status', error);
        }
    };

    const filteredOrders = useMemo(() => {
        const q = search.trim().toLowerCase();
        return orders.filter(o => {
            const matchesSearch =
                !q ||
                o.id.toLowerCase().includes(q) ||
                (userNames[o.userId] || '').toLowerCase().includes(q) ||
                (userNames[`${o.userId}:email`] || '').toLowerCase().includes(q);
            const matchesStatus = !statusFilter || o.status === statusFilter;
            return matchesSearch && matchesStatus;
        });
    }, [orders, search, statusFilter, userNames]);

    const {
        currentPage,
        totalPages,
        paginatedItems,
        handlePageChange,
        totalItems,
        itemsPerPage
    } = usePagination(filteredOrders, 8);

    const statusColors: Record<string, string> = {
        pending:    'bg-yellow-100 text-yellow-800',
        processing:  'bg-primary-100 text-primary-800',
        shipped:     'bg-indigo-100 text-indigo-800',
        delivered:   'bg-green-100 text-green-800',
        cancelled:   'bg-red-100 text-red-800',
    };

    if (loading) return (
        <div className="p-8 text-center text-slate-500 flex items-center justify-center gap-2">
            <PackageSearch className="animate-bounce" /> Loading orders...
        </div>
    );

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <h1 className="text-2xl font-bold text-slate-800">Orders Management</h1>
                <span className="text-sm text-slate-500">{filteredOrders.length} orders</span>
            </div>

            {/* Search + Filter Bar */}
            <div className="flex flex-wrap gap-3">
                <div className="relative flex-1 min-w-[200px]">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
                    <input
                        type="text"
                        placeholder="Search by order ID or customer name..."
                        value={search}
                        onChange={e => { setSearch(e.target.value); handlePageChange(1); }}
                        className="w-full pl-9 pr-8 py-2 border border-slate-200 rounded-lg text-sm focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none"
                    />
                    {search && (
                        <button type="button" onClick={() => setSearch('')} title="Clear search" className="absolute right-3 top-1/2 -translate-y-1/2">
                            <X className="h-3.5 w-3.5 text-slate-400 hover:text-slate-600" />
                        </button>
                    )}
                </div>
                <select
                    title="Filter by status"
                    value={statusFilter}
                    onChange={e => { setStatusFilter(e.target.value as Order['status'] | ''); handlePageChange(1); }}
                    className="px-3 py-2 border border-slate-200 rounded-lg text-sm bg-white focus:ring-2 focus:ring-primary-500 outline-none"
                >
                    <option value="">All Statuses</option>
                    {STATUS_OPTIONS.map(s => (
                        <option key={s} value={s} className="capitalize">{s.charAt(0).toUpperCase() + s.slice(1)}</option>
                    ))}
                </select>
            </div>

            <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
                <table className="w-full text-left border-collapse">
                    <thead>
                        <tr className="bg-slate-50 border-b border-slate-200">
                            <th className="px-6 py-4 font-semibold text-slate-600">Order ID &amp; Date</th>
                            <th className="px-6 py-4 font-semibold text-slate-600">Customer</th>
                            <th className="px-6 py-4 font-semibold text-slate-600">Total</th>
                            <th className="px-6 py-4 font-semibold text-slate-600">Status</th>
                            <th className="px-6 py-4 font-semibold text-slate-600">Items</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                        {paginatedItems.length === 0 ? (
                            <tr>
                                <td colSpan={5} className="px-6 py-8 text-center text-slate-500">
                                    {search || statusFilter ? 'No orders match your filters.' : 'No orders found.'}
                                </td>
                            </tr>
                        ) : paginatedItems.map((order) => {
                            const createdAt = order.createdAt?.toDate ? order.createdAt.toDate().toLocaleDateString() : 'N/A';
                            const customerName = userNames[order.userId] || order.userId;
                            const customerEmail = userNames[`${order.userId}:email`];

                            return (
                                <tr key={order.id} className="hover:bg-slate-50 transition-colors">
                                    <td className="px-6 py-4">
                                        <div className="font-mono text-sm text-slate-900">{order.id.slice(0, 8)}...</div>
                                        <div className="text-sm text-slate-500">{createdAt}</div>
                                    </td>
                                    <td className="px-6 py-4">
                                        <div className="text-sm font-medium text-slate-700 flex items-center gap-2">
                                            <Mail className="h-4 w-4 text-slate-400 shrink-0" />
                                            <span className="truncate max-w-[160px]" title={customerName}>{customerName}</span>
                                        </div>
                                        {customerEmail && (
                                            <div className="text-xs text-slate-400 truncate max-w-[160px]">{customerEmail}</div>
                                        )}
                                    </td>
                                    <td className="px-6 py-4 font-medium text-slate-900">
                                        {formatVND(order.totalAmount || 0)}
                                    </td>
                                    <td className="px-6 py-4">
                                        <select
                                            title="Change order status"
                                            value={order.status}
                                            onChange={(e) => updateOrderStatus(order.id, e.target.value as Order['status'])}
                                            className={`text-xs font-semibold rounded-full px-3 py-1 outline-none appearance-none cursor-pointer border-none ${statusColors[order.status] || statusColors.pending}`}
                                        >
                                            {STATUS_OPTIONS.map(s => (
                                                <option key={s} value={s}>{s.charAt(0).toUpperCase() + s.slice(1)}</option>
                                            ))}
                                        </select>
                                    </td>
                                    <td className="px-6 py-4 text-sm text-slate-600">
                                        {order.items?.length || 0} items
                                        <div className="text-xs text-slate-400 mt-1 line-clamp-1 max-w-[200px]">
                                            {order.items?.map(i => i.name).join(', ')}
                                        </div>
                                    </td>
                                </tr>
                            );
                        })}
                    </tbody>
                </table>
            </div>

            <Pagination
                currentPage={currentPage}
                totalPages={totalPages}
                onPageChange={handlePageChange}
                totalItems={totalItems}
                itemsPerPage={itemsPerPage}
            />
        </div>
    );
}
