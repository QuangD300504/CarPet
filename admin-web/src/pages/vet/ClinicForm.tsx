import { useState, useEffect, useRef, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { doc, getDoc, addDoc, updateDoc, collection } from 'firebase/firestore';
import { db } from '../../firebase/config';
import { ArrowLeft, Save, Loader2, MapPin, Building2, Phone, Search, X, Navigation } from 'lucide-react';
import type { Clinic } from './ClinicsList';

/** TomTom Fuzzy Search result — coords come directly in autocomplete response (no detail call needed) */
interface TomTomResult {
    id: string;
    type: string;           // 'POI', 'Street', 'Geography', 'Point Address', etc.
    poi?: {
        name: string;
        categories?: string[];
    };
    address: {
        freeformAddress: string;
        streetName?: string;
        streetNumber?: string;
        municipality?: string;
    };
    position: { lat: number; lon: number };
}

const TOMTOM_KEY = import.meta.env.VITE_TOMTOM_API_KEY as string;

/** TomTom Fuzzy Search — Vietnam only, includes POIs + addresses + streets */
async function tomtomSearch(query: string, signal?: AbortSignal): Promise<TomTomResult[]> {
    if (!TOMTOM_KEY || TOMTOM_KEY === 'your_tomtom_api_key_here') return [];

    const base = `https://api.tomtom.com/search/2/search/${encodeURIComponent(query)}.json`;
    const common = `?key=${TOMTOM_KEY}&countrySet=VN&language=vi-VN&limit=6`;

    const startsWithNumber = /^\d/.test(query.trim());

    const fetches: Promise<TomTomResult[]>[] = [];

    if (startsWithNumber) {
        // For address queries: prioritise PAD (Point Address Data) + Addr over POI
        fetches.push(
            fetch(`${base}${common}&typeahead=true&idxSet=PAD,Addr,Str,Geo,POI`, { signal })
                .then(r => r.json()).then(d => (d.results as TomTomResult[]) ?? []).catch(() => [])
        );
        // Also try without typeahead for better house-number matching
        fetches.push(
            fetch(`${base}${common}&idxSet=PAD,Addr,Str`, { signal })
                .then(r => r.json()).then(d => (d.results as TomTomResult[]) ?? []).catch(() => [])
        );
    } else {
        // For name/POI queries: POI first
        fetches.push(
            fetch(`${base}${common}&typeahead=true&idxSet=POI,Addr,PAD,Str,Geo`, { signal })
                .then(r => r.json()).then(d => (d.results as TomTomResult[]) ?? []).catch(() => [])
        );
    }

    const batches = await Promise.all(fetches);
    const seen = new Set<string>();
    const merged: TomTomResult[] = [];
    for (const batch of batches) {
        for (const item of batch) {
            if (!seen.has(item.id)) { seen.add(item.id); merged.push(item); }
        }
    }
    return merged.slice(0, 6);
}


/** Leaflet map injected into a sandboxed iframe. Two-way: click/drag → postMessage to parent */
function buildMapHtml(initLat: number | null, initLng: number | null, label: string): string {
    const hasCoords = initLat !== null && initLng !== null;
    const centerLat = hasCoords ? initLat : 16.0;
    const centerLng = hasCoords ? initLng : 106.0;
    const zoom = hasCoords ? 16 : 6;
    const safeLabel = (label || 'Clinic').replace(/'/g, "\\'").replace(/"/g, '&quot;');

    return `<!DOCTYPE html>
<html>
<head>
  <meta name="viewport" content="width=device-width,initial-scale=1.0">
  <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"/>
  <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
  <style>
    *{margin:0;padding:0;box-sizing:border-box}
    html,body,#map{width:100%;height:100%;font-family:system-ui,sans-serif}
    .leaflet-control-attribution{font-size:9px;opacity:.5}
    #hint{position:absolute;bottom:10px;left:50%;transform:translateX(-50%);
      background:rgba(255,255,255,.92);border-radius:20px;padding:5px 14px;
      font-size:12px;color:#475569;z-index:1000;box-shadow:0 2px 8px rgba(0,0,0,.15);
      pointer-events:none;white-space:nowrap;transition:opacity .4s}
  </style>
</head>
<body>
<div id="map"></div>
<div id="hint">📍 Click to place pin &nbsp;·&nbsp; Drag to move</div>
<script>
  var map = L.map('map').setView([${centerLat},${centerLng}],${zoom});
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{maxZoom:19,attribution:'© OSM'}).addTo(map);
  var marker=null, hint=document.getElementById('hint');

  function notify(ll){ window.parent.postMessage({type:'markerMoved',lat:ll.lat,lng:ll.lng},'*'); }

  function placeMarker(ll){
    if(marker){ marker.setLatLng(ll); }
    else{
      marker=L.marker(ll,{draggable:true}).addTo(map);
      marker.on('dragend',function(ev){ notify(ev.target.getLatLng()); });
      var pop=L.popup().setContent('<b>${safeLabel}</b>');
      marker.bindPopup(pop);
    }
    marker.openPopup();
    hint.style.opacity='0';
    notify(marker.getLatLng());
  }

  ${hasCoords ? `placeMarker(L.latLng(${initLat},${initLng}));` : ''}
  map.on('click',function(e){ placeMarker(e.latlng); map.panTo(e.latlng); });
</script>
</body>
</html>`;
}


export default function ClinicForm() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const isEditing = id !== 'new' && id !== undefined;

    const [loading, setLoading] = useState(isEditing);
    const [saving, setSaving] = useState(false);
    const [clinic, setClinic] = useState<Partial<Clinic>>({
        name: '', address: '', latitude: undefined, longitude: undefined, phone: '',
    });

    const [suggestions, setSuggestions] = useState<TomTomResult[]>([]);
    const [showSuggestions, setShowSuggestions] = useState(false);
    const [isSearching, setIsSearching] = useState(false);
    const debounceRef = useRef<ReturnType<typeof setTimeout> | undefined>(undefined);
    const abortRef = useRef<AbortController | undefined>(undefined);
    const wrapperRef = useRef<HTMLDivElement>(null);

    // mapKey increments to force iframe re-mount when coords change via autocomplete / Enter
    const [mapKey, setMapKey] = useState(0);
    const [mapJumpedHint, setMapJumpedHint] = useState(false);
    const lat = (clinic.latitude != null && !isNaN(Number(clinic.latitude))) ? Number(clinic.latitude) : null;
    const lng = (clinic.longitude != null && !isNaN(Number(clinic.longitude))) ? Number(clinic.longitude) : null;
    const hasPin = lat !== null && lng !== null && lat !== 0 && lng !== 0;

    useEffect(() => {
        if (isEditing && id) {
            (async () => {
                const snap = await getDoc(doc(db, 'clinics', id));
                if (snap.exists()) setClinic({ id: snap.id, ...snap.data() } as Clinic);
                else { alert('Clinic not found'); navigate('/vets/clinics'); }
                setLoading(false);
            })();
        }
    }, [id, isEditing, navigate]);

    // Receive pin position from iframe
    useEffect(() => {
        const handler = (e: MessageEvent) => {
            if (e.data?.type === 'markerMoved') {
                setClinic(prev => ({
                    ...prev,
                    latitude: parseFloat(e.data.lat.toFixed(6)),
                    longitude: parseFloat(e.data.lng.toFixed(6)),
                }));
            }
        };
        window.addEventListener('message', handler);
        return () => window.removeEventListener('message', handler);
    }, []);

    // Close dropdown on outside click
    useEffect(() => {
        const h = (e: MouseEvent) => {
            if (wrapperRef.current && !wrapperRef.current.contains(e.target as Node))
                setShowSuggestions(false);
        };
        document.addEventListener('mousedown', h);
        return () => document.removeEventListener('mousedown', h);
    }, []);

    /** Fire Goong Autocomplete, update suggestions list */
    const runSearch = useCallback(async (query: string, immediate = false) => {
        if (query.trim().length < 2) { setSuggestions([]); setShowSuggestions(false); return []; }
        abortRef.current?.abort();
        abortRef.current = new AbortController();
        if (!immediate) setIsSearching(true);
        try {
            const data = await tomtomSearch(query, abortRef.current.signal);
            setSuggestions(data);
            setShowSuggestions(data.length > 0);
            return data;
        } catch { return []; }
        finally { setIsSearching(false); }
    }, []);

    /** Debounced search as user types */
    const handleAddressChange = (value: string) => {
        setClinic(prev => ({ ...prev, address: value }));
        clearTimeout(debounceRef.current);
        debounceRef.current = setTimeout(() => runSearch(value), 400);
    };

    const getResultText = (r: TomTomResult) => {
        const isPoi = r.type === 'POI' && !!r.poi?.name;
        return isPoi ? `${r.poi!.name}, ${r.address.freeformAddress}` : r.address.freeformAddress;
    };

    /** Pick a TomTom result — coords included. Update input with full address. */
    const handleSelectSuggestion = (r: TomTomResult) => {
        setSuggestions([]);
        setShowSuggestions(false);
        setClinic(prev => ({
            ...prev,
            address: getResultText(r),
            latitude: parseFloat(r.position.lat.toFixed(6)),
            longitude: parseFloat(r.position.lon.toFixed(6)),
        }));
        setMapJumpedHint(true);
        setMapKey(k => k + 1);
    };

    /** Jump map to TomTom result (Enter / blur path) */
    const jumpMapToResult = (r: TomTomResult) => {
        setClinic(prev => ({
            ...prev,
            address: getResultText(r),
            latitude: parseFloat(r.position.lat.toFixed(6)),
            longitude: parseFloat(r.position.lon.toFixed(6)),
        }));
        setMapJumpedHint(true);
        setMapKey(k => k + 1);
    };

    /** Enter key: pick first suggestion OR search + auto-jump map */
    const handleKeyDown = async (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === 'Escape') { setShowSuggestions(false); return; }
        if (e.key !== 'Enter') return;
        e.preventDefault();
        if (showSuggestions && suggestions.length > 0) {
            handleSelectSuggestion(suggestions[0]);
        } else if (clinic.address && clinic.address.trim().length >= 2) {
            setIsSearching(true);
            const results = await runSearch(clinic.address, true);
            if (results.length > 0) jumpMapToResult(results[0]);
            setIsSearching(false);
        }
    };

    /** Blur: if no pin set yet, auto-jump map to best match */
    const handleBlur = async () => {
        await new Promise(r => setTimeout(r, 180));
        setShowSuggestions(false);
        if (hasPin) return;
        if (clinic.address && clinic.address.trim().length >= 2) {
            const results = await runSearch(clinic.address, true);
            if (results.length > 0) jumpMapToResult(results[0]);
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!clinic.name || !clinic.address) { alert('Name and address are required.'); return; }
        if (!hasPin) { alert('Please click the map or press Enter after typing an address to set the pin.'); return; }
        setSaving(true);
        try {
            const now = Date.now();
            const data = {
                name: clinic.name!, address: clinic.address!,
                latitude: lat!, longitude: lng!,
                phone: clinic.phone || '',
                updatedAt: now, ...(isEditing ? {} : { createdAt: now }),
            };
            if (isEditing && id) await updateDoc(doc(db, 'clinics', id), data);
            else await addDoc(collection(db, 'clinics'), data);
            navigate('/vets/clinics');
        } catch (err) {
            console.error(err);
            alert('Error saving clinic.');
        } finally { setSaving(false); }
    };

    if (loading) return <div className="p-8 text-center text-slate-500">Loading form...</div>;

    return (
        <div className="max-w-2xl mx-auto space-y-6">
            {/* Header */}
            <div className="flex items-center justify-between">
                <div className="flex items-center gap-4">
                    <button onClick={() => navigate('/vets/clinics')}
                        className="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-full transition-colors">
                        <ArrowLeft className="h-6 w-6" />
                    </button>
                    <div>
                        <h1 className="text-2xl font-bold text-slate-800">{isEditing ? 'Edit Clinic' : 'Add New Clinic'}</h1>
                        <p className="text-sm text-slate-400 mt-0.5">Clinic details and location</p>
                    </div>
                </div>
                <button onClick={handleSubmit} disabled={saving}
                    className="flex items-center gap-2 bg-blue-600 text-white px-6 py-2.5 rounded-xl hover:bg-blue-700 transition-colors disabled:opacity-50 font-semibold shadow-sm">
                    {saving ? <Loader2 className="h-5 w-5 animate-spin" /> : <Save className="h-5 w-5" />}
                    Save Clinic
                </button>
            </div>

            <form className="bg-white rounded-2xl border border-slate-200 shadow-sm p-6 space-y-5" onSubmit={handleSubmit}>

                {/* Clinic Name */}
                <div className="space-y-1.5">
                    <label className="block text-sm font-medium text-slate-700">Clinic Name *</label>
                    <div className="relative">
                        <Building2 className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
                        <input type="text" required value={clinic.name}
                            onChange={e => setClinic({ ...clinic, name: e.target.value })}
                            className="w-full pl-10 pr-4 py-2.5 border border-slate-300 rounded-xl focus:ring-2 focus:ring-blue-500 outline-none transition-all"
                            placeholder="Happy Paws Veterinary Clinic" />
                    </div>
                </div>

                {/* Address + autocomplete dropdown */}
                <div className="space-y-1.5">
                    <label className="block text-sm font-medium text-slate-700">Address or Place Name *</label>
                    <div className="relative" ref={wrapperRef}>
                        <div className="relative flex items-center">
                            {isSearching
                                ? <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-blue-400 animate-pulse pointer-events-none" />
                                : <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400 pointer-events-none" />
                            }
                            <input
                                type="text" required
                                value={clinic.address}
                                onChange={e => handleAddressChange(e.target.value)}
                                onFocus={() => suggestions.length > 0 && setShowSuggestions(true)}
                                onKeyDown={handleKeyDown}
                                onBlur={handleBlur}
                                className="w-full pl-10 pr-10 py-2.5 border border-slate-300 rounded-xl focus:ring-2 focus:ring-blue-500 outline-none transition-all"
                                placeholder="Street address, building name, or landmark…"
                                autoComplete="off"
                            />
                            {clinic.address && (
                                <button type="button"
                                    onMouseDown={e => e.preventDefault()}
                                    onClick={() => { setClinic(p => ({ ...p, address: '' })); setSuggestions([]); setShowSuggestions(false); }}
                                    className="absolute right-3 text-slate-300 hover:text-slate-500 transition-colors">
                                    <X className="h-4 w-4" />
                                </button>
                            )}
                        </div>

                        {/* Floating suggestions */}
                        {showSuggestions && suggestions.length > 0 && (
                            <div className="absolute left-0 right-0 top-full mt-1 z-50 bg-white border border-slate-200 rounded-xl shadow-xl overflow-hidden">
                                <div className="max-h-56 overflow-y-auto divide-y divide-slate-50">
                                     {suggestions.map((r, idx) => {
                                                // TomTom: POI results have r.poi.name; address/street results use freeformAddress
                                                const isPoi = r.type === 'POI' && !!r.poi?.name;
                                                const main = isPoi ? r.poi!.name : r.address.freeformAddress;
                                                const sub  = isPoi ? r.address.freeformAddress : (r.address.municipality ?? '');
                                                return (
                                                    <button key={r.id} type="button"
                                                        onMouseDown={e => e.preventDefault()}
                                                        onClick={() => handleSelectSuggestion(r)}
                                                        className={`w-full text-left px-4 py-3 hover:bg-blue-50 transition-colors flex items-start gap-3 group ${idx === 0 ? 'bg-slate-50/60' : ''}`}>
                                                        {isPoi
                                                            ? <Building2 className="h-4 w-4 text-violet-400 mt-0.5 flex-shrink-0 group-hover:text-violet-600" />
                                                            : <MapPin className="h-4 w-4 text-blue-400 mt-0.5 flex-shrink-0 group-hover:text-blue-600" />
                                                        }
                                                        <div className="min-w-0">
                                                            <p className="text-sm font-medium text-slate-800 leading-snug">{main}</p>
                                                            {sub && sub !== main && (
                                                                <p className="text-xs text-slate-400 truncate mt-0.5">{sub}</p>
                                                            )}
                                                        </div>
                                                    </button>
                                                );
                                    })}
                                </div>
                                <div className="px-4 py-1.5 bg-slate-50 border-t border-slate-100 flex items-center justify-between">
                                    <span className="text-[10px] text-slate-400">Powered by TomTom</span>
                                    <span className="text-[10px] text-slate-400">↵ Enter to pick first</span>
                                </div>
                            </div>
                        )}
                    </div>
                    <p className="text-xs text-slate-400">Search by address, building name, or landmark · <kbd className="px-1 py-0.5 text-[10px] bg-slate-100 rounded border border-slate-200">Enter</kbd> or click outside to jump map</p>
                </div>

                {/* Phone */}
                <div className="space-y-1.5">
                    <label className="block text-sm font-medium text-slate-700">Phone Number</label>
                    <div className="relative">
                        <Phone className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
                        <input type="tel" value={clinic.phone}
                            onChange={e => setClinic({ ...clinic, phone: e.target.value })}
                            className="w-full pl-10 pr-4 py-2.5 border border-slate-300 rounded-xl focus:ring-2 focus:ring-blue-500 outline-none transition-all"
                            placeholder="028-1234-5678" />
                    </div>
                </div>

                {/* Interactive Map */}
                <div className="space-y-2">
                    <div className="flex items-center justify-between">
                        <label className="block text-sm font-medium text-slate-700">Location Pin *</label>
                        {hasPin ? (
                            <span className="inline-flex items-center gap-1.5 text-xs text-emerald-700 font-medium bg-emerald-50 px-2.5 py-1 rounded-full">
                                <Navigation className="h-3 w-3" />
                                {lat!.toFixed(5)}, {lng!.toFixed(5)}
                            </span>
                        ) : (
                            <span className="text-xs text-amber-600 bg-amber-50 px-2.5 py-1 rounded-full font-medium">
                                No pin yet — search or click map
                            </span>
                        )}
                    </div>

                    <div className="rounded-xl overflow-hidden border border-slate-200 shadow-sm" style={{ height: 300 }}>
                        <iframe
                            key={mapKey}
                            srcDoc={buildMapHtml(lat, lng, clinic.name || 'Clinic')}
                            className="w-full h-full border-0"
                            title="Pick clinic location"
                            sandbox="allow-scripts"
                        />
                    </div>

                    {/* "Map jumped" nudge — shown after autocomplete/Enter search */}
                    {mapJumpedHint && !hasPin && (
                        <div className="flex items-start gap-2 bg-blue-50 border border-blue-200 text-blue-700 text-xs rounded-lg px-3 py-2">
                            <MapPin className="h-3.5 w-3.5 mt-0.5 flex-shrink-0" />
                            <span>Map jumped to the nearest match. <strong>Click exactly where the clinic is</strong> to drop the pin.</span>
                            <button type="button" onClick={() => setMapJumpedHint(false)} className="ml-auto text-blue-400 hover:text-blue-600">
                                <X className="h-3.5 w-3.5" />
                            </button>
                        </div>
                    )}
                    {hasPin && mapJumpedHint && (
                        <p className="text-xs text-emerald-600 flex items-center gap-1.5">
                            <Navigation className="h-3 w-3" /> Pin set! Drag the marker on the map to fine-tune.
                        </p>
                    )}
                    {!mapJumpedHint && (
                        <p className="text-xs text-slate-400 flex items-center gap-1.5">
                            <MapPin className="h-3 w-3 flex-shrink-0" />
                            Click anywhere on the map to drop a pin, or drag the pin to adjust.
                        </p>
                    )}
                </div>
            </form>
        </div>
    );
}
