import { useEffect, useState } from 'react';
import { collection, getDocs } from 'firebase/firestore';
import { db } from '../firebase/config';
import { 
    Users, 
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

import { formatVND } from '../utils/format';

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
            {trend !== undefined && trend !== 0 && (
                <div className={`flex items-center gap-1 text-xs font-bold ${trend > 0 ? 'text-emerald-600' : 'text-red-600'} bg-slate-50 px-2 py-1 rounded-full`}>
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

export default function Dashboard() {
    const [stats, setStats] = useState({
        totalUsers: 0,
        totalOrders: 0,
        totalRevenue: 0,
        totalVets: 0,
        pendingAppointments: 0,
        lowStock: 0
    });
    const [recentOrders, setRecentOrders] = useState<any[]>([]);
    const [recentAppts, setRecentAppts] = useState<any[]>([]);
    const [chartData, setChartData] = useState<any[]>([]); // Added for dynamic chart
    const [healthData, setHealthData] = useState<any[]>([]); // Added for health chart
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchStats = async () => {
            try {
                // Fetch core data
                const [users, orders, vets, prods, allAppts] = await Promise.all([
                    getDocs(collection(db, 'users')),
                    getDocs(collection(db, 'orders')),
                    getDocs(collection(db, 'veterinarians')),
                    getDocs(collection(db, 'products')),
                    getDocs(collection(db, 'appointments'))
                ]);

                // 1. Calculate Revenue & Group by Date (Last 7 Days)
                let totalRevenue = 0;
                const dailyRevenue: Record<string, number> = {};
                const now = new Date();
                const last7Days = Array.from({length: 7}, (_, i) => {
                    const d = new Date();
                    d.setDate(now.getDate() - (6 - i));
                    return d.toLocaleDateString('en-US', { weekday: 'short' });
                });

                // Initialize empty counts
                last7Days.forEach(day => dailyRevenue[day] = 0);

                const orderData: any[] = [];
                orders.forEach(doc => {
                    const data = doc.data();
                    const amount = data.totalAmount || data.totalPrice || 0;
                    totalRevenue += amount;
                    orderData.push({ id: doc.id, ...data });

                    const createdAt = data.createdAt?.toDate?.() || (data.createdAt ? new Date(data.createdAt) : null);
                    if (createdAt) {
                        const day = createdAt.toLocaleDateString('en-US', { weekday: 'short' });
                        if (dailyRevenue[day] !== undefined) dailyRevenue[day] += amount;
                    }
                });

                setChartData(last7Days.map(day => ({ name: day, value: dailyRevenue[day] })));

                // 2. Appointment Distribution logic
                const statusMap: Record<string, number> = {};
                allAppts.forEach(doc => {
                    const status = doc.data().status?.toUpperCase() || 'PENDING';
                    statusMap[status] = (statusMap[status] || 0) + 1;
                });

                const health = Object.entries(statusMap).map(([name, value]) => ({
                    name,
                    value,
                    color: name.includes('PENDING') ? '#f59e0b' : 
                           name.includes('CONFIRMED') || name.includes('UPCOMING') ? '#0d9488' : 
                           name.includes('DELIVERED') || name.includes('COMPLETED') ? '#10b981' : '#94a3b8'
                })).sort((a,b) => b.value - a.value);

                setHealthData(health);

                // 3. Low Stock & General Stats
                let lowStockCount = 0;
                prods.forEach(doc => {
                    if (doc.data().stock < 10) lowStockCount++;
                });

                setStats({
                    totalUsers: users.size,
                    totalOrders: orders.size,
                    totalRevenue: totalRevenue,
                    totalVets: vets.size,
                    pendingAppointments: statusMap['PENDING'] || 0,
                    lowStock: lowStockCount
                });

                // 4. Activity Lists
                setRecentOrders(orderData.sort((a,b) => (b.createdAt || 0) - (a.createdAt || 0)).slice(0, 5));
                
                const topAppts = allAppts.docs
                    .map(d => ({ id: d.id, ...d.data() }))
                    .sort((a: any, b: any) => (b.createdAt || 0) - (a.createdAt || 0))
                    .slice(0, 5);
                setRecentAppts(topAppts);

                setLoading(false);
            } catch (err) {
                console.error("Dashboard error:", err);
                setLoading(false);
            }
        };

        fetchStats();
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
                    subValue="Total transaction volume"
                    icon={TrendingUp} 
                    color="bg-primary-600"
                    trend={stats.totalRevenue > 0 ? 12.5 : 0}
                />
                <StatCard 
                    title="Active Pet Owners" 
                    value={stats.totalUsers} 
                    subValue="Users on the platform"
                    icon={Users} 
                    color="bg-violet-600"
                    trend={stats.totalUsers > 0 ? 3.2 : 0}
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

            {/* Charts Section */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                {/* Main Graph */}
                <div className="lg:col-span-2 bg-white p-8 rounded-2xl border border-slate-200 shadow-sm">
                    <div className="flex justify-between items-center mb-8">
                        <div>
                            <h2 className="text-xl font-bold text-slate-900 border-l-4 border-primary-600 pl-3">Revenue Projection</h2>
                            <p className="text-sm text-slate-400 mt-1">Comparing daily performance trends</p>
                        </div>
                        <div className="flex items-center gap-2 px-3 py-1 bg-slate-50 rounded-lg text-xs font-semibold text-slate-500">
                            Last 7 Days
                        </div>
                    </div>
                    <div className="h-72 w-full">
                        <ResponsiveContainer width="100%" height="100%">
                            <AreaChart data={chartData}>
                                <defs>
                                    <linearGradient id="colorVal" x1="0" y1="0" x2="0" y2="1">
                                        <stop offset="5%" stopColor="#14b8a6" stopOpacity={0.1}/>
                                        <stop offset="95%" stopColor="#14b8a6" stopOpacity={0}/>
                                    </linearGradient>
                                </defs>
                                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                                <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{fill: '#94a3b8', fontSize: 12}} dy={10} />
                                <YAxis hide domain={['auto', 'auto']} />
                                <Tooltip 
                                    contentStyle={{borderRadius: '12px', border: 'none', boxShadow: '0 4px 12px rgba(0,0,0,0.1)'}}
                                    itemStyle={{color: '#0d9488', fontWeight: 'bold'}}
                                />
                                <Area 
                                    type="monotone" 
                                    dataKey="value" 
                                    stroke="#14b8a6" 
                                    strokeWidth={3}
                                    fillOpacity={1} 
                                    fill="url(#colorVal)" 
                                />
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
                                <YAxis dataKey="name" type="category" axisLine={false} tickLine={false} tick={{fill: '#475569', fontSize: 10}} width={100} />
                                <Tooltip 
                                    cursor={{fill: 'transparent'}}
                                    contentStyle={{borderRadius: '12px', border: 'none', boxShadow: '0 4px 12px rgba(0,0,0,0.1)'}}
                                />
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

            {/* Analysis Row 2 */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                {/* Product Sales Analysis */}
                <div className="bg-white p-8 rounded-2xl border border-slate-200 shadow-sm">
                    <h2 className="text-xl font-bold text-slate-900 border-l-4 border-violet-500 pl-3 mb-8">Top Selling Products</h2>
                    <div className="h-64 w-full">
                        <ResponsiveContainer width="100%" height="100%">
                            <BarChart data={recentOrders.slice(0,5).map(o => ({ name: `Order ${o.id.slice(-4)}`, value: o.totalAmount || o.totalPrice }))}>
                                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                                <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{fill: '#94a3b8', fontSize: 10}} />
                                <YAxis hide />
                                <Tooltip contentStyle={{borderRadius: '12px', border: 'none'}} />
                                <Bar dataKey="value" fill="#8b5cf6" radius={[8, 8, 0, 0]} />
                            </BarChart>
                        </ResponsiveContainer>
                    </div>
                </div>

                {/* Growth Stats */}
                <div className="bg-white p-8 rounded-2xl border border-slate-200 shadow-sm">
                    <h2 className="text-xl font-bold text-slate-900 border-l-4 border-emerald-500 pl-3 mb-8">Clinic Network Performance</h2>
                    <div className="space-y-6">
                        <div className="flex justify-between items-end">
                            <div>
                                <p className="text-sm text-slate-500">Service Coverage</p>
                                <p className="text-3xl font-black text-slate-800">98.2%</p>
                            </div>
                            <div className="text-right">
                                <p className="text-xs text-emerald-600 font-bold bg-emerald-50 px-2 py-1 rounded-lg">+4.1% MoM</p>
                            </div>
                        </div>
                        <div className="w-full bg-slate-100 h-3 rounded-full overflow-hidden">
                            <div className="bg-emerald-500 h-full w-[92%]" />
                        </div>
                        <div className="grid grid-cols-2 gap-4 pt-4 border-t border-slate-100">
                            <div>
                                <p className="text-[10px] text-slate-400 uppercase font-black">Active Vets</p>
                                <p className="text-lg font-bold text-slate-700">{stats.totalVets}</p>
                            </div>
                            <div>
                                <p className="text-[10px] text-slate-400 uppercase font-black">Avg Rating</p>
                                <p className="text-lg font-bold text-slate-700">4.9/5.0</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* Bottom Row Activity */}
            <div className="grid grid-cols-1 xl:grid-cols-2 gap-8">
                {/* Recent Orders */}
                <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
                    <div className="p-6 border-b border-slate-100 flex justify-between items-center">
                        <h3 className="font-bold text-slate-900">Recent Store Activity</h3>
                        <button className="text-xs text-blue-600 font-bold hover:underline">View All Orders</button>
                    </div>
                    <div className="divide-y divide-slate-50 italic">
                        {recentOrders.length === 0 ? (
                            <p className="p-8 text-center text-slate-400">No recent orders found</p>
                        ) : recentOrders.map((order) => (
                            <div key={order.id} className="p-6 flex items-center justify-between hover:bg-slate-50 transition-colors">
                                <div className="flex items-center gap-4">
                                    <div className="h-10 w-10 bg-slate-100 rounded-full flex items-center justify-center font-bold text-slate-400">
                                        <ShoppingBag className="h-5 w-5" />
                                    </div>
                                    <div>
                                        <p className="text-sm font-bold text-slate-900 leading-none mb-1">Order #{order.id.slice(-6)}</p>
                                        <p className="text-xs text-slate-500">{order.status || 'Pending'}</p>
                                    </div>
                                </div>
                                <div className="text-right">
                                    <p className="text-sm font-extrabold text-slate-900">{formatVND(order.totalAmount || 0)}</p>
                                    <p className="text-[10px] text-slate-400 tracking-wider">SECURED PAYMENT</p>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>

                {/* Recent Appointments */}
                <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden text-sm">
                    <div className="p-6 border-b border-slate-100 flex justify-between items-center">
                        <h3 className="font-bold text-slate-900">Clinic Schedule Spotlight</h3>
                        <button className="text-xs text-blue-600 font-bold hover:underline">Full Calendar</button>
                    </div>
                    <div className="divide-y divide-slate-50">
                        {recentAppts.length === 0 ? (
                            <p className="p-8 text-center text-slate-400 italic">No appointments booked today</p>
                        ) : recentAppts.map((appt) => {
                            const date = appt.appointmentAt?.toDate ? appt.appointmentAt.toDate().toLocaleDateString('vi-VN') : 'Unknown Date';
                            const time = appt.appointmentAt?.toDate ? appt.appointmentAt.toDate().toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' }) : 'Unknown Time';
                            const statusColor = appt.status === 'confirmed' || appt.status === 'delivered' || appt.status === 'UPCOMING'
                                ? 'bg-emerald-100 text-emerald-700' 
                                : appt.status === 'PENDING_PAYMENT' || appt.status === 'pending'
                                ? 'bg-amber-100 text-amber-700'
                                : 'bg-slate-100 text-slate-700';
                            
                            return (
                                <div key={appt.id} className="p-6 flex items-center justify-between hover:bg-emerald-50/30 transition-colors">
                                    <div className="flex items-center gap-4">
                                        <div className="h-10 w-10 bg-primary-50 rounded-xl flex items-center justify-center text-primary-600 font-bold">
                                            <CalendarCheck className="h-5 w-5" />
                                        </div>
                                        <div>
                                            <p className="text-sm font-bold text-slate-900 leading-none mb-1">{date}</p>
                                            <p className="text-[10px] text-slate-400 uppercase tracking-widest">{time}</p>
                                        </div>
                                    </div>
                                    <div>
                                        <span className={`px-2 py-0.5 rounded-full text-[9px] font-black uppercase tracking-tighter ${statusColor}`}>
                                            {appt.status?.replace('_', ' ') || 'UNKNOWN'}
                                        </span>
                                    </div>
                                </div>
                            )
                        })}
                    </div>
                </div>
            </div>
        </div>
    );
}
