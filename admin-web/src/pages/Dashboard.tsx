import { useEffect, useState } from 'react';
import { collection, onSnapshot, Timestamp } from 'firebase/firestore';
import { db } from '../firebase/config';
import {
    ShoppingBag,
    CalendarCheck,
    Stethoscope,
    TrendingUp,
    AlertCircle,
    ArrowUpRight
} from 'lucide-react';
import {
    AreaChart,
    Area,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    ResponsiveContainer,
    BarChart,
    Bar,
    Cell
} from 'recharts';
import { Link } from 'react-router-dom';

import { formatVND } from '../utils/format';
import { seedSampleOrders } from '../utils/seedOrders';

interface StatCardProps {
    title: string;
    value: string | number;
    subValue: string;
    icon: any;
    color: string;
    trend?: number;
}

const StatCard = ({ title, value, subValue, icon: Icon, color, trend }: StatCardProps) => (
    <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm hover:shadow-md transition-all duration-300">
        <div className="flex justify-between items-start mb-4">
            <div className={`p-3 rounded-xl ${color}`}>
                <Icon className="h-6 w-6 text-white" />
            </div>
            {trend !== undefined && (
                <div className={`flex items-center gap-1 text-xs font-bold px-2 py-1 rounded-full ${
                    trend > 0 ? 'text-emerald-600 bg-emerald-50' :
                    trend < 0 ? 'text-red-600 bg-red-50' :
                    'text-slate-400 bg-slate-50'
                }`}>
                    {trend > 0 ? '+' : ''}{trend}%
                    <ArrowUpRight className="h-3 w-3" />
                </div>
            )}
        </div>
        <div>
            <h3 className="text-slate-500 text-sm font-medium mb-1">{title}</h3>
            <p className="text-2xl font-bold text-slate-900">{value}</p>
            {subValue && <p className="text-xs text-slate-400 mt-1">{subValue}</p>}
        </div>
    </div>
);

function getPeriodOrders(orders: any[], days: number): any[] {
    const cutoff = new Date();
    cutoff.setDate(cutoff.getDate() - days);
    return orders.filter(o => {
        const createdAt = o.createdAt instanceof Timestamp
            ? o.createdAt.toDate()
            : (typeof o.createdAt === 'number' ? new Date(o.createdAt) : (o.createdAt ? new Date(o.createdAt) : null));
        return createdAt && createdAt >= cutoff;
    });
}

function calcTrend(current: number, previous: number): number {
    if (previous === 0) return current > 0 ? 100 : 0;
    return Math.round(((current - previous) / previous) * 100);
}

export default function Dashboard() {
    // ── Keyboard Cheat Code ─────────────────────────────────────────────
    useEffect(() => {
        let keys = '';
        const handleKeyDown = (e: KeyboardEvent) => {
            keys += e.key.toLowerCase();
            if (keys.endsWith('seed')) {
                if (window.confirm('Clear existing sample orders and generate 20 new ones?')) {
                    seedSampleOrders();
                }
                keys = '';
            }
            if (keys.length > 10) keys = keys.slice(-10);
        };
        window.addEventListener('keydown', handleKeyDown);
        return () => window.removeEventListener('keydown', handleKeyDown);
    }, []);

    const [stats, setStats] = useState({
        totalUsers: 0,
        totalOrders: 0,
        totalRevenue: 0,
        totalVets: 0,
        pendingAppointments: 0,
        lowStock: 0,
        revenueTrend: 0,
        usersTrend: 0,
    });
    const [products, setProducts] = useState<any[]>([]);
    const [recentOrders, setRecentOrders] = useState<any[]>([]);
    const [recentAppts, setRecentAppts] = useState<any[]>([]);
    const [userMap, setUserMap] = useState<Record<string, string>>({});
    const [chartData, setChartData] = useState<any[]>([]);
    const [healthData, setHealthData] = useState<any[]>([]);
    const [topProducts, setTopProducts] = useState<{ name: string; count: number }[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const now = new Date();
        const last7Days = Array.from({ length: 7 }, (_, i) => {
            const d = new Date(now);
            d.setDate(d.getDate() - (6 - i));
            return d;
        });

        const dayLabel = (d: Date) => d.toLocaleDateString('en-US', { weekday: 'short' });
        const initRevenue = Object.fromEntries(last7Days.map(d => [dayLabel(d), 0]));
        const initOrders = Object.fromEntries(last7Days.map(d => [dayLabel(d), 0]));

        let unsubs: (() => void)[] = [];
        let usersData: any[] = [];
        let ordersData: any[] = [];
        let vetsData: any[] = [];
        let apptsData: any[] = [];

        // Track which collections have loaded (even if empty)
        const loaded = { users: false, orders: false, vets: false, appts: false };

        const tick = () => {
            if (Object.values(loaded).every(Boolean)) {
                buildDashboard(usersData, ordersData, vetsData, apptsData);
            }
        };

        const buildDashboard = (
            users: any[],
            orders: any[],
            vets: any[],
            appts: any[]
        ) => {
            // ── Revenue + chart ──────────────────────────────────────────────
            let totalRevenue = 0;
            const dailyRevenue = { ...initRevenue };
            const dailyOrders  = { ...initOrders };

            orders.forEach(o => {
                const amount = o.total || o.totalAmount || o.totalPrice || 0;
                totalRevenue += amount;
                const createdAt = o.createdAt instanceof Timestamp
                    ? o.createdAt.toDate()
                    : (typeof o.createdAt === 'number' ? new Date(o.createdAt) : (o.createdAt ? new Date(o.createdAt) : null));
                if (createdAt) {
                    const lbl = dayLabel(createdAt);
                    if (lbl in dailyRevenue) dailyRevenue[lbl] += amount;
                    if (lbl in dailyOrders)  dailyOrders[lbl]++;
                }
            });

            // ── Trends: compare last-7 vs prev-7 ─────────────────────────────
            const last7  = getPeriodOrders(orders, 7);
            const prev7  = getPeriodOrders(orders, 14).filter(o => !last7.includes(o));
            const revThis  = last7.reduce((s, o) => s + (o.total || o.totalAmount || o.totalPrice || 0), 0);
            const revPrev  = prev7.reduce((s, o) => s + (o.total || o.totalAmount || o.totalPrice || 0), 0);
            const revenueTrend  = calcTrend(revThis, revPrev);
            const ordersTrend    = calcTrend(last7.length, prev7.length);

            // ── Appointment distribution ─────────────────────────────────────
            const statusMap: Record<string, number> = {};
            appts.forEach(a => {
                const s = (a.status || 'PENDING').toUpperCase();
                statusMap[s] = (statusMap[s] || 0) + 1;
            });
            const health = Object.entries(statusMap).map(([name, value]) => ({
                name,
                value,
                color: name.includes('PENDING')         ? '#f59e0b' :
                       name.includes('CONFIRMED') ||
                       name.includes('UPCOMING')         ? '#0d9488' :
                       name.includes('COMPLETED') ||
                       name.includes('DELIVERED')        ? '#10b981' : '#94a3b8',
            })).sort((a, b) => b.value - a.value);

            // ── Top selling products (by order-line qty) ─────────────────────
            const salesCount: Record<string, number> = {};
            orders.forEach(o => {
                (o.items || []).forEach((item: any) => {
                    const name = item.productName || item.name || item.productId || 'Unknown';
                    salesCount[name] = (salesCount[name] || 0) + (item.quantity || 1);
                });
            });
            const topProducts = Object.entries(salesCount)
                .sort(([, a], [, b]) => b - a)
                .slice(0, 5)
                .map(([name, count]) => ({ name, count }));

            // ── User Mapping ─────────────────────────────────────────────
            const uMap: Record<string, string> = {};
            users.forEach(u => {
                uMap[u.id] = u.fullName || u.displayName || u.name || u.id;
            });
            setUserMap(uMap);

            // ── Low stock ────────────────────────────────────────────────────
            const lowStock = products.filter((p: any) => p.stock < 10).length;
            setProducts(products);

            setStats({
                totalUsers: users.length,
                totalOrders: orders.length,
                totalRevenue,
                totalVets: vets.length,
                pendingAppointments: statusMap['PENDING'] || 0,
                lowStock,
                revenueTrend,
                usersTrend: ordersTrend,
            });

            setChartData(last7Days.map(d => ({
                name: dayLabel(d),
                revenue: dailyRevenue[dayLabel(d)],
                orders: dailyOrders[dayLabel(d)],
            })));
            setHealthData(health);
            setTopProducts(topProducts);
            setRecentOrders([...orders]
                .sort((a, b) => {
                    const ta = a.createdAt instanceof Timestamp ? a.createdAt.toMillis() : (typeof a.createdAt === 'number' ? a.createdAt : (a.createdAt ? new Date(a.createdAt).getTime() : 0));
                    const tb = b.createdAt instanceof Timestamp ? b.createdAt.toMillis() : (typeof b.createdAt === 'number' ? b.createdAt : (b.createdAt ? new Date(b.createdAt).getTime() : 0));
                    return tb - ta;
                })
                .slice(0, 5));
            setRecentAppts([...appts]
                .sort((a, b) => {
                    const ta = a.appointmentAt instanceof Timestamp ? a.appointmentAt.toMillis() : 0;
                    const tb = b.appointmentAt instanceof Timestamp ? b.appointmentAt.toMillis() : 0;
                    return tb - ta;
                })
                .slice(0, 5));
            setLoading(false);
        };

        const collections = [
            { col: 'users',         key: 'users' as const,  set: (d: any[]) => { usersData = d;  loaded.users = true;  tick(); } },
            { col: 'storeOrders',   key: 'orders' as const, set: (d: any[]) => { ordersData = d; loaded.orders = true; tick(); } },
            { col: 'veterinarians', key: 'vets' as const,   set: (d: any[]) => { vetsData = d;   loaded.vets = true;   tick(); } },
            { col: 'appointments',  key: 'appts' as const,  set: (d: any[]) => { apptsData = d;  loaded.appts = true;  tick(); } },
        ];

        collections.forEach(({ col, set }) => {
            // No orderBy to avoid requiring composite Firestore indexes
            const unsub = onSnapshot(collection(db, col), snap => {
                set(snap.docs.map(d => ({ id: d.id, ...d.data() })));
            }, err => console.error(`Dashboard ${col} listener error:`, err));
            unsubs.push(unsub);
        });

        const unsubProducts = onSnapshot(
            collection(db, 'products'),
            snap => setProducts(snap.docs.map(d => ({ id: d.id, ...d.data() }))),
            err => console.error('Dashboard products listener error:', err)
        );
        unsubs.push(unsubProducts);

        return () => unsubs.forEach(u => u());
    }, []);

    if (loading) {
        return (
            <div className="p-8 animate-pulse space-y-8">
                <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
                    {[1,2,3,4].map(i => <div key={i} className="h-40 bg-slate-100 rounded-2xl"></div>)}
                </div>
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                    <div className="lg:col-span-2 h-80 bg-slate-100 rounded-2xl"></div>
                    <div className="h-80 bg-slate-100 rounded-2xl"></div>
                </div>
            </div>
        );
    }

    return (
        <div className="space-y-8 pb-12">
            <div>
                <h1 className="text-3xl font-extrabold text-slate-800 tracking-tight">Executive Dashboard</h1>
                <p className="text-slate-500 mt-1">Real-time overview of VetBook operations.</p>
            </div>

            {/* Metrics Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                <StatCard
                    title="Gross Revenue"
                    value={formatVND(stats.totalRevenue)}
                    subValue="Last 7 days vs prior 7 days"
                    icon={TrendingUp}
                    color="bg-primary-600"
                    trend={stats.revenueTrend}
                />
                <StatCard
                    title="Total Orders"
                    value={stats.totalOrders}
                    subValue="Across all time"
                    icon={ShoppingBag}
                    color="bg-violet-600"
                    trend={stats.usersTrend}
                />
                <StatCard
                    title="Clinic Network"
                    value={stats.totalVets}
                    subValue="Certified veterinarians"
                    icon={Stethoscope}
                    color="bg-teal-600"
                />
                <StatCard
                    title="Pending Tasks"
                    value={stats.pendingAppointments}
                    subValue="New appointments to review"
                    icon={AlertCircle}
                    color="bg-amber-500"
                />
            </div>

            {/* Charts Row */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                <div className="lg:col-span-2 bg-white p-8 rounded-2xl border border-slate-200 shadow-sm">
                    <div className="flex justify-between items-center mb-8">
                        <div>
                            <h2 className="text-xl font-bold text-slate-900 border-l-4 border-primary-600 pl-3">Revenue &amp; Orders</h2>
                            <p className="text-sm text-slate-400 mt-1">Last 7 days</p>
                        </div>
                        <div className="flex items-center gap-4 text-xs font-semibold">
                            <span className="flex items-center gap-1"><span className="inline-block w-3 h-0.5 bg-primary-500 rounded"></span> Revenue</span>
                            <span className="flex items-center gap-1"><span className="inline-block w-3 h-3 bg-violet-400 rounded-sm"></span> Orders</span>
                        </div>
                    </div>
                    <div className="h-72 w-full">
                        <ResponsiveContainer width="100%" height="100%">
                            <AreaChart data={chartData}>
                                <defs>
                                    <linearGradient id="colorRevenue" x1="0" y1="0" x2="0" y2="1">
                                        <stop offset="5%" stopColor="#14b8a6" stopOpacity={0.15}/>
                                        <stop offset="95%" stopColor="#14b8a6" stopOpacity={0}/>
                                    </linearGradient>
                                </defs>
                                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                                <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{fill: '#94a3b8', fontSize: 12}} dy={10} />
                                <YAxis hide domain={['auto', 'auto']} />
                                <Tooltip
                                    contentStyle={{borderRadius: '12px', border: 'none', boxShadow: '0 4px 12px rgba(0,0,0,0.1)'}}
                                    itemStyle={{color: '#0d9488', fontWeight: 'bold'}}
                                    formatter={(value) => [formatVND(value as number), 'Revenue']}
                                />
                                <Area type="monotone" dataKey="revenue" stroke="#14b8a6" strokeWidth={3} fillOpacity={1} fill="url(#colorRevenue)" />
                            </AreaChart>
                        </ResponsiveContainer>
                    </div>
                </div>

                <div className="bg-white p-8 rounded-2xl border border-slate-200 shadow-sm">
                    <h2 className="text-xl font-bold text-slate-900 border-l-4 border-amber-500 pl-3 mb-8">Appointment Distribution</h2>
                    <div className="h-72 w-full">
                        <ResponsiveContainer width="100%" height="100%">
                            <BarChart data={healthData} layout="vertical" barSize={32}>
                                <XAxis type="number" hide />
                                <YAxis dataKey="name" type="category" axisLine={false} tickLine={false} tick={{fill: '#475569', fontSize: 10}} width={110} />
                                <Tooltip cursor={{fill: 'transparent'}} contentStyle={{borderRadius: '12px', border: 'none', boxShadow: '0 4px 12px rgba(0,0,0,0.1)'}} />
                                <Bar dataKey="value" radius={[0, 8, 8, 0]}>
                                    {healthData.map((entry, index) => (
                                        <Cell key={`cell-${index}`} fill={entry.color} />
                                    ))}
                                </Bar>
                            </BarChart>
                        </ResponsiveContainer>
                    </div>
                </div>
            </div>

            {/* Bottom Row */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                {/* Top Selling Products */}
                <div className="bg-white p-8 rounded-2xl border border-slate-200 shadow-sm">
                    <h2 className="text-xl font-bold text-slate-900 border-l-4 border-violet-500 pl-3 mb-6">Top Selling Products</h2>
                    {topProducts.length === 0 ? (
                        <p className="text-slate-400 text-sm text-center py-8">No order data yet.</p>
                    ) : (
                        <div className="space-y-3">
                            {topProducts.map((p, i) => {
                                const pct = (p.count / topProducts[0].count) * 100;
                                return (
                                <div key={p.name} className="flex items-center gap-3">
                                    <span className="w-5 text-xs font-bold text-slate-400">#{i + 1}</span>
                                    <div className="flex-1 min-w-0">
                                        <p className="text-sm font-medium text-slate-800 truncate">{p.name}</p>
                                        <div className="w-full bg-slate-100 h-1.5 rounded-full mt-1 overflow-hidden">
                                            <div className="bg-violet-500 h-full rounded-full transition-all"
                                                style={{ width: `${pct}%` }}
                                            />
                                        </div>
                                    </div>
                                    <span className="text-sm font-bold text-slate-600">{p.count} sold</span>
                                </div>
                            );
                        })}
                        </div>
                    )}
                </div>

                {/* Inventory Alerts */}
                <div className="bg-white p-8 rounded-2xl border border-slate-200 shadow-sm">
                    <div className="flex justify-between items-center mb-6">
                        <h2 className="text-xl font-bold text-slate-900 border-l-4 border-red-400 pl-3">Inventory Alerts</h2>
                        <Link to="/store" className="text-xs text-blue-600 font-bold hover:underline">Manage Products</Link>
                    </div>
                    {stats.lowStock === 0 ? (
                        <div className="flex flex-col items-center justify-center py-8 text-slate-400">
                            <AlertCircle className="h-8 w-8 mb-2 text-emerald-400" />
                            <p className="text-sm font-medium">All products are well-stocked</p>
                        </div>
                    ) : (
                        <div className="space-y-3">
                            {products
                                .filter((p) => p.stock < 10)
                                .sort((a, b) => a.stock - b.stock)
                                .slice(0, 6)
                                .map((p: any) => (
                                    <div key={p.id} className="flex items-center justify-between py-2 border-b border-slate-50 last:border-0">
                                        <div className="flex items-center gap-3">
                                            {p.imageUrl
                                                ? <img src={p.imageUrl} alt={p.name} className="h-8 w-8 rounded object-cover" />
                                                : <div className="h-8 w-8 bg-slate-100 rounded flex items-center justify-center"><ShoppingBag className="h-4 w-4 text-slate-400" /></div>
                                            }
                                            <span className="text-sm font-medium text-slate-700 truncate max-w-[180px]">{p.name}</span>
                                        </div>
                                        <span className={`text-xs font-bold px-2 py-0.5 rounded-full ${
                                            p.stock === 0 ? 'bg-red-100 text-red-700' : 'bg-amber-100 text-amber-700'
                                        }`}>
                                            {p.stock === 0 ? 'OUT OF STOCK' : `${p.stock} left`}
                                        </span>
                                    </div>
                                ))}
                        </div>
                    )}
                </div>
            </div>

            {/* Activity Feed */}
            <div className="grid grid-cols-1 xl:grid-cols-2 gap-8">
                {/* Recent Orders */}
                <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
                    <div className="p-6 border-b border-slate-100 flex justify-between items-center">
                        <h3 className="font-bold text-slate-900">Recent Store Activity</h3>
                        <Link to="/store/orders" className="text-xs text-blue-600 font-bold hover:underline">View All Orders</Link>
                    </div>
                    <div className="divide-y divide-slate-50">
                        {recentOrders.length === 0 ? (
                            <p className="p-8 text-center text-slate-400">No recent orders found</p>
                        ) : recentOrders.map((order) => (
                            <div key={order.id} className="p-6 flex items-center justify-between hover:bg-slate-50 transition-colors">
                                <div className="flex items-center gap-4">
                                    <div className="h-10 w-10 bg-slate-100 rounded-full flex items-center justify-center">
                                        <ShoppingBag className="h-5 w-5 text-slate-400" />
                                    </div>
                                    <div>
                                        <p className="text-sm font-bold text-slate-900">Order #{order.orderCode || order.id.slice(-6)}</p>
                                        <p className="text-[10px] text-slate-500 font-medium">{order.receiverName || userMap[order.uid] || 'Anonymous Customer'}</p>
                                        <p className="text-[10px] text-slate-400 capitalize">{order.status || 'Pending'}</p>
                                    </div>
                                </div>
                                <div className="text-right">
                                    <p className="text-sm font-extrabold text-slate-900">{formatVND(order.total || order.totalAmount || 0)}</p>
                                    <p className="text-[10px] text-slate-400">SECURED PAYMENT</p>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>

                {/* Recent Appointments */}
                <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
                    <div className="p-6 border-b border-slate-100 flex justify-between items-center">
                        <h3 className="font-bold text-slate-900">Clinic Schedule Spotlight</h3>
                        <Link to="/vets/appointments" className="text-xs text-blue-600 font-bold hover:underline">Full Calendar</Link>
                    </div>
                    <div className="divide-y divide-slate-50">
                        {recentAppts.length === 0 ? (
                            <p className="p-8 text-center text-slate-400">No appointments booked</p>
                        ) : recentAppts.map((appt) => {
                            const date = appt.appointmentAt instanceof Timestamp
                                ? appt.appointmentAt.toDate()
                                : appt.appointmentAt ? new Date(appt.appointmentAt) : null;
                            const statusColor =
                                appt.status === 'confirmed' || appt.status === 'UPCOMING'  ? 'bg-emerald-100 text-emerald-700' :
                                appt.status === 'PENDING_PAYMENT' || appt.status === 'pending' ? 'bg-amber-100 text-amber-700' :
                                'bg-slate-100 text-slate-700';

                            return (
                                <div key={appt.id} className="p-6 flex items-center justify-between hover:bg-emerald-50/30 transition-colors">
                                    <div className="flex items-center gap-4">
                                        <div className="h-10 w-10 bg-primary-50 rounded-xl flex items-center justify-center">
                                            <CalendarCheck className="h-5 w-5 text-primary-600" />
                                        </div>
                                        <div>
                                            <p className="text-sm font-bold text-slate-900">
                                                {date ? date.toLocaleDateString('vi-VN') : 'Unknown Date'}
                                            </p>
                                            <p className="text-[10px] text-slate-400 uppercase tracking-widest">
                                                {date ? date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) : ''}
                                            </p>
                                        </div>
                                    </div>
                                    <span className={`px-2 py-0.5 rounded-full text-[9px] font-black uppercase tracking-tighter ${statusColor}`}>
                                        {(appt.status || 'UNKNOWN').replace('_', ' ')}
                                    </span>
                                </div>
                            );
                        })}
                    </div>
                </div>
            </div>
        </div>
    );
}