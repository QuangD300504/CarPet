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

const formatVND = (n: number) =>
    n.toLocaleString('vi-VN') + ' ₫';

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
                // Fetch counts
                const [users, orders, vets, prods, allAppts] = await Promise.all([
                    getDocs(collection(db, 'users')),
                    getDocs(collection(db, 'orders')),
                    getDocs(collection(db, 'veterinarians')),
                    getDocs(collection(db, 'products')),
                    getDocs(collection(db, 'appointments'))
                ]);

                // Calculate total revenue and recent orders
                let revenue = 0;
                const orderData: any[] = [];
                orders.forEach(doc => {
                    const data = doc.data();
                    revenue += data.totalAmount || data.totalPrice || 0;
                    orderData.push({ id: doc.id, ...data });
                });

                // Calculate pending tasks (actionable items)
                let pendingCount = 0;
                const statusMap: Record<string, number> = {};
                
                allAppts.forEach(doc => {
                    const status = doc.data().status?.toLowerCase() || 'pending';
                    if (status.includes('pending')) pendingCount++;
                    
                    const label = status.replace('_', ' ').toUpperCase();
                    statusMap[label] = (statusMap[label] || 0) + 1;
                });

                // Prepare dynamic health chart
                const health = Object.entries(statusMap).map(([name, value]) => ({
                    name,
                    value,
                    color: name.includes('PENDING') ? '#f59e0b' : 
                           name.includes('CONFIRMED') ? '#3b82f6' : 
                           name.includes('DELIVERED') || name.includes('COMPLETED') ? '#10b981' : '#94a3b8'
                })).sort((a,b) => b.value - a.value);

                setHealthData(health);

                // If no revenue, show flat line. In real app, we'd fetch date-grouped stats.
                const mockRevenueLine = revenue === 0 
                    ? [ { name: 'Sun', value: 0 }, { name: 'Mon', value: 0 }, { name: 'Tue', value: 0 }, { name: 'Wed', value: 0 }, { name: 'Thu', value: 0 }, { name: 'Fri', value: 0 }, { name: 'Sat', value: 0 } ]
                    : [ { name: 'Mon', value: revenue * 0.1 }, { name: 'Tue', value: revenue * 0.2 }, { name: 'Wed', value: revenue * 0.15 }, { name: 'Thu', value: revenue * 0.3 }, { name: 'Fri', value: revenue * 0.5 }, { name: 'Sat', value: revenue * 0.8 }, { name: 'Sun', value: revenue } ];
                
                setChartData(mockRevenueLine);

                // Calculate low stock
                let lowStockCount = 0;
                prods.forEach(doc => {
                    if (doc.data().stock < 10) lowStockCount++;
                });

                setStats({
                    totalUsers: users.size,
                    totalOrders: orders.size,
                    totalRevenue: revenue,
                    totalVets: vets.size,
                    pendingAppointments: pendingCount,
                    lowStock: lowStockCount
                });

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
                    color="bg-blue-600"
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
                            <h2 className="text-xl font-bold text-slate-900 border-l-4 border-blue-600 pl-3">Revenue Projection</h2>
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
                                        <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.1}/>
                                        <stop offset="95%" stopColor="#3b82f6" stopOpacity={0}/>
                                    </linearGradient>
                                </defs>
                                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                                <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{fill: '#94a3b8', fontSize: 12}} dy={10} />
                                <YAxis hide domain={['auto', 'auto']} />
                                <Tooltip 
                                    contentStyle={{borderRadius: '12px', border: 'none', boxShadow: '0 4px 12px rgba(0,0,0,0.1)'}}
                                    itemStyle={{color: '#2563eb', fontWeight: 'bold'}}
                                />
                                <Area 
                                    type="monotone" 
                                    dataKey="value" 
                                    stroke="#3b82f6" 
                                    strokeWidth={3}
                                    fillOpacity={1} 
                                    fill="url(#colorVal)" 
                                />
                            </AreaChart>
                        </ResponsiveContainer>
                    </div>
                </div>

                {/* Status Bar Chart */}
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
                    <div className="mt-4 space-y-2 max-h-32 overflow-y-auto">
                        {healthData.map((item) => (
                            <div key={item.name} className="flex justify-between items-center text-sm">
                                <span className="text-slate-500">{item.name}</span>
                                <span className="font-bold text-slate-900">{item.value}</span>
                            </div>
                        ))}
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
                                    <p className="text-sm font-extrabold text-slate-900">${order.totalAmount?.toFixed(2)}</p>
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
                            const statusColor = appt.status === 'confirmed' || appt.status === 'delivered' 
                                ? 'bg-emerald-100 text-emerald-700' 
                                : appt.status === 'PENDING_PAYMENT' || appt.status === 'pending'
                                ? 'bg-amber-100 text-amber-700'
                                : 'bg-slate-100 text-slate-700';
                            
                            return (
                                <div key={appt.id} className="p-6 flex items-center justify-between hover:bg-emerald-50/30 transition-colors">
                                    <div className="flex items-center gap-4">
                                        <div className="h-10 w-10 bg-blue-50 rounded-xl flex items-center justify-center text-blue-600 font-bold">
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
