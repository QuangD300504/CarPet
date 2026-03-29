import { useEffect, useState, useMemo } from 'react';
import { collection, onSnapshot, deleteDoc, doc, orderBy, query } from 'firebase/firestore';
import { db } from '../../firebase/config';
import { Edit2, Trash2, Image as ImageIcon, Search, X } from 'lucide-react';
import { Link } from 'react-router-dom';
import Pagination from '../../components/Common/Pagination';
import { usePagination } from '../../hooks/usePagination';
import { formatVND } from '../../utils/format';


export interface Product {
    id: string;
    name: string;
    description: string;
    price: number;
    salePrice?: number;
    category: string;
    imageUrl: string;
    stock: number;
    createdAt?: number;
    updatedAt?: number;
}

export default function ProductsList() {
    const [products, setProducts] = useState<Product[]>([]);
    const [loading, setLoading] = useState(true);
    const [search, setSearch] = useState('');
    const [categoryFilter, setCategoryFilter] = useState('');
    const {
        currentPage,
        totalPages,
        paginatedItems,
        handlePageChange,
        totalItems,
        itemsPerPage
    } = usePagination(products, 8);

    useEffect(() => {
        const q = query(collection(db, 'products'), orderBy('createdAt', 'desc'));
        const unsub = onSnapshot(q,
            snap => {
                setProducts(snap.docs.map(d => ({ id: d.id, ...d.data() } as Product)));
                setLoading(false);
            },
            err => { console.error('Products listener error:', err); setLoading(false); }
        );
        return () => unsub();
    }, []);

    const categories = useMemo(() =>
        [...new Set(products.map(p => p.category).filter(Boolean))].sort(),
        [products]
    );

    const filtered = useMemo(() => {
        const q = search.trim().toLowerCase();
        return products.filter(p => {
            const matchSearch = !q || p.name.toLowerCase().includes(q) || p.category.toLowerCase().includes(q);
            const matchCat    = !categoryFilter || p.category === categoryFilter;
            return matchSearch && matchCat;
        });
    }, [products, search, categoryFilter]);

    const handleDelete = async (id: string) => {
        if (window.confirm("Are you sure you want to delete this product?")) {
            try {
                await deleteDoc(doc(db, 'products', id));
            } catch (error) {
                console.error("Error deleting product", error);
            }
        }
    };

    if (loading) return <div className="p-8 text-center">Loading products...</div>;

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <h1 className="text-2xl font-bold text-slate-800">Products Management</h1>
                <div className="flex items-center gap-3">
                    <span className="text-sm text-slate-500">{filtered.length} products</span>
                    <Link
                        to="/store/products/new"
                        className="inline-flex items-center gap-2 px-4 py-2 bg-primary-600 text-white text-sm font-semibold rounded-lg hover:bg-primary-700 transition-colors shadow-sm"
                    >
                        <span className="text-lg leading-none">+</span>
                        Add Product
                    </Link>
                </div>
            </div>

            <div className="flex flex-wrap gap-3">
                <div className="relative flex-1 min-w-[200px]">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
                    <input
                        type="text"
                        placeholder="Search by name or category..."
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
                {categories.length > 0 && (
                    <select
                        title="Filter by category"
                        value={categoryFilter}
                        onChange={e => { setCategoryFilter(e.target.value); handlePageChange(1); }}
                        className="px-3 py-2 border border-slate-200 rounded-lg text-sm bg-white focus:ring-2 focus:ring-primary-500 outline-none"
                    >
                        <option value="">All Categories</option>
                        {categories.map(c => <option key={c} value={c}>{c}</option>)}
                    </select>
                )}
            </div>

            <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
                <table className="w-full text-left border-collapse">
                    <thead>
                        <tr className="bg-slate-50 border-b border-slate-200">
                            <th className="px-6 py-4 font-semibold text-slate-600">Product</th>
                            <th className="px-6 py-4 font-semibold text-slate-600">Price</th>
                            <th className="px-6 py-4 font-semibold text-slate-600">Stock</th>
                            <th className="px-6 py-4 font-semibold text-slate-600">Actions</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                        {paginatedItems.length === 0 ? (
                            <tr>
                                <td colSpan={4} className="px-6 py-8 text-center text-slate-500">
                                    {search || categoryFilter ? 'No products match your filters.' : 'No products found. Add a new product to get started.'}
                                </td>
                            </tr>
                        ) : paginatedItems.map((product) => (
                            <tr key={product.id} className="hover:bg-slate-50 transition-colors">
                                <td className="px-6 py-4">
                                    <div className="flex items-center gap-4">
                                        <div className="h-12 w-12 rounded-md bg-slate-100 flex items-center justify-center overflow-hidden border border-slate-200">
                                            {product.imageUrl ? (
                                                <img src={product.imageUrl} alt={product.name} className="h-full w-full object-cover" />
                                            ) : (
                                                <ImageIcon className="h-6 w-6 text-slate-400" />
                                            )}
                                        </div>
                                        <div>
                                            <p className="font-medium text-slate-900">{product.name}</p>
                                            <p className="text-sm text-slate-500 line-clamp-1 max-w-xs">{product.category}</p>
                                        </div>
                                    </div>
                                </td>
                                <td className="px-6 py-4">
                                    <div className="font-medium text-slate-900">{formatVND(product.price)}</div>
                                    {product.salePrice && <div className="text-sm text-emerald-600">Sale: {formatVND(product.salePrice)}</div>}
                                </td>
                                <td className="px-6 py-4">
                                    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                                        product.stock > 10 ? 'bg-green-100 text-green-800' :
                                        product.stock > 0 ? 'bg-yellow-100 text-yellow-800' : 'bg-red-100 text-red-800'
                                    }`}>
                                        {product.stock} in stock
                                    </span>
                                </td>
                                <td className="px-6 py-4">
                                    <div className="flex items-center gap-2">
                                        <Link to={`/store/products/edit/${product.id}`} className="p-2 text-slate-400 hover:text-primary-600 hover:bg-primary-50 rounded-lg transition-colors">
                                            <Edit2 className="h-5 w-5" />
                                        </Link>
                                        <button type="button" title="Delete product" onClick={() => handleDelete(product.id)} className="p-2 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors">
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