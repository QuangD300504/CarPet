import { useEffect, useState } from 'react';
import { collection, getDocs, deleteDoc, doc } from 'firebase/firestore';
import { db } from '../../firebase/config';
import { Plus, Edit2, Trash2, HeartPulse, Star } from 'lucide-react';
import { Link } from 'react-router-dom';
import Pagination from '../../components/Common/Pagination';
import { usePagination } from '../../hooks/usePagination';

export interface Service {
    id: string;
    title: string;
    shortDescription: string;
    about: string;
    iconUrl?: string;
    rating: number;
    reviewCount: number;
    isActive: boolean;
    bannerGradientColors?: number[];
    createdAt?: number;
    updatedAt?: number;
}

export default function ServicesList() {
    const [services, setServices] = useState<Service[]>([]);
    const [loading, setLoading] = useState(true);
    const { 
        currentPage, 
        totalPages, 
        paginatedItems, 
        handlePageChange, 
        totalItems, 
        itemsPerPage 
    } = usePagination(services, 8);

    const fetchServices = async () => {
        try {
            console.log("Fetching services from Firestore...");
            const querySnapshot = await getDocs(collection(db, 'services'));
            console.log(`Found ${querySnapshot.size} services`);
            const data: Service[] = [];
            querySnapshot.forEach((doc) => {
                const docData = doc.data();
                console.log("Service Doc:", doc.id, docData);
                data.push({ id: doc.id, ...docData } as Service);
            });
            setServices(data);
            setLoading(false);
        } catch (error) {
            console.error("Error fetching services: ", error);
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchServices();
    }, []);

    const handleDelete = async (id: string) => {
        if (window.confirm("Are you sure you want to delete this service?")) {
            try {
                await deleteDoc(doc(db, 'services', id));
                setServices(services.filter(s => s.id !== id));
            } catch (error) {
                console.error("Error deleting service", error);
                alert("Failed to delete service.");
            }
        }
    };

    if (loading) return <div className="p-8 text-center text-slate-500">Loading services...</div>;

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <h1 className="text-2xl font-bold text-slate-800">Veterinary Services</h1>
                <Link to="/settings/services/new" className="flex items-center gap-2 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors pointer-events-none opacity-50">
                    <Plus className="h-5 w-5" />
                    Feature Coming Soon
                </Link>
            </div>

            <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
                <table className="w-full text-left border-collapse">
                    <thead>
                        <tr className="bg-slate-50 border-b border-slate-200">
                            <th className="px-6 py-4 font-semibold text-slate-600">Service Category</th>
                            <th className="px-6 py-4 font-semibold text-slate-600">Short Description</th>
                            <th className="px-6 py-4 font-semibold text-slate-600">Rating</th>
                            <th className="px-6 py-4 font-semibold text-slate-600">Status</th>
                            <th className="px-6 py-4 font-semibold text-slate-600 text-right">Actions</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                        {services.length === 0 && (
                            <tr>
                                <td colSpan={5} className="px-6 py-8 text-center text-slate-500">
                                    No services configured yet.
                                </td>
                            </tr>
                        )}
                        {paginatedItems.map((service) => (
                            <tr key={service.id} className="hover:bg-slate-50 transition-colors">
                                <td className="px-6 py-4">
                                    <div className="flex items-center gap-4">
                                        <div className="h-10 w-10 rounded-lg bg-blue-100 flex items-center justify-center border border-blue-200 text-blue-600">
                                            {service.iconUrl ? (
                                                <img src={service.iconUrl} alt={service.title} className="h-full w-full object-cover rounded-lg" />
                                            ) : (
                                                <HeartPulse className="h-5 w-5" />
                                            )}
                                        </div>
                                        <div>
                                            <p className="font-medium text-slate-900">{service.title || service.id}</p>
                                            <p className="text-sm text-slate-500 max-w-sm line-clamp-1">{service.about || 'No description available'}</p>
                                        </div>
                                    </div>
                                </td>
                                <td className="px-6 py-4">
                                    <p className="text-sm text-slate-600 italic">{service.shortDescription || '-'}</p>
                                </td>
                                <td className="px-6 py-4">
                                    <div className="flex items-center gap-1 text-yellow-500">
                                        <Star className="h-4 w-4 fill-current" />
                                        <span className="font-semibold text-sm">{service.rating || 0}</span>
                                    </div>
                                    <p className="text-[10px] text-slate-400">{service.reviewCount || 0} reviews</p>
                                </td>
                                <td className="px-6 py-4">
                                    <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wider ${
                                        service.isActive !== false ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-100 text-slate-500'
                                    }`}>
                                        {service.isActive !== false ? 'Active' : 'Draft'}
                                    </span>
                                </td>
                                <td className="px-6 py-4 text-right">
                                    <div className="flex items-center justify-end gap-2">
                                        <button className="p-2 text-slate-300 cursor-not-allowed" title="Packages CRUD Coming Soon">
                                            <Plus className="h-5 w-5" />
                                        </button>
                                        <button className="p-2 text-slate-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors">
                                            <Edit2 className="h-5 w-5" />
                                        </button>
                                        <button onClick={() => handleDelete(service.id)} className="p-2 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors">
                                            <Trash2 className="h-5 w-5" />
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        ))}
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
