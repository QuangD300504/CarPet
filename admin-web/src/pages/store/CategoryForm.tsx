import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { doc, getDoc, addDoc, updateDoc, collection } from 'firebase/firestore';
import { db } from '../../firebase/config';
import { uploadToCloudinary } from '../../utils/cloudinary';
import { ArrowLeft, Save, Loader2, Image as ImageIcon } from 'lucide-react';
import type { Category } from './CategoriesList';

export default function CategoryForm() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const isEditing = id !== 'new' && id !== undefined;

    const [loading, setLoading] = useState(isEditing);
    const [saving, setSaving] = useState(false);
    const [category, setCategory] = useState<Partial<Category>>({
        name: '',
        label: '',
        description: '',
        imageUrl: ''
    });
    const [imageFile, setImageFile] = useState<File | null>(null);
    const [previewUrl, setPreviewUrl] = useState<string | null>(null);

    useEffect(() => {
        if (isEditing && id) {
            const fetchCategory = async () => {
                const docRef = doc(db, 'categories', id);
                const docSnap = await getDoc(docRef);
                if (docSnap.exists()) {
                    const docData = docSnap.data();
                    setCategory({
                        ...docData,
                        name: docData.name || docData.label || ''
                    });
                    setPreviewUrl(docData.imageUrl || null);
                } else {
                    alert("Category not found");
                    navigate('/store/categories');
                }
                setLoading(false);
            };
            fetchCategory();
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
            let photoUrl = category.imageUrl;

            if (imageFile) {
                photoUrl = await uploadToCloudinary(imageFile);
            }

            const now = Date.now();
            const categoryData = {
                ...category,
                label: category.name || category.label || '', // Ensure label is set
                imageUrl: photoUrl || '',
                updatedAt: now,
                ...(isEditing ? {} : { createdAt: now })
            };

            if (isEditing && id) {
                await updateDoc(doc(db, 'categories', id), categoryData);
            } else {
                await addDoc(collection(db, 'categories'), categoryData);
            }
            navigate('/store/categories');
        } catch (error) {
            console.error("Error saving category: ", error);
            alert("Error saving category.");
        } finally {
            setSaving(false);
        }
    };

    if (loading) return <div className="p-8 text-center text-slate-500">Loading form...</div>;

    return (
        <div className="max-w-xl mx-auto space-y-6">
            <div className="flex items-center justify-between">
                <div className="flex items-center gap-4">
                    <button onClick={() => navigate('/store/categories')} className="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-full transition-colors">
                        <ArrowLeft className="h-6 w-6" />
                    </button>
                    <h1 className="text-2xl font-bold text-slate-800">
                        {isEditing ? 'Edit Category' : 'Add New Category'}
                    </h1>
                </div>
                <button
                    onClick={handleSubmit}
                    disabled={saving}
                    className="flex items-center gap-2 bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50"
                >
                    {saving ? <Loader2 className="h-5 w-5 animate-spin" /> : <Save className="h-5 w-5" />}
                    Save Category
                </button>
            </div>

            <div className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
                <form className="p-6 space-y-6" onSubmit={handleSubmit}>
                    <div className="space-y-6">
                        <div className="space-y-2">
                            <label className="block text-sm font-medium text-slate-700">Category Icon/Image</label>
                            <label className="relative flex flex-col items-center justify-center w-32 h-32 border-2 border-slate-300 border-dashed rounded-xl cursor-pointer bg-slate-50 hover:bg-slate-100 overflow-hidden transition-colors mx-auto">
                                {previewUrl ? (
                                    <img src={previewUrl} alt="Preview" className="h-full w-full object-cover" />
                                ) : (
                                    <div className="flex flex-col items-center justify-center pt-5 pb-6 text-center">
                                        <ImageIcon className="w-8 h-8 mb-2 text-slate-400" />
                                        <p className="text-[10px] text-slate-500 line-clamp-2 px-2">Upload</p>
                                    </div>
                                )}
                                <input type="file" className="hidden" accept="image/*" onChange={handleImageChange} />
                            </label>
                        </div>

                        <div className="space-y-2">
                            <label className="block text-sm font-medium text-slate-700">Category Name</label>
                            <input
                                type="text"
                                required
                                value={category.name}
                                onChange={e => setCategory({...category, name: e.target.value})}
                                className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition-all"
                                placeholder="e.g. Food, Toys, Accessories"
                            />
                        </div>

                        <div className="space-y-2">
                            <label className="block text-sm font-medium text-slate-700">Description</label>
                            <textarea
                                rows={3}
                                value={category.description}
                                onChange={e => setCategory({...category, description: e.target.value})}
                                className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition-all"
                                placeholder="Describe this category..."
                            />
                        </div>
                    </div>
                </form>
            </div>
        </div>
    );
}
