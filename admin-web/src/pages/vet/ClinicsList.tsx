import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { collection, getDocs, deleteDoc, doc } from 'firebase/firestore';
import { db } from '../../firebase/config';
import { Plus, Pencil, Trash2, MapPin, Phone, Building2 } from 'lucide-react';

export interface Clinic {
    id: string;
    name: string;
    address: string;
    latitude: number;
    longitude: number;
    phone: string;
}

export default function ClinicsList() {
    const [clinics, setClinics] = useState<Clinic[]>([]);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    const fetchClinics = async () => {
        setLoading(true);
        const snap = await getDocs(collection(db, 'clinics'));
        const data = snap.docs.map(d => ({ id: d.id, ...d.data() } as Clinic));
        setClinics(data);
        setLoading(false);
    };

    useEffect(() => { fetchClinics(); }, []);

    const handleDelete = async (id: string) => {
        if (!confirm('Delete this clinic? Veterinarians linked to it will lose their clinic reference.')) return;
        await deleteDoc(doc(db, 'clinics', id));
        fetchClinics();
    };

    if (loading) return <div className="p-8 text-center text-slate-400">Loading clinics...</div>;

    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between">
                <div>
                    <h2 className="text-xl font-bold text-slate-800">Clinics</h2>
                    <p className="text-sm text-slate-400 mt-1">{clinics.length} clinic{clinics.length !== 1 ? 's' : ''} registered</p>
                </div>
                <button
                    onClick={() => navigate('/vets/clinics/new')}
                    className="flex items-center gap-2 bg-blue-600 text-white px-5 py-2.5 rounded-xl text-sm font-semibold hover:bg-blue-700 transition-colors shadow-sm"
                >
                    <Plus className="h-4 w-4" />
                    Add Clinic
                </button>
            </div>

            {clinics.length === 0 ? (
                <div className="bg-white rounded-2xl border border-slate-200 p-12 text-center">
                    <Building2 className="h-12 w-12 text-slate-300 mx-auto mb-4" />
                    <p className="text-slate-500 font-medium">No clinics yet</p>
                    <p className="text-slate-400 text-sm mt-1">Add a clinic to link veterinarians to locations.</p>
                </div>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
                    {clinics.map(clinic => (
                        <div key={clinic.id} className="bg-white rounded-2xl border border-slate-200 p-5 shadow-sm hover:shadow-md transition-all">
                            <div className="flex items-start justify-between mb-3">
                                <div className="flex items-center gap-3">
                                    <div className="p-2 bg-blue-50 rounded-xl">
                                        <Building2 className="h-5 w-5 text-blue-600" />
                                    </div>
                                    <h3 className="font-semibold text-slate-800 text-base">{clinic.name}</h3>
                                </div>
                                <div className="flex gap-1">
                                    <button
                                        onClick={() => navigate(`/vets/clinics/edit/${clinic.id}`)}
                                        className="p-2 text-slate-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                                    >
                                        <Pencil className="h-4 w-4" />
                                    </button>
                                    <button
                                        onClick={() => handleDelete(clinic.id)}
                                        className="p-2 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                                    >
                                        <Trash2 className="h-4 w-4" />
                                    </button>
                                </div>
                            </div>
                            <div className="space-y-1.5 text-sm text-slate-500">
                                <div className="flex items-start gap-2">
                                    <MapPin className="h-4 w-4 mt-0.5 flex-shrink-0 text-slate-400" />
                                    <span>{clinic.address}</span>
                                </div>
                                {clinic.phone && (
                                    <div className="flex items-center gap-2">
                                        <Phone className="h-4 w-4 flex-shrink-0 text-slate-400" />
                                        <span>{clinic.phone}</span>
                                    </div>
                                )}
                                {clinic.latitude != null && (
                                    <div className="flex items-center gap-2 text-xs text-slate-400 mt-2 font-mono">
                                        <span>{clinic.latitude.toFixed(4)}, {clinic.longitude.toFixed(4)}</span>
                                    </div>
                                )}
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}
