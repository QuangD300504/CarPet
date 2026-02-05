/**
 * Import function triggers from their respective submodules:
 *
 * import {onCall} from "firebase-functions/v2/https";
 * import {onDocumentWritten} from "firebase-functions/v2/firestore";
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

import {setGlobalOptions} from "firebase-functions";
import {onCall} from "firebase-functions/v2/https";
import * as logger from "firebase-functions/logger";

import * as admin from "firebase-admin";
import {Timestamp} from "firebase-admin/firestore";

admin.initializeApp();

// Start writing functions
// https://firebase.google.com/docs/functions/typescript

// For cost control, you can set the maximum number of containers that can be
// running at the same time. This helps mitigate the impact of unexpected
// traffic spikes by instead downgrading performance. This limit is a
// per-function limit. You can override the limit for each function using the
// `maxInstances` option in the function's options, e.g.
// `onRequest({ maxInstances: 5 }, (req, res) => { ... })`.
// NOTE: setGlobalOptions does not apply to functions using the v1 API. V1
// functions should each use functions.runWith({ maxInstances: 10 }) instead.
// In the v1 API, each function can only serve one request per container, so
// this will be the maximum concurrent request count.
setGlobalOptions({ maxInstances: 10 });

type ReserveSlotRequest = {
  veterinarianId: string;
  userId?: string;
  appointmentAt: string; // ISO string
  durationMinutes: number;
  totalPrice: number;
  serviceId?: string | null;
  petId?: string | null;
  packageId?: string | null;
  notes?: string | null;
};

function pad2(n: number) {
  return n.toString().padStart(2, "0");
}

function toDateKeyUTC(date: Date) {
  return `${date.getUTCFullYear()}${pad2(date.getUTCMonth() + 1)}${pad2(date.getUTCDate())}`;
}

function toTimeKeyUTC(date: Date) {
  return `${pad2(date.getUTCHours())}${pad2(date.getUTCMinutes())}`;
}

export const reserveSlotAndCreateAppointment = onCall<ReserveSlotRequest>(
  {
    region: "asia-southeast1",
  },
  async (request) => {
    if (!request.auth?.uid) {
      throw new Error("unauthenticated");
    }

    const data = request.data;
    const userId = request.auth.uid;

    if (!data?.veterinarianId || !data?.appointmentAt) {
      throw new Error("invalid-argument");
    }

    const appointmentAt = new Date(data.appointmentAt);
    if (Number.isNaN(appointmentAt.getTime())) {
      throw new Error("invalid-argument");
    }

    const now = new Date();
    if (appointmentAt.getTime() <= now.getTime()) {
      throw new Error("failed-precondition");
    }

    const veterinarianId = data.veterinarianId;
    const durationMinutes = Number(data.durationMinutes ?? 0);
    const totalPrice = Number(data.totalPrice ?? 0);

    if (!Number.isFinite(durationMinutes) || durationMinutes <= 0) {
      throw new Error("invalid-argument");
    }
    if (!Number.isFinite(totalPrice) || totalPrice < 0) {
      throw new Error("invalid-argument");
    }

    const dateKey = toDateKeyUTC(appointmentAt);
    const timeKey = toTimeKeyUTC(appointmentAt);
    const lockId = `${veterinarianId}_${dateKey}_${timeKey}`;

    const db = admin.firestore();
    const lockRef = db.collection("doctorSlotLocks").doc(lockId);
    const appointmentsCol = db.collection("appointments");

    const result = await db.runTransaction(async (tx) => {
      const lockSnap = await tx.get(lockRef);
      if (lockSnap.exists) {
        throw new Error("already-booked");
      }

      const dupQuery = appointmentsCol
        .where("userId", "==", userId)
        .where("veterinarianId", "==", veterinarianId)
        .where("appointmentAt", "==", Timestamp.fromDate(appointmentAt))
        .where("status", "==", "PENDING_PAYMENT")
        .limit(1);

      const dupSnap = await tx.get(dupQuery);
      if (!dupSnap.empty) {
        throw new Error("duplicate-pending");
      }

      const appointmentRef = appointmentsCol.doc();

      tx.set(lockRef, {
        id: lockId,
        veterinarianId,
        appointmentAt: Timestamp.fromDate(appointmentAt),
        createdAt: Timestamp.now(),
      });

      tx.set(appointmentRef, {
        id: appointmentRef.id,
        userId,
        veterinarianId,
        serviceId: data.serviceId ?? null,
        petId: data.petId ?? null,
        packageId: data.packageId ?? null,
        status: "PENDING_PAYMENT",
        paymentStatus: "UNPAID",
        appointmentAt: Timestamp.fromDate(appointmentAt),
        durationMinutes,
        notes: data.notes ?? null,
        totalPrice,
        createdAt: Timestamp.now(),
        updatedAt: Timestamp.now(),
        slotLockId: lockId,
      });

      logger.info("Reserved slot + created appointment", {
        appointmentId: appointmentRef.id,
        lockId,
        userId,
        veterinarianId,
      });

      return {
        appointmentId: appointmentRef.id,
        lockId,
      };
    });

    return result;
  }
);
