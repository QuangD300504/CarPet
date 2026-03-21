import { useEffect, useState, useMemo } from 'react';
import { collection, onSnapshot, deleteDoc, doc, orderBy, query } from 'firebase/firestore';
import { db } from '../../firebase/config';
import { Plus, Edit2, Trash2, User as UserIcon, Star, Search, X } from 'lucide-react';
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
    const [clinicsMap, setClinicsMap] = useState<Record<string, string>>({});
    const [loading, setLoading] = useState(true);
    const [search, setSearch] = useState('');
    const [statusFilter, setStatusFilter] = useState('');
    const {
        currentPage,
        totalPages,
        paginatedItems,
        handlePageChange,
        totalItems,
        itemsPerPage
    } = usePagination(vets, 8);

    useEffect(() => {
        const unsubClinics = onSnapshot(collection(db, 'clinics'), snap => {
            const cmap: Record<string, string> = {};
            snap.forEach(d => { cmap[d.id] = d.data().name; });
            setClinicsMap(cmap);
        }, err => console.error('Clinics listener error:', err));

        const q = query(collection(db, 'veterinarians'), orderBy('name', 'asc'));
        const unsubVets = onSnapshot(q,
            snap => {
                setVets(snap.docs.map(d => ({ id: d.id, ...d.data() } as Vet)));
                setLoading(false);
            },
            err => { console.error('Vets listener error:', err); setLoading(false); }
        );

        return () => { unsubVets(); unsubClinics(); };
    }, []);

    const filtered = useMemo(() => {
        const q = search.trim().toLowerCase();
        return vets.filter(v => {
            const matchSearch = !q ||
                v.name.toLowerCase().includes(q) ||
                v.specialty?.toLowerCase().includes(q) ||
                v.bio?.toLowerCase().includes(q);
            const matchStatus = !statusFilter ||
                (statusFilter === 'active' && v.isActive) ||
                (statusFilter === 'hidden' && !v.isActive);
            return matchSearch && matchStatus;
        });
    }, [vets, search, statusFilter]);

    const handleDelete = async (id: string) => {
        if (window.confirm("Delete this veterinarian profile?")) {
            try {
                await deleteDoc(doc(db, 'veterinarians', id));
            } catch (error) {
                console.error("Error deleting vet", error);
            }
        }
    };

    if (loading) return <div className="p-8 text-center text-slate-500">Loading veterinarians...</div>;

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <h1 className="text-2xl font-bold text-slate-800">Veterinarians</h1>
                <span className="text-sm text-slate-500">{filtered.length} vets</span>
            </div>

            <div className="flex flex-wrap gap-3">
                <div className="relative flex-1 min-w-[200px]">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
                    <input
                        type="text"
                        placeholder="Search by name, specialty, or bio..."
                        value={search}
                        onChange={e => { setSearch(e.target.value); handlePageChange(1); }}
                        className="w-full pl-9 pr-8 py-2 border border-slate-200 rounded-lg text-sm focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none"
                    />
                    {search && (
                        <button type="button" title="Clear search" onClick={() => setSearch('')} className="absolute right-3 top-1/2 -translate-y-1/2">
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
                    <option value="active">Active</option>
                    <option value="hidden">Hidden</option>
                </select>
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
                            <th className="px-6 py-4 font-semibold text-slate-600">Specialty &amp; Exp.</th>
                            <th className="px-6 py-4 font-semibold text-slate-600">Clinic</th>
                            <th className="px-6 py-4 font-semibold text-slate-600">Rating</th>
                            <th className="px-6 py-4 font-semibold text-slate-600">Status</th>
                            <th className="px-6 py-4 font-semibold text-slate-600 text-right">Actions</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                        {paginatedItems.length === 0 ? (
                            <tr>
                                <td colSpan={6} className="px-6 py-8 text-center text-slate-500">
                                    {search || statusFilter ? 'No veterinarians match your filters.' : 'No veterinarians found. Add a new one to get started.'}
                                </td>
                            </tr>
                        ) : paginatedItems.map((vet) => (
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
                                        {vet.clinicId && clinicsMap[vet.clinicId] ? clinicsMap[vet.clinicId] : (vet.clinicId || 'No clinic')}
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
                                        <button type="button" title="Delete veterinarian" onClick={() => handleDelete(vet.id)} className="p-2 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors">
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
