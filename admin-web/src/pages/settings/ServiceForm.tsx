import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { doc, getDoc, addDoc, updateDoc, collection } from 'firebase/firestore';
import { db } from '../../firebase/config';
import { uploadToCloudinary } from '../../utils/cloudinary';
import { ArrowLeft, Save, Loader2, Image as ImageIcon, Star } from 'lucide-react';
import { useToast } from '../../contexts/ToastContext';

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

export default function ServiceForm() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const isEditing = id !== 'new' && id !== undefined;

    const [loading, setLoading] = useState(isEditing);
    const { toast } = useToast();
    const [saving, setSaving] = useState(false);
    const [service, setService] = useState<Partial<Service>>({
        title: '',
        shortDescription: '',
        about: '',
        iconUrl: '',
        rating: 0,
        reviewCount: 0,
        isActive: true,
    });

    const [iconFile, setIconFile] = useState<File | null>(null);
    const [previewUrl, setPreviewUrl] = useState<string | null>(null);

    useEffect(() => {
        if (isEditing && id) {
            const fetchService = async () => {
                const docRef = doc(db, 'services', id);
                const docSnap = await getDoc(docRef);
                if (docSnap.exists()) {
                    setService(docSnap.data() as Service);
                    setPreviewUrl(docSnap.data().iconUrl || null);
                } else {
                    toast('Service not found', 'error');
                    navigate('/settings/services');
                }
                setLoading(false);
            };
            fetchService();
        }
    }, [id, isEditing, navigate]);

    const handleIconChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files[0]) {
            const file = e.target.files[0];
            setIconFile(file);
            setPreviewUrl(URL.createObjectURL(file));
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!service.title?.trim()) {
            toast('Title is required', 'error');
            return;
        }
        setSaving(true);
        try {
            let iconUrl = service.iconUrl || '';
            if (iconFile) {
                iconUrl = await uploadToCloudinary(iconFile);
            }
            const now = Date.now();
            const data = {
                ...service,
                iconUrl,
                updatedAt: now,
                ...(isEditing ? {} : { createdAt: now }),
            };
            if (isEditing && id) {
                await updateDoc(doc(db, 'services', id), data);
            } else {
                await addDoc(collection(db, 'services'), data);
            }
            navigate('/settings/services');
        } catch (error) {
            console.error('Error saving service:', error);
            toast('Failed to save service.', 'error');
        } finally {
            setSaving(false);
        }
    };

    if (loading) return <div className="p-8 text-center text-slate-500">Loading form...</div>;

    return (
        <div className="max-w-3xl mx-auto space-y-6">
            <div className="flex items-center justify-between">
                <div className="flex items-center gap-4">
                    <button type="button" title="Go back" onClick={() => navigate('/settings/services')} className="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-full transition-colors">
                        <ArrowLeft className="h-6 w-6" />
                    </button>
                    <h1 className="text-2xl font-bold text-slate-800">
                        {isEditing ? 'Edit Service' : 'Add New Service'}
                    </h1>
                </div>
                <button
                    type="button"
                    onClick={handleSubmit}
                    disabled={saving}
                    className="flex items-center gap-2 bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50"
                >
                    {saving ? <Loader2 className="h-5 w-5 animate-spin" /> : <Save className="h-5 w-5" />}
                    Save Service
                </button>
            </div>

            <div className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
                <form className="p-6 space-y-6" onSubmit={handleSubmit}>
                    {/* Icon Upload */}
                    <div className="space-y-2">
                        <label className="block text-sm font-medium text-slate-700">Service Icon</label>
                        <div className="flex items-center gap-4">
                            <label className="relative flex flex-col items-center justify-center w-24 h-24 border-2 border-slate-300 border-dashed rounded-xl cursor-pointer bg-slate-50 hover:bg-slate-100 overflow-hidden transition-colors shrink-0">
                                {previewUrl ? (
                                    <img src={previewUrl} alt="Icon preview" className="h-full w-full object-cover" />
                                ) : (
                                    <div className="flex flex-col items-center justify-center">
                                        <ImageIcon className="w-8 h-8 text-slate-400 mb-1" />
                                        <span className="text-xs text-slate-400">Upload</span>
                                    </div>
                                )}
                                <input type="file" title="Upload service icon" className="hidden" accept="image/*" onChange={handleIconChange} />
                            </label>
                            <div>
                                <p className="text-sm text-slate-500">Recommended: square icon, min 64×64px</p>
                                <p className="text-xs text-slate-400 mt-1">PNG, JPG or WEBP (MAX 2MB)</p>
                            </div>
                        </div>
                    </div>

                    {/* Title */}
                    <div className="space-y-2">
                        <label className="block text-sm font-medium text-slate-700">Service Title *</label>
                        <input
                            type="text"
                            required
                            value={service.title}
                            onChange={e => setService({ ...service, title: e.target.value })}
                            className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition-all"
                            placeholder="e.g. Vaccination, Grooming, Surgery"
                        />
                    </div>

                    {/* Short Description */}
                    <div className="space-y-2">
                        <label className="block text-sm font-medium text-slate-700">Short Description</label>
                        <input
                            type="text"
                            value={service.shortDescription}
                            onChange={e => setService({ ...service, shortDescription: e.target.value })}
                            className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition-all"
                            placeholder="Brief tagline shown in cards"
                        />
                    </div>

                    {/* About */}
                    <div className="space-y-2">
                        <label className="block text-sm font-medium text-slate-700">Full Description</label>
                        <textarea
                            rows={4}
                            value={service.about}
                            onChange={e => setService({ ...service, about: e.target.value })}
                            className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition-all resize-none"
                            placeholder="Detailed service description shown on the service detail screen..."
                        />
                    </div>

                    {/* Rating + Review Count */}
                    <div className="grid grid-cols-2 gap-6">
                        <div className="space-y-2">
                            <label className="block text-sm font-medium text-slate-700">
                                <span className="flex items-center gap-1"><Star className="h-4 w-4 text-yellow-500" /> Rating (0–5)</span>
                            </label>
                            <input
                                type="number"
                                min="0"
                                max="5"
                                step="0.1"
                                value={service.rating}
                                onChange={e => setService({ ...service, rating: parseFloat(e.target.value) || 0 })}
                                className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition-all"
                            />
                        </div>
                        <div className="space-y-2">
                            <label className="block text-sm font-medium text-slate-700">Review Count</label>
                            <input
                                type="number"
                                min="0"
                                value={service.reviewCount}
                                onChange={e => setService({ ...service, reviewCount: parseInt(e.target.value) || 0 })}
                                className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition-all"
                            />
                        </div>
                    </div>

                    {/* Active Toggle */}
                    <div className="flex items-center gap-3">
                        <div className="flex items-center h-5">
                            <input
                                id="isActive"
                                type="checkbox"
                                checked={service.isActive}
                                onChange={(e) => setService({ ...service, isActive: e.target.checked })}
                                className="w-5 h-5 text-blue-600 rounded border-slate-300 focus:ring-blue-500 cursor-pointer"
                            />
                        </div>
                        <label htmlFor="isActive" className="text-sm font-semibold text-slate-700 cursor-pointer">
                            Active — show this service in the app
                        </label>
                    </div>
                </form>
            </div>
        </div>
    );
}
