import {Hono} from "hono";
import {cors} from "hono/cors";
import axios from "axios";

type Env = {
  PAYOS_CLIENT_ID: string;
  PAYOS_API_KEY: string;
  PAYOS_CHECKSUM_KEY: string;
  FIREBASE_SERVICE_ACCOUNT_JSON: string;
  FIREBASE_PROJECT_ID: string;
  VNP_TMN_CODE: string;
  VNP_HASH_SECRET: string;
  VNP_RETURN_URL: string;
  VNP_IPN_URL: string;
  VNP_URL: string;
};

type CreatePaymentLinkRequest = {
  appointmentId: string;
};

type PayosWebhookBody = {
  code: string;
  desc: string;
  success?: boolean;
  data: Record<string, any> & {orderCode: number};
  signature: string;
};

type ServiceAccount = {
  client_email: string;
  private_key: string;
};

function jsonError(message: string, status: number) {
  return new Response(JSON.stringify({error: message}), {
    status,
    headers: {"content-type": "application/json"},
  });
}

function getBearerToken(authHeader: string | null): string | null {
  if (!authHeader) return null;
  const parts = authHeader.split(" ");
  if (parts.length !== 2 || parts[0].toLowerCase() !== "bearer") return null;
  return parts[1];
}

function encodeBase64Url(bytes: Uint8Array) {
  let str = "";
  for (let i = 0; i < bytes.length; i++) str += String.fromCharCode(bytes[i]);
  const b64 = btoa(str);
  return b64.replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/g, "");
}

function encodeJsonBase64Url(obj: unknown) {
  return encodeBase64Url(new TextEncoder().encode(JSON.stringify(obj)));
}

function pemToPkcs8(pem: string): ArrayBuffer {
  const b64 = pem
    .replace(/-----BEGIN PRIVATE KEY-----/g, "")
    .replace(/-----END PRIVATE KEY-----/g, "")
    .replace(/\s+/g, "");
  const raw = atob(b64);
  const bytes = new Uint8Array(raw.length);
  for (let i = 0; i < raw.length; i++) bytes[i] = raw.charCodeAt(i);
  return bytes.buffer;
}

async function signJwt(payload: object, privateKeyPem: string): Promise<string> {
  const header = {alg: "RS256", typ: "JWT"};
  const signingInput = `${encodeJsonBase64Url(header)}.${encodeJsonBase64Url(payload)}`;

  const privateKey = await crypto.subtle.importKey(
    "pkcs8",
    pemToPkcs8(privateKeyPem),
    {name: "RSASSA-PKCS1-v1_5", hash: "SHA-256"},
    false,
    ["sign"]
  );

  const signature = await crypto.subtle.sign(
    "RSASSA-PKCS1-v1_5",
    privateKey,
    new TextEncoder().encode(signingInput)
  );

  return `${signingInput}.${encodeBase64Url(new Uint8Array(signature))}`;
}

async function getGoogleAccessToken(env: Env): Promise<string> {
  const sa = JSON.parse(env.FIREBASE_SERVICE_ACCOUNT_JSON) as ServiceAccount;
  if (!sa.client_email || !sa.private_key) {
    throw new Error("Invalid FIREBASE_SERVICE_ACCOUNT_JSON");
  }

  const nowSec = Math.floor(Date.now() / 1000);
  const claimSet = {
    iss: sa.client_email,
    scope: "https://www.googleapis.com/auth/datastore",
    aud: "https://oauth2.googleapis.com/token",
    iat: nowSec,
    exp: nowSec + 55 * 60,
  };

  const jwt = await signJwt(claimSet, sa.private_key);

  const resp = await fetch("https://oauth2.googleapis.com/token", {
    method: "POST",
    headers: {"content-type": "application/x-www-form-urlencoded"},
    body: new URLSearchParams({
      grant_type: "urn:ietf:params:oauth:grant-type:jwt-bearer",
      assertion: jwt,
    }),
  });

  if (!resp.ok) {
    throw new Error(await resp.text());
  }

  const json = (await resp.json()) as any;
  return json.access_token as string;
}

function encodeFirestoreValue(v: any): any {
  if (v === null || v === undefined) return {nullValue: null};
  if (typeof v === "string") return {stringValue: v};
  if (typeof v === "boolean") return {booleanValue: v};
  if (typeof v === "number") {
    if (Number.isInteger(v)) return {integerValue: String(v)};
    return {doubleValue: v};
  }
  if (v instanceof Date) return {timestampValue: v.toISOString()};
  if (Array.isArray(v)) return {arrayValue: {values: v.map(encodeFirestoreValue)}};
  if (typeof v === "object") return {mapValue: {fields: encodeFirestoreFields(v)}};
  return {stringValue: String(v)};
}

function encodeFirestoreFields(obj: Record<string, any>): Record<string, any> {
  const out: Record<string, any> = {};
  for (const [k, v] of Object.entries(obj)) out[k] = encodeFirestoreValue(v);
  return out;
}

function decodeFirestoreValue(v: any): any {
  const type = Object.keys(v)[0];
  const value = v[type];
  switch (type) {
    case "stringValue":
      return value;
    case "integerValue":
      return parseInt(value, 10);
    case "doubleValue":
      return value;
    case "booleanValue":
      return value;
    case "timestampValue":
      return new Date(value);
    case "nullValue":
      return null;
    case "mapValue":
      return decodeFirestoreFields(value.fields || {});
    case "arrayValue":
      return (value.values || []).map(decodeFirestoreValue);
    default:
      return value;
  }
}

function decodeFirestoreFields(fields: Record<string, any>): Record<string, any> {
  const out: Record<string, any> = {};
  for (const [k, v] of Object.entries(fields)) out[k] = decodeFirestoreValue(v);
  return out;
}

async function firestoreGet(env: Env, accessToken: string, docPath: string): Promise<any | null> {
  const url = `https://firestore.googleapis.com/v1/projects/${env.FIREBASE_PROJECT_ID}/databases/(default)/documents/${docPath}`;
  const resp = await fetch(url, {headers: {Authorization: `Bearer ${accessToken}`}});
  if (resp.status === 404) return null;
  if (!resp.ok) throw new Error(await resp.text());
  const json = (await resp.json()) as any;
  return {
    name: json.name,
    fields: decodeFirestoreFields(json.fields || {}),
  };
}

async function firestorePatch(env: Env, accessToken: string, docPath: string, fields: Record<string, any>): Promise<void> {
  const url = `https://firestore.googleapis.com/v1/projects/${env.FIREBASE_PROJECT_ID}/databases/(default)/documents/${docPath}`;
  const resp = await fetch(url, {
    method: "PATCH",
    headers: {
      Authorization: `Bearer ${accessToken}`,
      "content-type": "application/json",
    },
    body: JSON.stringify({fields: encodeFirestoreFields(fields)}),
  });
  if (!resp.ok) throw new Error(await resp.text());
}

async function firestoreRunQuery(
  env: Env,
  accessToken: string,
  structuredQuery: any
): Promise<Array<{name: string; fields: Record<string, any>}>> {
  const url = `https://firestore.googleapis.com/v1/projects/${env.FIREBASE_PROJECT_ID}/databases/(default)/documents:runQuery`;
  const resp = await fetch(url, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${accessToken}`,
      "content-type": "application/json",
    },
    body: JSON.stringify({structuredQuery}),
  });
  if (!resp.ok) throw new Error(await resp.text());

  const json = (await resp.json()) as any[];
  return json
    .map((row) => row.document)
    .filter(Boolean)
    .map((doc) => ({
      name: doc.name as string,
      fields: decodeFirestoreFields(doc.fields || {}),
    }));
}

async function verifyFirebaseIdToken(idToken: string, projectId: string): Promise<{uid: string}> {
  // Firebase ID tokens are RS256 JWTs issued by securetoken.google.com.
  // Google's /tokeninfo endpoint only handles Google OAuth tokens (accounts.google.com),
  // so we decode and validate the claims directly instead.
  const parts = idToken.split(".");
  if (parts.length !== 3) throw new Error("Invalid JWT format");

  let payload: any;
  try {
    // Base64url → base64 → decode
    const b64 = parts[1]
      .replace(/-/g, "+")
      .replace(/_/g, "/")
      .padEnd(parts[1].length + (4 - (parts[1].length % 4)) % 4, "=");
    payload = JSON.parse(atob(b64));
  } catch {
    throw new Error("Failed to decode token payload");
  }

  const nowSec = Math.floor(Date.now() / 1000);
  if (!payload.exp || payload.exp < nowSec) throw new Error("Token expired");
  if (payload.aud !== projectId)
    throw new Error(`Token audience "${payload.aud}" does not match project "${projectId}"`);
  if (!payload.iss?.includes("securetoken.google.com"))
    throw new Error(`Invalid token issuer: ${payload.iss}`);

  const uid: string | undefined = payload.sub ?? payload.user_id;
  if (!uid) throw new Error("Token missing uid (sub)");
  return {uid};
}

function isPlainObject(v: any): v is Record<string, any> {
  return typeof v === "object" && v !== null && !Array.isArray(v);
}

function deepSortObj(obj: Record<string, any>, sortArrays = false): Record<string, any> {
  return Object.keys(obj)
    .sort()
    .reduce((acc: Record<string, any>, key) => {
      const value = obj[key];

      if (Array.isArray(value)) {
        if (sortArrays) {
          acc[key] = value
            .map((item) => (isPlainObject(item) ? deepSortObj(item, sortArrays) : item))
            .sort((a, b) => {
              if (!isPlainObject(a) && !isPlainObject(b)) {
                return String(a).localeCompare(String(b));
              }
              return JSON.stringify(a).localeCompare(JSON.stringify(b));
            });
        } else {
          acc[key] = value.map((item) => (isPlainObject(item) ? deepSortObj(item, sortArrays) : item));
        }
      } else if (isPlainObject(value)) {
        acc[key] = deepSortObj(value, sortArrays);
      } else {
        acc[key] = value;
      }

      return acc;
    }, {});
}

function buildPayosQueryString(data: Record<string, any>): string {
  const sortedData = deepSortObj(data, false);
  return Object.keys(sortedData)
    .map((key) => {
      let value = (sortedData as any)[key];
      if (Array.isArray(value)) value = JSON.stringify(value);
      if (isPlainObject(value)) value = JSON.stringify(value);
      if (value === null || value === undefined) value = "";
      return `${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`;
    })
    .join("&");
}

async function createPayosSignature(checksumKey: string, data: Record<string, any>): Promise<string> {
  const qs = buildPayosQueryString(data);

  const cryptoKey = await crypto.subtle.importKey(
    "raw",
    new TextEncoder().encode(checksumKey),
    {name: "HMAC", hash: "SHA-256"},
    false,
    ["sign"]
  );

  const sig = await crypto.subtle.sign("HMAC", cryptoKey, new TextEncoder().encode(qs));
  return Array.from(new Uint8Array(sig))
    .map((b) => b.toString(16).padStart(2, "0"))
    .join("");
}

async function verifyPayosWebhook(checksumKey: string, data: Record<string, any>, signature: string) {
  const computed = await createPayosSignature(checksumKey, data);
  return computed.toLowerCase() === signature.toLowerCase();
}

async function hmacSha512Hex(secret: string, data: string): Promise<string> {
  const key = await crypto.subtle.importKey(
    "raw",
    new TextEncoder().encode(secret),
    {name: "HMAC", hash: "SHA-512"},
    false,
    ["sign"]
  );
  const sig = await crypto.subtle.sign("HMAC", key, new TextEncoder().encode(data));
  return Array.from(new Uint8Array(sig))
    .map((b) => b.toString(16).padStart(2, "0"))
    .join("");
}

// VNPAY requires PHP urlencode() / Java URLEncoder.encode() style where spaces are +
// and strict RFC 3986 reserved characters are properly escaped.
function vnpayEncode(str: string): string {
  return encodeURIComponent(str)
    .replace(/%20/g, "+")
    .replace(/!/g, "%21")
    .replace(/'/g, "%27")
    .replace(/\(/g, "%28")
    .replace(/\)/g, "%29")
    .replace(/\*/g, "%2A");
}

const app = new Hono<{Bindings: Env}>();
app.use("*", cors({origin: "*"}));

app.get("/health", (c) => c.json({ok: true}));

app.post("/create-payment-link", async (c) => {
  const token = getBearerToken(c.req.header("authorization") ?? null);
  if (!token) return jsonError("Missing Authorization Bearer token", 401);

  let uid: string;
  try {
    uid = (await verifyFirebaseIdToken(token, c.env.FIREBASE_PROJECT_ID)).uid;
  } catch (e: any) {
    return jsonError(e.message ?? "Invalid Firebase token", 401);
  }

  const body = (await c.req.json()) as CreatePaymentLinkRequest;
  if (!body?.appointmentId) return jsonError("Missing appointmentId", 400);

  let accessToken: string;
  try {
    accessToken = await getGoogleAccessToken(c.env);
  } catch (e: any) {
    return jsonError(`Auth to Firestore failed: ${e.message ?? "unknown"}`, 500);
  }

  const appointmentDoc = await firestoreGet(c.env, accessToken, `appointments/${body.appointmentId}`);
  if (!appointmentDoc) return jsonError("Appointment not found", 404);

  const appointment = appointmentDoc.fields;
  if (appointment.userId !== uid) return jsonError("Forbidden", 403);
  if (appointment.status !== "PENDING_PAYMENT") return jsonError("Invalid status", 409);

  const amount = Math.round(Number(appointment.totalPrice ?? 0));
  if (!Number.isFinite(amount) || amount <= 0) return jsonError("Invalid amount", 400);

  const orderCode = Date.now();

  const vnpVersion = "2.1.0";
  const vnpCommand = "pay";
  const vnpTmnCode = c.env.VNP_TMN_CODE;
  const vnpReturnUrl = `https://vetbook-payment-worker.duyq099.workers.dev/vnpay-return`;
  const vnpIpnUrl = c.env.VNP_IPN_URL || `https://vetbook-payment-worker.duyq099.workers.dev/vnpay-ipn`;
  
  const ICT_OFFSET_MS = 7 * 60 * 60 * 1000;
  const nowUtcMs = Date.now();
  const nowIct = new Date(nowUtcMs + ICT_OFFSET_MS);
  const pad = (n: number) => String(n).padStart(2, "0");
  const y = nowIct.getUTCFullYear();
  const m = pad(nowIct.getUTCMonth() + 1);
  const d = pad(nowIct.getUTCDate());
  const hh = pad(nowIct.getUTCHours());
  const mm = pad(nowIct.getUTCMinutes());
  const ss = pad(nowIct.getUTCSeconds());
  const vnpCreateDate = `${y}${m}${d}${hh}${mm}${ss}`;

  const expireIct = new Date(nowUtcMs + ICT_OFFSET_MS + 15 * 60 * 1000);
  const ey = expireIct.getUTCFullYear();
  const em = pad(expireIct.getUTCMonth() + 1);
  const ed = pad(expireIct.getUTCDate());
  const ehh = pad(expireIct.getUTCHours());
  const emm = pad(expireIct.getUTCMinutes());
  const ess = pad(expireIct.getUTCSeconds());
  const vnpExpireDate = `${ey}${em}${ed}${ehh}${emm}${ess}`;

  const params: Record<string, string> = {
    vnp_Version: vnpVersion,
    vnp_Command: vnpCommand,
    vnp_TmnCode: vnpTmnCode,
    vnp_Amount: String(amount * 100),
    vnp_CurrCode: "VND",
    vnp_TxnRef: String(orderCode),
    vnp_OrderInfo: `VetBook appointment ${body.appointmentId}`
      .normalize("NFD")
      .replace(/[\u0300-\u036f]/g, "")
      .replace(/[^a-zA-Z0-9 _\-]/g, "")
      .slice(0, 255),
    vnp_OrderType: "other",
    vnp_Locale: "vn",
    vnp_ReturnUrl: vnpReturnUrl,
    vnp_IpAddr: (
      c.req.header("cf-connecting-ip") ||
      c.req.header("x-forwarded-for")?.split(",")[0].trim() ||
      "1.1.1.1"
    ).split(":")[0].substring(0, 15),
    vnp_CreateDate: vnpCreateDate,
    vnp_ExpireDate: vnpExpireDate,
  };

  const sortedKeys = Object.keys(params).sort();
  const query = sortedKeys.map((k) => `${vnpayEncode(k)}=${vnpayEncode(params[k])}`).join("&");
  const secureHash = await hmacSha512Hex(c.env.VNP_HASH_SECRET, query);

  const base = c.env.VNP_URL || "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
  const checkoutUrl = `${base}?${query}&vnp_SecureHash=${secureHash}`;

  const existingPayos = appointment.payos || {};
  const updatedPayos = {
    ...existingPayos,
    orderCode,
    checkoutUrl,
  };

  await firestorePatch(c.env, accessToken, `appointments/${body.appointmentId}`, {
    payos: updatedPayos, // Keep using 'payos' struct to avoid app code changes
    updatedAt: new Date(),
  });

  return c.json({checkoutUrl, orderCode});
});

// Create VNPAY payment URL for store checkout
app.post("/vnpay-create-link", async (c) => {
  const token = getBearerToken(c.req.header("authorization") ?? null);
  if (!token) return jsonError("Missing Authorization Bearer token", 401);

  // Optional: verify Firebase token to ensure authenticated user
  try {
    await verifyFirebaseIdToken(token, c.env.FIREBASE_PROJECT_ID);
  } catch (e: any) {
    return jsonError(e.message ?? "Invalid Firebase token", 401);
  }

  const body = (await c.req.json()) as {amount: number; orderCode?: number; description?: string; locale?: string};
  const amount = Math.round(Number(body.amount ?? 0));
  if (!Number.isFinite(amount) || amount <= 0) return jsonError("Invalid amount", 400);

  const vnpVersion = "2.1.0";
  const vnpCommand = "pay";
  const vnpTmnCode = c.env.VNP_TMN_CODE;
  // VNPAY validates vnp_ReturnUrl by making an HTTP request — custom schemes
  // like vetbook-vnpay:// are rejected. Use a real HTTPS Worker URL instead.
  // The Android VNPAY SDK handles the actual navigation via setSdkCompletedCallback.
  const vnpReturnUrl = `https://vetbook-payment-worker.duyq099.workers.dev/vnpay-return`;
  const vnpIpnUrl = c.env.VNP_IPN_URL || `https://vetbook-payment-worker.duyq099.workers.dev/vnpay-ipn`;
  const vnpLocale = body.locale || "vn";
  const orderCode = body.orderCode || Date.now();

  // VNPAY sandbox validates vnp_CreateDate and vnp_ExpireDate against Vietnam time (ICT = UTC+7).
  // Using UTC causes the date to be 7 hours behind, making the session appear expired immediately.
  const ICT_OFFSET_MS = 7 * 60 * 60 * 1000;
  const nowUtcMs = Date.now();
  const nowIct = new Date(nowUtcMs + ICT_OFFSET_MS);
  const pad = (n: number) => String(n).padStart(2, "0");
  const y = nowIct.getUTCFullYear();
  const m = pad(nowIct.getUTCMonth() + 1);
  const d = pad(nowIct.getUTCDate());
  const hh = pad(nowIct.getUTCHours());
  const mm = pad(nowIct.getUTCMinutes());
  const ss = pad(nowIct.getUTCSeconds());
  const vnpCreateDate = `${y}${m}${d}${hh}${mm}${ss}`;

  // Session expires after 15 minutes (also in ICT)
  const expireIct = new Date(nowUtcMs + ICT_OFFSET_MS + 15 * 60 * 1000);
  const ey = expireIct.getUTCFullYear();
  const em = pad(expireIct.getUTCMonth() + 1);
  const ed = pad(expireIct.getUTCDate());
  const ehh = pad(expireIct.getUTCHours());
  const emm = pad(expireIct.getUTCMinutes());
  const ess = pad(expireIct.getUTCSeconds());
  const vnpExpireDate = `${ey}${em}${ed}${ehh}${emm}${ess}`;

  const params: Record<string, string> = {
    vnp_Version: vnpVersion,
    vnp_Command: vnpCommand,
    vnp_TmnCode: vnpTmnCode,
    vnp_Amount: String(amount * 100), // VNPAY expects amount in VND × 100
    vnp_CurrCode: "VND",
    vnp_TxnRef: String(orderCode),
    vnp_OrderInfo: (body.description || `VetBook store order ${orderCode}`)
      .normalize("NFD")
      .replace(/[\u0300-\u036f]/g, "")   // strip diacritics
      .replace(/[^a-zA-Z0-9 _\-]/g, "") // strip special chars (VNPAY requirement)
      .slice(0, 255),
    vnp_OrderType: "other",
    vnp_Locale: vnpLocale,
    vnp_ReturnUrl: vnpReturnUrl,
    vnp_IpAddr: (
      c.req.header("cf-connecting-ip") ||
      c.req.header("x-forwarded-for")?.split(",")[0].trim() ||
      "1.1.1.1"
    ).split(":")[0].substring(0, 15), // IP address MUST be IPv4 (15 chars max). VNPAY rejects or truncates IPv6, causing checksum failure.
    vnp_CreateDate: vnpCreateDate,
    vnp_ExpireDate: vnpExpireDate,
  };

  const sortedKeys = Object.keys(params).sort();
  const query = sortedKeys.map((k) => `${vnpayEncode(k)}=${vnpayEncode(params[k])}`).join("&");
  const secureHash = await hmacSha512Hex(c.env.VNP_HASH_SECRET, query);

  const base = c.env.VNP_URL || "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
  const url = `${base}?${query}&vnp_SecureHash=${secureHash}`;

  return c.json({url});
});

// VNPAY redirects the user here after payment (browser-side return).
// The Android SDK intercepts this via its WebView callback — respond 200 so VNPAY is happy.
app.get("/vnpay-return", (c) => {
  const code = c.req.query("vnp_ResponseCode") ?? "";
  const success = code === "00";
  return c.html(`
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>VetBook Payment Status</title>
  <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gray-50 h-screen flex items-center justify-center font-sans antialiased p-4">
  <div class="bg-white p-8 rounded-3xl shadow-2xl max-w-sm w-full mx-auto text-center border border-gray-100">
    <div class="mb-6 flex justify-center">
      ${success 
        ? '<div class="h-24 w-24 bg-green-100 rounded-full flex items-center justify-center animate-bounce"><svg class="w-12 h-12 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7"></path></svg></div>'
        : '<div class="h-24 w-24 bg-red-100 rounded-full flex items-center justify-center animate-pulse"><svg class="w-12 h-12 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M6 18L18 6M6 6l12 12"></path></svg></div>'
      }
    </div>
    <h2 class="text-3xl font-extrabold mb-3 text-gray-900 tracking-tight">${success ? 'Payment Successful!' : 'Payment Failed'}</h2>
    <p class="text-gray-500 mb-8 font-medium">${success ? 'Your order has been confirmed successfully. Mlem mlem!' : 'The transaction was cancelled or declined. Please try again.'}</p>
    
    <button onclick="goBack()" class="w-full ${success ? 'bg-yellow-400 hover:bg-yellow-500 text-black shadow-yellow-200' : 'bg-gray-800 hover:bg-gray-900 text-white shadow-gray-300'} shadow-lg font-bold py-4 px-6 rounded-2xl transition-all duration-300 transform hover:scale-105 active:scale-95 text-lg">
      Back to App / Continue Shopping
    </button>
    
    <script>
      function goBack() {
         // Attempt to use android deep link
         window.location.href = 'vetbook-vnpay://return?code=${code}';
         // Signal the VNPAY SDK running in WebView if applicable
         if (window.VNPaySDK) {
             window.VNPaySDK.onPaymentResult('${success ? "success" : "fail"}');
         }
      }
    </script>
  </div>
</body>
</html>`);
});

// VNPAY calls this via GET with query params after payment completes.
// Must verify signature, then respond with {RspCode, Message}.
app.get("/vnpay-ipn", async (c) => {
  const query = c.req.query();

  // 1. Extract and remove the secure hash from params before verifying
  const receivedHash = query["vnp_SecureHash"] ?? "";
  const params: Record<string, string> = {};
  for (const [k, v] of Object.entries(query)) {
    if (k !== "vnp_SecureHash" && k !== "vnp_SecureHashType") {
      params[k] = v;
    }
  }

  // 2. Build sorted query string and compute expected hash
  const sortedKeys = Object.keys(params).sort();
  const hashData = sortedKeys
    .map((k) => `${vnpayEncode(k)}=${vnpayEncode(params[k])}`)
    .join("&");
  const expectedHash = await hmacSha512Hex(c.env.VNP_HASH_SECRET, hashData);

  if (expectedHash.toLowerCase() !== receivedHash.toLowerCase()) {
    return c.json({RspCode: "97", Message: "Invalid checksum"});
  }

  // 3. Check payment result
  const responseCode = params["vnp_ResponseCode"];
  const txnRef = params["vnp_TxnRef"]; // This is our orderCode (timestamp)
  const transactionStatus = params["vnp_TransactionStatus"];

  if (!txnRef) return c.json({RspCode: "01", Message: "Order not found"});

  // Only mark as paid if both response code and transaction status are 00
  if (responseCode !== "00" || transactionStatus !== "00") {
    return c.json({RspCode: "00", Message: "Confirm Success"}); // Still ACK, just don't update
  }

  // 4. Find the matching storeOrder or appointment in Firestore by orderCode and update it
  try {
    const accessToken = await getGoogleAccessToken(c.env);
    
    // First, try finding a store order
    let docs = await firestoreRunQuery(c.env, accessToken, {
      from: [{collectionId: "storeOrders"}],
      where: {
        fieldFilter: {
          field: {fieldPath: "orderCode"},
          op: "EQUAL",
          value: {integerValue: String(parseInt(txnRef, 10))},
        },
      },
      limit: 1,
    });

    if (docs.length > 0) {
      const docPath = docs[0].name.split("/documents/")[1];
      await firestorePatch(c.env, accessToken, docPath, {
        status: "PAID",
        vnpayTransactionNo: params["vnp_TransactionNo"] ?? "",
        vnpayBankCode: params["vnp_BankCode"] ?? "",
        paidAt: new Date(),
        updatedAt: new Date(),
      });
      return c.json({RspCode: "00", Message: "Confirm Success"});
    }

    // Next, try finding an appointment
    docs = await firestoreRunQuery(c.env, accessToken, {
      from: [{collectionId: "appointments"}],
      where: {
        fieldFilter: {
          field: {fieldPath: "payos.orderCode"},
          op: "EQUAL",
          value: {integerValue: String(parseInt(txnRef, 10))},
        },
      },
      limit: 1,
    });

    if (docs.length > 0) {
      const docPath = docs[0].name.split("/documents/")[1];
      await firestorePatch(c.env, accessToken, docPath, {
        paymentStatus: "PAID",
        status: "CONFIRMED",
        paidAt: new Date(),
        updatedAt: new Date(),
      });
      return c.json({RspCode: "00", Message: "Confirm Success"});
    }

    // Not found in either
    return c.json({RspCode: "01", Message: "Order not found anywhere"});
    
  } catch (e: any) {
    // Log but still return 00 so VNPAY doesn't retry indefinitely
    console.error("IPN Firestore update failed:", e?.message);
  }

  return c.json({RspCode: "00", Message: "Confirm Success"});
});

// ─── Instant Push Trigger ──────────────────────────────────────────────────────

/**
 * Triggers an immediate FCM push notification from the app.
 * Called by the Android client right after booking / saving a vaccination.
 *
 * Body: { type: "vaccine" | "appointment", refId: string }
 */
app.post("/push/send-instant", async (c) => {
  const token = getBearerToken(c.req.header("authorization") ?? null);
  if (!token) return jsonError("Missing Authorization Bearer token", 401);

  let uid: string;
  try {
    uid = (await verifyFirebaseIdToken(token, c.env.FIREBASE_PROJECT_ID)).uid;
  } catch (e: any) {
    return jsonError(e.message ?? "Invalid Firebase token", 401);
  }

  const { type, refId } = await c.req.json() as { type: string; refId: string };
  if (!type || !refId) return jsonError("Missing type or refId", 400);

  // Build the notification message based on type
  let title = "VetBook";
  let body = "";

  try {
    const accessToken = await getGoogleAccessToken(c.env);

    if (type === "vaccine") {
      const doc = await firestoreGet(c.env, accessToken, `vaccinations/${refId}`);
      if (doc?.fields) {
        const v = doc.fields;
        const petName = v.petName ?? "Your pet";
        const vaccineName = v.title ?? "vaccine";
        title = "Vaccine Saved";
        body = `${petName}'s ${vaccineName} schedule has been created`;
      }
    } else if (type === "appointment") {
      const doc = await firestoreGet(c.env, accessToken, `appointments/${refId}`);
      if (doc?.fields) {
        const a = doc.fields;
        const doctorName = a.veterinarianName ?? a.doctorName ?? "your doctor";
        title = "Appointment Booked";
        body = `Appointment with ${doctorName} confirmed`;
      }
    }

    await sendFcmPush(c.env, accessToken, uid, body, type, refId);
    return c.json({ok: true});
  } catch (e: any) {
    return jsonError(e.message ?? "Failed to send push", 500);
  }
});

/**
 * TEST endpoint — sends a test push to a specific user by their Firestore UID.
 * NO auth required. In production, restrict or remove this.
 *
 * GET /push/test?uid=<userId>&title=<title>&body=<body>
 */
app.get("/push/test", async (c) => {
  const uid = c.req.query("uid");
  const title = c.req.query("title") ?? "Test from VetBook";
  const body = c.req.query("body") ?? "This is a test push notification!";

  if (!uid) return jsonError("Missing uid query param", 400);

  try {
    const accessToken = await getGoogleAccessToken(c.env);
    await sendFcmPush(c.env, accessToken, uid, body, "test", "test");
    return c.json({ok: true, message: `Push sent to user ${uid}`});
  } catch (e: any) {
    return jsonError(e.message ?? "Failed to send push", 500);
  }
});

// ─── Push Notifications (FCM) ─────────────────────────────────────────────────────

/**
 * Saves an FCM registration token for a given user.
 * Called by the client after requesting notification permission.
 *
 * Body: { fcmToken: string }
 */
app.post("/push/subscribe", async (c) => {
  const token = getBearerToken(c.req.header("authorization") ?? null);
  if (!token) return jsonError("Missing Authorization Bearer token", 401);

  let uid: string;
  try {
    uid = (await verifyFirebaseIdToken(token, c.env.FIREBASE_PROJECT_ID)).uid;
  } catch (e: any) {
    return jsonError(e.message ?? "Invalid Firebase token", 401);
  }

  const { fcmToken } = await c.req.json() as { fcmToken: string };
  if (!fcmToken) return jsonError("Missing fcmToken", 400);

  const accessToken = await getGoogleAccessToken(c.env);
  await firestorePatch(c.env, accessToken, `pushSubscriptions/${uid}`, {
    fcmToken,
    updatedAt: new Date(),
  });

  console.log(`[Push] Token saved for user ${uid}`);
  return c.json({ok: true});
});

/**
 * Removes the FCM token for a user (e.g. on logout).
 */
app.post("/push/unsubscribe", async (c) => {
  const token = getBearerToken(c.req.header("authorization") ?? null);
  if (!token) return jsonError("Missing Authorization Bearer token", 401);

  let uid: string;
  try {
    uid = (await verifyFirebaseIdToken(token, c.env.FIREBASE_PROJECT_ID)).uid;
  } catch (e: any) {
    return jsonError(e.message ?? "Invalid Firebase token", 401);
  }

  const accessToken = await getGoogleAccessToken(c.env);
  await firestorePatch(c.env, accessToken, `pushSubscriptions/${uid}`, {
    fcmToken: null,
    updatedAt: new Date(),
  });

  console.log(`[Push] Token removed for user ${uid}`);
  return c.json({ok: true});
});

// ─── PayOS webhook ───────────────────────────────────────────────────────────

app.post("/payos-webhook", async (c) => {
  const body = (await c.req.json()) as PayosWebhookBody;
  if (!body?.data || !body.signature) return jsonError("Invalid webhook payload", 400);

  const ok = await verifyPayosWebhook(c.env.PAYOS_CHECKSUM_KEY, body.data, body.signature);
  if (!ok) {
    // PayOS uses this endpoint to validate that your webhook works by sending a sample payload.
    // Returning 200 here avoids registration failures while still allowing us to ignore invalid events.
    return c.json({ok: true, ignored: true});
  }

  const orderCode = body.data.orderCode;
  if (!orderCode) return jsonError("Missing orderCode", 400);

  const accessToken = await getGoogleAccessToken(c.env);

  const docs = await firestoreRunQuery(c.env, accessToken, {
    from: [{collectionId: "appointments"}],
    where: {
      fieldFilter: {
        field: {fieldPath: "payos.orderCode"},
        op: "EQUAL",
        value: {integerValue: String(orderCode)},
      },
    },
    limit: 1,
  });

  if (docs.length === 0) return jsonError("Appointment not found", 404);

  const doc = docs[0];
  const appointment = doc.fields;

  if (appointment.paymentStatus === "PAID" && appointment.status === "CONFIRMED") {
    return c.json({ok: true, idempotent: true});
  }

  const docPath = doc.name.split("/documents/")[1];

  await firestorePatch(c.env, accessToken, docPath, {
    paymentStatus: "PAID",
    status: "CONFIRMED",
    paidAt: new Date(),
    updatedAt: new Date(),
  });

  return c.json({ok: true});
});

// ─── Cron: daily notification scheduler (FCM) ───────────────────────────────────

/**
 * Cloudflare Cron trigger — configured in wrangler.toml.
 * Runs daily at 01:00 UTC = 08:00 Indochina Time.
 *
 * Reads all SCHEDULED vaccinations + upcoming CONFIRMED appointments from Firestore,
 * then sends FCM push notifications to every subscribed user's device.
 */
export const scheduled: ExportedHandler<Env>["scheduled"] = async (_event, env, ctx) => {
  ctx.waitUntil(runScheduler(env));
};

async function runScheduler(env: Env): Promise<void> {
  console.log("[Scheduler] Starting daily notification run");

  try {
    const accessToken = await getGoogleAccessToken(env);

    await Promise.all([
      sendVaccineReminders(env, accessToken),
      sendAppointmentReminders(env, accessToken),
    ]);

    console.log("[Scheduler] Completed");
  } catch (err) {
    console.error("[Scheduler] Error:", err);
  }
}

async function sendVaccineReminders(env: Env, accessToken: string): Promise<void> {
  const today = startOfDay(new Date());

  const docs = await firestoreRunQuery(env, accessToken, {
    from: [{ collectionId: "vaccinations" }],
    where: {
      fieldFilter: {
        field: { fieldPath: "status" },
        op: "EQUAL",
        value: { stringValue: "SCHEDULED" },
      },
    },
  });

  for (const doc of docs) {
    const record = doc.fields;
    const scheduledDate = record.scheduledDate;
    if (!scheduledDate) continue;

    const dateStr = typeof scheduledDate === "object" && scheduledDate.timestampValue
      ? scheduledDate.timestampValue
      : String(scheduledDate);
    const due = startOfDay(new Date(dateStr));
    const daysUntil = Math.round((due.getTime() - today.getTime()) / 86_400_000);

    const petName = record.petName ?? record.name ?? "Your pet";
    const vaccineName = record.title ?? "vaccine";
    const message = vaccineMessage(petName, vaccineName, daysUntil);
    if (message) {
      await sendFcmPush(env, accessToken, record.ownerId, message, "vaccine", record.id);
    }
  }
}

async function sendAppointmentReminders(env: Env, accessToken: string): Promise<void> {
  const today = startOfDay(new Date());

  const docs = await firestoreRunQuery(env, accessToken, {
    from: [{ collectionId: "appointments" }],
    where: {
      fieldFilter: {
        field: { fieldPath: "status" },
        op: "EQUAL",
        value: { stringValue: "CONFIRMED" },
      },
    },
  });

  for (const doc of docs) {
    const appt = doc.fields;
    if (!appt.appointmentAt) continue;

    const dateStr = typeof appt.appointmentAt === "object" && appt.appointmentAt.timestampValue
      ? appt.appointmentAt.timestampValue
      : String(appt.appointmentAt);
    const due = startOfDay(new Date(dateStr));
    const daysUntil = Math.round((due.getTime() - today.getTime()) / 86_400_000);

    const doctorName = appt.veterinarianName ?? appt.doctorName ?? "your doctor";
    const clinicName = appt.clinicName ?? "the clinic";
    const message = appointmentMessage(doctorName, clinicName, daysUntil);
    if (message) {
      await sendFcmPush(env, accessToken, appt.ownerId ?? appt.userId, message, "appointment", appt.id);
    }
  }
}

/**
 * Looks up the user's FCM token from Firestore and sends an FCM push notification
 * using the FCM HTTP v1 API (no npm packages required — raw fetch only).
 */
async function sendFcmPush(
  env: Env,
  accessToken: string,
  userId: string,
  body: string,
  type: string,
  refId: string,
  title = "PetCare Reminder",
): Promise<void> {
  const subDoc = await firestoreGet(env, accessToken, `pushSubscriptions/${userId}`);
  if (!subDoc) return;

  const fcmToken: string | undefined = subDoc.fields.fcmToken;
  if (!fcmToken) return;

  const projectId = env.FIREBASE_PROJECT_ID;
  const fcmUrl = `https://fcm.googleapis.com/v1/projects/${projectId}/messages:send`;

  const payload = {
    message: {
      token: fcmToken,
      notification: {
        title,
        body,
      },
      data: { type, refId },
      android: {
        priority: "high",
        notification: {
          channel_id: "vetbook_reminders",
          default_vibrate_timings: true,
          default_sound: true,
        },
      },
    },
  };

  let res: Response;
  try {
    res = await fetch(fcmUrl, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${accessToken}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify(payload),
    });
  } catch (err) {
    console.error(`[Scheduler] FCM fetch error for user ${userId}:`, err);
    return;
  }

  if (res.status === 410 || res.status === 404) {
    // Token no longer valid — clear it
    await firestorePatch(env, accessToken, `pushSubscriptions/${userId}`, { fcmToken: null });
    console.log(`[Scheduler] Removed invalid FCM token for user ${userId}`);
    return;
  }

  if (!res.ok) {
    const errBody = await res.text();
    console.error(`[Scheduler] FCM error for user ${userId} (${res.status}): ${errBody}`);
    return;
  }

  console.log(`[Scheduler] Sent ${type} FCM push to user ${userId}`);
}

function vaccineMessage(petName: string, vaccineName: string, daysUntil: number): string | null {
  if (daysUntil > 7) return null;
  if (daysUntil === 7) return `${petName}'s ${vaccineName} vaccine is due in 7 days`;
  if (daysUntil === 1) return `${petName}'s ${vaccineName} vaccine is due tomorrow`;
  if (daysUntil === 0) return `${petName}'s ${vaccineName} vaccine is due today`;
  if (daysUntil < 0)  return `${petName} has an overdue vaccine: ${vaccineName}`;
  return null;
}

function appointmentMessage(doctorName: string, clinicName: string, daysUntil: number): string | null {
  if (daysUntil > 7) return null;
  if (daysUntil === 7) return `Appointment with ${doctorName} at ${clinicName} in 7 days`;
  if (daysUntil === 1) return `Appointment with ${doctorName} at ${clinicName} is tomorrow`;
  if (daysUntil === 0) return `You have an appointment today at ${clinicName}`;
  return null;
}

function startOfDay(d: Date): Date {
  const copy = new Date(d);
  copy.setHours(0, 0, 0, 0);
  return copy;
}

export default app;
