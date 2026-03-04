import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { doc, getDoc, addDoc, updateDoc, collection, getDocs } from 'firebase/firestore';
import { ref, uploadBytes, getDownloadURL } from 'firebase/storage';
import { db, storage } from '../../firebase/config';
import { ArrowLeft, Save, Loader2, Image as ImageIcon } from 'lucide-react';
import type { Vet } from './VetsList';

export default function VetForm() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const isEditing = id !== 'new' && id !== undefined;

    const [loading, setLoading] = useState(isEditing);
    const [saving, setSaving] = useState(false);
    const [vet, setVet] = useState<Partial<Vet>>({
        name: '',
        specialty: '',
        bio: '',
        experience: '',
        imageUrl: '',
        clinicId: '',
        rating: 5.0,
        reviewsCount: 0,
        initials: '',
        email: '',
        phone: '',
        isActive: true
    });
    
    const [imageFile, setImageFile] = useState<File | null>(null);
    const [previewUrl, setPreviewUrl] = useState<string | null>(null);
    const [specialties, setSpecialties] = useState<string[]>([]);
    const [isNewSpecialty, setIsNewSpecialty] = useState(false);
    const [newSpecialtyName, setNewSpecialtyName] = useState('');
    const [clinics, setClinics] = useState<string[]>([]);
    const [isNewClinic, setIsNewClinic] = useState(false);
    const [newClinicName, setNewClinicName] = useState('');

    useEffect(() => {
        const fetchMetaData = async () => {
            try {
                const querySnapshot = await getDocs(collection(db, 'veterinarians'));
                const existingSpecialties = new Set<string>();
                const existingClinics = new Set<string>();
                querySnapshot.forEach((doc: any) => {
                    const data = doc.data();
                    if (data.specialty) existingSpecialties.add(data.specialty);
                    if (data.clinicRef) existingClinics.add(data.clinicRef);
                });
                setSpecialties(Array.from(existingSpecialties).sort());
                setClinics(Array.from(existingClinics).sort());
            } catch (error) {
                console.error("Error fetching metadata:", error);
            }
        };

        fetchMetaData();

        if (isEditing && id) {
            const fetchVet = async () => {
                const docRef = doc(db, 'veterinarians', id);
                const docSnap = await getDoc(docRef);
                if (docSnap.exists()) {
                    setVet(docSnap.data() as Vet);
                    setPreviewUrl(docSnap.data().imageUrl);
                } else {
                    alert("Veterinarian not found");
                    navigate('/vets/list');
                }
                setLoading(false);
            };
            fetchVet();
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
            let photoUrl = vet.imageUrl;

            if (imageFile) {
                const fileRef = ref(storage, `veterinarians/${Date.now()}_${imageFile.name}`);
                await uploadBytes(fileRef, imageFile);
                photoUrl = await getDownloadURL(fileRef);
            }

            const finalSpecialty = isNewSpecialty ? newSpecialtyName : vet.specialty;
            const finalClinic = isNewClinic ? newClinicName : vet.clinicId;
            
            // Generate initials if not provided
            const initials = vet.initials || (vet.name ? vet.name.split(' ').map(n => n[0]).join('').toUpperCase() : '');

            const now = Date.now();
            const vetData = {
                ...vet,
                specialty: finalSpecialty,
                clinicId: finalClinic,
                initials: initials,
                imageUrl: photoUrl || '',
                updatedAt: now,
                ...(isEditing ? {} : { createdAt: now })
            };

            if (isEditing && id) {
                await updateDoc(doc(db, 'veterinarians', id), vetData);
            } else {
                await addDoc(collection(db, 'veterinarians'), vetData);
            }
            navigate('/vets/list');
        } catch (error) {
            console.error("Error saving vet: ", error);
            alert("Error saving veterinarian. Check console.");
        } finally {
            setSaving(false);
        }
    };

    if (loading) return <div className="p-8 text-center text-slate-500">Loading form...</div>;

    return (
        <div className="max-w-3xl mx-auto space-y-6">
            <div className="flex items-center justify-between">
                <div className="flex items-center gap-4">
                    <button onClick={() => navigate('/vets/list')} className="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-full transition-colors">
                        <ArrowLeft className="h-6 w-6" />
                    </button>
                    <h1 className="text-2xl font-bold text-slate-800">
                        {isEditing ? 'Edit Veterinarian' : 'Add New Veterinarian'}
                    </h1>
                </div>
                <button
                    onClick={handleSubmit}
                    disabled={saving}
                    className="flex items-center gap-2 bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50"
                >
                    {saving ? <Loader2 className="h-5 w-5 animate-spin" /> : <Save className="h-5 w-5" />}
                    Save Veterinarian
                </button>
            </div>

            <div className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
                <form className="p-6 space-y-6" onSubmit={handleSubmit}>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        
                        <div className="col-span-1 md:col-span-2 space-y-2">
                            <label className="block text-sm font-medium text-slate-700">Profile Image</label>
                            <label className="relative flex flex-col items-center justify-center w-full h-48 border-2 border-slate-300 border-dashed rounded-xl cursor-pointer bg-slate-50 hover:bg-slate-100 overflow-hidden transition-colors">
                                {previewUrl ? (
                                    <img src={previewUrl} alt="Preview" className="h-full w-full object-contain" />
                                ) : (
                                    <div className="flex flex-col items-center justify-center pt-5 pb-6">
                                        <ImageIcon className="w-10 h-10 mb-3 text-slate-400" />
                                        <p className="mb-2 text-sm text-slate-500"><span className="font-semibold">Click to upload</span> or drag and drop</p>
                                    </div>
                                )}
                                <input type="file" className="hidden" accept="image/*" onChange={handleImageChange} />
                            </label>
                        </div>

                        <div className="space-y-2">
                            <label className="block text-sm font-medium text-slate-700">Full Name</label>
                            <input
                                type="text"
                                required
                                value={vet.name}
                                onChange={e => setVet({...vet, name: e.target.value})}
                                className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition-all"
                                placeholder="Dr. John Doe"
                            />
                        </div>

                        <div className="space-y-2">
                            <label className="block text-sm font-medium text-slate-700">Specialty</label>
                            {!isNewSpecialty ? (
                                <select
                                    required
                                    value={vet.specialty}
                                    onChange={e => {
                                        if (e.target.value === 'NEW') {
                                            setIsNewSpecialty(true);
                                        } else {
                                            setVet({...vet, specialty: e.target.value});
                                        }
                                    }}
                                    className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition-all bg-white text-slate-900"
                                >
                                    <option value="">Select a specialty</option>
                                    {specialties.map(spec => (
                                        <option key={spec} value={spec}>{spec}</option>
                                    ))}
                                    <option value="NEW" className="font-bold text-blue-600">+ Other (Add New...)</option>
                                </select>
                            ) : (
                                <div className="flex flex-col gap-2">
                                    <input
                                        type="text"
                                        required
                                        autoFocus
                                        value={newSpecialtyName}
                                        onChange={e => setNewSpecialtyName(e.target.value)}
                                        className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition-all"
                                        placeholder="Enter new specialty..."
                                    />
                                    <button 
                                        type="button"
                                        onClick={() => setIsNewSpecialty(false)}
                                        className="text-xs text-blue-600 hover:text-blue-800 self-start p-1"
                                    >
                                        ← Back to existing list
                                    </button>
                                </div>
                            )}
                        </div>

                        <div className="space-y-2">
                            <label className="block text-sm font-medium text-slate-700">Experience (Years/Summary)</label>
                            <input
                                type="text"
                                required
                                value={vet.experience}
                                onChange={e => setVet({...vet, experience: e.target.value})}
                                className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition-all"
                                placeholder="e.g. 5 years"
                            />
                        </div>

                        <div className="space-y-2">
                            <label className="block text-sm font-medium text-slate-700">Email Address</label>
                            <input
                                type="email"
                                required
                                value={vet.email}
                                onChange={e => setVet({...vet, email: e.target.value})}
                                className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition-all"
                                placeholder="vet@example.com"
                            />
                        </div>

                        <div className="space-y-2">
                            <label className="block text-sm font-medium text-slate-700">Phone Number</label>
                            <input
                                type="tel"
                                required
                                value={vet.phone}
                                onChange={e => setVet({...vet, phone: e.target.value})}
                                className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition-all"
                                placeholder="+1234567890"
                            />
                        </div>

                        <div className="space-y-2 pt-6">
                            <label className="flex items-center gap-3 cursor-pointer mt-2">
                                <input
                                    type="checkbox"
                                    checked={vet.isActive}
                                    onChange={e => setVet({...vet, isActive: e.target.checked})}
                                    className="w-5 h-5 text-blue-600 rounded border-slate-300 focus:ring-blue-500 transition-all"
                                />
                                <span className="text-sm font-medium text-slate-700">Active Profile (Visible in app)</span>
                            </label>
                        </div>
                        
                        <div className="space-y-2">
                            <label className="block text-sm font-medium text-slate-700">Clinic Reference/ID</label>
                            {!isNewClinic ? (
                                <select
                                    value={vet.clinicId}
                                    onChange={e => {
                                        if (e.target.value === 'NEW') {
                                            setIsNewClinic(true);
                                        } else {
                                            setVet({...vet, clinicId: e.target.value});
                                        }
                                    }}
                                    className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition-all bg-white text-slate-900"
                                >
                                    <option value="">Select a clinic</option>
                                    {clinics.map(clinic => (
                                        <option key={clinic} value={clinic}>{clinic}</option>
                                    ))}
                                    <option value="NEW" className="font-bold text-blue-600">+ Other (Add New...)</option>
                                </select>
                            ) : (
                                <div className="flex flex-col gap-2">
                                    <div className="flex gap-2">
                                        <input
                                            type="text"
                                            autoFocus
                                            value={newClinicName}
                                            onChange={e => setNewClinicName(e.target.value)}
                                            className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition-all"
                                            placeholder="Enter clinic reference..."
                                        />
                                        <button 
                                            type="button"
                                            onClick={() => setIsNewClinic(false)}
                                            className="px-3 text-slate-400 hover:text-slate-600"
                                        >
                                            Cancel
                                        </button>
                                    </div>
                                </div>
                            )}
                        </div>

                        <div className="col-span-1 md:col-span-2 space-y-2">
                            <label className="block text-sm font-medium text-slate-700">Biography</label>
                            <textarea
                                required
                                rows={4}
                                value={vet.bio}
                                onChange={e => setVet({...vet, bio: e.target.value})}
                                className="w-full px-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition-all"
                                placeholder="Write a short biography about the veterinarian..."
                            />
                        </div>
                    </div>
                </form>
            </div>
        </div>
    );
}
