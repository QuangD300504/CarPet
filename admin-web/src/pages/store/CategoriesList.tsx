import { useEffect, useState, useMemo } from 'react';
import { collection, onSnapshot, deleteDoc, doc, orderBy, query } from 'firebase/firestore';
import { db } from '../../firebase/config';
import { Plus, Edit2, Trash2, Tag, Search, X } from 'lucide-react';
import { Link } from 'react-router-dom';
import Pagination from '../../components/Common/Pagination';
import { usePagination } from '../../hooks/usePagination';

export interface Category {
    id: string;
    name: string;
    label?: string;
    description: string;
    imageUrl?: string;
    createdAt?: number;
}

export default function CategoriesList() {
    const [categories, setCategories] = useState<Category[]>([]);
    const [loading, setLoading] = useState(true);
    const [search, setSearch] = useState('');
    const {
        currentPage,
        totalPages,
        paginatedItems,
        handlePageChange,
        totalItems,
        itemsPerPage
    } = usePagination(categories, 8);

    useEffect(() => {
        const q = query(collection(db, 'categories'), orderBy('createdAt', 'desc'));
        const unsub = onSnapshot(q,
            snap => {
                const data: Category[] = snap.docs.map(d => {
                    const docData = d.data();
                    return { id: d.id, ...docData, name: docData.label || docData.name || '' } as Category;
                });
                setCategories(data);
                setLoading(false);
            },
            err => { console.error('Categories listener error:', err); setLoading(false); }
        );
        return () => unsub();
    }, []);

    const filtered = useMemo(() => {
        const q = search.trim().toLowerCase();
        return categories.filter(c =>
            !q || c.name.toLowerCase().includes(q) || c.description?.toLowerCase().includes(q)
        );
    }, [categories, search]);

    const handleDelete = async (id: string) => {
        if (window.confirm("Are you sure you want to delete this category?")) {
            try {
                await deleteDoc(doc(db, 'categories', id));
            } catch (error) {
                console.error("Error deleting category", error);
            }
        }
    };

    if (loading) return <div className="p-8 text-center text-slate-500">Loading categories...</div>;

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <h1 className="text-2xl font-bold text-slate-800">Product Categories</h1>
                <span className="text-sm text-slate-500">{filtered.length} categories</span>
            </div>

            <div className="flex flex-wrap gap-3">
                <div className="relative flex-1 min-w-[200px]">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
                    <input
                        type="text"
                        placeholder="Search categories..."
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
                <Link to="/store/categories/new" className="flex items-center gap-2 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors">
                    <Plus className="h-5 w-5" />
                    Add Category
                </Link>
            </div>

            <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
                <table className="w-full text-left border-collapse">
                    <thead>
                        <tr className="bg-slate-50 border-b border-slate-200">
                            <th className="px-6 py-4 font-semibold text-slate-600">Category</th>
                            <th className="px-6 py-4 font-semibold text-slate-600">Description</th>
                            <th className="px-6 py-4 font-semibold text-slate-600">Actions</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                        {paginatedItems.length === 0 ? (
                            <tr>
                                <td colSpan={3} className="px-6 py-8 text-center text-slate-500">
                                    {search ? 'No categories match your search.' : 'No categories found. Add a new category to get started.'}
                                </td>
                            </tr>
                        ) : paginatedItems.map((category) => (
                            <tr key={category.id} className="hover:bg-slate-50 transition-colors">
                                <td className="px-6 py-4">
                                    <div className="flex items-center gap-4">
                                        <div className="h-10 w-10 rounded-lg bg-blue-50 flex items-center justify-center border border-blue-100 text-blue-600">
                                            {category.imageUrl ? (
                                                <img src={category.imageUrl} alt={category.name} className="h-full w-full object-cover rounded-lg" />
                                            ) : (
                                                <Tag className="h-5 w-5" />
                                            )}
                                        </div>
                                        <div className="font-medium text-slate-900">{category.name}</div>
                                    </div>
                                </td>
                                <td className="px-6 py-4 text-slate-500 max-w-md line-clamp-1">
                                    {category.description || <span className="italic text-slate-300">No description</span>}
                                </td>
                                <td className="px-6 py-4">
                                    <div className="flex items-center gap-2">
                                        <Link to={`/store/categories/edit/${category.id}`} className="p-2 text-slate-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors">
                                            <Edit2 className="h-5 w-5" />
                                        </Link>
                                        <button type="button" title="Delete category" onClick={() => handleDelete(category.id)} className="p-2 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors">
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
