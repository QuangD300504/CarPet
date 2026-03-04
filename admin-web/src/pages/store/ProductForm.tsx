import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { doc, getDoc, addDoc, updateDoc, collection, getDocs } from 'firebase/firestore';
import { ref, uploadBytes, getDownloadURL } from 'firebase/storage';
import { db, storage } from '../../firebase/config';
import { ArrowLeft, Save, Loader2, Image as ImageIcon } from 'lucide-react';
import type { Product } from './ProductsList';

export default function ProductForm() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const isEditing = id !== 'new' && id !== undefined;

    const [loading, setLoading] = useState(isEditing);
    const [saving, setSaving] = useState(false);
    const [product, setProduct] = useState<Partial<Product>>({
        name: '',
        description: '',
        price: 0,
        category: '',
        imageUrl: '',
        stock: 0
    });
    const [imageFile, setImageFile] = useState<File | null>(null);
    const [previewUrl, setPreviewUrl] = useState<string | null>(null);
    const [categories, setCategories] = useState<string[]>([]);
    const [isNewCategory, setIsNewCategory] = useState(false);
    const [newCategoryName, setNewCategoryName] = useState('');

    useEffect(() => {
        const fetchCategories = async () => {
            try {
                const querySnapshot = await getDocs(collection(db, 'categories'));
                const existingCategories: string[] = [];
                querySnapshot.forEach((doc: any) => {
                    const data = doc.data();
                    const catName = data.label || data.name || data.id;
                    if (catName) existingCategories.push(String(catName));
                });
                setCategories(existingCategories.sort());
            } catch (error) {
                console.error("Error fetching categories:", error);
            }
        };

        fetchCategories();

        if (isEditing && id) {
            const fetchProduct = async () => {
                const docRef = doc(db, 'products', id);
                const docSnap = await getDoc(docRef);
                if (docSnap.exists()) {
                    const data = docSnap.data() as Product;
                    setProduct(data);
                    setPreviewUrl(data.imageUrl);
                } else {
                    alert("Product not found");
                    navigate('/store');
                }
                setLoading(false);
            };
            fetchProduct();
        }
    }, [id, isEditing, navigate]);

    const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files[0]) {
            const file = e.target.files[0];
            setImageFile(file);
            setPreviewUrl(URL.createObjectURL(file));
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setSaving(true);
        try {
            let photoUrl = product.imageUrl;

            if (imageFile) {
                const fileRef = ref(storage, `products/${Date.now()}_${imageFile.name}`);
                await uploadBytes(fileRef, imageFile);
                photoUrl = await getDownloadURL(fileRef);
            }

            const finalCategory = isNewCategory ? newCategoryName : product.category;
            if (!finalCategory && !isEditing) {
                alert("Please select or enter a category");
                setSaving(false);
                return;
            }

            const now = Date.now();
            const productData = {
                ...product,
                category: finalCategory,
                imageUrl: photoUrl || '',
                updatedAt: now,
                ...(isEditing ? {} : { createdAt: now })
            };

            if (isEditing && id) {
                await updateDoc(doc(db, 'products', id), productData);
            } else {
                await addDoc(collection(db, 'products'), productData);
            }
            navigate('/store');
        } catch (error) {
            console.error("Error saving product: ", error);
            alert("Error saving product. Check console.");
        } finally {
            setSaving(false);
        }
    };

    if (loading) return <div className="p-8 text-center border">Loading form...</div>;

    return (
        <div className="max-w-3xl mx-auto space-y-6">
            <div className="flex items-center justify-between">
                <div className="flex items-center gap-4">
                    <button onClick={() => navigate('/store')} className="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-full transition-colors">
                        <ArrowLeft className="h-6 w-6" />
                    </button>
                    <h1 className="text-2xl font-bold text-slate-800">
                        {isEditing ? 'Edit Product' : 'Add New Product'}
                    </h1>
                </div>
                <button
                    onClick={handleSubmit}
                    disabled={saving}
                    className="flex items-center gap-2 bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50"
                >
                    {saving ? <Loader2 className="h-5 w-5 animate-spin" /> : <Save className="h-5 w-5" />}
                    Save Product
                </button>
            </div>

            <div className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
                <form className="p-6 space-y-6" onSubmit={handleSubmit}>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        {/* Image Upload Area */}
                        <div className="col-span-1 md:col-span-2 space-y-2">
                            <label className="block text-sm font-medium text-slate-700">Product Image</label>
                            <label className="relative flex flex-col items-center justify-center w-full h-64 border-2 border-slate-300 border-dashed rounded-xl cursor-pointer bg-slate-50 hover:bg-slate-100 overflow-hidden transition-colors">
                                {previewUrl ? (
                                    <img src={previewUrl} alt="Preview" className="h-full w-full object-contain" />
                                ) : (
                                    <div className="flex flex-col items-center justify-center pt-5 pb-6">
                                        <ImageIcon className="w-10 h-10 mb-3 text-slate-400" />
                                        <p className="mb-2 text-sm text-slate-500"><span className="font-semibold">Click to upload</span> or drag and drop</p>
                                        <p className="text-xs text-slate-500">PNG, JPG or WEBP (MAX. 2MB)</p>
                                    </div>
                                )}
                                <input type="file" className="hidden" accept="image/*" onChange={handleImageChange} />
                            </label>
                        </div>

                        {/* Text Fields */}
                        <div className="space-y-2">
                            <label className="block text-sm font-medium text-slate-700">Product Name</label>
                            <input
                                type="text"
                                required
                                value={product.name}
                                onChange={e => setProduct({...product, name: e.target.value})}
                                className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all"
                                placeholder="e.g. Premium Dog Food"
                            />
                        </div>

                        <div className="space-y-2">
                            <label className="block text-sm font-medium text-slate-700">Category</label>
                            {!isNewCategory ? (
                                <div className="flex gap-2">
                                    <select
                                        required
                                        value={product.category}
                                        onChange={e => {
                                            if (e.target.value === 'NEW') {
                                                setIsNewCategory(true);
                                            } else {
                                                setProduct({...product, category: e.target.value});
                                            }
                                        }}
                                        className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all bg-white"
                                    >
                                        <option value="">Select a category</option>
                                        {categories.map(cat => (
                                            <option key={cat} value={cat}>{cat}</option>
                                        ))}
                                        <option value="NEW" className="font-bold text-blue-600">+ Other (Add New...)</option>
                                    </select>
                                </div>
                            ) : (
                                <div className="flex flex-col gap-2">
                                    <input
                                        type="text"
                                        required
                                        autoFocus
                                        value={newCategoryName}
                                        onChange={e => setNewCategoryName(e.target.value)}
                                        className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all"
                                        placeholder="Enter new category name..."
                                    />
                                    <button 
                                        type="button"
                                        onClick={() => setIsNewCategory(false)}
                                        className="text-xs text-blue-600 hover:text-blue-800 self-start p-1"
                                    >
                                        ← Back to existing list
                                    </button>
                                </div>
                            )}
                        </div>

                        <div className="space-y-2">
                            <label className="block text-sm font-medium text-slate-700">Regular Price ($)</label>
                            <input
                                type="number"
                                required
                                min="0"
                                step="0.01"
                                value={product.price}
                                onChange={e => setProduct({...product, price: parseFloat(e.target.value)})}
                                className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all"
                            />
                        </div>

                        <div className="space-y-2">
                            <label className="block text-sm font-medium text-slate-700">Sale Price ($) - Optional</label>
                            <input
                                type="number"
                                min="0"
                                step="0.01"
                                value={product.salePrice || ''}
                                onChange={e => setProduct({...product, salePrice: e.target.value ? parseFloat(e.target.value) : undefined})}
                                className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all"
                            />
                        </div>

                        <div className="space-y-2">
                            <label className="block text-sm font-medium text-slate-700">Stock Quantity</label>
                            <input
                                type="number"
                                required
                                min="0"
                                value={product.stock}
                                onChange={e => setProduct({...product, stock: parseInt(e.target.value)})}
                                className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all"
                            />
                        </div>

                        <div className="col-span-1 md:col-span-2 space-y-2">
                            <label className="block text-sm font-medium text-slate-700">Description</label>
                            <textarea
                                required
                                rows={4}
                                value={product.description}
                                onChange={e => setProduct({...product, description: e.target.value})}
                                className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-all"
                                placeholder="Write a detailed product description..."
                            />
                        </div>
                    </div>
                </form>
            </div>
        </div>
    );
}
