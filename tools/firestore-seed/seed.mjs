import fs from "node:fs";
import path from "node:path";
import process from "node:process";
import { fileURLToPath } from "node:url";
import admin from "firebase-admin";

// Robust cross-platform __dirname for ESM (fixes Windows paths like D:\D:\...)
const __dirname = path.dirname(fileURLToPath(import.meta.url));
const dataPath = path.join(__dirname, "seed-data.json");

function requireEnv(name) {
  const v = process.env[name];
  if (!v) throw new Error(`Missing env var: ${name}`);
  return v;
}

const projectId = requireEnv("FIREBASE_PROJECT_ID");
const serviceAccountPath = requireEnv("GOOGLE_APPLICATION_CREDENTIALS");

const seed = JSON.parse(fs.readFileSync(dataPath, "utf8"));

admin.initializeApp({
  credential: admin.credential.cert(JSON.parse(fs.readFileSync(serviceAccountPath, "utf8"))),
  projectId,
});

const db = admin.firestore();

const now = Date.now();

async function upsertCategories() {
  // categories/{id} => custom IDs for stable lookups
  const batch = db.batch();
  for (const c of seed.categories) {
    const ref = db.collection("categories").doc(c.id);
    batch.set(
      ref,
      {
        id: c.id,
        label: c.label,
        updatedAt: now,
        createdAt: now,
      },
      { merge: true }
    );
  }
  await batch.commit();
  console.log(`Upserted ${seed.categories.length} categories.`);
}

async function seedProducts() {
  // products => auto IDs are fine unless you need stable slugs/sku
  // We avoid duplicating products by using a deterministic key: name + category
  const productsCol = db.collection("products");
  let created = 0;
  let skipped = 0;

  for (const p of seed.products) {
    const key = `${p.category}__${p.name}`.toLowerCase();
    const existing = await productsCol.where("seedKey", "==", key).limit(1).get();
    if (!existing.empty) {
      skipped++;
      continue;
    }

    await productsCol.add({
      ...p,
      seedKey: key,
      createdAt: now,
      updatedAt: now,
      isActive: true,
      stock: Math.floor(20 + Math.random() * 200),
      rating: Math.round((3.8 + Math.random() * 1.2) * 10) / 10,
      reviewCount: Math.floor(5 + Math.random() * 500),
    });
    created++;
  }

  console.log(`Products: created=${created}, skipped(existing)=${skipped}`);
}

async function main() {
  console.log(`Seeding Firestore project: ${projectId}`);
  await upsertCategories();
  await seedProducts();
  console.log("Done.");
}

main()
  .then(() => process.exit(0))
  .catch((e) => {
    console.error(e);
    process.exit(1);
  });

