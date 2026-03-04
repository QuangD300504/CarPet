import { initializeApp } from "firebase/app";
import { getFirestore } from "firebase/firestore";
import { getAuth } from "firebase/auth";
import { getStorage } from "firebase/storage";

const firebaseConfig = {
  apiKey: "AIzaSyCJblG2a3CbrbgppT2wxf_ZhylDVfHN51o",
  authDomain: "vetbookexe.firebaseapp.com",
  projectId: "vetbookexe",
  storageBucket: "vetbookexe.firebasestorage.app",
  messagingSenderId: "1074707230062",
  appId: "1:1074707230062:web:ddd03e0fe422048035257f",
  measurementId: "G-MCSCW4Q05S"
};

export const app = initializeApp(firebaseConfig);
export const db = getFirestore(app);
export const auth = getAuth(app);
export const storage = getStorage(app);
