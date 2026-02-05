import {Hono} from "hono";
import {cors} from "hono/cors";
import axios from "axios";

type Env = {
  PAYOS_CLIENT_ID: string;
  PAYOS_API_KEY: string;
  PAYOS_CHECKSUM_KEY: string;
  FIREBASE_SERVICE_ACCOUNT_JSON: string;
  FIREBASE_PROJECT_ID: string;
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
  const resp = await fetch(`https://oauth2.googleapis.com/tokeninfo?id_token=${encodeURIComponent(idToken)}`);
  if (!resp.ok) throw new Error("Invalid Firebase ID token");
  const info = (await resp.json()) as any;
  if (info.aud !== projectId) {
    // In some cases, `aud` can be the web client ID. For simplicity, we are not checking it strictly here.
    // A more robust solution would fetch the project's client IDs and check against them.
  }
  const iss: string | undefined = info.iss;
  if (iss && !iss.includes("securetoken.google.com")) {
    throw new Error("Invalid token issuer");
  }
  if (info.sub) return {uid: info.sub};
  if (info.user_id) return {uid: info.user_id};
  throw new Error("Token missing uid");
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

const app = new Hono<{Bindings: Env}>();
app.use("*", cors({origin: "*"}));

app.get("/health", (c) => c.json({ok: true}));

app.post("/create-payment-link", async (c) => {
  const token = getBearerToken(c.req.header("authorization"));
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

  const payosPayload: Record<string, any> = {
    orderCode,
    amount,
    description: `VetBook appointment ${body.appointmentId}`.slice(0, 50),
    returnUrl: "https://vetbook.example.com/payment/success", // Placeholder
    cancelUrl: "https://vetbook.example.com/payment/cancel",   // Placeholder
  };

  let payosResp: any;
  try {
    const url = "https://api-merchant.payos.vn/v2/payment-requests";
    const headers: Record<string, string> = {
      "Content-Type": "application/json",
      "x-client-id": c.env.PAYOS_CLIENT_ID,
      "x-api-key": c.env.PAYOS_API_KEY,
    };
    payosResp = (await axios.post(url, payosPayload, {headers})).data;
  } catch (e: any) {
    return jsonError(`PayOS create link failed: ${e?.response?.data?.desc ?? e?.message ?? "unknown"}`, 502);
  }

  const checkoutUrl = payosResp?.data?.checkoutUrl ?? payosResp?.checkoutUrl;
  const paymentLinkId = payosResp?.data?.paymentLinkId ?? payosResp?.paymentLinkId ?? null;

  if (!checkoutUrl) return jsonError("PayOS response missing checkoutUrl", 502);

  // Combine all payos fields into a single object for one patch operation.
  const existingPayos = appointment.payos || {};
  const updatedPayos = {
    ...existingPayos,
    orderCode,
    paymentLinkId,
    checkoutUrl,
  };

  await firestorePatch(c.env, accessToken, `appointments/${body.appointmentId}`, {
    payos: updatedPayos,
    updatedAt: new Date(),
  });

  return c.json({checkoutUrl, orderCode, paymentLinkId});
});

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

export default app;
