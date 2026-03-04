import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { doc, getDoc, addDoc, updateDoc, collection } from 'firebase/firestore';
import { ref, uploadBytes, getDownloadURL } from 'firebase/storage';
import { db, storage } from '../../firebase/config';
import { ArrowLeft, Save, Loader2, Image as ImageIcon } from 'lucide-react';
import type { Sponsor } from './SponsorsList';

export default function SponsorForm() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const isEditing = id !== 'new' && id !== undefined;

    const [loading, setLoading] = useState(isEditing);
    const [saving, setSaving] = useState(false);
    const [sponsor, setSponsor] = useState<Partial<Sponsor>>({
        title: '',
        subtitle: '',
        imageUrl: '',
        targetUrl: '',
        isActive: true,
        sortOrder: 0
    });
    
    const [imageFile, setImageFile] = useState<File | null>(null);
    const [previewUrl, setPreviewUrl] = useState<string | null>(null);

    useEffect(() => {
        if (isEditing && id) {
            const fetchSponsor = async () => {
                const docRef = doc(db, 'banners', id);
                const docSnap = await getDoc(docRef);
                if (docSnap.exists()) {
                    setSponsor(docSnap.data() as Sponsor);
                    setPreviewUrl(docSnap.data().imageUrl);
                } else {
                    alert("Sponsor not found");
                    navigate('/settings/sponsors');
                }
                setLoading(false);
            };
            fetchSponsor();
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
            let photoUrl = sponsor.imageUrl;

            if (imageFile) {
                const fileRef = ref(storage, `banners/${Date.now()}_${imageFile.name}`);
                await uploadBytes(fileRef, imageFile);
                photoUrl = await getDownloadURL(fileRef);
            }

            const now = Date.now();
            const sponsorData = {
                ...sponsor,
                imageUrl: photoUrl || '',
                updatedAt: now,
                ...(isEditing ? {} : { createdAt: now })
            };

            if (isEditing && id) {
                await updateDoc(doc(db, 'banners', id), sponsorData);
            } else {
                await addDoc(collection(db, 'banners'), sponsorData);
            }
            navigate('/settings/sponsors');
        } catch (error) {
            console.error("Error saving sponsor: ", error);
            alert("Error saving sponsor.");
        } finally {
            setSaving(false);
        }
    };

    if (loading) return <div className="p-8 text-center text-slate-500">Loading form...</div>;

    return (
        <div className="max-w-3xl mx-auto space-y-6">
            <div className="flex items-center justify-between">
                <div className="flex items-center gap-4">
                    <button onClick={() => navigate('/settings/sponsors')} className="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-full transition-colors">
                        <ArrowLeft className="h-6 w-6" />
                    </button>
                    <h1 className="text-2xl font-bold text-slate-800">
                        {isEditing ? 'Edit Sponsor' : 'Add New Sponsor'}
                    </h1>
                </div>
                <button
                    onClick={handleSubmit}
                    disabled={saving}
                    className="flex items-center gap-2 bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50"
                >
                    {saving ? <Loader2 className="h-5 w-5 animate-spin" /> : <Save className="h-5 w-5" />}
                    Save Sponsor
                </button>
            </div>

            <div className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
                <form className="p-6 space-y-6" onSubmit={handleSubmit}>
                    <div className="grid grid-cols-1 gap-6">
                        
                        <div className="space-y-2">
                            <label className="block text-sm font-medium text-slate-700">Sponsor Image (Wide aspect ratio recommended)</label>
                            <label className="relative flex flex-col items-center justify-center w-full h-56 border-2 border-slate-300 border-dashed rounded-xl cursor-pointer bg-slate-50 hover:bg-slate-100 overflow-hidden transition-colors">
                                {previewUrl ? (
                                    <img src={previewUrl} alt="Preview" className="h-full w-full object-cover" />
                                ) : (
                                    <div className="flex flex-col items-center justify-center pt-5 pb-6">
                                        <ImageIcon className="w-10 h-10 mb-3 text-slate-400" />
                                        <p className="mb-2 text-sm text-slate-500"><span className="font-semibold">Click to upload</span> or drag and drop</p>
                                    </div>
                                )}
                                <input type="file" className="hidden" accept="image/*" onChange={handleImageChange} />
                            </label>
                        </div>

                        <div className="space-y-4">
                            <div className="space-y-2">
                                <label className="block text-sm font-medium text-slate-700">Display Title</label>
                                <input
                                    type="text"
                                    required
                                    value={sponsor.title}
                                    onChange={e => setSponsor({...sponsor, title: e.target.value})}
                                    className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition-all"
                                    placeholder="e.g. Summer Sale 2024"
                                />
                            </div>

                            <div className="space-y-2">
                                <label className="block text-sm font-medium text-slate-700">Subtitle / Promotion Text</label>
                                <input
                                    type="text"
                                    required
                                    value={sponsor.subtitle}
                                    onChange={e => setSponsor({...sponsor, subtitle: e.target.value})}
                                    className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition-all"
                                    placeholder="e.g. Up to 50% off on all items"
                                />
                            </div>

                            <div className="space-y-2">
                                <label className="block text-sm font-medium text-slate-700">Target URL (Deep Link or Web)</label>
                                <input
                                    type="text"
                                    value={sponsor.targetUrl}
                                    onChange={e => setSponsor({...sponsor, targetUrl: e.target.value})}
                                    className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition-all"
                                    placeholder="e.g. vetbook://store/category/toys"
                                />
                            </div>

                            <div className="grid grid-cols-1 md:grid-cols-2 gap-6 pt-2">
                                <div className="space-y-2">
                                    <label className="block text-sm font-medium text-slate-700">Sort Order (Lower appears first)</label>
                                    <input
                                        type="number"
                                        required
                                        value={sponsor.sortOrder}
                                        onChange={e => setSponsor({...sponsor, sortOrder: parseInt(e.target.value) || 0})}
                                        className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition-all"
                                    />
                                </div>
                                <div className="flex items-center gap-3 md:pt-8">
                                    <div className="flex items-center h-5">
                                        <input
                                            id="isActive"
                                            type="checkbox"
                                            checked={sponsor.isActive}
                                            onChange={(e) => setSponsor({...sponsor, isActive: e.target.checked})}
                                            className="w-5 h-5 text-blue-600 rounded border-slate-300 focus:ring-blue-500 transition-all cursor-pointer"
                                        />
                                    </div>
                                    <label htmlFor="isActive" className="text-sm font-semibold text-slate-700 cursor-pointer">
                                        Visible on Homepage
                                    </label>
                                </div>
                            </div>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    );
}
