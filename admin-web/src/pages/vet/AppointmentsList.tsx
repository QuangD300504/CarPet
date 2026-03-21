import { useEffect, useState, useMemo } from 'react';
import { collection, getDocs, onSnapshot, doc, updateDoc, orderBy, query } from 'firebase/firestore';
import { db } from '../../firebase/config';
import { CalendarClock, User, Stethoscope, Search, X } from 'lucide-react';
import Pagination from '../../components/Common/Pagination';
import { usePagination } from '../../hooks/usePagination';

export interface Appointment {
    id: string;
    userId: string;
    veterinarianId: string;
    vetName?: string;
    userName?: string;
    status: string;
    notes?: string;
    appointmentAt: any;
    createdAt: any;
}

const ALL_STATUSES = [
    'pending', 'PENDING', 'PENDING_PAYMENT', 'UPCOMING',
    'confirmed', 'CONFIRMED', 'completed', 'COMPLETED',
    'cancelled', 'CANCELLED',
];

const STATUS_LABELS: Record<string, string> = {
    pending: 'Pending',
    PENDING: 'Pending',
    PENDING_PAYMENT: 'Pending Payment',
    UPCOMING: 'Upcoming (Paid)',
    confirmed: 'Confirmed',
    CONFIRMED: 'Confirmed',
    completed: 'Completed',
    COMPLETED: 'Completed',
    cancelled: 'Cancelled',
    CANCELLED: 'Cancelled',
};

const STATUS_COLORS: Record<string, string> = {
    pending:          'bg-yellow-100 text-yellow-800',
    PENDING:          'bg-yellow-100 text-yellow-800',
    PENDING_PAYMENT:  'bg-amber-100 text-amber-800',
    UPCOMING:         'bg-primary-50 text-primary-700 border border-primary-100 font-bold',
    confirmed:        'bg-primary-100 text-primary-800',
    CONFIRMED:        'bg-primary-100 text-primary-800',
    completed:        'bg-emerald-100 text-emerald-800',
    COMPLETED:        'bg-emerald-100 text-emerald-800',
    cancelled:        'bg-red-100 text-red-800',
    CANCELLED:        'bg-red-100 text-red-800',
};

export default function AppointmentsList() {
    const [appointments, setAppointments] = useState<Appointment[]>([]);
    const [loading, setLoading] = useState(true);
    const [search, setSearch] = useState('');
    const [statusFilter, setStatusFilter] = useState('');

    // Cache vetId → vet name
    const [vetNames, setVetNames] = useState<Record<string, string>>({});
    // Cache userId → user name
    const [userNames, setUserNames] = useState<Record<string, string>>({});

    // Real-time appointments listener
    useEffect(() => {
        const q = query(collection(db, 'appointments'), orderBy('createdAt', 'desc'));
        const unsub = onSnapshot(q,
            snap => {
                const data: Appointment[] = snap.docs.map(d => ({ id: d.id, ...d.data() } as Appointment));
                setAppointments(data);
                setLoading(false);
            },
            err => {
                console.error('Appointments listener error:', err);
                setLoading(false);
            }
        );
        return () => unsub();
    }, []);

    // One-time fetch of veterinarian names
    useEffect(() => {
        const fetchVets = async () => {
            try {
                const snap = await getDocs(collection(db, 'veterinarians'));
                const map: Record<string, string> = {};
                snap.forEach(d => {
                    const data = d.data();
                    map[d.id] = data.name || d.id;
                });
                setVetNames(map);
            } catch (e) {
                console.error('Error fetching vet names:', e);
            }
        };
        fetchVets();
    }, []);

    // One-time fetch of user names
    useEffect(() => {
        const fetchUsers = async () => {
            try {
                const snap = await getDocs(collection(db, 'users'));
                const map: Record<string, string> = {};
                snap.forEach(d => {
                    const data = d.data();
                    map[d.id] = data.displayName || data.name || d.id;
                });
                setUserNames(map);
            } catch (e) {
                console.error('Error fetching user names:', e);
            }
        };
        fetchUsers();
    }, []);

    const updateStatus = async (appointmentId: string, newStatus: string) => {
        try {
            await updateDoc(doc(db, 'appointments', appointmentId), { status: newStatus });
        } catch (error) {
            console.error('Error updating appointment status', error);
        }
    };

    const filteredAppts = useMemo(() => {
        const q = search.trim().toLowerCase();
        return appointments.filter(a => {
            const matchesSearch =
                !q ||
                a.id.toLowerCase().includes(q) ||
                (vetNames[a.veterinarianId] || '').toLowerCase().includes(q) ||
                (userNames[a.userId] || '').toLowerCase().includes(q);
            const matchesStatus = !statusFilter || a.status === statusFilter;
            return matchesSearch && matchesStatus;
        });
    }, [appointments, search, statusFilter, vetNames, userNames]);

    const {
        currentPage,
        totalPages,
        paginatedItems,
        handlePageChange,
        totalItems,
        itemsPerPage
    } = usePagination(filteredAppts, 8);

    if (loading) return (
        <div className="p-8 text-center text-slate-500 flex items-center justify-center gap-2">
            <CalendarClock className="animate-spin" /> Loading appointments...
        </div>
    );

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <h1 className="text-2xl font-bold text-slate-800">Veterinary Appointments</h1>
                <span className="text-sm text-slate-500">{filteredAppts.length} appointments</span>
            </div>

            {/* Search + Filter Bar */}
            <div className="flex flex-wrap gap-3">
                <div className="relative flex-1 min-w-[200px]">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
                    <input
                        type="text"
                        placeholder="Search by ID, vet name, or patient..."
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
                    onChange={e => { setStatusFilter(e.target.value); handlePageChange(1); }}
                    className="px-3 py-2 border border-slate-200 rounded-lg text-sm bg-white focus:ring-2 focus:ring-primary-500 outline-none"
                >
                    <option value="">All Statuses</option>
                    {Object.entries(STATUS_LABELS).map(([val, label]) => (
                        <option key={val} value={val}>{label}</option>
                    ))}
                </select>
            </div>

            <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
                <table className="w-full text-left border-collapse">
                    <thead>
                        <tr className="bg-slate-50 border-b border-slate-200">
                            <th className="px-6 py-4 font-semibold text-slate-600">ID &amp; Booking time</th>
                            <th className="px-6 py-4 font-semibold text-slate-600">Patient / User</th>
                            <th className="px-6 py-4 font-semibold text-slate-600">Veterinarian</th>
                            <th className="px-6 py-4 font-semibold text-slate-600">Schedule</th>
                            <th className="px-6 py-4 font-semibold text-slate-600">Status</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                        {paginatedItems.length === 0 ? (
                            <tr>
                                <td colSpan={5} className="px-6 py-8 text-center text-slate-500">
                                    {search || statusFilter ? 'No appointments match your filters.' : 'No appointments found.'}
                                </td>
                            </tr>
                        ) : paginatedItems.map((appointment) => {
                            const createdAt = appointment.createdAt?.toDate
                                ? appointment.createdAt.toDate().toLocaleDateString()
                                : 'N/A';
                            const vetName = vetNames[appointment.veterinarianId] || appointment.veterinarianId;
                            const patientName = userNames[appointment.userId] || appointment.userId;
                            const apptDate = appointment.appointmentAt?.toDate
                                ? appointment.appointmentAt.toDate()
                                : null;
                            const statusColor = STATUS_COLORS[appointment.status] || STATUS_COLORS.PENDING;

                            return (
                                <tr key={appointment.id} className="hover:bg-slate-50 transition-colors">
                                    <td className="px-6 py-4">
                                        <div className="font-mono text-sm text-slate-900">{appointment.id?.slice(0, 8) || 'Unknown'}...</div>
                                        <div className="text-sm text-slate-500">Booked: {createdAt}</div>
                                    </td>
                                    <td className="px-6 py-4">
                                        <div className="flex items-center gap-2 text-sm text-slate-700">
                                            <User className="h-4 w-4 text-slate-400 shrink-0" />
                                            <span className="truncate max-w-[140px]" title={patientName}>{patientName}</span>
                                        </div>
                                    </td>
                                    <td className="px-6 py-4 text-sm text-slate-700">
                                        <div className="flex items-center gap-2">
                                            <Stethoscope className="h-4 w-4 text-slate-400 shrink-0" />
                                            <span className="truncate max-w-[140px]" title={vetName}>{vetName}</span>
                                        </div>
                                    </td>
                                    <td className="px-6 py-4">
                                        {apptDate ? (
                                            <>
                                                <div className="font-medium text-slate-900">
                                                    {apptDate.toLocaleDateString('vi-VN')}
                                                </div>
                                                <div className="text-sm text-slate-500">
                                                    {apptDate.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })}
                                                </div>
                                                {appointment.notes && (
                                                    <div className="text-xs text-slate-400 max-w-[150px] line-clamp-1 mt-1">
                                                        Note: {appointment.notes}
                                                    </div>
                                                )}
                                            </>
                                        ) : (
                                            <span className="text-slate-400">N/A</span>
                                        )}
                                    </td>
                                    <td className="px-6 py-4">
                                        <select
                                            title="Change appointment status"
                                            value={appointment.status}
                                            onChange={(e) => updateStatus(appointment.id, e.target.value)}
                                            className={`text-xs font-semibold rounded-full px-3 py-1 outline-none appearance-none cursor-pointer border-none ${statusColor}`}
                                        >
                                            {ALL_STATUSES.map(s => (
                                                <option key={s} value={s}>{STATUS_LABELS[s] || s}</option>
                                            ))}
                                        </select>
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
