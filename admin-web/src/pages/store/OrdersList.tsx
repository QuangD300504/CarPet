import { useEffect, useState } from 'react';
import { collection, getDocs, doc, updateDoc, orderBy, query } from 'firebase/firestore';
import { db } from '../../firebase/config';
import { PackageSearch, Mail } from 'lucide-react';
import Pagination from '../../components/Common/Pagination';
import { usePagination } from '../../hooks/usePagination';
import { formatVND } from '../../utils/format';


export interface Order {
    id: string;
    userId: string;
    totalAmount: number;
    status: 'pending' | 'processing' | 'shipped' | 'delivered' | 'cancelled';
    shippingAddress: string;
    createdAt: any;
    items: Array<{ productId: string, quantity: number, price: number, name: string }>;
}

export default function OrdersList() {
    const [orders, setOrders] = useState<Order[]>([]);
    const [loading, setLoading] = useState(true);
    const { 
        currentPage, 
        totalPages, 
        paginatedItems, 
        handlePageChange, 
        totalItems, 
        itemsPerPage 
    } = usePagination(orders, 8);

    const fetchOrders = async () => {
        try {
            const q = query(collection(db, 'orders'), orderBy('createdAt', 'desc'));
            const querySnapshot = await getDocs(q);
            const ords: Order[] = [];
            querySnapshot.forEach((doc) => {
                ords.push({ id: doc.id, ...doc.data() } as Order);
            });
            setOrders(ords);
            setLoading(false);
        } catch (error) {
            console.error("Error fetching orders: ", error);
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchOrders();
    }, []);

    const updateOrderStatus = async (orderId: string, newStatus: Order['status']) => {
        try {
            await updateDoc(doc(db, 'orders', orderId), { status: newStatus });
            setOrders(orders.map(o => o.id === orderId ? { ...o, status: newStatus } : o));
        } catch (error) {
            console.error("Error updating order status", error);
            alert("Failed to update status.");
        }
    };

    if (loading) return <div className="p-8 text-center text-slate-500 flex items-center justify-center gap-2"><PackageSearch className="animate-bounce" /> Loading orders...</div>;

    const statusColors = {
        pending: 'bg-yellow-100 text-yellow-800',
        processing: 'bg-primary-100 text-primary-800',
        shipped: 'bg-indigo-100 text-indigo-800',
        delivered: 'bg-green-100 text-green-800',
        cancelled: 'bg-red-100 text-red-800',
    };

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <h1 className="text-2xl font-bold text-slate-800">Orders Management</h1>
            </div>

            <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
                <table className="w-full text-left border-collapse">
                    <thead>
                        <tr className="bg-slate-50 border-b border-slate-200">
                            <th className="px-6 py-4 font-semibold text-slate-600">Order ID & Date</th>
                            <th className="px-6 py-4 font-semibold text-slate-600">Customer</th>
                            <th className="px-6 py-4 font-semibold text-slate-600">Total</th>
                            <th className="px-6 py-4 font-semibold text-slate-600">Status</th>
                            <th className="px-6 py-4 font-semibold text-slate-600">Items</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                        {orders.length === 0 && (
                            <tr>
                                <td colSpan={5} className="px-6 py-8 text-center text-slate-500">
                                    No orders found.
                                </td>
                            </tr>
                        )}
                        {paginatedItems.map((order) => {
                            const date = order.createdAt?.toDate ? order.createdAt.toDate().toLocaleDateString() : 'N/A';
                            
                            return (
                                <tr key={order.id} className="hover:bg-slate-50 transition-colors">
                                    <td className="px-6 py-4">
                                        <div className="font-mono text-sm text-slate-900">{order.id.slice(0,8)}...</div>
                                        <div className="text-sm text-slate-500">{date}</div>
                                    </td>
                                    <td className="px-6 py-4">
                                        <div className="flex items-center gap-2 text-sm text-slate-600">
                                            <Mail className="h-4 w-4" /> {order.userId.slice(0, 10)}...
                                        </div>
                                    </td>
                                    <td className="px-6 py-4 font-medium text-slate-900">
                                        {formatVND(order.totalAmount || 0)}
                                    </td>
                                    <td className="px-6 py-4">
                                        <select 
                                            value={order.status}
                                            onChange={(e) => updateOrderStatus(order.id, e.target.value as Order['status'])}
                                            className={`text-xs font-semibold rounded-full px-3 py-1 outline-none appearance-none cursor-pointer border-none ${statusColors[order.status] || statusColors.pending}`}
                                        >
                                            <option value="pending">Pending</option>
                                            <option value="processing">Processing</option>
                                            <option value="shipped">Shipped</option>
                                            <option value="delivered">Delivered</option>
                                            <option value="cancelled">Cancelled</option>
                                        </select>
                                    </td>
                                    <td className="px-6 py-4 text-sm text-slate-600">
                                        {order.items?.length || 0} items
                                        <div className="text-xs text-slate-400 mt-1 line-clamp-1 max-w-[200px]">
                                            {order.items?.map(i => i.name).join(', ')}
                                        </div>
                                    </td>
                                </tr>
                            )
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
