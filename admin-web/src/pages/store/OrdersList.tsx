import { useEffect, useState, useMemo } from 'react';
import { collection, getDocs, onSnapshot, doc, updateDoc, orderBy, query } from 'firebase/firestore';
import { db } from '../../firebase/config';
import { PackageSearch, Mail, Search, X, Calendar, MapPin, ShoppingBag, ChevronRight, User } from 'lucide-react';
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
    const [selectedOrder, setSelectedOrder] = useState<Order | null>(null);

    // Cache userId → display name
    const [userNames, setUserNames] = useState<Record<string, string>>({});

    // Real-time orders listener
    useEffect(() => {
        const q = query(collection(db, 'storeOrders'), orderBy('createdAt', 'desc'));
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
                    const name = data.fullName || data.displayName || data.name || data.email || d.id;
                    map[d.id] = name;
                    if (data.email) map[`${d.id}:email`] = data.email;
                    if (data.phone) map[`${d.id}:phone`] = data.phone;
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
                                <tr key={order.id} className="hover:bg-slate-50 transition-colors cursor-pointer" onClick={() => setSelectedOrder(order)}>
                                    <td className="px-6 py-4">
                                        <div className="font-mono text-sm text-slate-900 flex items-center gap-2 group">
                                            {order.id.slice(0, 8)}...
                                            <ChevronRight className="h-3.5 w-3.5 text-slate-300 group-hover:text-primary-500 transition-colors" />
                                        </div>
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
                                    <td className="px-6 py-4" onClick={(e) => e.stopPropagation()}>
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

            {/* Order Detail Modal */}
            {selectedOrder && (
                <div className="fixed inset-0 bg-slate-900/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
                    <div className="bg-white rounded-2xl shadow-2xl w-full max-w-2xl max-h-[90vh] overflow-hidden flex flex-col">
                        <div className="p-6 border-b border-slate-100 flex justify-between items-center bg-slate-50">
                            <div>
                                <h2 className="text-xl font-bold text-slate-900">Order Details</h2>
                                <p className="text-sm text-slate-500 font-mono mt-1">ID: {selectedOrder.id}</p>
                            </div>
                            <button
                                onClick={() => setSelectedOrder(null)}
                                className="p-2 hover:bg-slate-200 rounded-full transition-colors"
                            >
                                <X className="h-5 w-5 text-slate-500" />
                            </button>
                        </div>

                        <div className="flex-1 overflow-y-auto p-6 space-y-8">
                            {/* Summary Cards */}
                            <div className="grid grid-cols-2 gap-4">
                                <div className="p-4 bg-slate-50 rounded-xl border border-slate-100">
                                    <div className="flex items-center gap-2 text-slate-500 text-xs font-bold uppercase tracking-wider mb-2">
                                        <Calendar className="h-3.5 w-3.5" /> Date & Time
                                    </div>
                                    <p className="text-sm font-semibold text-slate-900">
                                        {selectedOrder.createdAt?.toDate ? selectedOrder.createdAt.toDate().toLocaleString('vi-VN') : 'N/A'}
                                    </p>
                                </div>
                                <div className="p-4 bg-primary-50 rounded-xl border border-primary-100">
                                    <div className="flex items-center gap-2 text-primary-600 text-xs font-bold uppercase tracking-wider mb-2">
                                        <ShoppingBag className="h-3.5 w-3.5" /> Total Amount
                                    </div>
                                    <p className="text-lg font-bold text-primary-700">
                                        {formatVND(selectedOrder.totalAmount)}
                                    </p>
                                </div>
                            </div>

                            {/* Customer & Shipping */}
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                                <div className="space-y-4">
                                    <h3 className="text-sm font-bold text-slate-900 uppercase tracking-tight flex items-center gap-2">
                                        <User className="h-4 w-4 text-primary-500" /> Customer Information
                                    </h3>
                                    <div className="space-y-2">
                                        <p className="text-sm font-medium text-slate-800">{userNames[selectedOrder.userId] || 'Unknown'}</p>
                                        <p className="text-sm text-slate-500 flex items-center gap-2">
                                            <Mail className="h-3.5 w-3.5" /> {userNames[`${selectedOrder.userId}:email`] || 'No email'}
                                        </p>
                                        {userNames[`${selectedOrder.userId}:phone`] && (
                                            <p className="text-sm text-slate-500 flex items-center gap-2">
                                                <span className="h-3.5 w-3.5 text-xs">📞</span> {userNames[`${selectedOrder.userId}:phone`]}
                                            </p>
                                        )}
                                    </div>
                                </div>
                                <div className="space-y-4">
                                    <h3 className="text-sm font-bold text-slate-900 uppercase tracking-tight flex items-center gap-2">
                                        <MapPin className="h-4 w-4 text-red-500" /> Shipping Address
                                    </h3>
                                    <p className="text-sm text-slate-600 bg-slate-50 p-3 rounded-lg border border-slate-100 italic">
                                        {selectedOrder.shippingAddress || 'No address provided'}
                                    </p>
                                </div>
                            </div>

                            {/* Order Items */}
                            <div className="space-y-4">
                                <h3 className="text-sm font-bold text-slate-900 uppercase tracking-tight flex items-center gap-2">
                                    <PackageSearch className="h-4 w-4 text-indigo-500" /> Order Items ({selectedOrder.items?.length || 0})
                                </h3>
                                <div className="bg-white border border-slate-100 rounded-xl overflow-hidden">
                                    <table className="w-full text-sm">
                                        <thead className="bg-slate-50 text-slate-500 font-medium">
                                            <tr>
                                                <th className="px-4 py-3 text-left">Product</th>
                                                <th className="px-4 py-3 text-center">Qty</th>
                                                <th className="px-4 py-3 text-right">Price</th>
                                                <th className="px-4 py-3 text-right">Subtotal</th>
                                            </tr>
                                        </thead>
                                        <tbody className="divide-y divide-slate-50">
                                            {selectedOrder.items?.map((item, idx) => (
                                                <tr key={idx}>
                                                    <td className="px-4 py-3 font-medium text-slate-800">{item.name}</td>
                                                    <td className="px-4 py-3 text-center text-slate-600">{item.quantity}</td>
                                                    <td className="px-4 py-3 text-right text-slate-600">{formatVND(item.price)}</td>
                                                    <td className="px-4 py-3 text-right font-bold text-slate-900">{formatVND(item.price * item.quantity)}</td>
                                                </tr>
                                            ))}
                                        </tbody>
                                        <tfoot className="bg-slate-50 font-bold">
                                            <tr>
                                                <td colSpan={3} className="px-4 py-3 text-right text-slate-500">Total</td>
                                                <td className="px-4 py-3 text-right text-primary-600">{formatVND(selectedOrder.totalAmount)}</td>
                                            </tr>
                                        </tfoot>
                                    </table>
                                </div>
                            </div>
                        </div>

                        <div className="p-6 bg-slate-50 border-t border-slate-100 flex justify-between items-center">
                            <div className="flex items-center gap-3">
                                <span className="text-sm font-bold text-slate-500">Status:</span>
                                <select
                                    value={selectedOrder.status}
                                    onChange={(e) => updateOrderStatus(selectedOrder.id, e.target.value as Order['status'])}
                                    className={`text-sm font-bold rounded-full px-4 py-1.5 border-none outline-none ring-2 ring-white shadow-sm ${statusColors[selectedOrder.status]}`}
                                >
                                    {STATUS_OPTIONS.map(s => (
                                        <option key={s} value={s}>{s.charAt(0).toUpperCase() + s.slice(1)}</option>
                                    ))}
                                </select>
                            </div>
                            <button
                                onClick={() => setSelectedOrder(null)}
                                className="px-6 py-2 bg-slate-800 text-white text-sm font-bold rounded-xl hover:bg-slate-900 transition-colors shadow-lg shadow-slate-200"
                            >
                                Done
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
