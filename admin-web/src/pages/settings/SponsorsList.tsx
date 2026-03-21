import { useEffect, useState, useMemo } from 'react';
import { collection, onSnapshot, deleteDoc, doc, updateDoc, orderBy, query } from 'firebase/firestore';
import { db } from '../../firebase/config';
import { Plus, Edit2, Trash2, Image as ImageIcon, Search, X } from 'lucide-react';
import { Link } from 'react-router-dom';
import Pagination from '../../components/Common/Pagination';
import { usePagination } from '../../hooks/usePagination';

export interface Sponsor {
    id: string;
    title: string;
    subtitle: string;
    imageUrl: string;
    targetUrl: string;
    isActive: boolean;
    sortOrder: number;
    createdAt?: number;
    updatedAt?: number;
}

export default function SponsorsList() {
    const [sponsors, setSponsors] = useState<Sponsor[]>([]);
    const [loading, setLoading] = useState(true);
    const [search, setSearch] = useState('');
    const {
        currentPage,
        totalPages,
        paginatedItems,
        handlePageChange,
        totalItems,
        itemsPerPage
    } = usePagination(sponsors, 8);

    useEffect(() => {
        const q = query(collection(db, 'banners'), orderBy('sortOrder', 'asc'));
        const unsub = onSnapshot(q,
            snap => {
                setSponsors(snap.docs.map(d => ({ id: d.id, ...d.data() } as Sponsor)));
                setLoading(false);
            },
            err => { console.error('Sponsors listener error:', err); setLoading(false); }
        );
        return () => unsub();
    }, []);

    const filtered = useMemo(() => {
        const q = search.trim().toLowerCase();
        return sponsors.filter(b =>
            !q || b.title.toLowerCase().includes(q) || b.subtitle?.toLowerCase().includes(q)
        );
    }, [sponsors, search]);

    const handleDelete = async (id: string) => {
        if (window.confirm("Are you sure you want to delete this promotional sponsor?")) {
            try {
                await deleteDoc(doc(db, 'banners', id));
            } catch (error) {
                console.error("Error deleting sponsor", error);
            }
        }
    };

    const toggleActive = async (sponsor: Sponsor) => {
        try {
            const newValue = !sponsor.isActive;
            await updateDoc(doc(db, 'banners', sponsor.id), { isActive: newValue });
        } catch (error) {
            console.error("Error toggling sponsor status", error);
        }
    };

    if (loading) return <div className="p-8 text-center text-slate-500">Loading sponsors...</div>;

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <h1 className="text-2xl font-bold text-slate-800">Sponsors Management</h1>
                <span className="text-sm text-slate-500">{filtered.length} sponsors</span>
            </div>

            <div className="flex flex-wrap gap-3">
                <div className="relative flex-1 min-w-[200px]">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
                    <input
                        type="text"
                        placeholder="Search sponsors..."
                        value={search}
                        onChange={e => { setSearch(e.target.value); handlePageChange(1); }}
                        className="w-full pl-9 pr-8 py-2 border border-site-200 rounded-lg text-sm focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none"
                    />
                    {search && (
                        <button type="button" title="Clear search" onClick={() => setSearch('')} className="absolute right-3 top-1/2 -translate-y-1/2">
                            <X className="h-3.5 w-3.5 text-slate-400 hover:text-slate-600" />
                        </button>
                    )}
                </div>
                <Link to="/settings/sponsors/new" className="flex items-center gap-2 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors">
                    <Plus className="h-5 w-5" />
                    Add Sponsor
                </Link>
            </div>

            <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
                <table className="w-full text-left border-collapse">
                    <thead>
                        <tr className="bg-slate-50 border-b border-slate-200">
                            <th className="px-6 py-4 font-semibold text-slate-600">Sponsor Preview</th>
                            <th className="px-6 py-4 font-semibold text-slate-600">Target URL</th>
                            <th className="px-6 py-4 font-semibold text-slate-600">Order</th>
                            <th className="px-6 py-4 font-semibold text-slate-600">Status</th>
                            <th className="px-6 py-4 font-semibold text-slate-600 text-right">Actions</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                        {paginatedItems.length === 0 ? (
                            <tr>
                                <td colSpan={5} className="px-6 py-8 text-center text-slate-500">
                                    {search ? 'No sponsors match your search.' : 'No sponsors found. Add a sponsor to display on the app.'}
                                </td>
                            </tr>
                        ) : paginatedItems.map((sponsor) => (
                            <tr key={sponsor.id} className="hover:bg-slate-50 transition-colors">
                                <td className="px-6 py-4">
                                    <div className="flex items-center gap-4">
                                        <div className="h-20 w-40 rounded-lg bg-slate-100 flex items-center justify-center overflow-hidden border border-slate-200">
                                            {sponsor.imageUrl ? (
                                                <img src={sponsor.imageUrl} alt={sponsor.title} className="h-full w-full object-cover" />
                                            ) : (
                                                <ImageIcon className="h-6 w-6 text-slate-400" />
                                            )}
                                        </div>
                                        <div>
                                            <p className="font-medium text-slate-900">{sponsor.title}</p>
                                            <p className="text-xs text-slate-400 max-w-xs truncate">{sponsor.subtitle || sponsor.id}</p>
                                        </div>
                                    </div>
                                </td>
                                <td className="px-6 py-4">
                                    <div className="text-xs text-slate-500 truncate max-w-[150px]">
                                        {sponsor.targetUrl || 'No target'}
                                    </div>
                                </td>
                                <td className="px-6 py-4">
                                    <span className="px-2 py-1 bg-slate-100 rounded text-xs font-mono font-bold">
                                        #{sponsor.sortOrder ?? 0}
                                    </span>
                                </td>
                                <td className="px-6 py-4">
                                    <button
                                        type="button"
                                        title="Toggle active status"
                                        onClick={() => toggleActive(sponsor)}
                                        className={`inline-flex items-center px-3 py-1 rounded-full text-[10px] font-bold uppercase tracking-wider transition-colors ${sponsor.isActive ? 'bg-emerald-100 text-emerald-700 hover:bg-emerald-200' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'}`}
                                    >
                                        {sponsor.isActive ? 'Active' : 'Inactive'}
                                    </button>
                                </td>
                                <td className="px-6 py-4 text-right">
                                    <div className="flex items-center justify-end gap-2">
                                        <Link to={`/settings/sponsors/edit/${sponsor.id}`} className="p-2 text-slate-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors">
                                            <Edit2 className="h-5 w-5" />
                                        </Link>
                                        <button type="button" title="Delete sponsor" onClick={() => handleDelete(sponsor.id)} className="p-2 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors">
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
