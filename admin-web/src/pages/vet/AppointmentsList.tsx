import { useEffect, useState } from 'react';
import { collection, getDocs, doc, updateDoc, orderBy, query } from 'firebase/firestore';
import { db } from '../../firebase/config';
import { CalendarClock, User, Stethoscope } from 'lucide-react';
import Pagination from '../../components/Common/Pagination';
import { usePagination } from '../../hooks/usePagination';

export interface Appointment {
    id: string;
    userId: string;
    veterinarianId: string;
    status: string;
    notes?: string;
    appointmentAt: any;
    createdAt: any;
}

export default function AppointmentsList() {
    const [appointments, setAppointments] = useState<Appointment[]>([]);
    const [loading, setLoading] = useState(true);
    const { 
        currentPage, 
        totalPages, 
        paginatedItems, 
        handlePageChange, 
        totalItems, 
        itemsPerPage 
    } = usePagination(appointments, 8);

    const fetchAppointments = async () => {
        try {
            const q = query(collection(db, 'appointments'), orderBy('createdAt', 'desc'));
            const querySnapshot = await getDocs(q);
            const data: Appointment[] = [];
            querySnapshot.forEach((doc) => {
                data.push({ id: doc.id, ...doc.data() } as Appointment);
            });
            setAppointments(data);
            setLoading(false);
        } catch (error) {
            console.error("Error fetching appointments: ", error);
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchAppointments();
    }, []);

    const updateStatus = async (appointmentId: string, newStatus: Appointment['status']) => {
        try {
            await updateDoc(doc(db, 'appointments', appointmentId), { status: newStatus });
            setAppointments(appointments.map(a => a.id === appointmentId ? { ...a, status: newStatus } : a));
        } catch (error) {
            console.error("Error updating appointment status", error);
            alert("Failed to update status.");
        }
    };

    if (loading) return <div className="p-8 text-center text-slate-500 flex items-center justify-center gap-2"><CalendarClock className="animate-spin" /> Loading appointments...</div>;

    const statusColors: Record<string, string> = {
        pending: 'bg-yellow-100 text-yellow-800',
        PENDING: 'bg-yellow-100 text-yellow-800',
        PENDING_PAYMENT: 'bg-amber-100 text-amber-800',
        confirmed: 'bg-primary-100 text-primary-800',
        CONFIRMED: 'bg-primary-100 text-primary-800',
        UPCOMING: 'bg-primary-50 text-primary-700 font-bold border border-primary-100',
        completed: 'bg-emerald-100 text-emerald-800',
        COMPLETED: 'bg-emerald-100 text-emerald-800',
        cancelled: 'bg-red-100 text-red-800',
        CANCELLED: 'bg-red-100 text-red-800',
    };

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <h1 className="text-2xl font-bold text-slate-800">Veterinary Appointments</h1>
            </div>

            <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
                <table className="w-full text-left border-collapse">
                    <thead>
                        <tr className="bg-slate-50 border-b border-slate-200">
                            <th className="px-6 py-4 font-semibold text-slate-600">ID & Booking time</th>
                            <th className="px-6 py-4 font-semibold text-slate-600">Patient / User</th>
                            <th className="px-6 py-4 font-semibold text-slate-600">Veterinarian</th>
                            <th className="px-6 py-4 font-semibold text-slate-600">Schedule</th>
                            <th className="px-6 py-4 font-semibold text-slate-600">Status</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                        {appointments.length === 0 && (
                            <tr>
                                <td colSpan={5} className="px-6 py-8 text-center text-slate-500">
                                    No appointments found.
                                </td>
                            </tr>
                        )}
                        {paginatedItems.map((appointment) => {
                            const createdAt = appointment.createdAt?.toDate ? appointment.createdAt.toDate().toLocaleDateString() : 'N/A';
                            
                            return (
                                <tr key={appointment.id} className="hover:bg-slate-50 transition-colors">
                                    <td className="px-6 py-4">
                                        <div className="font-mono text-sm text-slate-900">{appointment.id?.slice(0,8) || 'Unknown'}...</div>
                                        <div className="text-sm text-slate-500">Booked: {createdAt}</div>
                                    </td>
                                    <td className="px-6 py-4 flex items-center gap-2">
                                        <User className="h-4 w-4 text-slate-400" />
                                        <span className="text-sm text-slate-700">{appointment.userId?.slice(0,12) || 'Unknown User'}...</span>
                                    </td>
                                    <td className="px-6 py-4 text-sm text-slate-700">
                                        <div className="flex items-center gap-2">
                                            <Stethoscope className="h-4 w-4 text-slate-400" />
                                            {appointment.veterinarianId?.slice(0, 12) || 'Unknown Vet'}...
                                        </div>
                                    </td>
                                    <td className="px-6 py-4">
                                        <div className="font-medium text-slate-900">
                                            {appointment.appointmentAt?.toDate ? appointment.appointmentAt.toDate().toLocaleDateString('vi-VN') : 'N/A'}
                                        </div>
                                        <div className="text-sm text-slate-500">
                                            {appointment.appointmentAt?.toDate ? appointment.appointmentAt.toDate().toLocaleTimeString('vi-VN', {hour:'2-digit', minute:'2-digit'}) : 'N/A'}
                                        </div>
                                        {appointment.notes && <div className="text-xs text-slate-400 max-w-[150px] line-clamp-1 mt-1">Note: {appointment.notes}</div>}
                                    </td>
                                    <td className="px-6 py-4">
                                        <select 
                                            value={appointment.status}
                                            onChange={(e) => updateStatus(appointment.id, e.target.value as any)}
                                            className={`text-xs font-semibold rounded-full px-3 py-1 outline-none appearance-none cursor-pointer border-none ${statusColors[appointment.status] || statusColors.pending}`}
                                        >
                                            <option value="pending">Pending</option>
                                            <option value="PENDING_PAYMENT">Pending Payment</option>
                                            <option value="UPCOMING">Upcoming (Paid)</option>
                                            <option value="confirmed">Confirmed</option>
                                            <option value="completed">Completed</option>
                                            <option value="cancelled">Cancelled</option>
                                        </select>
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
