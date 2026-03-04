import { useEffect, useState } from 'react';
import { collection, getDocs, deleteDoc, doc } from 'firebase/firestore';
import { db } from '../../firebase/config';
import { Plus, Edit2, Trash2, User as UserIcon, Star } from 'lucide-react';
import { Link } from 'react-router-dom';
import Pagination from '../../components/Common/Pagination';
import { usePagination } from '../../hooks/usePagination';

export interface Vet {
    id: string;
    name: string;
    specialty: string;
    experience: string;
    rating: number;
    reviewsCount: number;
    initials: string;
    bio: string;
    imageUrl: string;
    email: string;
    phone: string;
    clinicId?: string;
    isActive: boolean;
    createdAt?: number;
    updatedAt?: number;
}

export default function VetsList() {
    const [vets, setVets] = useState<Vet[]>([]);
    const [loading, setLoading] = useState(true);
    const { 
        currentPage, 
        totalPages, 
        paginatedItems, 
        handlePageChange, 
        totalItems, 
        itemsPerPage 
    } = usePagination(vets, 8);

    const fetchVets = async () => {
        try {
            const querySnapshot = await getDocs(collection(db, 'veterinarians'));
            const data: Vet[] = [];
            querySnapshot.forEach((doc) => {
                data.push({ id: doc.id, ...doc.data() } as Vet);
            });
            setVets(data);
            setLoading(false);
        } catch (error) {
            console.error("Error fetching vets: ", error);
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchVets();
    }, []);

    const handleDelete = async (id: string) => {
        if (window.confirm("Delete this veterinarian profile?")) {
            try {
                await deleteDoc(doc(db, 'veterinarians', id));
                setVets(vets.filter(v => v.id !== id));
            } catch (error) {
                console.error("Error deleting vet", error);
                alert("Failed to delete veterinarian.");
            }
        }
    };

    if (loading) return <div className="p-8 text-center text-slate-500">Loading veterinarians...</div>;

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <h1 className="text-2xl font-bold text-slate-800">Veterinarians</h1>
                <Link to="/vets/new" className="flex items-center gap-2 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors">
                    <Plus className="h-5 w-5" />
                    Add Veterinarian
                </Link>
            </div>

            <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
                <table className="w-full text-left border-collapse">
                    <thead>
                        <tr className="bg-slate-50 border-b border-slate-200">
                            <th className="px-6 py-4 font-semibold text-slate-600">Profile</th>
                            <th className="px-6 py-4 font-semibold text-slate-600">Specialty & Exp.</th>
                            <th className="px-6 py-4 font-semibold text-slate-600">Clinic</th>
                            <th className="px-6 py-4 font-semibold text-slate-600">Rating</th>
                            <th className="px-6 py-4 font-semibold text-slate-600">Status</th>
                            <th className="px-6 py-4 font-semibold text-slate-600 text-right">Actions</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                        {vets.length === 0 && (
                            <tr>
                                <td colSpan={6} className="px-6 py-8 text-center text-slate-500">
                                    No veterinarians found. Add a new one to get started.
                                </td>
                            </tr>
                        )}
                        {paginatedItems.map((vet) => (
                            <tr key={vet.id} className="hover:bg-slate-50 transition-colors">
                                <td className="px-6 py-4">
                                    <div className="flex items-center gap-4">
                                        <div className="h-12 w-12 rounded-full bg-slate-100 flex items-center justify-center overflow-hidden border border-slate-200">
                                            {vet.imageUrl ? (
                                                <img src={vet.imageUrl} alt={vet.name} className="h-full w-full object-cover" />
                                            ) : (
                                                <UserIcon className="h-6 w-6 text-slate-400" />
                                            )}
                                        </div>
                                        <div>
                                            <p className="font-medium text-slate-900">{vet.name}</p>
                                            <p className="text-sm text-slate-500 max-w-xs line-clamp-1">{vet.bio}</p>
                                        </div>
                                    </div>
                                </td>
                                <td className="px-6 py-4">
                                    <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                                        {vet.specialty || 'General'}
                                    </span>
                                    <div className="text-sm text-slate-500 mt-1">{vet.experience} exp</div>
                                </td>
                                <td className="px-6 py-4">
                                    <div className="text-sm text-slate-600 font-medium">
                                        {vet.clinicId || 'No clinic'}
                                    </div>
                                </td>
                                <td className="px-6 py-4">
                                    <div className="flex items-center gap-1 text-yellow-500 mb-0.5">
                                        <Star className="h-4 w-4 fill-current" />
                                        <span className="font-semibold">{vet.rating || 0}</span>
                                    </div>
                                    <div className="text-[10px] text-slate-400 uppercase tracking-wider">
                                        {vet.reviewsCount || 0} reviews
                                    </div>
                                </td>
                                <td className="px-6 py-4">
                                    <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wider ${
                                        vet.isActive ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-100 text-slate-500'
                                    }`}>
                                        {vet.isActive ? 'Active' : 'Hidden'}
                                    </span>
                                </td>
                                <td className="px-6 py-4 text-right">
                                    <div className="flex items-center justify-end gap-2">
                                        <Link to={`/vets/edit/${vet.id}`} className="p-2 text-slate-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors">
                                            <Edit2 className="h-5 w-5" />
                                        </Link>
                                        <button onClick={() => handleDelete(vet.id)} className="p-2 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors">
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
