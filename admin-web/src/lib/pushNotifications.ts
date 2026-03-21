/**
 * Firebase Cloud Messaging helpers for the admin-web client.
 *
 * Uses Firebase Messaging which works across web, Android, and iOS.
 * The Cloudflare Worker stores FCM tokens in Firestore and sends
 * push notifications via the FCM REST API using Firebase SA credentials.
 */

import { initializeApp, getApps } from 'firebase/app';
import { getToken, isSupported as isMessagingSupported, getMessaging } from 'firebase/messaging';

const firebaseConfig = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY,
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN,
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID,
  storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET,
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID,
  appId: import.meta.env.VITE_FIREBASE_APP_ID,
};

const VAPID_KEY = import.meta.env.VITE_FIREBASE_VAPID_KEY;

const WORKER_BASE = 'https://vetbook-payment-worker.duyq099.workers.dev';

// Initialize Firebase app once
const app = getApps().length === 0 ? initializeApp(firebaseConfig) : getApps()[0];

async function getMessagingInstance() {
  const supported = await isMessagingSupported();
  if (!supported) throw new Error('FCM not supported');
  return getMessaging(app);
}

/**
 * Requests notification permission, obtains an FCM token, and registers
 * it with the Cloudflare Worker (authenticated via Firebase ID token).
 */
export async function subscribeToPush(
  user: { uid: string; getIdToken: () => Promise<string> }
): Promise<void> {
  if (!('serviceWorker' in navigator) || !('PushManager' in window)) {
    console.warn('[Push] Service Worker or PushManager not supported');
    return;
  }

  const permission = await Notification.requestPermission();
  if (permission !== 'granted') {
    console.warn('[Push] Notification permission denied');
    return;
  }

  // Register service worker (needed for FCM to work in the background)
  await navigator.serviceWorker.register('/sw.js');

  // Get FCM token
  let token: string | null = null;
  try {
    const messaging = await getMessagingInstance();
    token = await getToken(messaging, { vapidKey: VAPID_KEY });
  } catch (err) {
    console.error('[Push] FCM token error:', err);
    return;
  }

  if (!token) {
    console.warn('[Push] No FCM token obtained');
    return;
  }

  // Register token with Worker
  const idToken = await user.getIdToken();
  const res = await fetch(`${WORKER_BASE}/push/subscribe`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${idToken}`,
    },
    body: JSON.stringify({ fcmToken: token }),
  });

  if (!res.ok) {
    console.error('[Push] Failed to register token:', res.status);
    return;
  }

  console.log('[Push] Subscribed successfully for user', user.uid);
}

/**
 * Removes the FCM token for the user (call on logout).
 */
export async function unsubscribeFromPush(
  user: { uid: string; getIdToken: () => Promise<string> }
): Promise<void> {
  const reg = await navigator.serviceWorker.getRegistration('/sw.js');
  if (reg) {
    const sub = await reg.pushManager.getSubscription();
    if (sub) await sub.unsubscribe();
  }

  const idToken = await user.getIdToken();
  await fetch(`${WORKER_BASE}/push/unsubscribe`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${idToken}`,
    },
    body: JSON.stringify({}),
  });

  console.log('[Push] Unsubscribed for user', user.uid);
}
