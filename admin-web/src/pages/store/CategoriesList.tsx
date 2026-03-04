import { useEffect, useState } from 'react';
import { collection, getDocs, deleteDoc, doc } from 'firebase/firestore';
import { db } from '../../firebase/config';
import { Plus, Edit2, Trash2, Tag } from 'lucide-react';
import { Link } from 'react-router-dom';
import Pagination from '../../components/Common/Pagination';
import { usePagination } from '../../hooks/usePagination';

export interface Category {
    id: string;
    name: string;
    label?: string;
    description: string;
    imageUrl?: string;
}

export default function CategoriesList() {
    const [categories, setCategories] = useState<Category[]>([]);
    const [loading, setLoading] = useState(true);
    const { 
        currentPage, 
        totalPages, 
        paginatedItems, 
        handlePageChange, 
        totalItems, 
        itemsPerPage 
    } = usePagination(categories, 8);

    const fetchCategories = async () => {
        try {
            const querySnapshot = await getDocs(collection(db, 'categories'));
            const data: Category[] = [];
            querySnapshot.forEach((doc) => {
                const docData = doc.data();
                data.push({ 
                    id: doc.id, 
                    ...docData,
                    name: docData.label || docData.name || '' // Map label to name if necessary
                } as Category);
            });
            setCategories(data);
            setLoading(false);
        } catch (error) {
            console.error("Error fetching categories: ", error);
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchCategories();
    }, []);

    const handleDelete = async (id: string) => {
        if (window.confirm("Are you sure you want to delete this category?")) {
            try {
                await deleteDoc(doc(db, 'categories', id));
                setCategories(categories.filter(c => c.id !== id));
            } catch (error) {
                console.error("Error deleting category", error);
                alert("Failed to delete category.");
            }
        }
    };

    if (loading) return <div className="p-8 text-center text-slate-500">Loading categories...</div>;

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <h1 className="text-2xl font-bold text-slate-800">Product Categories</h1>
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
                        {categories.length === 0 ? (
                            <tr>
                                <td colSpan={3} className="px-6 py-8 text-center text-slate-500">
                                    No categories found. Add a new category to get started.
                                </td>
                            </tr>
                        ) : null}
                        {paginatedItems.map((category) => (
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
                                    {category.description}
                                </td>
                                <td className="px-6 py-4">
                                    <div className="flex items-center gap-2">
                                        <Link to={`/store/categories/edit/${category.id}`} className="p-2 text-slate-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors">
                                            <Edit2 className="h-5 w-5" />
                                        </Link>
                                        <button onClick={() => handleDelete(category.id)} className="p-2 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors">
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
